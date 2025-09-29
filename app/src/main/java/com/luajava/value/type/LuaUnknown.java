package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaType;

public class LuaUnknown extends AbstractLuaRefValue {
    public LuaUnknown(Lua L, LuaType type) {
        super(L, type);
    }

    public LuaUnknown(Lua L, LuaType type, int index) {
        super(L, type, index);
    }

    public LuaUnknown(int ref, Lua L, LuaType type) {
        super(ref, L, type);
    }
}
