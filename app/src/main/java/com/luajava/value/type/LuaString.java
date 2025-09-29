package com.luajava.value.type;

import com.luajava.Lua;
import com.luajava.cleaner.LuaReferable;
import com.luajava.value.AbstractLuaValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class LuaString extends AbstractLuaValue implements LuaReferable {
    private final int ref;

    public LuaString(Lua L) {
        super(L, LuaType.STRING);
        this.ref = L.ref();
    }

    public LuaString(Lua L, int index) {
        super(L, LuaType.STRING);
        this.ref = L.ref(index);
    }

    public LuaString(int ref, Lua L) {
        super(L, LuaType.STRING);
        this.ref = ref;
        L.refGet(ref);
        L.pop(1);
    }

    public LuaString(String string, Lua L) {
        super(L, LuaType.STRING);
        L.push(string);
        this.ref = L.ref();
        L.pop(1);
    }

    public LuaString(Buffer buffer, Lua L) {
        super(L, LuaType.STRING);
        L.push(buffer);
        this.ref = L.ref();
        L.pop(1);
    }

    @Override
    public void push(Lua L1) {
        L.refGet(ref);
        if (L != L1) {
            L.copyString(L1);
        }
    }

    @Override
    public LuaValue copyTo(Lua L1) {
        push();
        L.copyString(L1);
        L.pop(1);
        return new LuaString(L1, -1);
    }

    @Override
    public boolean isString() {
        return true;
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
