package com.nexlua.module;

import com.luajava.Lua;
import com.nexlua.LuaUtil;

public final class LuaAssetsModule extends LuaAbstractModule {
    private final String assetsName;

    public LuaAssetsModule(String path, String assetsName) {
        super(path);
        this.assetsName = assetsName;
    }

    @Override
    public int load(Lua L) throws Exception {
        byte[] content = LuaUtil.readAssetBytes(assetsName);
        L.loadStringBuffer(content, content.length, "@" + assetsName);
        return L.pCall(0, -1);
    }
}
