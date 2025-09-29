package com.luajava.value;

import com.luajava.Lua;

public abstract class AbstractLuaPtrValue extends AbstractLuaValue {
    protected final long ptr;

    public AbstractLuaPtrValue(Lua L, LuaType type) {
        this(L, type, -1);
    }

    public AbstractLuaPtrValue(Lua L, LuaType type, int index) {
        super(L, type);
        this.ptr = L.toPointer(index);
    }

    public AbstractLuaPtrValue(long ptr, Lua L, LuaType type) {
        super(L, type);
        this.ptr = ptr;
    }

    public long getPointer() {
        return ptr;
    }
}
