package com.nexlua;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.widget.Toast;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.LuaValue;
import com.luajava.value.referable.LuaFunction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.justlikecheese.nextoast.NexToast;

public class LuaApplication extends Application implements LuaContext {
    private static LuaApplication mApplication;
    private static final HashMap<String, Object> data = new HashMap<>();
    private static final String LUA_APPLICATION_ENTRY = "app.lua";
    private File luaDir, luaFile;
    private String luaLpath, luaCpath;
    private Lua L;
    private LuaFunction mOnTerminate, mOnLowMemory, mOnTrimMemory, mOnConfigurationChanged;
    private NexToast mToast;
    private StringBuilder mToastBuilder = new StringBuilder();
    private long mToastTime;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        // 初始化 LuaUtil, CrashHandler
        LuaUtil.init(this);
        LuaConfig.onConfig(this);
        CrashHandler.getInstance().init(this);
        // 获取 luaDir, luaFile, luaCpath, luaLpath
        luaDir = LuaConfig.LUA_ROOT_DIR;
        luaFile = new File(luaDir, LUA_APPLICATION_ENTRY);
        File luaLibDir = getDir("lua", Context.MODE_PRIVATE);
        File libDir = getDir("lib", Context.MODE_PRIVATE);
        luaCpath = getApplicationInfo().nativeLibraryDir + "/lib?.so;" + libDir + "/lib?.so;";
        luaLpath = luaLibDir + "/?.lua;" + luaLibDir + "/lua/?.lua;" + luaLibDir + "/?/init.lua;";
        try {
            Class<?> clazz = LuaConfig.LUA_DEX_MAP.get(LUA_APPLICATION_ENTRY);
            if (clazz != null) {
                initializeLua();
                LuaModule module = (LuaModule) clazz.newInstance();
                module.load(L, this);
            } else if (luaFile.exists()) {
                initializeLua();
                L.loadBuffer(LuaUtil.readFileBuffer(luaFile), luaFile.getPath());
            } else {
                return;
            }
            mOnTerminate = L.getLuaFunction("onTerminate");
            mOnLowMemory = L.getLuaFunction("onLowMemory");
            mOnTrimMemory = L.getLuaFunction("onTrimMemory");
            mOnConfigurationChanged = L.getLuaFunction("onConfigurationChanged");
            runFunc("onCreate");
        } catch (Exception e) {
            sendError(e);
        }
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

    @Override
    public void onTerminate() {
        super.onTerminate();
        onLuaEvent(mOnTerminate);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        onLuaEvent(mOnLowMemory);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        onLuaEvent(mOnTrimMemory);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onLuaEvent(mOnConfigurationChanged, newConfig);
    }

    public static LuaApplication getInstance() {
        return mApplication;
    }

    @Override
    public void showToast(String message) {
        long now = System.currentTimeMillis();
        if (mToast == null || now - mToastTime > 1000) {
            mToastBuilder.setLength(0);
            mToast = NexToast.makeText(this, message, Toast.LENGTH_LONG);
            mToastBuilder.append(message);
            mToast.show();
        } else {
            mToastBuilder.append("\n");
            mToastBuilder.append(message);
            mToast.setText(mToastBuilder.toString());
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToastTime = now;
    }

    // @formatter:off
    public ArrayList<ClassLoader> getClassLoaders() { return null; }
    public Lua getLua() { return L; }
    public File getLuaFile() { return luaFile; }
    public File getLuaDir() { return luaDir; }
    public String getLuaPath() { return luaFile.getPath(); }
    public String getLuaLpath() { return luaLpath; }
    public String getLuaCpath() { return luaCpath; }
    public Context getContext() { return this; }
    // @formatter:on
    public void initializeLua() {
        L = new Lua();
        L.openLibraries();
        L.setExternalLoader(new LuaModuleLoader(this));
        // Lua Application
        // package.path 和 cpath
        L.getGlobal("package");
        if (L.isTable(-1)) {
            L.push(getLuaLpath());
            L.setField(-2, "path");
            L.push(getLuaCpath());
            L.setField(-2, "cpath");
        }
        L.pushJavaObject(this);
        L.pushValue(-1);
        L.setGlobal("application");
        L.setGlobal("this");
    }

    public static void setClipboardText(String text) {
        setClipboardText("text", text);
    }

    @SuppressLint("ObsoleteSdkInt")
    @SuppressWarnings("deprecation")
    public static void setClipboardText(String label, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        } else {
            android.text.ClipboardManager clipboard =
                    (android.text.ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @SuppressWarnings("deprecation")
    public static String getClipboardText() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    if (clip.getItemCount() > 0) {
                        CharSequence text = clip.getItemAt(0).getText();
                        if (text != null) return text.toString();
                    }
                }
            }
        } else {
            android.text.ClipboardManager clipboard =
                    (android.text.ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.hasText()) {
                CharSequence text = clipboard.getText();
                if (text != null) return text.toString();
            }
        }
        return null;
    }
}



