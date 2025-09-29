package com.luajava.value.type;

import static com.luajava.Lua.C;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaPtrValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

public class LuaCFunction extends AbstractLuaPtrValue {
    public LuaCFunction(Lua L) {
        super(L, LuaType.FUNCTION);
    }

    public LuaCFunction(Lua L, int index) {
        super(L, LuaType.FUNCTION, index);
    }

    public LuaCFunction(long value, Lua L) {
        super(value, L, LuaType.FUNCTION);
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public boolean isCFunction() {
        return true;
    }

    @Override
    public void push(Lua L) {
        L.checkStack(1);
        C.lua_pushcfunction(L.getPointer(), ptr);
    }

    @Override
    public LuaValue copyTo(Lua L) {
        return new LuaCFunction(ptr, L);
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
        return null;
    }
}