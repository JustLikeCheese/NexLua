package com.luajava;

import com.luajava.util.ClassUtils;
import com.luajava.value.LuaValue;
import com.luajava.value.referable.LuaTable;

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
}
