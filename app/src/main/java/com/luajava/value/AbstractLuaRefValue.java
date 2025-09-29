package com.luajava.value;

import com.luajava.Lua;
import com.luajava.LuaConsts;
import com.luajava.LuaException;
import com.luajava.cleaner.LuaReferable;

public abstract class AbstractLuaRefValue extends AbstractLuaValue implements LuaReferable {
    protected final int ref;

    public AbstractLuaRefValue(Lua L, LuaType type) {
        super(L, type);
        this.ref = L.ref();
    }

    public AbstractLuaRefValue(Lua L, LuaType type, int index) {
        super(L, type);
        this.ref = L.ref(index);
    }

    public AbstractLuaRefValue(int ref, Lua L, LuaType type) {
        super(L, type);
        this.ref = ref;
    }

    @Override
    public void push(Lua L) {
        if (this.L != L) {
            throw new LuaException(LuaException.LuaError.JAVA, "Cannot push a reference to a different Lua instance");
        }
        L.refGet(ref);
    }

    @Override
    public int getRef() {
        return ref;
    }

    @Override
    public void unRef() {
        L.unRef(ref);
    }
}
