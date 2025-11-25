package com.nexlua.module;

public abstract class LuaAbstractModule implements LuaModule {
    protected final String path;
    public LuaAbstractModule(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
