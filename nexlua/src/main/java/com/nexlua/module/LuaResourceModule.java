package com.nexlua.module;

import com.luajava.Lua;
import com.nexlua.LuaContext;
import com.nexlua.LuaUtil;

public final class LuaResourceModule extends LuaAbstractModule {
    private final int resId;

    public LuaResourceModule(String path, int resId) {
        super(path);
        this.resId = resId;
    }

    @Override
    public int load(Lua L, LuaContext luaContext) {
        int top = L.getTop();
        try {
            L.loadStringBuffer(LuaUtil.readRaw(resId), "@" + resId + ".lua");
            return L.pCall(0, -1);
        } catch (Exception e) {
            luaContext.sendError(e);
        } finally {
            L.setTop(top);
        }
        return 0;
    }
}
