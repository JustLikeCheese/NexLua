package com.nexlua.module;

import com.luajava.Lua;

public final class LuaStringModule extends LuaAbstractModule {
    private final String content;
    private final int length;

    public LuaStringModule(String path, String content) {
        super(path);
        this.content = content;
        this.length = content.length();
    }

    @Override
    public int load(Lua L) throws Exception {
        L.loadStringBuffer(content, length, "@" + path);
        return L.pCall(0, -1);
    }
}
