package com.nexlua;

import android.content.Context;
import android.util.Log;

import com.luajava.Lua;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LuaConfig {
    public static final int APP_THEME = android.R.style.Theme_Material_Light;
    public static final int WELCOME_THEME = android.R.style.Theme_Material_Light;
    // 在 Welcome 启动时申请的权限
    public static final String[] REQUIRED_PERMISSIONS_IN_WELCOME = new String[]{
            // Manifest.permission.INTERNET,
            // Manifest.permission.WRITE_EXTERNAL_STORAGE,
            // Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    // 在 Main 启动时申请的权限
    public static final String[] REQUIRED_PERMISSIONS = new String[]{
            // Manifest.permission.INTERNET,
            // Manifest.permission.WRITE_EXTERNAL_STORAGE,
            // Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    public static final String[] ONLY_DECOMPRESS = new String[]{
            // 指定解压的文件
            // "res/gradle.tar.xz"
    };
    public static final String[] SKIP_DECOMPRESS = new String[]{
            // 跳过解压的文件
            // "res/gradle.tar.xz"
    };
    // Lua 入口文件
    // 抽离到 Dex 的 Lua 的映射表
    public static Map<String, Class<?>> LUA_DEX_MAP;
    public static File LUA_ENTRY;
    public static File LUA_ROOT_DIR;
    public static File FILES_DIR;

    public static void onConfig(Context context) {
        if (FILES_DIR != null) return;
        FILES_DIR = context.getFilesDir();
        // Lua Root Dir
        LUA_ROOT_DIR = FILES_DIR;
        LUA_ENTRY = new File(LUA_ROOT_DIR, "main.lua");
        // Lua Module: Put your modules here
        Map<String, Class<?>> map = new HashMap<>();
        map.put(new File(LUA_ROOT_DIR, "main2.lua").getAbsolutePath(), Main2.class);
        LUA_DEX_MAP = Collections.unmodifiableMap(map);
    }

    public static LuaModule getModule(String path) {
        Lua.log("我操你妈b" + LUA_DEX_MAP);
        Class<?> module = LUA_DEX_MAP.get(path);
        if (module != null) {
            try {
                return (LuaModule) module.newInstance();
            } catch (IllegalAccessException | InstantiationException ignored) {
            }
        }
        return null;
    }
}
