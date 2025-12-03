package com.luajava.value.immutable;

import androidx.annotation.NonNull;

import com.luajava.Lua;
import com.luajava.LuaException;
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
    public int push(Lua L) throws LuaException {
        return L.pushNil();
    }

    @NonNull
    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public String LtoString() {
        return toString();
    }

    @Override
    public boolean toBoolean() {
        return false;
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
                || clazz == LuaNil.class
                || !clazz.isPrimitive();
    }

    @Override
    public Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == LuaValue.class || clazz == LuaNil.class)
            return this;
        else if (clazz == Object.class)
            return null;
        else if (clazz == Boolean.class || clazz == boolean.class)
            return false;
        else if (!clazz.isPrimitive())
            return null;
        return super.toJavaObject(clazz);
    }
}
