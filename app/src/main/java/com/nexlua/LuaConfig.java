package com.nexlua;

import android.Manifest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LuaConfig {
    public static final int APP_THEME = android.R.style.Theme_Material_Light;
    public static final int WELCOME_THEME = android.R.style.Theme_Material_Light;
    // 在 Welcome 启动时申请的权限
    public static final String[] REQUIRED_PERMISSIONS_IN_WELCOME = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
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
    public static final String LUA_ENTRY = "main.lua";
    // 抽离到 Dex 的 Lua 的映射表
    public static final Map<String, Class<?>> LUA_DEX_MAP;

    static {
        Map<String, Class<?>> map = new HashMap<>();
        map.put("main2.lua", com.nexlua.Main2.class);
        LUA_DEX_MAP = Collections.unmodifiableMap(map);
    }
}
