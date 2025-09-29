package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaPtrValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;


public class LuaLightUserdata extends AbstractLuaPtrValue {
    public LuaLightUserdata(Lua L) {
        super(L, LuaType.LIGHTUSERDATA);
    }

    public LuaLightUserdata(Lua L, int index) {
        super(L, LuaType.LIGHTUSERDATA, index);
    }

    public LuaLightUserdata(long ptr, Lua L) {
        super(ptr, L, LuaType.LIGHTUSERDATA);
    }

    @Override
    public void push(Lua L) {
        L.pushLightUserdata(ptr);
    }

    @Override
    public LuaValue copyTo(Lua L) {
        return new LuaLightUserdata(ptr, L);
    }

    @Override
    public boolean isLightUserdata() {
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
        push();
        Object obj = L.toJavaObject(-1);
        L.pop(1);
        return obj;
    }
}
