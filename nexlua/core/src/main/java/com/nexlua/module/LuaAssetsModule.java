package com.nexlua.module;

import com.nexlua.LuaUtil;

public final class LuaAssetsModule extends LuaCacheModule {
    private final String assetsName;

    public LuaAssetsModule(String path, String assetsName) {
        super(path);
        this.assetsName = assetsName;
    }

    @Override
    public byte[] getBytes() throws Exception {
        return LuaUtil.readAssetBytes(assetsName);
    }
}