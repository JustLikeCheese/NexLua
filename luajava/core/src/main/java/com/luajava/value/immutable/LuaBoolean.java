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

public class LuaBoolean extends AbstractLuaValue {
    private final boolean value;

    public LuaBoolean(Lua L) {
        super(L, LuaType.BOOLEAN);
        this.value = L.toBoolean(-1);
    }

    public LuaBoolean(Lua L, int index) {
        super(L, LuaType.BOOLEAN);
        this.value = L.toBoolean(index);
    }

    private LuaBoolean(boolean value, Lua L) {
        super(L, LuaType.BOOLEAN);
        this.value = value;
    }

    public static LuaBoolean from(Lua L, boolean value) {
        return new LuaBoolean(value, L);
    }

    @Override
    public int push(Lua L) throws LuaException {
        return L.push(value);
    }

    @NonNull
    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    public String LtoString() {
        return toString();
    }

    @Override
    public boolean toBoolean() {
        return value;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public LuaBoolean checkBoolean() {
        return this;
    }

    @Override
    public Object toJavaObject() {
        return value;
    }

    @Override
    public boolean isJavaObject(Class<?> clazz) {
        return clazz == Object.class
                || clazz == LuaValue.class
                || clazz == LuaBoolean.class
                || clazz == Boolean.class || clazz == boolean.class;
    }

    @Override
    public @Nullable Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == LuaValue.class || clazz == LuaBoolean.class)
            return this;
        else if (clazz == Object.class || clazz == Boolean.class)
            return value;
        return super.toJavaObject(clazz);
    }
}
