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

import com.luajava.util.ClassUtils;
import com.luajava.value.LuaValue;
import com.luajava.value.referable.LuaTable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class LuaJava {
    public static int bindClass(long ptr, String name) throws ClassNotFoundException, LuaException {
        Lua L = Jua.get(ptr);
        return L.push(ClassUtils.forName(name));
    }

    public static int bindMethod(long ptr, Object object, String name, Class<?>[] paramTypes) throws LuaException, NoSuchMethodException {
        Lua L = Jua.get(ptr);
        Class<?> clazz;
        Class<?> objectClass = object.getClass();
        if (objectClass == Class.class) {
            clazz = (Class<?>) object;
        } else if (objectClass.isArray()) {
            clazz = objectClass.getComponentType();
        } else {
            clazz = objectClass;
        }
        Objects.requireNonNull(clazz);
        if (name.equals("new")) {
            Constructor<?> constructor = clazz.getConstructor(paramTypes);
            return L.push(new CFunction() {
                @Override
                public int __call(Lua L) throws LuaException {
                    LuaValue[] values = L.getAll();
                    return L.push(JuaAPI.callConstructor(constructor, values));
                }
            });
        } else {
            Method method = clazz.getMethod(name, paramTypes);
            return L.push(new CFunction() {
                @Override
                public int __call(Lua L) throws LuaException {
                    Object object = L.toJavaObject(1, Object.class);
                    LuaValue[] values = L.getAll(2);
                    return L.push(JuaAPI.callMethod(object, method, values));
                }
            });
        }
    }

    public static int toJavaObject(long ptr, Class<?> clazz) throws LuaException {
        Lua L = Jua.get(ptr);
        clazz = clazz == null ? Object.class : clazz;
        return L.push(L.toJavaObject(1, clazz), clazz);
    }

    public static int toJavaArray(long ptr, Class<?> clazz) throws LuaException {
        Lua L = Jua.get(ptr);
        clazz = clazz == null ? Object.class : clazz;
        return L.push(L.toJavaArray(1, clazz), clazz);
    }

    public static int toJavaMap(long ptr, Class<?> keyClazz, Class<?> valueClazz) throws LuaException {
        Lua L = Jua.get(ptr);
        keyClazz = keyClazz == null ? Object.class : keyClazz;
        valueClazz = valueClazz == null ? Object.class : valueClazz;
        return L.push(L.toJavaMap(1, keyClazz, valueClazz));
    }

    public static int asTable(long ptr, Object object) throws LuaException {
        Lua L = Jua.get(ptr);
        Class<?> clazz = object.getClass();
        if (clazz.isArray()) {
            return L.pushArray(object);
        } else if (clazz.isAssignableFrom(Collection.class)) {
            return L.pushCollection((Collection<?>) object);
        } else if (clazz.isAssignableFrom(Map.class)) {
            return L.pushMap((Map<?, ?>) object);
        } else if (clazz.isAssignableFrom(LuaTable.class)) {
            return L.push((LuaTable) object);
        }
        throw new LuaException("cannot convert " + object + " (" + clazz.getName() + ") as table");
    }

    public static int createArray(long ptr, Class<?> clazz, int[] dims) throws LuaException {
        Lua L = Jua.get(ptr);
        return L.pushJavaObject(Array.newInstance(clazz, dims));
    }

    public static int createProxy(long ptr, Class<?> clazz) throws LuaException {
        Lua L = Jua.get(ptr);
        return L.push(L.createProxy(2, clazz, Lua.Conversion.SEMI));
    }

    public static int unwrap(long ptr, Object object) throws LuaException {
        return JuaAPI.unwrap(ptr, object);
    }
}
