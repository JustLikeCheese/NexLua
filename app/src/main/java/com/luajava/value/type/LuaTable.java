package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaType;

public class LuaTable extends AbstractLuaRefValue {
    public LuaTable(Lua L) {
        super(L, LuaType.TABLE);
    }

    public LuaTable(Lua L, int index) {
        super(L, LuaType.TABLE, index);
    }

    public LuaTable(int ref, Lua L) {
        super(ref, L, LuaType.TABLE);
    }

    @Override
    public boolean isTable() {
        return true;
    }

    @Override
    public boolean toBoolean() {
        return true;
    }

    @Override
    public long toInteger() {
        return 0;
    }

    @Override
    public double toNumber() {
        return 0;
    }

    @Override
    public Object toJavaObject() {
        // TODO: Table to map
        return null;
    }
}
