package com.nexlua;

import java.nio.Buffer;

public class Main2 implements LuaModule {
    public Buffer load(LuaContext luaContext) {
        String code = "function main(code)\n" +
                "    if code ~= nil then\n" +
                "        load(code)()\n" +
                "    end\n" +
                "end";
        return LuaModule.toBuffer(code);
    }
}