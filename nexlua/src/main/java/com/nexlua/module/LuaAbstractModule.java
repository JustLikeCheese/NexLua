package com.nexlua.module;

import androidx.annotation.NonNull;

public abstract class LuaAbstractModule implements LuaModule {
    protected final String path;
    public LuaAbstractModule(@NonNull String path) {
        this.path = path;
    }

    @Override
    public @NonNull String getAbsolutePath() {
        return path;
    }
}
