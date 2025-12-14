package com.nexlua.module;

import com.nexlua.LuaUtil;

import java.io.File;

public final class LuaFileModule extends LuaCacheModule {
    private final File file;

    public LuaFileModule(String path, File file) {
        super(path);
        this.file = file;
    }

    @Override
    public byte[] getBytes() throws Exception {
        return LuaUtil.readFileBytes(file);
    }
}
