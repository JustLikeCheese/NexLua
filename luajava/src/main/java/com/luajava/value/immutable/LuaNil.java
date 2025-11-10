package com.luajava.value.immutable;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

public class LuaNil extends AbstractLuaValue {
    public LuaNil(Lua L) {
        super(L, LuaType.NIL);
    }

    public LuaNil(Lua L, int index) {
        super(L, LuaType.NIL);
    }

    public static LuaNil from(Lua L) {
        if (L.NIL != null)
            return L.NIL;
        return new LuaNil(L);
    }

    @Override
    public int push(Lua L) {
        return L.pushNil();
    }

    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public String ltoString() {
        return toString();
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
    public boolean isNil() {
        return true;
    }

    @Override
    public LuaNil checkNil() {
        return this;
    }

    @Override
    public Object toJavaObject() {
        return null;
    }

    @Override
    public boolean isJavaObject(Class<?> clazz) {
        return clazz == Object.class
                || clazz == LuaValue.class
                || clazz == LuaNil.class;
    }

    @Override
    public Object toJavaObject(Class<?> clazz) throws IllegalArgumentException {
        if (clazz == LuaValue.class || clazz == LuaNil.class)
            return this;
        else if (clazz == Object.class)
            return null;
        return super.toJavaObject(clazz);
    }
}
