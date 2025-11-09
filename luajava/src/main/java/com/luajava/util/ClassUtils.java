package com.luajava.util;


import com.luajava.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public abstract class ClassUtils {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = new IdentityHashMap<>(9);
    private final static Set<String> OBJECT_DEFAULT_METHODS;
    private static final Method METHOD_IS_DEFAULT;

    static {
        // Primitives wrapper types
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Character.class, char.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Double.class, double.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Float.class, float.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Long.class, long.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Short.class, short.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Void.class, void.class);
        // Method.isDefault method
        Method method;
        try {
            method = Method.class.getMethod("isDefault");
        } catch (NoSuchMethodException e) {
            method = null;
        }
        METHOD_IS_DEFAULT = method;
        // Object default methods
        Set<String> methods = new HashSet<>();
        Collections.addAll(methods, "equals", "hashCode", "toString");
        OBJECT_DEFAULT_METHODS = Collections.unmodifiableSet(methods);
    }

    /**
     * Returns the method name if the class is considered a functional interface in a wilder sense
     *
     * @param clazz interface
     * @return {@code null} if not applicable
     */
    public static @Nullable String getSingleInterfaceMethodName(Class<?> clazz) {
        if (!clazz.isInterface() || clazz.isAnnotation())
            return null;
        String methodName = null;
        for (Method method : clazz.getMethods()) {
            if (OBJECT_DEFAULT_METHODS.contains(method.getName()))
                continue;
            if (Modifier.isAbstract(method.getModifiers())) {
                if (methodName == null) {
                    methodName = method.getName();
                } else if (!methodName.equals(method.getName())) {
                    return null;  // More than one abstract method found
                }
            }
        }
        return methodName;
    }

    /**
     * Wraps {@code isDefault} method, which is unavailable on lower Android versions
     *
     * <p>
     * {@code Call requires API level 24 (current min is 19): java.lang.reflect.Method#isDefault}
     * </p>
     *
     * @param method the method
     * @return true if the method is a default method
     */
    public static boolean isDefault(Method method) {
        if (METHOD_IS_DEFAULT == null) {
            return false;
        } else {
            try {
                return (boolean) METHOD_IS_DEFAULT.invoke(method);
            } catch (Throwable e) {
                return false;
            }
        }
    }

    public static Class<?> getWrapperType(Class<?> type) {
        Class<?> wrapperType = PRIMITIVE_WRAPPER_TYPE_MAP.get(type);
        if (wrapperType != null) {
            return wrapperType;
        }
        return type;
    }

    private static byte getJNIShortSignature(Class<?> c) {
        if (c.isPrimitive()) {
            if (c == void.class) return 'V';
            if (c == boolean.class) return 'Z';
            if (c == char.class) return 'C';
            if (c == byte.class) return 'B';
            if (c == short.class) return 'S';
            if (c == int.class) return 'I';
            if (c == long.class) return 'J';
            if (c == float.class) return 'F';
            if (c == double.class) return 'D';
        }
        return 'L';
    }

    private static @Nullable String getJNISignature(Class<?> c) {
        if (c.isPrimitive()) {
            if (c == int.class) return "I";
            if (c == long.class) return "J";
            if (c == boolean.class) return "Z";
            if (c == double.class) return "D";
            if (c == float.class) return "F";
            if (c == byte.class) return "B";
            if (c == char.class) return "C";
            if (c == short.class) return "S";
            if (c == void.class) return "V";
            return null;
        }
        if (c.isArray()) {
            Class<?> type = c.getComponentType();
            if (type == null) return null;
            return "[" + getJNISignature(type);
        }
        return "L" + c.getName().replace('.', '/') + ";";
    }

    public static Field getPublicStaticField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            if (Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                return field;
            }
            return null;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static Method getPublicStaticNoArgsMethod(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName);
            if (Modifier.isStatic(method.getModifiers())) {
                return method;
            }
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static boolean hasPublicStaticMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()) &&
                    methodName.equals(method.getName())) {
                return true;
            }
        }
        return false;
    }

    public static Object callMethod(Object object, Method method, Object[] params) throws InvocationTargetException, IllegalAccessException {
        if (!Modifier.isPublic(method.getModifiers()))
            method.setAccessible(true);
        return method.invoke(object, params);
    }

    public static Object getField(Field field) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(null);
    }

    public static Object getMethodField(Method method) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(null);
    }
}