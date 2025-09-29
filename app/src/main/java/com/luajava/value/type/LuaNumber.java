package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

public class LuaNumber extends AbstractLuaValue {
    private final Number value;

    public LuaNumber(Lua L) {
        this(L, -1);
    }

    public LuaNumber(Lua L, int index) {
        super(L, LuaType.NUMBER);
        this.value = L.toNumber(index);
    }

    public LuaNumber(Number value, Lua L) {
        super(L, LuaType.NUMBER);
        this.value = value;
    }

    @Override
    public LuaValue copyTo(Lua L) {
        return new LuaNumber(value, L);
    }

    @Override
    public void push(Lua L) {
        L.push(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean toBoolean() {
        return value.longValue() != 0;
    }

    @Override
    public double toNumber() {
        return value.doubleValue();
    }

    @Override
    public long toInteger() {
        return value.longValue();
    }

    @Override
    public Object toJavaObject() {
        return value;
    }

    @Override
    public boolean isNumber() {
        return true;
    }
}
