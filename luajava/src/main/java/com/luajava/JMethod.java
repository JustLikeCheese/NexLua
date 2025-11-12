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

package com.luajava;

import com.luajava.value.LuaValue;

import java.lang.reflect.Method;

public class JMethod implements CFunction {
    private final Object object;
    private final Class<?> clazz;
    private final String name;
    private Method method;
    private Class<?> returnType;

    public JMethod(Object object, Class<?> clazz, String methodName) {
        if (clazz == null || methodName == null) {
            throw new IllegalArgumentException("Class and method name cannot be null");
        }
        this.object = object;
        this.clazz = clazz;
        this.name = methodName;
    }

    @Override
    public int __call(Lua L) throws LuaException {
        LuaValue[] values = L.getAll();
        if (method == null) {
            method = JuaAPI.matchMethod(object, clazz.getMethods(), name, values);
            returnType = method.getReturnType();
        }
        Object result = JuaAPI.callMethod(object, method, values);
        L.push(result, returnType);
        return 1;
    }
}