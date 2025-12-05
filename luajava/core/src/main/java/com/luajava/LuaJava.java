package com.luajava;

import com.luajava.util.ClassUtils;
import com.luajava.value.LuaValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

public class LuaJava {
    public static int bindClass(long ptr, String name) throws ClassNotFoundException, LuaException {
        Lua L = Jua.get(ptr);
        L.push(ClassUtils.forName(name));
        return 1;
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
}
