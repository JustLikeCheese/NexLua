package com.nexlua;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public interface LuaModule {
    Buffer load(LuaContext luaContext);
    static Buffer toBuffer(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }
}
