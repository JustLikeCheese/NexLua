package com.nexlua;

import com.luajava.ExternalLoader;
import com.luajava.Lua;
import com.luajava.LuaException;

public class LuaModuleLoader implements ExternalLoader {
    private LuaContext luaContext;

    public LuaModuleLoader(LuaContext luaContext) {
        this.luaContext = luaContext;
    }

    @Override
    public int load(Lua L, String moduleName) throws LuaException {
        Class<?> clazz = LuaConfig.LUA_DEX_MAP.get(moduleName);
        if (clazz == null) {
            String luaDir = luaContext.getLuaDir().getAbsolutePath(); // /data/data/xxx/files
            if (moduleName.startsWith(luaDir)) // maybe /data/data/xxx/files/test.lua, but test.lua has bean moved to lua dex map
                clazz = LuaConfig.LUA_DEX_MAP.get(moduleName.substring(luaDir.length() + 1));
        }
        if (clazz != null) {
            try {
                LuaModule module = (LuaModule) clazz.newInstance();
                return module.load(L, luaContext);
            } catch (Exception e) {
                luaContext.sendError(e);
            }
        }
        return 0;
    }
}
