package com.nexlua;

import android.content.Intent;

import java.io.Serializable;

public class LuaIntent implements Serializable {
    public static final String NAME = "com.nexlua.LuaIntent";
    public final int theme;
    public final String name;
    public final Object[] args;

    public LuaIntent(int theme, String name, Object[] args) {
        this.theme = theme;
        this.name = name;
        this.args = args;
    }

    public LuaIntent(int theme, String name) {
        this(theme, name, null);
    }

    public LuaIntent(String name, Object[] args) {
        this(LuaConfig.APP_THEME, name, args);
    }

    public LuaIntent(String name) {
        this(LuaConfig.APP_THEME, name, null);
    }

    public static LuaIntent from(Intent intent) {
        return intent.getSerializableExtra(NAME, LuaIntent.class);
    }
}
