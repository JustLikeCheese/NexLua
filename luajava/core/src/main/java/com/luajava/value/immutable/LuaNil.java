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

package com.luajava.value.immutable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.AbstractLuaValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

public class LuaNil extends AbstractLuaValue {
    public LuaNil(Lua L) {
        super(L, LuaType.NIL);
    }

    public LuaNil(Lua L, int index) {
        super(L, LuaType.NIL);
    }

    public static LuaNil from(Lua L) {
        if (L.NIL != null)
            return L.NIL;
        return new LuaNil(L);
    }

    @Override
    public int push(Lua L) throws LuaException {
        return L.pushNil();
    }

    @NonNull
    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public String LtoString() {
        return toString();
    }

    @Override
    public boolean toBoolean() {
        return false;
    }

    @Override
    public boolean isNil() {
        return true;
    }

    @Override
    public LuaNil checkNil() {
        return this;
    }

    @Override
    public Object toJavaObject() {
        return null;
    }

    @Override
    public boolean isJavaObject(Class<?> clazz) {
        return clazz == Object.class
                || clazz == LuaValue.class
                || clazz == LuaNil.class
                || !clazz.isPrimitive();
    }

    @Override
    public @Nullable Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == LuaValue.class || clazz == LuaNil.class)
            return this;
        else if (clazz == Object.class)
            return null;
        return super.toJavaObject(clazz);
    }
}
