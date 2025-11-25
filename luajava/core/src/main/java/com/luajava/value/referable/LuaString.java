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

package com.luajava.value.referable;

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class LuaString extends AbstractLuaRefValue {
    private String string;
    private Buffer buffer;

    public LuaString(Lua L) {
        super(L, LuaType.STRING);
    }

    public LuaString(Lua L, int index) {
        super(L, LuaType.STRING, index);
    }

    private LuaString(int ref, Lua L) {
        super(ref, L, LuaType.STRING);
    }

    private LuaString(String string, Lua L) {
        super(L, LuaType.STRING);
    }

    public static LuaString fromRef(Lua L, int ref) {
        return new LuaString(ref, L);
    }

    public static LuaString from(Lua L, String string) {
        return new LuaString(L);
    }

    public static LuaString from(Lua L, Buffer buffer) {
        return new LuaString(L);
    }

    @Override
    public String toString() {
        if (string != null) return string;
        if (buffer != null) return string = buffer.toString();
        return super.toString();
    }

    @Override
    public String LtoString() {
        return toString();
    }

    @Override
    public long toInteger() throws LuaException {
        push();
        long result = L.toInteger(-1);
        L.pop(1);
        return result;
    }

    @Override
    public double toNumber() throws LuaException {
        push();
        double result = L.toNumber(-1);
        L.pop(1);
        return result;
    }

    @Override
    public Buffer toBuffer() throws LuaException {
        if (buffer != null) return buffer;
        if (string != null) return buffer = ByteBuffer.wrap(string.getBytes());
        return buffer = super.toBuffer();
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public LuaString checkString() {
        return this;
    }

    @Override
    public Object toJavaObject() {
        return toString();
    }

    @Override
    public boolean isJavaObject(Class<?> clazz) {
        return clazz == Object.class || clazz == LuaValue.class || clazz == LuaString.class || CharSequence.class.isAssignableFrom(clazz);
    }

    @Override
    public Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == LuaValue.class || clazz == LuaString.class)
            return this;
        else if (clazz == Object.class || CharSequence.class.isAssignableFrom(clazz))
            return toString();
        return super.toJavaObject(clazz);
    }
}
