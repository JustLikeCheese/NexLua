package com.nexlua;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.luajava.Lua;
import com.luajava.value.LuaValue;
import com.luajava.value.referable.LuaTable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.justlikecheese.nextoast.NexToast;

public class LuaApplication extends Application implements LuaContext {
    private static LuaApplication mApplication;
    private static final HashMap<String, Object> data = new HashMap<>();
    private static final String LUA_APPLICATION_ENTRY = "app.lua";
    private SharedPreferences mSharedPreferences;
    private File luaDir, luaFile;
    private String luaLpath, luaCpath;
    private Lua L;
    private LuaValue mOnTerminate, mOnLowMemory, mOnTrimMemory, mOnConfigurationChanged;
    private NexToast mToast;
    private StringBuilder mToastBuilder = new StringBuilder();
    private long mToastTime;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        mSharedPreferences = getSharedPreferences(this);
        // 初始化 LuaUtil, CrashHandler
        LuaUtil.init(this);
        CrashHandler.getInstance().init(this);
        // 获取 luaDir, luaFile, luaCpath, luaLpath
        luaDir = getFilesDir();
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
            LuaValue mOnCreate = L.getFunction("onCreate");
            mOnTerminate = L.getFunction("onTerminate");
            mOnLowMemory = L.getFunction("onLowMemory");
            mOnTrimMemory = L.getFunction("onTrimMemory");
            mOnConfigurationChanged = L.getFunction("onConfigurationChanged");
            if (mOnCreate != null) mOnCreate.call();
        } catch (Exception e) {
            sendError(e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mOnTerminate != null) mOnTerminate.call();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mOnLowMemory != null) mOnLowMemory.call();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (mOnTrimMemory != null) mOnTrimMemory.call(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mOnConfigurationChanged != null) mOnConfigurationChanged.call(newConfig);
    }

    public static LuaApplication getInstance() {
        return mApplication;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        // Android 7 及以上
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Context deContext = context.createDeviceProtectedStorageContext();
            if (deContext != null)
                return PreferenceManager.getDefaultSharedPreferences(deContext);
            else
                return PreferenceManager.getDefaultSharedPreferences(context);
        }
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public HashMap<String, Object> getGlobalData() {
        return data;
    }

    public Object getGlobalData(String key) {
        return data.get(key);
    }

    public Object getGlobalData(String key, Object def) {
        Object ret = data.get(key);
        if (ret == null)
            return def;
        return ret;
    }

    public void setGlobalData(String key, Object value) {
        data.put(key, value);
    }

    public Object getSharedData() {
        return mSharedPreferences.getAll();
    }

    public Object getSharedData(String key) {
        return mSharedPreferences.getAll().get(key);
    }

    public Object getSharedData(String key, Object def) {
        Object ret = mSharedPreferences.getAll().get(key);
        if (ret == null)
            return def;
        return ret;
    }

    public boolean setSharedData(String key, Object value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        if (value == null)
            edit.remove(key);
        else if (value instanceof String)
            edit.putString(key, value.toString());
        else if (value instanceof Long)
            edit.putLong(key, (Long) value);
        else if (value instanceof Integer)
            edit.putInt(key, (Integer) value);
        else if (value instanceof Float)
            edit.putFloat(key, (Float) value);
        else if (value instanceof Set)
            edit.putStringSet(key, (Set<String>) value);
        else if (value instanceof LuaTable) {
            LuaTable table = (LuaTable) value;
            HashSet<String> stringSet = new HashSet<>();
            for (Map.Entry<LuaValue, LuaValue> entry : table.toMap().entrySet()) {
                LuaValue val = entry.getValue();
                if (val != null) {
                    stringSet.add((String) val.toJavaObject());
                }
            }
            edit.putStringSet(key, stringSet);
        } else if (value instanceof Boolean)
            edit.putBoolean(key, (Boolean) value);
        else
            return false;
        edit.apply();
        return true;
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



