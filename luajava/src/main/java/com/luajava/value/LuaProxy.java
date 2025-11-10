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

import com.luajava.JuaAPI;
import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.util.ClassUtils;
import com.luajava.value.referable.LuaTable;
import com.luajava.value.referable.LuaFunction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Class that implements the InvocationHandler interface.
 * This class is used in the LuaJava's proxy system.
 * When a proxy object is accessed, the method invoked is
 * called from Lua
 *
 * @author Rizzato
 * @author Thiago Ponte
 */
public final class LuaProxy implements InvocationHandler {
    private final Lua L;
    private final LuaValue value;
    private final Lua.Conversion degree;
    private final Class<?> interfaces;
    private final String name;

    private LuaProxy(Lua L, LuaFunction value, Lua.Conversion degree, Class<?> interfaces, String name) {
        this.L = L;
        this.value = value;
        this.degree = degree;
        this.interfaces = interfaces;
        this.name = name;
    }

    private LuaProxy(Lua L, LuaTable value, Lua.Conversion degree, Class<?> interfaces) {
        this.L = L;
        this.value = value;
        this.degree = degree;
        this.interfaces = interfaces;
        this.name = null;
    }

    public static LuaProxy newInstance(Lua L, int idx, Class<?> interfaces, Lua.Conversion degree) {
        LuaType type = L.type(idx);
        switch (type) {
            case FUNCTION:
                String name = ClassUtils.getSingleInterfaceMethodName(interfaces);
                if (name == null)
                    throw new IllegalArgumentException("Unable to merge interfaces into a functional one");
                return new LuaProxy(L, new LuaFunction(L, idx), degree, interfaces, name);
            case TABLE:
                return new LuaProxy(L, new LuaTable(L, idx), degree, interfaces);
            default:
                throw new IllegalArgumentException("Expecting a table / function and interfaces");
        }
    }

    public static LuaProxy newInstance(LuaFunction value, Class<?> interfaces, Lua.Conversion degree) {
        String name = ClassUtils.getSingleInterfaceMethodName(interfaces);
        if (name == null)
            throw new IllegalArgumentException("Unable to merge interfaces into a functional one");
        return new LuaProxy(value.L, value, degree, interfaces, name);
    }

    public static LuaProxy newInstance(LuaTable value, Class<?> interfaces, Lua.Conversion degree) {
        return new LuaProxy(value.L, value, degree, interfaces);
    }

    @Override
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
        synchronized (L.getMainState()) {
            return syncInvoke(object, method, objects);
        }
    }

    private Object syncInvoke(Object object, Method method, Object[] objects) throws Throwable {
        if (value.isFunction()) {
            int top = L.getTop();
            try {
                LuaFunction func = (LuaFunction) value;
                L.push(func);
                L.pCall(objects, 1);
                Object result = L.get().toJavaObject(Object.class);
                L.pop(1);
                return result;
            } catch (final Exception e) {
                L.onError(e);
                Class<?> returnType = method.getReturnType();
                if (returnType.isPrimitive()) {
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == char.class) {
                        return '\0';
                    }
                    return 0;
                } else {
                    return null;
                }
            } finally {
                L.setTop(top);
            }
        }
        int top = L.getTop();
        L.push(value);
        L.getField(-1, method.getName());
        if (L.isNil(-1)) {
            L.setTop(top);
            return callDefaultMethod(object, method, objects);
        }
        L.pushJavaObject(object);

        int nResults = method.getReturnType() == Void.TYPE ? 0 : 1;

        if (objects == null) {
            L.pCall(1, nResults);
        } else {
            for (Object o : objects) {
                L.push(o, degree);
            }
            L.pCall(objects.length + 1, nResults);
        }

        try {
            if (method.getReturnType() == Void.TYPE) {
                L.setTop(top);
                return null;
            } else {
                Object o = JuaAPI.convertFromLua(L, method.getReturnType(), -1);
                L.setTop(top);
                return o;
            }
        } catch (IllegalArgumentException e) {
            L.setTop(top);
            throw e;
        }
    }

    private Object callDefaultMethod(Object o, Method method, Object[] objects) throws Throwable {
        if (ClassUtils.isDefault(method)) {
            return L.invokeSpecial(o, method, objects);
        }
        return callObjectDefault(o, method, objects);
    }

    private Object callObjectDefault(Object o, Method method, Object[] objects) {
        if (methodEquals(method, int.class, "hashCode")) {
            return hashCode();
        }
        if (methodEquals(method, boolean.class, "equals", Object.class)) {
            return o == objects[0];
        }
        if (methodEquals(method, String.class, "toString")) {
            return "LuaProxy" + interfaces.toString() + "@" + Integer.toHexString(hashCode());
        }
        throw new LuaException(LuaException.LuaError.JAVA, "method not implemented: " + method);
    }

    public static boolean methodEquals(Method method, Class<?> returnType,
                                       String name, Class<?>... parameters) {
        return method.getReturnType() == returnType
                && name.equals(method.getName())
                && Arrays.equals(method.getParameterTypes(), parameters);
    }

    public int unwrap() {
        return L.refGet(value.getRef());
    }

    public Lua state() {
        return L;
    }

    public Object toProxy() {
        return Proxy.newProxyInstance(interfaces.getClassLoader(), new Class[]{interfaces}, this);
    }
}
