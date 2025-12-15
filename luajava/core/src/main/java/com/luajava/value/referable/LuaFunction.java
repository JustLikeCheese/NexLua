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

import androidx.annotation.Nullable;

import com.luajava.CFunction;
import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaProxy;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

public class LuaFunction extends AbstractLuaRefValue {
    private CFunction javaFunction;
    private Boolean isJavaFunction;
    private Boolean isCFunction;

    public LuaFunction(Lua L) {
        super(L, LuaType.FUNCTION);
    }

    public LuaFunction(Lua L, int index) {
        super(L, LuaType.FUNCTION, index);
    }

    protected LuaFunction(int ref, Lua L) {
        super(ref, L, LuaType.FUNCTION);
    }

    public static LuaFunction fromRef(Lua L, int ref) {
        return new LuaFunction(ref, L);
    }

    @Override
    public LuaFunction checkFunction() {
        return this;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    protected void initValue() throws LuaException {
        push();
        isCFunction = L.isCFunction(-1);
        if (isCFunction) {
            isJavaFunction = L.isJavaFunction(-1);
            if (isJavaFunction) {
                javaFunction = L.toJavaFunction(-1);
            }
        } else {
            isJavaFunction = false;
            javaFunction = null;
        }
        L.pop(1);
    }

    @Override
    public boolean isCFunction() throws LuaException {
        if (isCFunction == null) initValue();
        return isCFunction;
    }

    @Override
    public LuaFunction checkCFunction() throws LuaException {
        if (isCFunction()) {
            return this;
        }
        return null;
    }

    @Override
    public boolean isJavaFunction() throws LuaException {
        if (javaFunction == null) initValue();
        return isJavaFunction;
    }

    @Override
    public CFunction toJavaFunction() throws LuaException {
        if (isJavaFunction()) {
            return javaFunction;
        }
        return null;
    }

    @Override
    public CFunction checkJavaFunction() throws LuaException {
        if (isJavaFunction()) {
            return javaFunction;
        }
        return super.checkJavaFunction();
    }

    @Override
    public Object toJavaObject() {
        return this;
    }

    @Override
    public boolean isJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == Object.class || clazz == LuaValue.class || clazz == LuaFunction.class || clazz.isInterface()) {
            return true;
        } else if (isJavaFunction()) {
            return clazz.isAssignableFrom(CFunction.class);
        }
        return false;
    }

    @Override
    public @Nullable Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == LuaValue.class || clazz == LuaFunction.class)
            return this;
        else if (clazz == Object.class || clazz.isInterface())
            return LuaProxy.newInstance(this, clazz, Lua.Conversion.SEMI).toProxy();
        else if (isJavaFunction()) {
            if (clazz.isAssignableFrom(CFunction.class))
                return javaFunction;
        }
        return super.toJavaObject(clazz);
    }
}
