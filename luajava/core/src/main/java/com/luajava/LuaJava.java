package com.luajava;

import com.luajava.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Objects;

public class LuaJava {
    public static int bindClass(long ptr, String name) throws ClassNotFoundException, LuaException {
        Lua L = Jua.get(ptr);
        L.push(ClassUtils.forName(name));
        return 1;
    }

    public static int bindMethod(long ptr, Object object, String name, Class<?>[] paramTypes) throws LuaException {
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
        for (Method method : Objects.requireNonNull(clazz).getMethods()) {
            Class<?>[] methodParamTypes = method.getParameterTypes();
            if (method.getName().equals(name) && methodParamTypes.length == paramTypes.length) {
                boolean match = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!methodParamTypes[i].isAssignableFrom(paramTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return L.push(new LuaJavaMethod(clazz, method));
                }
            }
        }
        return 0;
    }
}
