package com.nexlua;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.luajit.LuaJit;
import com.luajava.value.LuaValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class LuaActivity extends Activity implements LuaBroadcastReceiver.OnReceiveListener, LuaContext {
    private int mWidth, mHeight;
    private LuaValue mOnKeyDown, mOnKeyUp, mOnKeyLongPress, mOnKeyShortcut, mOnTouchEvent, mOnAccessibilityEvent;
    private LuaValue mOnCreateOptionsMenu, mOnCreateContextMenu, mOnOptionsItemSelected, mOnMenuItemSelected, mOnContextItemSelected;
    private LuaValue mOnActivityResult, onRequestPermissionsResult, mOnSaveInstanceState, mOnRestoreInstanceState;
    private LuaValue mOnStart, mOnResume, mOnPause, mOnStop, mOnRestarted;
    private LuaValue mOnConfigurationChanged;
    private LuaValue mOnError, mOnReceive, mOnNewIntent, mOnResult;
    private LuaBroadcastReceiver mReceiver;
    private File luaDir, luaFile;
    private String luaPath, luaLpath, luaCpath;
    private final Lua L = new LuaJit();
    private LuaApplication app;
    private Menu optionsMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // 设置主题
        if (LuaConfig.APP_THEME != 0) setTheme(LuaConfig.APP_THEME);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(null);
        // 获取屏幕大小
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;
        // 获取传入 path 参数, 定义 luaDir
        app = LuaApplication.getInstance();
        Intent intent = getIntent();
        luaFile = new File(intent.getStringExtra(LuaContext.LUA_PATH));
        luaDir = luaFile.getParentFile();
        luaPath = luaFile.getAbsolutePath();
        luaCpath = app.getLuaCpath();
        luaLpath = app.getLuaLpath();
        // 初始化 Lua 环境
        initializeLua();
        // 执行 Lua
        try {
            loadLua();
            // onCreate
            LuaValue mOnCreate = L.getFunction("onCreate");
            // onKeyEvent
            mOnKeyShortcut = L.getFunction("onKeyShortcut");
            mOnKeyDown = L.getFunction("onKeyDown");
            mOnKeyUp = L.getFunction("onKeyUp");
            mOnKeyLongPress = L.getFunction("onKeyLongPress");
            // onTouchEvent
            mOnTouchEvent = L.getFunction("onTouchEvent");
            // onAccessibilityEvent
            mOnAccessibilityEvent = L.getFunction("onAccessibilityEvent");
            // onCreateOptionsMenu
            mOnCreateOptionsMenu = L.getFunction("onCreateOptionsMenu");
            // mOnCreateContextMenu
            mOnCreateContextMenu = L.getFunction("onCreateContextMenu");
            // onOptionsItemSelected
            mOnOptionsItemSelected = L.getFunction("onOptionsItemSelected");
            // onMenuItemSelected
            mOnMenuItemSelected = L.getFunction("onMenuItemSelected");
            // onContextItemSelected
            mOnContextItemSelected = L.getFunction("onContextItemSelected");
            // onActivityResult
            mOnActivityResult = L.getFunction("onActivityResult");
            // onRequestPermissionsResult
            onRequestPermissionsResult = L.getFunction("onRequestPermissionsResult");
            // onConfigurationChanged
            mOnConfigurationChanged = L.getFunction("onConfigurationChanged");
            // onReceive
            mOnReceive = L.getFunction("onReceive");
            // onError
            mOnError = L.getFunction("onError");
            // onNewIntent
            mOnNewIntent = L.getFunction("onNewIntent");
            // onResult
            mOnResult = L.getFunction("onResult");
            // onSaveInstanceState
            mOnSaveInstanceState = L.getFunction("onSaveInstanceState");
            // onRestoreInstanceState
            mOnRestoreInstanceState = L.getFunction("onRestoreInstanceState");
            // onStart
            mOnStart = L.getFunction("onStart");
            // onResume
            mOnResume = L.getFunction("onResume");
            // onPause
            mOnPause = L.getFunction("onPause");
            // onStop
            mOnStop = L.getFunction("onStop");
            // onRestart
            mOnRestarted = L.getFunction("onRestart");
            onLuaEvent(mOnCreate);
            Object[] arg = (Object[]) intent.getSerializableExtra(LuaContext.LUA_ARG);
            if (arg != null) runFunc("main", arg);
        } catch (Exception e) {
            sendError(e);
        }
    }

    public void loadLua() throws Exception {
        String filesPath = getFilesDir().getAbsolutePath() + "/";
        if (luaPath.startsWith(filesPath)) {
            String temp = luaPath.substring(filesPath.length());
            Class<?> clazz = LuaConfig.LUA_DEX_MAP.get(temp);
            Log.i("FUCK", String.valueOf(clazz));
            if (clazz != null) {
                LuaModule module = (LuaModule) clazz.newInstance();
                module.run(this);
                Log.i("FUCK", "fuck");
            } else {
                doFile(getLuaFile());
            }
        }
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

    private boolean onLuaEvent(LuaValue event, Object... args) {
        if (event != null) {
            try {
                LuaValue[] ret = event.call(args);
                return ret != null && ret.length > 0 && ret[0].toBoolean();
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
        if (mOnReceive != null) mOnReceive.call(context, intent);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        isViewInflated = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mOnStart != null) mOnStart.call();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOnResume != null) mOnResume.call();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOnPause != null) mOnPause.call();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mOnStop != null) mOnStop.call();
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) unregisterReceiver(mReceiver);
        runFunc("onDestroy");
        super.onDestroy();
        System.gc();
        L.gc();
        L.close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mOnRestarted != null) mOnRestarted.call();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mOnSaveInstanceState != null) mOnSaveInstanceState.call(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        if (mOnRestoreInstanceState != null) mOnRestoreInstanceState.call(savedInstanceState, persistentState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mOnNewIntent != null) mOnNewIntent.call(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (onRequestPermissionsResult != null)
            onRequestPermissionsResult.call(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    public Menu getOptionsMenu() {
        return optionsMenu;
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
        if (mOnCreateContextMenu != null) mOnCreateContextMenu.call(menu, view, info);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return onLuaEvent(mOnContextItemSelected, item) | super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics outMetrics = new DisplayMetrics();
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;
        if (mOnConfigurationChanged != null) mOnConfigurationChanged.call(newConfig);
    }


    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @SuppressLint("ObsoleteSdkInt")
    public void finish(boolean finishTask) {
        if (finishTask && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setTaskDescription(new ActivityManager.TaskDescription(title.toString()));
    }

    // 运行lua脚本
    public void doString(String code, String name, Object... args) {
        doString(code.getBytes(), name, args);
    }

    public void doString(byte[] bytes, String name, Object... arg) {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(bytes.length);
        directBuffer.put(bytes);
        directBuffer.flip();
        doString(directBuffer, name, arg);
    }

    public void doString(ByteBuffer directBuffer, String name, Object... args) {
        synchronized (L) {
            final int oldTop = L.getTop();
            try {
                L.load(directBuffer, name);
                if (args != null) {
                    for (Object arg : args) L.push(arg, Lua.Conversion.SEMI);
                    L.pCall(args.length, 0);
                } else {
                    L.pCall(0, 0);
                }
            } catch (Exception e) {
                sendError(e);
            } finally {
                L.setTop(oldTop);
            }
        }
    }

    public void doFile(File filePath) {
        doFile(filePath, new Object[0]);
    }

    public void doFile(File file, Object... args) {
        try {
            doString(LuaUtil.readFileBuffer(file), file.getPath(), args);
        } catch (IOException e) {
            sendError(e);
        }
    }

    public void doAsset(String name, Object[] args) {
        try {
            doString(LuaUtil.readAssetBuffer(name), name, args);
        } catch (IOException e) {
            sendError(e);
        }
    }

    //运行lua函数
    public Object runFunc(String funcName, Object... args) {
        synchronized (L) {
            final int oldTop = L.getTop();
            try {
                L.getGlobal(funcName);
                if (!L.isFunction(-1)) {
                    return null;
                }
                for (Object arg : args) {
                    L.push(arg, Lua.Conversion.SEMI);
                }
                L.pCall(args.length, 1);
                return L.toObject(-1);
            } catch (LuaException e) {
                sendError(e);
                return null;
            } finally {
                L.setTop(oldTop);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent != null) {
            String name = intent.getStringExtra(LuaContext.LUA_NEW_ACTIVITY_NAME);
            if (name != null) {
                Object[] res = (Object[]) intent.getSerializableExtra(LuaContext.LUA_NEW_ACTIVITY_DATA);
                boolean ret;
                if (res == null) {
                    ret = onLuaEvent(mOnResult, name);
                } else {
                    Object[] arg = new Object[res.length + 1];
                    arg[0] = name;
                    System.arraycopy(res, 0, arg, 1, res.length);
                    ret = onLuaEvent(mOnResult, arg);
                }
                if (ret) return;
            }
        }
        onLuaEvent(mOnActivityResult, requestCode, resultCode, intent);
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void newActivity(String name, Object... args) {
        File file = new File(name);
        if (!file.exists()) file = new File(luaDir, name);
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(LuaContext.LUA_PATH, file.getPath());
        intent.putExtra(LuaContext.LUA_ARG, args);
        startActivity(intent);
    }

    public void newActivityForResult(String name, int requestCode, Object... args) {
        File file = new File(name);
        if (!file.exists()) file = new File(luaDir, name);
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(LuaContext.LUA_NEW_ACTIVITY_NAME, file.getPath());
        intent.putExtra(LuaContext.LUA_PATH, file.getPath());
        intent.putExtra(LuaContext.LUA_ARG, args);
        startActivityForResult(intent, requestCode);
    }

    public void setActivityResult(Object[] data) {
        Intent res = new Intent();
        res.putExtra(LuaContext.LUA_NEW_ACTIVITY_NAME, getIntent().getStringExtra(LuaContext.LUA_NEW_ACTIVITY_NAME));
        res.putExtra(LuaContext.LUA_NEW_ACTIVITY_DATA, data);
        setResult(0, res);
        finish();
    }

    public static void setClipboardText(String text) {
        LuaApplication.setClipboardText(text);
    }

    public static void setClipboardText(String label, String text) {
        LuaApplication.setClipboardText(label, text);
    }

    public static String getClipboardText() {
        return LuaApplication.getClipboardText();
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
        } else {
            showToast(message);
        }
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
        } else {
            showToast(message);
        }
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
    public void initializeLua() {
        LuaContext.super.initializeLua();
        if (!luaDir.equals(app.getLuaDir())) {
            luaCpath = luaCpath + luaDir + "/lib?.so;";
            luaLpath = luaLpath + luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;";
        }
        // package.path 和 cpath
        L.getGlobal("package");
        if (L.isTable(-1)) {
            L.push(luaLpath);
            L.setField(-2, "path");
            L.push(luaCpath);
            L.setField(-2, "cpath");
        }
        L.pop(1); // pop package 或 nil
        // 插入 LuaActivity
        L.pushJavaObject(this);
        L.pushValue(-1);
        L.setGlobal("activity");
        L.setGlobal("this");
        // 插入 LuaPrint
        LuaPrint print = new LuaPrint(this);
        L.push(print);
        L.setGlobal("print");
        // 插入 LuaApplication
        L.pushJavaObject(app);
        L.setGlobal("application");
    }
}
