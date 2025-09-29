package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaType;

public class LuaThread extends AbstractLuaRefValue {
    public LuaThread(Lua L) {
        super(L, LuaType.THREAD);
    }

    public LuaThread(Lua L, int index) {
        super(L, LuaType.THREAD, index);
    }

    @Override
    public boolean isThread() {
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
        return null;
    }
}
