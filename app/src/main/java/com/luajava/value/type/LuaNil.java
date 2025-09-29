package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

public class LuaNil extends AbstractLuaValue {
    public LuaNil(Lua L) {
        super(L, LuaType.NIL);
    }

    @Override
    public void push(Lua L) {
        L.pushNil();
    }

    @Override
    public LuaValue copyTo(Lua L) {
        return new LuaNil(L);
    }

    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public boolean toBoolean() {
        return false;
    }

    @Override
    public double toNumber() {
        return 0;
    }

    @Override
    public long toInteger() {
        return 0;
    }

    @Override
    public Object toJavaObject() {
        return null;
    }

    @Override
    public boolean isNil() {
        return true;
    }
}
