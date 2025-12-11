package com.nexlua.module;

import com.luajava.Lua;

public final class LuaDexModule extends LuaAbstractModule {
    private final String content;
    private final int length;

    public LuaDexModule(String path, String content) {
        super(path);
        this.content = content;
        this.length = content.length();
    }

    @Override
    public int load(Lua L) throws Exception {
        int top = L.getTop();
        try {
            L.loadStringBuffer(content, length, "@" + path);
            return L.pCall(0, -1);
        } finally {
            L.setTop(top);
        }
    }
}
