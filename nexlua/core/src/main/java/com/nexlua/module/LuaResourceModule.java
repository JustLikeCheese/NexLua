package com.nexlua.module;

import com.nexlua.LuaUtil;

public final class LuaResourceModule extends LuaCacheModule {
    private final int resId;

    public LuaResourceModule(String path, int resId) {
        super(path);
        this.resId = resId;
    }

    @Override
    public byte[] getBytes() throws Exception {
        return LuaUtil.readRawBytes(resId);
    }
}
