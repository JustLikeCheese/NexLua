package com.nexlua;

import com.luajava.Lua;

public interface LuaModule {
    int load(Lua L, LuaContext luaContext);
}
