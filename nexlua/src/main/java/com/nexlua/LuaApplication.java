package com.nexlua;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.Toast;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.referable.LuaFunction;
import com.nexlua.module.LuaModule;
import com.nexlua.module.LuaModuleLoader;

import java.io.File;
import java.util.ArrayList;

import io.github.justlikecheese.nextoast.NexToast;

public class LuaApplication extends Application implements LuaContext {
    protected static LuaApplication mApplication;
    protected String luaPath, luaDir, luaLpath, luaCpath;
    protected Lua L;
    protected LuaFunction mOnTerminate, mOnLowMemory, mOnTrimMemory, mOnConfigurationChanged;
    protected NexToast mToast;
    protected StringBuilder mToastBuilder = new StringBuilder();
    protected long mToastTime;
    protected LuaConfig config;
    protected LuaModule module;
    protected String baseCpath;
    protected String baseLpath;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        // 初始化 LuaUtil, CrashHandler
        LuaUtil.init(this);
        // 获取 luaDir, luaFile, luaCpath, luaLpath
        config = getConfig();
        module = config.application;
        luaPath = module.getPath();
        luaDir = LuaUtil.getParentPath(luaPath);
        String luaLibDir = new File(luaDir, "lua").getAbsolutePath();
        String libDir = new File(luaDir, "lib").getAbsolutePath();
        baseCpath = getApplicationInfo().nativeLibraryDir + "/lib?.so;" + libDir + "/lib?.so;";
        baseLpath = luaLibDir + "/?.lua;" + luaLibDir + "/lua/?.lua;" + luaLibDir + "/?/init.lua;";
        luaCpath = getLuaCpath(luaDir);
        luaLpath = getLuaLpath(luaDir);
        try {
            L = new Lua();
            initialize(L);
            loadLua();
        } catch (Exception e) {
            sendError(e);
        }
    }

    public void loadLua() throws Exception {
        module.load(L, this);
    }

    protected boolean onLuaEvent(LuaFunction event, Object... args) {
        if (event != null) {
            try {
                return event.vpCall(args, Lua.Conversion.SEMI, 1)[0].toBoolean();
            } catch (LuaException e) {
                sendError(e);
            }
        }
        return false;
    }

    protected boolean runFunc(String funcName, Object... args) throws LuaException {
        return onLuaEvent(L.getLuaFunction(funcName), args);
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

    public ArrayList<ClassLoader> getClassLoaders() {
        return null;
    }

    public Lua getLua() {
        return L;
    }

    @Override
    public LuaConfig getConfig() {
        if (config == null) {
            config = new LuaConfig(this);
        }
        return config;
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

    public String getBaseLpath() {
        return baseLpath;
    }

    public String getBaseCpath() {
        return baseCpath;
    }

    public String getLuaLpath(String luaDir) {
        return getBaseLpath() + luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;";
    }

    public String getLuaCpath(String luaDir) {
        return getBaseCpath() + luaDir + "/lib?.so;";
    }

    public Context getContext() {
        return this;
    }

    @Override
    public void initialize(Lua L) throws LuaException {
        L.openLibraries();
        L.setExternalLoader(new LuaModuleLoader(this));
        // Lua Application
        L.getGlobal("package");
        if (L.isTable(1)) {
            L.setField(1, "cpath", luaCpath);
            L.setField(1, "path", luaLpath);
            L.pop(1);
        }
        L.pushGlobal(this, "application", "app", "this");
        mOnTerminate = L.getLuaFunction("onTerminate");
        mOnLowMemory = L.getLuaFunction("onLowMemory");
        mOnTrimMemory = L.getLuaFunction("onTrimMemory");
        mOnConfigurationChanged = L.getLuaFunction("onConfigurationChanged");
        runFunc("onCreate");
    }
}
