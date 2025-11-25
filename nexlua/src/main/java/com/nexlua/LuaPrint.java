package com.nexlua;

import com.luajava.CFunction;
import com.luajava.Lua;

public class LuaPrint implements CFunction {
    private final LuaContext mLuaContext;
    private final StringBuilder output = new StringBuilder();

    public LuaPrint(LuaContext luaContext) {
        super();
        mLuaContext = luaContext;
    }

    @Override
    public int __call(Lua L) {
        int top = L.getTop();
        if (top > 0) {
            output.append(L.LtoString(1));
            for (int i = 2; i <= top; i++) {
                output.append("\t");
                output.append(L.LtoString(i));
            }
            mLuaContext.sendMessage(output.toString());
            output.setLength(0);
        } else {
            mLuaContext.sendMessage("");
        }
        return 0;
    }
}