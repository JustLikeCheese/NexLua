package com.nexlua;

import com.luajava.Lua;

public class Main2 implements LuaModule {
    public int load(final Lua L, final LuaContext luaContext) {
        L.doString("function main(code)\n" +
                "    if code ~= nil then\n" +
                "        load(code)()\n" +
                "    end\n" +
                "end\n");
        return 0;
    }
}