package com.nexlua.module;

import com.luajava.Lua;

import java.io.File;

public final class LuaFileModule extends LuaAbstractModule {
    private final File file;

    public LuaFileModule(String path, File file) {
        super(path);
        this.file = file;
    }

    @Override
    public int load(Lua L) throws Exception {
        L.loadFile(file.getAbsolutePath());
        return L.pCall(0, -1);
    }
}
