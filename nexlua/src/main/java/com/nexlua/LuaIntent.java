package com.nexlua;

import android.content.Intent;

import java.io.File;
import java.io.Serializable;

public class LuaIntent implements Serializable {
    public static final String NAME = "com.nexlua.LuaIntent";
    public final int theme;
    public final File file;
    public final Object[] args;

    // Keep Theme
    public LuaIntent(int theme, File file, Object[] args) {
        this.theme = theme;
        this.file = file;
        this.args = args;
    }

    public LuaIntent(int theme, File file) {
        this(theme, file, null);
    }

    public LuaIntent(int theme, Object[] args) {
        this(theme, null, args);
    }

    public LuaIntent(int theme) {
        this(theme, null, null);
    }

    // Keep Name
    public LuaIntent(File file, Object[] args) {
        this(LuaConfig.APP_THEME, file, args);
    }

    public LuaIntent(File file) {
        this(LuaConfig.APP_THEME, file, null);
    }

    // Keep Args
    public LuaIntent(Object[] args) {
        this(LuaConfig.APP_THEME, null, args);
    }

    public static LuaIntent from(Intent intent) {
        return (LuaIntent) intent.getSerializableExtra(NAME);
    }
}
