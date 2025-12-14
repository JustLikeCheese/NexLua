package com.nexlua.module;

import com.luajava.Lua;

import java.lang.ref.SoftReference;

public abstract class LuaCacheModule extends LuaAbstractModule {
    protected transient SoftReference<byte[]> cache;

    public LuaCacheModule(String path) {
        super(path);
    }

    public abstract byte[] getBytes() throws Exception;

    @Override
    public int __call(Lua L) throws Exception {
        byte[] content;
        if (cache != null) {
            content = this.cache.get();
        } else {
            content = getBytes();
            this.cache = new SoftReference<>(content);
        }
        L.loadStringBuffer(content, content.length, "@" + path);
        L.insert(-2);
        return L.pCall(1, -1);
    }
}
