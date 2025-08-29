package com.nexlua;

import com.luajava.ExternalLoader;
import com.luajava.Lua;

import java.nio.Buffer;

public class LuaModuleLoader implements ExternalLoader {
    private LuaContext luaContext;

    public LuaModuleLoader(LuaContext luaContext) {
        this.luaContext = luaContext;
    }

    @Override
    public Buffer load(String module, Lua L) {
        Class<?> clazz = LuaConfig.LUA_DEX_MAP.get(module);
        if (clazz == null) {
            String luaDir = luaContext.getLuaDir().getAbsolutePath();
            if (module.startsWith(luaDir))
                clazz = LuaConfig.LUA_DEX_MAP.get(module.substring(luaDir.length() + 1));
        }
        if (clazz != null) {
            try {
                return ((LuaModule) clazz.newInstance()).load(luaContext);
            } catch (Exception e) {
                luaContext.sendError(e);
            }
        }
        return null;
    }
}
