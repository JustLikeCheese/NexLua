package com.nexlua.module;

import com.luajava.Lua;
import com.nexlua.LuaContext;

import java.io.File;

public final class LuaFileModule extends LuaAbstractModule {
    private final File file;

    public LuaFileModule(String path, File file) {
        super(path);
        this.file = file;
    }

    @Override
    public int load(Lua L, LuaContext luaContext) {
        int top = L.getTop();
        try {
            L.loadFile(file.getAbsolutePath());
            return L.pCall(0, -1);
        } catch (Exception e) {
            luaContext.sendError(e);
        } finally {
            L.setTop(top);
        }
        return 0;
    }
}
