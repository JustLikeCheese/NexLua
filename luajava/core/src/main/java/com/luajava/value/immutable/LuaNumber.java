package com.luajava.value.immutable;

import androidx.annotation.NonNull;

import com.luajava.Lua;
import com.luajava.LuaException;
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

    private LuaNumber(Number value, Lua L) {
        super(L, LuaType.NUMBER);
        this.value = value;
    }

    public static LuaNumber from(Lua L, Number value) {
        return new LuaNumber(value, L);
    }

    @Override
    public int push(Lua L) throws LuaException {
        return L.push(value.doubleValue());
    }

    @NonNull
    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public String LtoString() {
        return toString();
    }

    @Override
    public boolean toBoolean() {
        return true;
    }

    @Override
    public long toInteger() {
        return value.longValue();
    }

    @Override
    public double toNumber() {
        return value.doubleValue();
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public LuaNumber checkNumber() {
        return this;
    }

    @Override
    public Object toJavaObject() {
        return value;
    }

    @Override
    public boolean isJavaObject(Class<?> clazz) {
        return clazz == Object.class
                || clazz == LuaValue.class
                || clazz == LuaNumber.class
                || clazz == byte.class || clazz == Byte.class
                || clazz == short.class || clazz == Short.class
                || clazz == int.class || clazz == Integer.class
                || clazz == long.class || clazz == Long.class
                || clazz == float.class || clazz == Float.class
                || clazz == double.class || clazz == Double.class
                || clazz == char.class || clazz == Character.class
                || clazz == Number.class;
    }

    @Override
    public Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == LuaValue.class || clazz == LuaNumber.class)
            return this;
        else if (clazz == Object.class)
            return toNumber();
        else if (clazz == Number.class)
            return value;
        else if (clazz == long.class || clazz == Long.class)
            return value.longValue();
        else if (clazz == int.class || clazz == Integer.class)
            return value.intValue();
        else if (clazz == short.class || clazz == Short.class)
            return value.shortValue();
        else if (clazz == byte.class || clazz == Byte.class)
            return value.byteValue();
        else if (clazz == char.class || clazz == Character.class)
            return (char) value.intValue();
        else if (clazz == float.class || clazz == Float.class)
            return value.floatValue();
        else if (clazz == double.class || clazz == Double.class)
            return value.doubleValue();
        return super.toJavaObject(clazz);
    }
}
