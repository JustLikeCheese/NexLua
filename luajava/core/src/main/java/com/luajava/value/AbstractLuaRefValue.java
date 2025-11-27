/*
 * Copyright (C) 2025 JustLikeCheese
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.luajava.value;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.cleaner.LuaReferable;

import java.nio.Buffer;

public abstract class AbstractLuaRefValue extends AbstractLuaValue implements LuaReferable {
    protected final int ref;

    public AbstractLuaRefValue(Lua L, LuaType type) {
        super(L, type);
        this.ref = L.refSafe();
        L.registerReference(this);
    }

    public AbstractLuaRefValue(Lua L, LuaType type, int index) {
        super(L, type);
        this.ref = L.refSafe(index);
        L.registerReference(this);
    }

    public AbstractLuaRefValue(int ref, Lua L, LuaType type) {
        super(L, type);
        this.ref = ref;
        L.registerReference(this);
    }

    @Override
    public int push(Lua L) throws LuaException {
        if (this.L != L) {
            throw new LuaException("Cannot push a reference to a different Lua instance");
        }
        L.checkStack(1);
        return L.refGet(ref);
    }

    @Override
    public void copyTo(Lua L) throws LuaException {
        L.refCopyTo(L, ref);
    }

    // RefValue Performance Optimization
    @Override
    public int length() {
        return L.refLength(ref);
    }

    @Override
    public void setMetatable(String tname) {
        L.refSetMetatable(ref, tname);
    }

    @Override
    public LuaValue callMetatable(String method) throws LuaException {
        if (L.refCallMeta(ref, method)) {
            LuaValue result = L.get();
            L.pop(2);
            return result;
        }
        return L.NIL;
    }

    @Override
    public String LtoString() throws LuaException {
        return L.refLtoString(ref);
    }

    @Override
    public long getPointer() {
        return L.refGetPointer(ref);
    }

    @Override
    public boolean isRef() {
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
