package com.luajava;

import com.luajava.value.LuaValue;

import java.lang.reflect.Method;

public class LuaJavaMethod implements CFunction {
    private final Class<?> clazz;
    private final Method method;
    private final Class<?> returnType;
    private final Class<?>[] paramTypes;

    public LuaJavaMethod(Class<?> clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
        this.returnType = method.getReturnType();
        this.paramTypes = method.getParameterTypes();
    }

    @Override
    public int __call(Lua L) throws LuaException {
        Object object = L.toJavaObject(1);
        LuaValue[] values = L.getAll(2);
        Object result = JuaAPI.callMethod(object, method, values);
        return L.push(result, returnType);
    }
}
