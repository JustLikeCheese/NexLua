package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

public class LuaBoolean extends AbstractLuaValue {
    private final boolean value;

    public LuaBoolean(Lua L) {
        this(L, -1);
    }

    public LuaBoolean(Lua L, int index) {
        super(L, LuaType.BOOLEAN);
        this.value = L.toBoolean(index);
    }

    public LuaBoolean(boolean value, Lua L) {
        super(L, LuaType.BOOLEAN);
        this.value = value;
    }

    @Override
    public LuaValue copyTo(Lua L) {
        return new LuaBoolean(value, L);
    }

    @Override
    public void push(Lua L) {
        L.push(value);
    }

    @Override
    public String toString() {
        return value
                ? "true"
                : "false";
    }

    @Override
    public boolean toBoolean() {
        return value;
    }

    @Override
    public long toInteger() {
        return value ? 1 : 0;
    }

    @Override
    public double toNumber() {
        return value ? 1.0 : 0.0;
    }

    @Override
    public Object toJavaObject() {
        return value;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }
}
