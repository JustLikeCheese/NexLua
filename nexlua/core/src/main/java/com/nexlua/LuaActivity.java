package com.nexlua;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.LuaHandler;
import com.luajava.value.referable.LuaFunction;
import com.nexlua.module.LuaModule;
import com.nexlua.utils.SingleObject;

import java.util.ArrayList;

public class LuaActivity extends Activity implements LuaBroadcastReceiver.OnReceiveListener, LuaContext, LuaHandler {
    protected LuaFunction mOnKeyDown, mOnKeyUp, mOnKeyLongPress, mOnKeyShortcut;
    protected LuaFunction mOnCreateOptionsMenu, mOnCreateContextMenu, mOnOptionsItemSelected, mOnMenuItemSelected, mOnContextItemSelected;
    protected LuaFunction mOnActivityResult, onRequestPermissionsResult, mOnSaveInstanceState, mOnRestoreInstanceState;
    protected LuaFunction mOnStart, mOnResume, mOnPause, mOnStop, mOnRestarted, mOnConfigurationChanged, mOnTouchEvent;
    protected LuaFunction mOnReceive, mOnNewIntent, mOnResult, mOnDestroy;
    protected SingleObject<LuaFunction> mOnMessage, mOnError;
    protected LuaBroadcastReceiver mReceiver;
    protected final LuaApplication app = LuaApplication.getInstance();
    protected final LuaConfig config = app.getConfig();
    protected final Lua L = new Lua(this);
    protected LuaPrint print;
    protected String luaPath, luaDir, luaLpath, luaCpath;
    protected Bundle savedInstanceState;
    protected LuaIntent intent;
    protected LuaModule module;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            super.onCreate(null);
            this.savedInstanceState = savedInstanceState;
            this.intent = LuaIntent.from(getIntent());
            if (intent != null) {
                module = intent.module;
                luaPath = module.getAbsolutePath();
                luaDir = LuaUtil.getParentPath(luaPath);
            }
            initLua();
            loadLua();
            loadEvent();
        } catch (Exception e) {
            sendError(e);
        }
    }

    public void loadLua() throws Exception {
        module.run(L);
        runFunc("onCreate", savedInstanceState);
        L.getGlobal("main");
        if (L.isFunction(-1)) {
            int nArgs = L.pushAll(intent.args, Lua.Conversion.SEMI);
            L.pCall(nArgs);
        } else {
            L.pop(1);
        }
    }

    public void initLua() throws LuaException {
        L.openLibraries();
        L.openLibrary("luajava");
        L.setExternalLoader(config);
        luaCpath = app.getLuaCpath(luaDir);
        luaLpath = app.getLuaLpath(luaDir);
        // package.path 和 cpath
        L.getGlobal("package");
        if (L.isTable(1)) {
            L.setField(1, "path", luaLpath);
            L.setField(1, "cpath", luaCpath);
        }
        L.pop(1);
        // 插入 LuaActivity
        print = new LuaPrint(this);
        L.pushGlobal(this, "activity", "context", "this");
        L.pushGlobal(app, "application", "app");
        L.pushGlobal(print, "print");
    }

    public void loadEvent() throws Exception {
        // onKeyEvent
        mOnKeyShortcut = L.getLuaFunction("onKeyShortcut");
        mOnKeyDown = L.getLuaFunction("onKeyDown");
        mOnKeyUp = L.getLuaFunction("onKeyUp");
        mOnKeyLongPress = L.getLuaFunction("onKeyLongPress");
        // onTouchEvent
        mOnTouchEvent = L.getLuaFunction("onTouchEvent");
        // onAccessibilityEvent
        // onCreateOptionsMenu
        mOnCreateOptionsMenu = L.getLuaFunction("onCreateOptionsMenu");
        // mOnCreateContextMenu
        mOnCreateContextMenu = L.getLuaFunction("onCreateContextMenu");
        // onOptionsItemSelected
        mOnOptionsItemSelected = L.getLuaFunction("onOptionsItemSelected");
        // onMenuItemSelected
        mOnMenuItemSelected = L.getLuaFunction("onMenuItemSelected");
        // onContextItemSelected
        mOnContextItemSelected = L.getLuaFunction("onContextItemSelected");
        // onActivityResult
        mOnActivityResult = L.getLuaFunction("onActivityResult");
        // onRequestPermissionsResult
        onRequestPermissionsResult = L.getLuaFunction("onRequestPermissionsResult");
        // onConfigurationChanged
        mOnConfigurationChanged = L.getLuaFunction("onConfigurationChanged");
        // onReceive
        mOnReceive = L.getLuaFunction("onReceive");
        // onMessage
        mOnMessage = SingleObject.wrapNonNull(L.getLuaFunction("onMessage"));
        // onError
        mOnError = SingleObject.wrapNonNull(L.getLuaFunction("onError"));
        // onNewIntent
        mOnNewIntent = L.getLuaFunction("onNewIntent");
        // onResult
        mOnResult = L.getLuaFunction("onResult");
        // onSaveInstanceState
        mOnSaveInstanceState = L.getLuaFunction("onSaveInstanceState");
        // onRestoreInstanceState
        mOnRestoreInstanceState = L.getLuaFunction("onRestoreInstanceState");
        // onStart
        mOnStart = L.getLuaFunction("onStart");
        // onResume
        mOnResume = L.getLuaFunction("onResume");
        // onPause
        mOnPause = L.getLuaFunction("onPause");
        // onStop
        mOnStop = L.getLuaFunction("onStop");
        // onRestart
        mOnRestarted = L.getLuaFunction("onRestart");
        // onDestroy
        mOnDestroy = L.getLuaFunction("onDestroy");
    }

    private boolean isViewInflated = false;
    private LinearLayout consoleLayout;
    private ArrayAdapter<String> adapter;

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        isViewInflated = true;
    }

    public void setHomeAsUpEnabled(boolean enabled) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enabled);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        isViewInflated = true;
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        isViewInflated = true;
    }

    public void setConsoleLayout() {
        if (isViewInflated) return;
        if (consoleLayout == null) {
            // 获取主题颜色
            TypedArray array = getTheme().obtainStyledAttributes(new int[]{
                    android.R.attr.colorBackground,
                    android.R.attr.textColorPrimary,
                    android.R.attr.textColorHighlightInverse,
            });
            int backgroundColor = array.getColor(0, 0xFF00FF);
            int textColor = array.getColor(1, 0xFF00FF);
            array.recycle();
            // 初始化控件
            consoleLayout = new LinearLayout(this);
            consoleLayout.setFitsSystemWindows(true);
            ListView listView = new ListView(this);
            listView.setFastScrollEnabled(true);
            listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTextIsSelectable(true);
                    view.setTextColor(textColor);
                    return view;
                }
            };
            listView.setAdapter(adapter);
            consoleLayout.addView(listView);
            consoleLayout.setBackgroundColor(backgroundColor);
            consoleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            listView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            View footerView = new View(this);
            footerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 0));
            listView.addFooterView(footerView, null, false);
        }
        setHomeAsUpEnabled(true);
        setContentView(consoleLayout);
        isViewInflated = false;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public Intent registerReceiver(LuaBroadcastReceiver receiver, IntentFilter filter) {
        return super.registerReceiver(receiver, filter);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public Intent registerReceiver(LuaBroadcastReceiver.OnReceiveListener ltr, IntentFilter filter) {
        LuaBroadcastReceiver receiver = new LuaBroadcastReceiver(ltr);
        return super.registerReceiver(receiver, filter);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public Intent registerReceiver(IntentFilter filter) {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = new LuaBroadcastReceiver(this);
        return super.registerReceiver(mReceiver, filter);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        super.unregisterReceiver(receiver);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onLuaEvent(mOnReceive, context, intent);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        isViewInflated = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        onLuaEvent(mOnStart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onLuaEvent(mOnResume);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onLuaEvent(mOnPause);
    }

    @Override
    protected void onStop() {
        super.onStop();
        onLuaEvent(mOnStop);
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) unregisterReceiver(mReceiver);
        onLuaEvent(mOnDestroy);
        System.gc();
        L.gc();
        L.close();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        onLuaEvent(mOnRestarted);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        onLuaEvent(mOnSaveInstanceState, outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        onLuaEvent(mOnRestoreInstanceState, savedInstanceState, persistentState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onLuaEvent(mOnNewIntent, intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onLuaEvent(onRequestPermissionsResult, requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return onLuaEvent(mOnKeyShortcut, keyCode, event) || super.onKeyShortcut(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return onLuaEvent(mOnKeyDown, keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return onLuaEvent(mOnKeyUp, keyCode, event) || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return onLuaEvent(mOnKeyLongPress, keyCode, event) || super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onLuaEvent(mOnTouchEvent, event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return onLuaEvent(mOnCreateOptionsMenu, menu) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return onLuaEvent(mOnMenuItemSelected, item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        if (onLuaEvent(mOnMenuItemSelected, featureId, item)) {
            return true;
        } else if (!isViewInflated && item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else {
            return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        onLuaEvent(mOnCreateContextMenu, menu, view, info);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        return onLuaEvent(mOnContextItemSelected, item) || super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onLuaEvent(mOnConfigurationChanged, newConfig);
    }

    @SuppressLint("ObsoleteSdkInt")
    public void finish(boolean finishTask) {
        if (finishTask && Build.VERSION.SDK_INT >= 21) {
            Intent intent = getIntent();
            if (intent != null && (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
                finishAndRemoveTask();
        }
        super.finish();
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (Build.VERSION.SDK_INT >= 21)
            setTaskDescription(new ActivityManager.TaskDescription(title.toString()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent != null) {
            LuaIntent result = LuaIntent.from(intent);
            if (onLuaEvent(mOnResult, (Object[]) result.args)) {
                return;
            }
        }
        onLuaEvent(mOnActivityResult, requestCode, resultCode, intent);
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Lua Activity
    public void newActivity(LuaModule module, Serializable... args) {
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(LuaIntent.NAME, new LuaIntent(module, args));
        startActivity(intent);
    }

    public void newActivityForResult(LuaModule module, int requestCode, Serializable... args) {
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(LuaIntent.NAME, new LuaIntent(module, args));
        startActivityForResult(intent, requestCode);
    }

    public void newActivity(String name, Serializable... args) {
        newActivity(config.getModule(this, name), args);
    }

    public void newActivityForResult(String name, int requestCode, Serializable... args) {
        newActivityForResult(config.getModule(this, name), requestCode, args);
    }

    public void setActivityResult(int resultCode, Serializable... args) {
        Intent intent = new Intent(this, LuaActivity.class);
        LuaIntent intentArgs = new LuaIntent(null, args);
        intent.putExtra(LuaIntent.NAME, intentArgs);
        setResult(resultCode, intent);
    }

    @Override
    public void showToast(String message) {
        app.showToast(message);
    }

    @Override
    public void sendMessage(String message) {
        if (mOnMessage != null && mOnMessage.lock()) {
            try {
                LuaFunction func = mOnMessage.get();
                if (onLuaEvent(func, message)) {
                    return;
                }
            } catch (Exception exception) {
                String error = LuaException.getFullMessage(exception);
                showToast(error);
                Lua.logError("onMessage:\n" + error);
            } finally {
                mOnMessage.unlock();
            }
        }
        if (!isViewInflated) {
            setConsoleLayout();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.add(message);
                    adapter.notifyDataSetChanged();
                }
            });
        }
        showToast(message);
        Lua.logWarn(message);
    }

    @Override
    public void sendError(Exception e) {
        String type = LuaException.getType(e);
        String message = LuaException.getFullMessage(e);
        if (mOnError != null && mOnError.lock()) {
            try {
                LuaFunction func = mOnError.get();
                if (onLuaEvent(func, e, type, message)) {
                    return;
                }
            } catch (Exception exception) {
                String error = LuaException.getFullMessage(exception);
                showToast(error);
                Lua.logError("onError:\n" + error);
            } finally {
                mOnError.unlock();
            }
        }
        if (!isViewInflated) {
            setConsoleLayout();
            setTitle(type);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.add(message);
                    adapter.notifyDataSetChanged();
                }
            });
        }
        showToast(message);
        Lua.logError(message);
    }

    public ArrayList<ClassLoader> getClassLoaders() {
        return null;
    }

    public Lua getLua() {
        return L;
    }

    @Override
    public LuaConfig getConfig() {
        return app.getConfig();
    }

    @Override
    public String getLuaDir() {
        return luaDir;
    }

    public String getLuaPath() {
        return luaPath;
    }

    public String getLuaLpath() {
        return luaLpath;
    }

    public String getLuaCpath() {
        return luaCpath;
    }

    public Context getContext() {
        return this;
    }
}
