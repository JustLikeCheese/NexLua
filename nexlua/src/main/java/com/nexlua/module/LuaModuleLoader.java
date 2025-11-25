package com.nexlua.module;

import com.luajava.ExternalLoader;
import com.luajava.Lua;
import com.luajava.LuaException;
import com.nexlua.LuaContext;

public class LuaModuleLoader implements ExternalLoader {
    private final LuaContext luaContext;

    public LuaModuleLoader(LuaContext luaContext) {
        this.luaContext = luaContext;
    }

    @Override
    public int load(Lua L, String moduleName) throws LuaException {
        return luaContext.getConfig().load(luaContext, L, moduleName);
    }
}
