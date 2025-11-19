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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.LuaValue;
import com.luajava.value.referable.LuaFunction;

import java.io.File;
import java.util.ArrayList;

public class LuaActivity extends Activity implements LuaBroadcastReceiver.OnReceiveListener, com.nexlua.LuaContext {
    protected LuaFunction mOnKeyDown;
    protected LuaFunction mOnKeyUp;
    protected LuaFunction mOnKeyLongPress;
    protected LuaFunction mOnKeyShortcut;
    protected LuaFunction mOnTouchEvent;
    protected LuaFunction mOnCreateOptionsMenu, mOnCreateContextMenu, mOnOptionsItemSelected, mOnMenuItemSelected, mOnContextItemSelected;
    protected LuaFunction mOnActivityResult, onRequestPermissionsResult, mOnSaveInstanceState, mOnRestoreInstanceState;
    protected LuaFunction mOnStart, mOnResume, mOnPause, mOnStop, mOnRestarted;
    protected LuaFunction mOnConfigurationChanged;
    protected LuaFunction mOnError, mOnReceive, mOnNewIntent, mOnResult, mOnDestroy;
    protected LuaBroadcastReceiver mReceiver;
    protected File luaDir, luaFile;
    protected String luaPath, luaLpath, luaCpath;
    protected final Lua L = new Lua(this::sendError);
    protected final LuaPrint print = new LuaPrint(this);
    protected final LuaApplication app = LuaApplication.getInstance();
    protected LuaIntent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.intent = LuaIntent.from(getIntent());
        if (intent != null) {
            setTheme(intent.theme);
            luaFile = intent.file;
            luaPath = luaFile.getAbsolutePath();
            luaDir = luaFile.getParentFile();
        }
        super.onCreate(null);
        try {
            L.openLibraries();
            L.setExternalLoader(new LuaModuleLoader(this));
            luaCpath = app.getLuaCpath();
            luaLpath = app.getLuaLpath();
            if (!luaDir.equals(app.getLuaDir())) {
                luaCpath = luaCpath + luaDir + "/lib?.so;";
                luaLpath = luaLpath + luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;";
            }
            // package.path 和 cpath
            L.getGlobal("package");
            if (L.isTable(1)) {
                L.setField(1, "path", luaLpath);
                L.setField(1, "cpath", luaCpath);
                L.pop(1);
            }
            // 插入 LuaActivity
            L.pushGlobal(this, "activity", "this");
            L.pushGlobal(app, "application", "app");
            L.pushGlobal(print, "print");
            loadLua();
            loadEvent();
            // onCreate
            runFunc("onCreate", savedInstanceState);
            runFunc("main", intent.args);
        } catch (Exception e) {
            sendError(e);
        }
    }

    public void loadLua() throws Exception {
        L.loadExternal(luaPath);
    }

    public void loadEvent() {
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
        // onError
        mOnError = L.getLuaFunction("onError");
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
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTextIsSelectable(true);
                    view.setTextColor(textColor);
                    return view;
                }
            };
            listView.setAdapter(adapter);
            consoleLayout.addView(listView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            consoleLayout.setBackgroundColor(backgroundColor);
        }
        setContentView(consoleLayout);
        isViewInflated = false;
    }

    protected boolean onLuaEvent(LuaValue event, Object... args) {
        if (event != null) {
            try {
                L.push(event);
                if (L.isFunction(-1)) {
                    L.pCall(args, Lua.Conversion.SEMI, 1);
                    Object object = L.get().toJavaObject();
                    return object != Boolean.FALSE && object != null;
                }
                return false;
            } catch (LuaException e) {
                sendError(e);
            }
        }
        return false;
    }

    protected boolean runFunc(String funcName, Object... args) {
        if (funcName != null) {
            try {
                L.getGlobal(funcName);
                if (L.isFunction(-1)) {
                    L.pCall(args, Lua.Conversion.SEMI, 1);
                    Object object = L.get().toJavaObject();
                    return object != Boolean.FALSE && object != null;
                }
                return false;
            } catch (LuaException e) {
                sendError(e);
            }
        }
        return false;
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
    protected void onSaveInstanceState(Bundle outState) {
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onLuaEvent(onRequestPermissionsResult, requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return onLuaEvent(mOnKeyShortcut, keyCode, event) | super.onKeyShortcut(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return onLuaEvent(mOnKeyDown, keyCode, event) | super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return onLuaEvent(mOnKeyUp, keyCode, event) | super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return onLuaEvent(mOnKeyLongPress, keyCode, event) | super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onLuaEvent(mOnTouchEvent, event) | super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return onLuaEvent(mOnCreateOptionsMenu, menu) | super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (item.hasSubMenu())
                ? super.onOptionsItemSelected(item)
                : onLuaEvent(mOnOptionsItemSelected, item);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (!isViewInflated && mOnOptionsItemSelected == null && item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return (item.hasSubMenu())
                ? super.onMenuItemSelected(featureId, item)
                : onLuaEvent(mOnMenuItemSelected, featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        onLuaEvent(mOnCreateContextMenu, menu, view, info);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return onLuaEvent(mOnContextItemSelected, item) | super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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
            if (onLuaEvent(mOnResult, result.args)) {
                return;
            }
        }
        onLuaEvent(mOnActivityResult, requestCode, resultCode, intent);
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void newActivity(String name) {
        newActivity(name, null);
    }

    public void newActivity(String name, Object[] args) {
        File file = new File(name);
        if (!file.exists()) file = new File(luaDir, name);
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(LuaIntent.NAME, new LuaIntent(file, args));
        startActivity(intent);
    }

    public void newActivityForResult(String name, int requestCode, Object... args) {
        File file = new File(name);
        if (!file.exists()) file = new File(luaDir, name);
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(LuaIntent.NAME, new LuaIntent(file, args));
        startActivityForResult(intent, requestCode);
    }

    public void setActivityResult(int resultCode, Object[] data) {
        Intent intent = new Intent();
        intent.putExtra(LuaIntent.NAME, new LuaIntent(this.intent.file, this.intent.args));
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void showToast(String message) {
        app.showToast(message);
    }

    @Override
    public void sendMessage(String message) {
        if (!isViewInflated) {
            setConsoleLayout();
            ActionBar actionBar = getActionBar();
            if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.add(message);
                    adapter.notifyDataSetChanged();
                }
            });
        }
        showToast(message);
    }

    @Override
    public void sendError(String title, String message) {
        if (!isViewInflated) {
            setConsoleLayout();
            ActionBar actionBar = getActionBar();
            if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
            boolean ret = onLuaEvent(mOnError, message);
            if (!ret) {
                setTitle(title);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(message);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
        showToast(message);
    }

    // @formatter:off
    public ArrayList<ClassLoader> getClassLoaders() { return null; }
    public Lua getLua() { return L; }
    public File getLuaFile() { return luaFile; }
    public File getLuaDir() { return luaDir; }
    public String getLuaPath() { return luaPath; }
    public String getLuaLpath() { return luaLpath; }
    public String getLuaCpath() { return luaCpath; }
    public Context getContext() { return this; }
    // @formatter:on
}
