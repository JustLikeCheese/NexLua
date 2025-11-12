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
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;

// LuaJava Helper
public final class JuaAPI {
    public static boolean matchParams(Class<?>[] paramTypes, LuaValue[] values) {
        for (int i = 0; i < paramTypes.length; i++) {
            if (!values[i].isJavaObject(paramTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public static Object[] convertParams(Class<?>[] paramTypes, LuaValue[] values) {
        int length = values.length;
        Object[] objects = new Object[length];
        for (int i = 0; i < length; i++) {
            objects[i] = values[i].toJavaObject(paramTypes[i]);
        }
        return objects;
    }

    public static Object callMethod(Object object, Method method, Class<?>[] paramTypes, LuaValue[] values) throws InvocationTargetException, IllegalAccessException {
        Object[] objects = convertParams(paramTypes, values);
        return ClassUtils.callMethod(object, method, objects);
    }

    public static Object callMethod(Object object, Method method, LuaValue[] values) throws LuaException, IllegalArgumentException {
        Class<?>[] paramTypes = method.getParameterTypes();
        try {
            return callMethod(object, method, paramTypes, values);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            Object cause = (throwable == null) ? e : throwable;
            throw new LuaException("Invalid method call." +
                    "\n  at " + method +
                    "\n  -> " + cause +
                    "\n");
        }
    }

    public static Method matchMethod(Object object, Method[] methods, String name, LuaValue[] values) throws LuaException {
        ArrayList<Method> matchedMethod = new ArrayList<>();
        for (Method method : methods) {
            if (!method.getName().equals(name)) continue;
            matchedMethod.add(method);
            if (object == null && Modifier.isStatic(method.getModifiers())) continue;
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

    public static Object callConstructor(Constructor<?> constructor, Class<?>[] paramTypes, LuaValue[] values) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Object[] objects = convertParams(paramTypes, values);
        return constructor.newInstance(objects);
    }

    public static Object callConstructor(Constructor<?> constructor, LuaValue[] values) throws LuaException, IllegalArgumentException {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        try {
            return callConstructor(constructor, paramTypes, values);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            Object cause = (throwable == null) ? e : throwable;
            throw new LuaException("Invalid constructor method call." +
                    "\n  at " + constructor +
                    "\n  -> " + cause +
                    "\n");
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
    public static int jclassIndex(long ptr, Class<?> clazz, String name) throws InvocationTargetException, IllegalAccessException {
        // Get lua instance
        Lua L = Jua.get(ptr);
        // Class.STATIC_FIELD
        Field field = ClassUtils.getPublicStaticField(clazz, name);
        if (field != null) return L.push(ClassUtils.getField(field), field.getType());
        // Class.innerClass
        Class<?> innerClass = ClassUtils.getInnerClass(clazz, name);
        if (innerClass != null) return L.push(innerClass);
        // Class.staticMethod(XXX)
        Method[] methods = clazz.getMethods();
        char prefix = name.charAt(0);
        String suffix = name.substring(1);
        boolean isLowerCase = Character.isLowerCase(prefix);
        final String PREFIX = "get";
        String methodName1; // get Xxx
        String methodName2; // get xXX
        if (isLowerCase) {
            methodName1 = PREFIX + Character.toUpperCase(prefix) + suffix;
            methodName2 = PREFIX + prefix + suffix;
        } else {
            methodName1 = PREFIX + prefix + suffix;
            methodName2 = PREFIX + Character.toLowerCase(prefix) + suffix;
        }
        Method matchMethod1 = null;
        Method matchMethod2 = null;
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                String methodName = method.getName();
                if (name.equals(method.getName())) {
                    return L.push(new JMethod(null, clazz, name));
                } else if (matchMethod1 == null && method.getParameterCount() == 0) {
                    if (methodName1.equals(methodName)) {
                        matchMethod1 = method;
                    } else if (matchMethod2 == null && methodName2.equals(methodName)) {
                        matchMethod2 = method;
                    }
                }
            }
        }
        if (matchMethod1 != null)
            return L.push(ClassUtils.getMethodField(matchMethod1), matchMethod1.getReturnType());
        else if (matchMethod2 != null)
            return L.push(ClassUtils.getMethodField(matchMethod2), matchMethod2.getReturnType());
        throw new LuaException(String.format("%s@%s is not a field or method", clazz.getName(), name));
    }

    public static int jclassNewIndex(long ptr, Class<?> clazz, String name) throws InvocationTargetException, IllegalAccessException {
        // Get lua instance
        Lua L = Jua.get(ptr);
        // Class.STATIC_FIELD = value
        LuaValue[] values = L.getAll(3);
        Field field = ClassUtils.getPublicStaticField(clazz, name);
        if (field != null) {
            if (!Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                field.set(null, values[0].toJavaObject(field.getType()));
                return 0;
            }
            throw new LuaException(String.format("%s@%s is a final field that cannot be changed", clazz.getName(), name));
        }
        // Class.STATIC_METHOD = value (setter)
        Method[] methods = clazz.getMethods();
        char prefix = name.charAt(0);
        String suffix = name.substring(1);
        boolean isLowerCase = Character.isLowerCase(prefix);
        final String PREFIX = "set";
        String methodName1; // set Xxx
        String methodName2; // set xXX
        if (isLowerCase) {
            methodName1 = PREFIX + Character.toUpperCase(prefix) + suffix;
            methodName2 = PREFIX + prefix + suffix;
        } else {
            methodName1 = PREFIX + prefix + suffix;
            methodName2 = PREFIX + Character.toLowerCase(prefix) + suffix;
        }
        // Method matchMethod1 = null;
        Method matchMethod2 = null;
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == values.length) {
                String methodName = method.getName();
                if (methodName1.equals(methodName)) {
                    callMethod(null, method, method.getParameterTypes(), values);
                    return 0;
                } else if (matchMethod2 == null && methodName2.equals(methodName)) {
                    matchMethod2 = method;
                }
            }
        }
        if (matchMethod2 != null) {
            callMethod(null, matchMethod2, matchMethod2.getParameterTypes(), values);
            return 0;
        }
        throw new LuaException(String.format("%s@%s is not a field", clazz.getName(), name));
    }

    public static int jclassNew(long ptr, Class<?> clazz) throws LuaException {
        Lua L = Jua.get(ptr);
        LuaValue[] values = L.getAll(2);
        ArrayList<Constructor<?>> matchedConstructors = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        for (Constructor<?> constructor : clazz.getConstructors()) {
            matchedConstructors.add(constructor);
            try {
                return L.push(JuaAPI.callConstructor(constructor, values));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (values.length == 1) {
            LuaValue value = values[0];
            if (value.isTable()) {
                return L.push(value.toJavaArray(clazz));
            }
        }
        msg.append("Invalid constructor call. Invalid Parameters.").append("\n");
        for (Constructor<?> constructor : matchedConstructors) {
            msg.append(constructor);
            msg.append("\n");
        }
        throw new LuaException(msg.toString());
    }

    /* Java Object */
    public static int jobjectIndex(long ptr, Object instance, String name) throws IllegalAccessException, InvocationTargetException {
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
        char prefix = name.charAt(0);
        String suffix = name.substring(1);
        boolean isLowerCase = Character.isLowerCase(prefix);
        String methodName1; // get Xxx
        String methodName2; // get xXX
        final String PREFIX = "get";
        if (isLowerCase) {
            methodName1 = PREFIX + Character.toUpperCase(prefix) + suffix;
            methodName2 = PREFIX + prefix + suffix;
        } else {
            methodName1 = PREFIX + prefix + suffix;
            methodName2 = PREFIX + Character.toLowerCase(prefix) + suffix;
        }
        Method matchedGetMethod1 = null;
        Method matchedGetMethod2 = null;
        Method matchedStaticGetMethod1 = null;
        Method matchedStaticGetMethod2 = null;
        Method matchedStaticMethod = null;
        for (Method method : methods) {
            String methodName = method.getName();
            if (!Modifier.isStatic(method.getModifiers())) { // instance method first
                if (name.equals(method.getName())) { // object.methodName
                    L.push(new JMethod(instance, clazz, name));
                    return 1;
                } else if (matchedGetMethod1 == null) { // object.get Xxx
                    if (methodName1.equals(methodName)) {
                        matchedGetMethod1 = method;
                    } else if (methodName2.equals(methodName)) { // object.get xXX
                        matchedGetMethod2 = method;
                    }
                }
            } else {
                if (name.equals(method.getName())) { // Class.methodName
                    matchedStaticMethod = method;
                } else if (matchedStaticGetMethod1 == null) { // Class.get Xxx
                    if (methodName1.equals(methodName)) {
                        matchedStaticGetMethod1 = method;
                    } else if (methodName2.equals(methodName)) { // Class.get xXX
                        matchedStaticGetMethod2 = method;
                    }
                }
            }
        }
        if (matchedStaticMethod != null)
            return L.push(new JMethod(null, clazz, name));
        else if (matchedGetMethod1 != null)
            return L.push(ClassUtils.callMethod(instance, matchedGetMethod1, null), matchedGetMethod1.getReturnType());
        else if (matchedGetMethod2 != null)
            return L.push(ClassUtils.callMethod(instance, matchedGetMethod2, null), matchedGetMethod2.getReturnType());
        else if (matchedStaticGetMethod1 != null)
            return L.push(ClassUtils.callMethod(null, matchedStaticGetMethod1, null), matchedStaticGetMethod1.getReturnType());
        else if (matchedStaticGetMethod2 != null)
            return L.push(ClassUtils.callMethod(null, matchedStaticGetMethod2, null), matchedStaticGetMethod2.getReturnType());
        throw new LuaException(String.format("%s@%s is not a field or method", clazz.getName(), name));
    }

    public static int jobjectNewIndex(long ptr, Object object, String name) throws InvocationTargetException, IllegalAccessException {
        // Get lua instance
        Lua L = Jua.get(ptr);
        Class<?> clazz = object.getClass();
        LuaValue[] values = L.getAll(3);
        Field field = ClassUtils.getPublicField(clazz, name);
        if (field != null) {
            if (!Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                field.set(object, values[0].toJavaObject(field.getType()));
                return 0;
            }
            throw new LuaException(String.format("%s@%s is a final field that cannot be changed", clazz.getName(), name));
        }
        // Class.STATIC_METHOD = value (setter)
        Method[] methods = clazz.getMethods();
        char prefix = name.charAt(0);
        String suffix = name.substring(1);
        boolean isLowerCase = Character.isLowerCase(prefix);
        final String PREFIX = "set";
        String methodName1; // set Xxx
        String methodName2; // set xXX
        if (isLowerCase) {
            methodName1 = PREFIX + Character.toUpperCase(prefix) + suffix;
            methodName2 = PREFIX + prefix + suffix;
        } else {
            methodName1 = PREFIX + prefix + suffix;
            methodName2 = PREFIX + Character.toLowerCase(prefix) + suffix;
        }
        // Method matchMethod1 = null;
        Method matchStaticMethod1 = null;
        Method matchStaticMethod2 = null;
        Method matchMethod2 = null;
        for (Method method : methods) {
            if (method.getParameterCount() == values.length) {
                String methodName = method.getName();
                if (!Modifier.isStatic(method.getModifiers())) { // instance method first
                    if (methodName1.equals(methodName)) {
                        callMethod(object, method, method.getParameterTypes(), values);
                        return 0;
                    } else if (matchMethod2 == null && methodName2.equals(methodName)) {
                        matchMethod2 = method;
                    }
                } else {
                    if (methodName1.equals(methodName)) {
                        matchStaticMethod1 = method;
                    } else if (matchStaticMethod2 == null && methodName2.equals(methodName)) {
                        matchStaticMethod2 = method;
                    }
                }
            }
        }
        if (matchMethod2 != null) {
            callMethod(object, matchMethod2, matchMethod2.getParameterTypes(), values);
        } else if (matchStaticMethod1 != null) {
            callMethod(object, matchStaticMethod1, matchStaticMethod1.getParameterTypes(), values);
        } else if (matchStaticMethod2 != null) {
            callMethod(object, matchStaticMethod2, matchStaticMethod2.getParameterTypes(), values);
        } else {
            throw new LuaException(String.format("%s@%s is not a field", clazz.getName(), name));
        }
        return 0;
    }

    private final static String[] LENGTH_DEFAULT_METHOD_NAME = new String[]{"length", "size"};

    public static int jobjectLength(long ptr, Object instance) {
        Lua L = Jua.get(ptr);
        Class<?> clazz = instance.getClass();
        for (String name : LENGTH_DEFAULT_METHOD_NAME) {
            Object result = ClassUtils.callObjectNoArgsMethod(instance, name);
            if (result != null) return L.push(result);
        }
        throw new LuaException(String.format("%s has no default method to get length", clazz.getName()));
    }

    public static int jarrayIndex(long ptr, Object array) {
        Lua L = Jua.get(ptr);
        int index = (int) L.get(2).toJavaObject(int.class);
        Class<?> type = array.getClass().getComponentType();
        Object object = Array.get(array, index);
        return L.push(object, type);
    }

    public static int jarrayNewIndex(long ptr, Object array) {
        Lua L = Jua.get(ptr);
        Class<?> type = array.getClass().getComponentType();
        int index = (int) L.get(2).toJavaObject(int.class);
        Object value = L.get(3).toJavaObject(type);
        Array.set(array, index, value);
        return 0;
    }

    public static int jarrayIpairsIterator(long ptr, Object array) {
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

    public static int bindClass(long ptr, String name) throws ClassNotFoundException {
        Lua L = Jua.get(ptr);
        L.push(ClassUtils.forName(name));
        return 1;
    }

    public static String getStackTrace(Throwable throwable) {
        if (throwable.getClass() == LuaException.class) {
            return throwable.getMessage();
        }
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
    public static int unwrap(long id, Object obj) {
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
    public static int jmoduleLoad(long id, String module) {
        Lua L = Jua.get(id);
        return L.loadExternal(module);
    }

    /**
     * Loads a Java static method that accepts a single {@link Lua} parameter and returns an integer
     *
     * @param id         see {@link Jua#get(long)}
     * @param className  the clazz name
     * @param methodName the method name
     * @return the number of elements pushed onto the stack
     */
    public static int loadLib(long id, String className, String methodName) {
        Lua L = Jua.get(id);
        try {
            Class<?> clazz = Class.forName(className);
            final Method method = clazz.getDeclaredMethod(methodName, Lua.class);
            if (method.getReturnType() == int.class) {
                //noinspection Convert2Lambda
                return L.push(new CFunction() {
                    @Override
                    public int __call(Lua l) {
                        try {
                            return (Integer) method.invoke(null, l);
                        } catch (IllegalAccessException e) {
                            return l.error(e);
                        } catch (InvocationTargetException e) {
                            return l.error(e.getCause());
                        }
                    }
                });
            } else {
                L.pushNil();
                L.push("\n  no method '" + methodName + "': not returning int values");
                return 2;
            }
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            L.pushNil();
            L.push("\n  no method '" + methodName + "': no such method");
            return 2;
        }
    }

    /**
     * Calls a {@link CFunction}
     *
     * @param index the id of {@link Jua} thread calling this method
     * @param obj   the {@link CFunction} object
     * @return the number result pushed on stack
     */
    public static int jfunctionCall(long index, Object obj) {
        Lua L = Jua.get(index);
        if (obj instanceof CFunction) {
            return ((CFunction) obj).__call(L);
        } else {
            L.push("error invoking object (expecting a CFunction)");
            return -1;
        }
    }

    /**
     * Gets an element of an array
     *
     * @param index the lua state index
     * @param obj   the array
     * @param i     the index (lua index, starting from 1)
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int arrayIndex(long index, Object obj, int i) {
        Lua L = Jua.get(index);
        try {
            Object e = Array.get(obj, i - 1);
            L.push(e, Lua.Conversion.SEMI);
            return 1;
        } catch (Exception e) {
            return L.error(e);
        }
    }

    /**
     * Assigns to an element of an array
     *
     * @param index the lua state index
     * @param obj   the array
     * @param i     the index (lua index, starting from 1)
     * @return the number of values pushed onto the stack
     */
    public static int arrayNewIndex(long index, Object obj, int i) {
        Lua L = Jua.get(index);
        try {
            Array.set(obj, i - 1, L.toObject(L.getTop(), obj.getClass().getComponentType()));
            return 0;
        } catch (Exception e) {
            return L.error(e);
        }
    }

    /**
     * @param obj the array
     * @return the array length
     */
    public static int arrayLength(Object obj) {
        try {
            return Array.getLength(obj);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Converts an element on the lua stack at <code>index</code> to Java
     *
     * @param L     the lua state
     * @param clazz the expected return type
     * @param index a <b>lua</b> index (that is, starts from 1)
     * @return the converted element
     * @throws IllegalArgumentException when unable to convert
     */
    @Nullable
    public static Object convertFromLua(Lua L, Class<?> clazz, int index)
            throws IllegalArgumentException {
        LuaType type = L.type(index);
        if (type == LuaType.NIL) {
            if (clazz.isPrimitive()) {
                throw new IllegalArgumentException("Primitive not accepting null values");
            } else {
                return null;
            }
        } else if (type == LuaType.BOOLEAN) {
            if (clazz == boolean.class || clazz == Boolean.class) {
                return L.toBoolean(index);
            }
        } else if (type == LuaType.STRING && clazz.isAssignableFrom(String.class)) {
            return L.toString(index);
        } else if (type == LuaType.NUMBER) {
            if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)) {
                Number v = L.toNumber(index);
                return convertNumber(v, clazz);
            } else if (Character.class == clazz) {
                return (char) L.toNumber(index);
            } else if (Boolean.class == clazz) {
                return L.toNumber(index) != 0;
            } else if (clazz == Object.class) {
                return L.toNumber(index);
            }
        } else if (type == LuaType.USERDATA) {
            Object object = L.toJavaObject(index);
            if (object != null && clazz.isAssignableFrom(object.getClass())) {
                return object;
            }
        } else if (type == LuaType.TABLE) {
            if (clazz.isAssignableFrom(List.class)) {
                return L.toList(index);
            } else if (clazz.isArray() && clazz.getComponentType() == Object.class) {
                return Objects.requireNonNull(L.toList(index)).toArray(new Object[0]);
            } else if (clazz.isAssignableFrom(Map.class)) {
                return L.toMap(index);
            } else if (clazz.isInterface() && !clazz.isAnnotation()) {
                L.pushValue(index);
                return L.createProxy(-1, clazz, Lua.Conversion.SEMI);
            }
        } else if (type == LuaType.FUNCTION) {
            String descriptor = ClassUtils.getSingleInterfaceMethodName(clazz);
            if (descriptor != null) {
                L.pushValue(index);
                L.createTable(0, 1);
                L.insert(L.getTop() - 1);
                L.setField(-2, descriptor);
                return L.createProxy(-1, clazz, Lua.Conversion.SEMI);
            }
        }
        if (clazz.isAssignableFrom(LuaValue.class)) {
            L.pushValue(index);
            return L.get();
        }
        throw new IllegalArgumentException("Unable to convert to " + clazz.getName());
    }

    private static Object convertNumber(Number i, Class<?> clazz)
            throws IllegalArgumentException {
        if (clazz.isPrimitive()) {
            if (boolean.class == clazz) {
                return i.intValue() != 0;
            }
            if (char.class == clazz) {
                return (char) i.byteValue();
            } else if (byte.class == clazz) {
                return i.byteValue();
            } else if (short.class == clazz) {
                return i.shortValue();
            } else if (int.class == clazz) {
                return i.intValue();
            } else if (long.class == clazz) {
                return i.longValue();
            } else if (float.class == clazz) {
                return i.floatValue();
            } else /* if (double.clazz == clazz) */ {
                return i.doubleValue();
            }
        } else {
            return convertBoxedNumber(i, clazz);
        }
    }

    private static Number convertBoxedNumber(Number i, Class<?> clazz)
            throws IllegalArgumentException {
        if (Byte.class == clazz) {
            return i.byteValue();
        } else if (Short.class == clazz) {
            return i.shortValue();
        } else if (Integer.class == clazz) {
            return i.intValue();
        } else if (Long.class == clazz) {
            return i.longValue();
        } else if (Float.class == clazz) {
            return i.floatValue();
        } else if (Double.class == clazz) {
            return i.doubleValue();
        }
        throw new IllegalArgumentException("Unsupported conversion");
    }
}
