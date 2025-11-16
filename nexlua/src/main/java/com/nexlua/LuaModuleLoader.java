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
        int top = L.getTop();
        try {
            File file1 = new File(moduleName);
            File file2 = new File(luaContext.getLuaDir(), moduleName);
            LuaModule module = LuaConfig.getModule(file1.getAbsolutePath());
            LuaModule module2 = LuaConfig.getModule(file2.getAbsolutePath());
            if (module != null) {
                return module.load(L, luaContext);
            } else if (module2 != null) {
                return module2.load(L, luaContext);
            } else if (file1.exists()) {
                L.doFile(file1.getAbsolutePath());
            } else if (file2.exists()) {
                L.doFile(file2.getAbsolutePath());
            } else {
                throw new LuaException(LuaException.LuaError.FILE, "module not found: " + moduleName);
            }
            int nArgs = L.getTop() - top;
            if (nArgs > 0) {
                return nArgs;
            }
        } catch (Exception e) {
            luaContext.sendError(e);
        } finally {
            L.setTop(top);
        }
        return 0;
    }
}
