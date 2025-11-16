package com.nexlua;

import com.luajava.ExternalLoader;
import com.luajava.Lua;
import com.luajava.LuaException;

import java.io.File;

public class LuaModuleLoader implements ExternalLoader {
    private final LuaContext luaContext;

    public LuaModuleLoader(LuaContext luaContext) {
        this.luaContext = luaContext;
    }

    @Override
    public int load(Lua L, String moduleName) throws LuaException {
        try {
            LuaModule module = LuaConfig.getModule(new File(luaContext.getLuaDir(), moduleName));
            if (module != null) {
                return module.load(L, luaContext);
            }
        } catch (Exception e) {
            luaContext.sendError(e);
        }
        return 0;
    }
}
