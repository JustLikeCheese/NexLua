package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaType;

public class LuaUserdata extends AbstractLuaRefValue {
    public LuaUserdata(Lua L) {
        super(L, LuaType.USERDATA);
    }

    public LuaUserdata(Lua L, int index) {
        super(L, LuaType.USERDATA, index);
    }

    public LuaUserdata(int ref, Lua L) {
        super(ref, L, LuaType.USERDATA);
    }

    @Override
    public boolean isUserdata() {
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