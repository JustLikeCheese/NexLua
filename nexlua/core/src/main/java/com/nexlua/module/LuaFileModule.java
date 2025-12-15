package com.nexlua.module;

import com.luajava.Lua;
import com.nexlua.LuaUtil;

import java.io.File;

public final class LuaFileModule extends LuaAbstractModule {
    private final File file;

    public LuaFileModule(String path, File file) {
        super(path);
        this.file = file;
    }

    @Override
    public int load(Lua L) throws Exception {
        byte[] content = LuaUtil.readFileBytes(file);
        L.loadStringBuffer(content, content.length, "@" + path);
        L.insert(-2);
        return L.pCall(1, -1);
    }
}
