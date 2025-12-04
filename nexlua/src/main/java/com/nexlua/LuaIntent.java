package com.nexlua;

import android.content.Intent;

import com.nexlua.module.LuaModule;

import java.io.Serializable;

public class LuaIntent implements Serializable {
    public static final String NAME = "com.nexlua.LuaIntent";
    public final Serializable[] args;
    public final LuaModule module;

    public LuaIntent(LuaModule module, Serializable... args) {
        if (module == null) {
            throw new RuntimeException("module cannot be null");
        }
        this.module = module;
        this.args = args;
    }

    public LuaIntent(LuaContext context, String name, Serializable... args) {
        this(context.getConfig().get(context.getLuaDir(), name), args);
    }

    public static LuaIntent from(Intent intent) {
        return (LuaIntent) intent.getSerializableExtra(NAME);
    }
}
