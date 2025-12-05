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
import com.luajava.value.LuaProxy;
import com.luajava.value.LuaValue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;

// LuaJava Helper
public final class JuaAPI {
    public static boolean matchParams(Class<?>[] paramTypes, LuaValue[] values) throws LuaException {
        for (int i = 0; i < paramTypes.length; i++) {
            if (!values[i].isJavaObject(paramTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public static Object[] convertParams(Class<?>[] paramTypes, LuaValue[] values) throws LuaException {
        int length = values.length;
        Object[] objects = new Object[length];
        for (int i = 0; i < length; i++) {
            objects[i] = values[i].toJavaObject(paramTypes[i]);
        }
        return objects;
    }

    public static Object callMethod(Object object, Method method, Class<?>[] paramTypes, LuaValue[] values) throws InvocationTargetException, IllegalAccessException, LuaException {
        Object[] objects = convertParams(paramTypes, values);
        return ClassUtils.callMethod(object, method, objects);
    }

    public static Object callMethod(Object object, Method method, LuaValue[] values) throws LuaException, IllegalArgumentException {
        Class<?>[] paramTypes = method.getParameterTypes();
        try {
            return callMethod(object, method, paramTypes, values);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            Throwable cause = (throwable == null) ? e : throwable;
            throw new LuaException("Invalid method call." +
                    "\n  at " + method +
                    "\n  -> ",  cause);
        }
    }

    public static Method matchMethod(Object object, Method[] methods, String name, LuaValue[] values) throws LuaException {
        ArrayList<Method> matchedMethod = new ArrayList<>();
        for (Method method : methods) {
            if (!method.getName().equals(name)) continue;
            matchedMethod.add(method);
            if (object == null && !Modifier.isStatic(method.getModifiers())) continue;
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != values.length) continue;
            if (!matchParams(paramTypes, values)) continue;
            return method;
        }
        StringBuilder msg = new StringBuilder("Invalid method call. Invalid Parameters.\n");
        for (Method method : matchedMethod) {
            msg.append(method);
            msg.append("\n");
        }
        throw new LuaException(msg.toString());
    }

    public static Object callConstructor(Constructor<?> constructor, Class<?>[] paramTypes, LuaValue[] values) throws InvocationTargetException, IllegalAccessException, InstantiationException, LuaException {
        Object[] objects = convertParams(paramTypes, values);
        return constructor.newInstance(objects);
    }

    public static Object callConstructor(Constructor<?> constructor, LuaValue[] values) throws LuaException, IllegalArgumentException {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        try {
            return callConstructor(constructor, paramTypes, values);
        } catch (Exception e) {
            throw new LuaException("Invalid constructor method call." +
                    "\n  at " + constructor +
                    "\n  -> ", e);
        }
    }

    public static Constructor<?> matchConstructor(Constructor<?>[] constructors, LuaValue[] values) throws LuaException, IllegalArgumentException {
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length != values.length) continue;
            if (!matchParams(paramTypes, values)) continue;
            return constructor;
        }
        StringBuilder msg = new StringBuilder("Invalid constructor call. Invalid Parameters.\n");
        for (Constructor<?> constructor : constructors) {
            msg.append(constructor);
            msg.append("\n");
        }
        throw new LuaException(msg.toString());
    }


    // 0=>null, 1=>field, 2=>method, 3=>get method, 4=>get method(first char to upper case), 5=>get method(first char to lower case)
    public static int jclassIndex(long ptr, Class<?> clazz, String name) throws InvocationTargetException, IllegalAccessException, LuaException {
        // Get lua instance
        Lua L = Jua.get(ptr);
        // Class.STATIC_FIELD
        Field field = ClassUtils.getPublicStaticField(clazz, name);
        if (field != null) return L.push(ClassUtils.getField(field), field.getType());
        // Class.InnerClass
        Class<?> innerClass = ClassUtils.getInnerClass(clazz, name);
        if (innerClass != null) return L.push(innerClass);
        // Class.staticMethod(XXX)
        Method[] methods = clazz.getMethods();
        String fieldMethodName1; // get Xxx
        String fieldMethodName2; // get xXX
        Method fieldMethod1 = null;
        Method fieldMethod2 = null;
        {
            char firstChar = name.charAt(0);
            boolean isLowerCase = Character.isLowerCase(firstChar);
            final String SUFFIX = name.substring(1);
            final String PREFIX = "get";
            if (isLowerCase) {
                fieldMethodName1 = PREFIX + Character.toUpperCase(firstChar) + SUFFIX;
                fieldMethodName2 = PREFIX + firstChar + SUFFIX;
            } else {
                fieldMethodName1 = PREFIX + firstChar + SUFFIX;
                fieldMethodName2 = PREFIX + Character.toLowerCase(firstChar) + SUFFIX;
            }
        }
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                String methodName = method.getName();
                if (name.equals(method.getName())) {
                    return L.push(new JMethod(null, clazz, name));
                } else if (fieldMethod1 == null && method.getParameterCount() == 0) {
                    if (fieldMethodName1.equals(methodName)) {
                        fieldMethod1 = method;
                    } else if (fieldMethod2 == null && fieldMethodName2.equals(methodName)) {
                        fieldMethod2 = method;
                    }
                }
            }
        }
        if (fieldMethod1 != null)
            return L.push(ClassUtils.getMethodField(fieldMethod1), fieldMethod1.getReturnType());
        else if (fieldMethod2 != null)
            return L.push(ClassUtils.getMethodField(fieldMethod2), fieldMethod2.getReturnType());
        throw new LuaException(String.format("%s@%s is not a field or method", clazz.getName(), name));
    }

    public static int jclassNewIndex(long ptr, Class<?> clazz, String name) throws InvocationTargetException, IllegalAccessException, LuaException {
        // Get lua instance
        Lua L = Jua.get(ptr);
        // Class.STATIC_FIELD = value
        LuaValue[] values = L.getAll(3);
        Field field = ClassUtils.getPublicStaticField(clazz, name);
        if (field != null) {
            if (!Modifier.isFinal(field.getModifiers())) {
                field.set(null, values[0].toJavaObject(field.getType()));
                return 0;
            }
            throw new LuaException(String.format("%s@%s is a final field that cannot be changed", clazz.getName(), name));
        }
        // Class.STATIC_METHOD = value (setter)
        Method[] methods = clazz.getMethods();
        String fieldMethodName1; // set Xxx
        String fieldMethodName2; // set xXX
        Method fieldMethod1 = null;
        Method fieldMethod2 = null;
        {
            char firstChar = name.charAt(0);
            boolean isLowerCase = Character.isLowerCase(firstChar);
            final String SUFFIX = name.substring(1);
            final String PREFIX = "set";
            if (isLowerCase) {
                fieldMethodName1 = PREFIX + Character.toUpperCase(firstChar) + SUFFIX;
                fieldMethodName2 = PREFIX + firstChar + SUFFIX;
            } else {
                fieldMethodName1 = PREFIX + firstChar + SUFFIX;
                fieldMethodName2 = PREFIX + Character.toLowerCase(firstChar) + SUFFIX;
            }
        }
        for (Method method : methods) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (Modifier.isStatic(method.getModifiers()) && paramTypes.length == values.length && matchParams(paramTypes, values)) {
                String methodName = method.getName();
                if (fieldMethodName1.equals(methodName)) {
                    callMethod(null, method, method.getParameterTypes(), values);
                    return 0;
                } else if (fieldMethod2 == null && fieldMethodName2.equals(methodName)) {
                    fieldMethod2 = method;
                }
            }
        }
        if (fieldMethod2 != null) {
            callMethod(null, fieldMethod2, fieldMethod2.getParameterTypes(), values);
            return 0;
        }
        throw new LuaException(String.format("%s@%s is not a field", clazz.getName(), name));
    }

    public static int jclassNew(long ptr, Class<?> clazz) throws LuaException {
        Lua L = Jua.get(ptr);
        LuaValue[] values = L.getAll(2);
        Constructor<?> constructor;
        try {
            constructor = matchConstructor(clazz.getConstructors(), values);
        } catch (LuaException e) {
            if (values.length == 1) {
                LuaValue value = values[0];
                if (value.isTable()) {
                    return L.push(value.toJavaArray(clazz));
                }
            }
            throw e;
        }
        return L.push(callConstructor(constructor, values));
    }

    /* Java Object */
    public static int jobjectIndex(long ptr, Object instance, String name) throws IllegalAccessException, InvocationTargetException, LuaException {
        Lua L = Jua.get(ptr);
        // Class.STATIC_FIELD
        Class<?> clazz = instance.getClass();
        Field field = ClassUtils.getPublicField(clazz, name);
        if (field != null) return L.push(ClassUtils.getField(instance, field), field.getType());
        // Class.innerClass
        Class<?> innerClass = ClassUtils.getInnerClass(clazz, name);
        if (innerClass != null) return L.push(innerClass);
        // Class.staticMethod(XXX)
        Method[] methods = clazz.getMethods();
        String fieldMethodName1; // get Xxx
        String fieldMethodName2; // get xXX
        Method fieldMethod1 = null;
        Method fieldMethod2 = null;
        Method staticFieldMethod1 = null;
        Method staticFieldMethod2 = null;
        Method staticMethod = null;
        {
            char firstChar = name.charAt(0);
            boolean isLowerCase = Character.isLowerCase(firstChar);
            final String SUFFIX = name.substring(1);
            final String PREFIX = "get";
            if (isLowerCase) {
                fieldMethodName1 = PREFIX + Character.toUpperCase(firstChar) + SUFFIX;
                fieldMethodName2 = PREFIX + firstChar + SUFFIX;
            } else {
                fieldMethodName1 = PREFIX + firstChar + SUFFIX;
                fieldMethodName2 = PREFIX + Character.toLowerCase(firstChar) + SUFFIX;
            }
        }
        for (Method method : methods) {
            String methodName = method.getName();
            if (!Modifier.isStatic(method.getModifiers())) { // instance method first
                if (name.equals(method.getName())) { // object.methodName
                    return L.push(new JMethod(instance, clazz, name));
                } else if (fieldMethod1 == null) { // object.get Xxx
                    if (fieldMethodName1.equals(methodName)) {
                        fieldMethod1 = method;
                    } else if (fieldMethodName2.equals(methodName)) { // object.get xXX
                        fieldMethod2 = method;
                    }
                }
            } else if (staticMethod == null) { // Class.methodName
                if (name.equals(methodName)) {
                    staticMethod = method;
                } else if (staticFieldMethod1 == null) {
                    if (fieldMethodName1.equals(methodName)) {
                        staticFieldMethod1 = method;
                    } else if (staticFieldMethod2 == null && fieldMethodName2.equals(methodName)) { // Class.get xXX
                        staticFieldMethod2 = method;
                    }
                }
            }
        }
        if (staticMethod != null)
            return L.push(new JMethod(null, clazz, name));
        else if (fieldMethod1 != null)
            return L.push(ClassUtils.callMethod(instance, fieldMethod1, null), fieldMethod1.getReturnType());
        else if (fieldMethod2 != null)
            return L.push(ClassUtils.callMethod(instance, fieldMethod2, null), fieldMethod2.getReturnType());
        else if (staticFieldMethod1 != null)
            return L.push(ClassUtils.callMethod(null, staticFieldMethod1, null), staticFieldMethod1.getReturnType());
        else if (staticFieldMethod2 != null)
            return L.push(ClassUtils.callMethod(null, staticFieldMethod2, null), staticFieldMethod2.getReturnType());
        throw new LuaException(String.format("%s@%s is not a field or method", clazz.getName(), name));
    }

    public static int jobjectNewIndex(long ptr, Object object, String name) throws InvocationTargetException, IllegalAccessException, LuaException {
        // Get lua instance
        Lua L = Jua.get(ptr);
        Class<?> clazz = object.getClass();
        LuaValue[] values = L.getAll(3);
        // object.field = value
        Field field = ClassUtils.getPublicField(clazz, name);
        if (field != null) {
            if (!Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                field.set(object, values[0].toJavaObject(field.getType()));
                return 0;
            }
            throw new LuaException(String.format("%s@%s is a final field that cannot be changed", clazz.getName(), name));
        }
        // object.method = value (setter)
        Method[] methods = clazz.getMethods();
        String fieldMethodName1; // set Xxx
        String fieldMethodName2; // set xXx
        Method fieldMethod1 = null;
        Method fieldMethod2 = null;
        Method staticFieldMethod1 = null;
        Method staticFieldMethod2 = null;
        {
            char firstChar = name.charAt(0);
            boolean isLowerCase = Character.isLowerCase(firstChar);
            final String SUFFIX = name.substring(1);
            final String PREFIX = "set";
            if (isLowerCase) {
                fieldMethodName1 = PREFIX + Character.toUpperCase(firstChar) + SUFFIX;
                fieldMethodName2 = PREFIX + firstChar + SUFFIX;
            } else {
                fieldMethodName1 = PREFIX + firstChar + SUFFIX;
                fieldMethodName2 = PREFIX + Character.toLowerCase(firstChar) + SUFFIX;
            }
        }
        for (Method method : methods) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == values.length && matchParams(paramTypes, values)) {
                String methodName = method.getName();
                if (!Modifier.isStatic(method.getModifiers())) {
                    if (fieldMethodName1.equals(methodName)) {
                        callMethod(object, method, paramTypes, values);
                        return 0;
                    } else if (fieldMethod2 == null && fieldMethodName2.equals(methodName)) {
                        fieldMethod2 = method;
                    }
                } else {
                    if (fieldMethodName1.equals(methodName)) {
                        staticFieldMethod1 = method;
                    } else if (staticFieldMethod2 == null && fieldMethodName2.equals(methodName)) {
                        staticFieldMethod2 = method;
                    }
                }
            }
        }
        if (fieldMethod2 != null) {
            callMethod(object, fieldMethod2, fieldMethod2.getParameterTypes(), values);
        } else if (staticFieldMethod1 != null) {
            callMethod(null, staticFieldMethod1, staticFieldMethod1.getParameterTypes(), values);
        } else if (staticFieldMethod2 != null) {
            callMethod(null, staticFieldMethod2, staticFieldMethod2.getParameterTypes(), values);
        } else {
            throw new LuaException(String.format("%s@%s is not a field", clazz.getName(), name));
        }
        return 0;
    }

    private final static String[] LENGTH_DEFAULT_METHOD_NAME = new String[]{"length", "size"};

    public static int jobjectLength(long ptr, Object instance) throws LuaException {
        Lua L = Jua.get(ptr);
        Class<?> clazz = instance.getClass();
        for (String name : LENGTH_DEFAULT_METHOD_NAME) {
            Object result = ClassUtils.callObjectNoArgsMethod(instance, name);
            if (result != null) return L.push(result);
        }
        throw new LuaException(String.format("%s has no default method to get length", clazz.getName()));
    }

    public static int jarrayIndex(long ptr, Object array) throws LuaException {
        Lua L = Jua.get(ptr);
        int index = (int) L.get(2).toJavaObject(int.class);
        Class<?> type = array.getClass().getComponentType();
        Object object = Array.get(array, index);
        return L.push(object, type);
    }

    public static int jarrayNewIndex(long ptr, Object array) throws LuaException {
        Lua L = Jua.get(ptr);
        Class<?> type = array.getClass().getComponentType();
        int index = (int) L.get(2).toJavaObject(int.class);
        Object value = L.get(3).toJavaObject(type);
        Array.set(array, index, value);
        return 0;
    }

    public static int jarrayIpairsIterator(long ptr, Object array) throws LuaException {
        Lua L = Jua.get(ptr);
        int index = (int) L.get(2).toJavaObject(int.class);
        int nextIndex = index + 1;
        if (nextIndex >= Array.getLength(array)) {
            return 0;
        }
        L.push(nextIndex);
        L.push(Array.get(array, nextIndex), array.getClass().getComponentType());
        return 2;
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter(2048);
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Allocates a direct buffer whose memory is managed by Java
     *
     * @param size the buffer size
     * @return a direct buffer
     */
    @SuppressWarnings("unused")
    public static ByteBuffer allocateDirectBuffer(int size) {
        return ByteBuffer.allocateDirect(size);
    }

    /**
     * Pushes on stack the backing Lua table for a proxy
     *
     * @param id  the Lua state id
     * @param obj the proxy object
     * @return -1 on failure, 1 if successfully pushed
     */
    @SuppressWarnings("unused")
    public static int unwrap(long id, Object obj) throws LuaException {
        Lua L = Jua.get(id);
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(obj);
            if (handler instanceof LuaProxy) {
                LuaProxy proxy = (LuaProxy) handler;
                if (proxy.state() == L) {
                    return proxy.unwrap();
                }
                throw new IllegalArgumentException("Cannot unwrap LuaProxy on different LuaState");
            }
            throw new IllegalArgumentException("Not a LuaProxy");
        } catch (IllegalArgumentException | SecurityException e) {
            return L.error(e);
        }
    }

    /**
     * Loads a Lua chunk according with {@link Lua#loadExternal(String)}
     *
     * <p>
     * Used in <code>jmoduleLoad</code> in <code>jni/luajava/juaapi.cpp</code>
     * </p>
     *
     * @param id     see {@link Jua#get(long)}
     * @param module the module name
     * @return always 1
     */
    public static int jmoduleLoad(long id, String module) throws Exception {
        Lua L = Jua.get(id);
        return L.loadExternal(module);
    }

    /**
     * Calls a {@link CFunction}
     *
     * @param index the id of {@link Jua} thread calling this method
     * @param obj   the {@link CFunction} object
     * @return the number result pushed on stack
     */
    public static int jfunctionCall(long index, Object obj) throws Exception {
        Lua L = Jua.get(index);
        if (obj instanceof CFunction) {
            return ((CFunction) obj).__call(L);
        } else {
            L.push("error invoking object (expecting a CFunction)");
            return -1;
        }
    }
}
