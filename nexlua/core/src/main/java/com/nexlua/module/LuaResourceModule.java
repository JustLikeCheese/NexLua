package com.nexlua.module;

import com.luajava.Lua;
import com.nexlua.LuaUtil;

public final class LuaResourceModule extends LuaAbstractModule {
    private final int resId;

    public LuaResourceModule(String path, int resId) {
        super(path);
        this.resId = resId;
    }

    @Override
    public int load(Lua L) throws Exception {
        byte[] content = LuaUtil.readRawBytes(resId);
        L.loadStringBuffer(content, content.length, "@" + resId + ".lua");
        return L.pCall(0, -1);
    }
}
