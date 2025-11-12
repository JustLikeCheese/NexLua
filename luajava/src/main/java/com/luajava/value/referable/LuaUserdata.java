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
import com.luajava.util.ClassUtils;
import com.luajava.value.AbstractLuaRefValue;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

public class LuaUserdata extends AbstractLuaRefValue {
    private Object javaObject;

    public LuaUserdata(Lua L) {
        super(L, LuaType.USERDATA);
    }

    public LuaUserdata(Lua L, int index) {
        super(L, LuaType.USERDATA, index);
    }

    private LuaUserdata(int ref, Lua L) {
        super(ref, L, LuaType.USERDATA);
    }

    public static LuaUserdata fromRef(Lua L, int ref) {
        return new LuaUserdata(ref, L);
    }

    @Override
    public boolean isUserdata() {
        return true;
    }

    @Override
    public LuaUserdata checkUserdata() {
        return this;
    }

    @Override
    public Object checkJavaObject() throws LuaException {
        return toJavaObject();
    }

    @Override
    public Object toJavaObject() throws LuaException {
        if (javaObject == null) {
            Object obj = super.toJavaObject();
            javaObject = (obj != null) ? obj : this;
        }
        return javaObject;
    }

    @Override
    public boolean isJavaObject() throws LuaException {
        return toJavaObject() != this;
    }

    @Override
    public boolean isJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == Object.class || clazz == LuaValue.class || clazz == LuaUserdata.class) {
            return true;
        }
        Object object = toJavaObject();
        if (object != this) {
            Class<?> wrapperClass = ClassUtils.getWrapperType(clazz);
            Class<?> objectClass = ClassUtils.getWrapperType(object.getClass());
            return wrapperClass.isAssignableFrom(objectClass);
        }
        return false;
    }

    @Override
    public Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == LuaValue.class || clazz == LuaUserdata.class) {
            return this;
        }
        Object object = toJavaObject();
        if (object == this) {
            if (clazz == Object.class) {
                return null;
            }
            return super.toJavaObject(clazz);
        }
        Class<?> objClass = ClassUtils.getWrapperType(object.getClass());
        Class<?> wrapperClass = ClassUtils.getWrapperType(clazz);
        if (clazz == Object.class || wrapperClass.isAssignableFrom(objClass)) {
            return object;
        }
        return super.toJavaObject(clazz);
    }
}