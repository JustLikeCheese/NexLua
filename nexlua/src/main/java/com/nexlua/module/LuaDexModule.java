package com.nexlua.module;

import com.luajava.Lua;
import com.nexlua.LuaContext;

public final class LuaDexModule extends LuaAbstractModule {
    private final String content;

    public LuaDexModule(String path, String content) {
        super(path);
        this.content = content;
    }

    @Override
    public int load(Lua L, LuaContext luaContext) {
        int top = L.getTop();
        try {
            L.loadStringBuffer(content, "@" + path);
            return L.pCall(0, -1);
        } catch (Exception e) {
            luaContext.sendError(e);
        } finally {
            L.setTop(top);
        }
        return 0;
    }
}
