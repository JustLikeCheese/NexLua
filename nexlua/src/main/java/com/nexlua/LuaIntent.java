package com.nexlua;

import android.content.Intent;

import com.nexlua.module.LuaModule;

import java.io.File;
import java.io.Serializable;

public class LuaIntent implements Serializable {
    public static final String NAME = "com.nexlua.LuaIntent";
    public final Object[] args;
    public final LuaModule module;
    public final LuaContext context;

    public LuaIntent(LuaContext context, LuaModule module, Object... args) {
        this.context = context;
        this.module = module;
        this.args = args;
    }

    public LuaIntent(LuaContext context, String path, Object... args) {
        this(context, context.getConfig().get(path), args);
    }

    public static Intent newIntent(LuaIntent intent) {
        return new Intent().putExtra(NAME, intent);
    }

    public static Intent newIntent(LuaContext context, LuaModule module, Object... args) {
        return newIntent(new LuaIntent(context, module, args));
    }

    public static Intent newIntent(LuaContext context, String path, Object... args) {
        return newIntent(new LuaIntent(context, path, args));
    }

    public static LuaIntent from(Intent intent) {
        return (LuaIntent) intent.getSerializableExtra(NAME);
    }
}
