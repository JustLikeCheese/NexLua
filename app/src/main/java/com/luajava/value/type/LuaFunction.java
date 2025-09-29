package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.Nullable;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

import java.nio.Buffer;

public class LuaFunction extends AbstractLuaRefValue {
    private Buffer bufferCache;

    public LuaFunction(Lua L) {
        super(L, LuaType.FUNCTION);
    }

    public LuaFunction(Lua L, int index) {
        super(L, LuaType.FUNCTION, index);
    }

    public LuaFunction(int ref, Lua L) {
        super(ref, L, LuaType.FUNCTION);
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public LuaValue copyTo(Lua L1) {
        push();
        if (L.copyFunction(L1)) {
            int ref = L1.ref();
            return new LuaFunction(ref, L1);
        }
        return null;
    }

    @Override
    public Buffer dump() {
        if (bufferCache == null) {
            push();
            bufferCache = L.dump();
        }
        return bufferCache;
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
