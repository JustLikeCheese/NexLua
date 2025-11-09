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

import android.app.AlertDialog;
import android.widget.Button;

import com.luajava.util.ClassUtils;
import com.luajava.util.LRUCacheFactory;
import com.luajava.value.LuaProxy;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Pattern;

// LuaJava Helper
public final class JuaAPI {
    public static boolean matchParams(Class<?>[] paramTypes, LuaValue[] values) {
        if (paramTypes.length != values.length) {
            return false;
        }
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
        if ((object != null || Modifier.isStatic(method.getModifiers())) && matchParams(paramTypes, values)) {
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
        throw new IllegalArgumentException("Invalid method call. Invalid Parameters.");
    }

    public static Object callMethod(Object object, Method[] methods, String name, LuaValue[] values) throws LuaException {
        ArrayList<Method> matchedMethod = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        for (Method method : methods) {
            if (!method.getName().equals(name)) continue;
            matchedMethod.add(method);
            try {
                return JuaAPI.callMethod(object, method, values);
            } catch (IllegalArgumentException ignored) {
            }
            // catch (LuaException e) {}
        }
        msg.append("Invalid method call. Invalid Parameters.").append("\n");
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
        if (matchParams(paramTypes, values)) {
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
        throw new IllegalArgumentException("Invalid constructor method call. Invalid Parameters.");
    }

    public static Object callConstructor(Constructor<?>[] constructors, LuaValue[] values) throws LuaException {
        ArrayList<Constructor<?>> matchedConstructors = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        for (Constructor<?> constructor : constructors) {
            matchedConstructors.add(constructor);
            try {
                return JuaAPI.callConstructor(constructor, values);
            } catch (IllegalArgumentException ignored) {
            }
        }
        msg.append("Invalid constructor call. Invalid Parameters.").append("\n");
        for (Constructor<?> constructor : matchedConstructors) {
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
        if (field != null) {
            L.push(ClassUtils.getField(field));
            return 1;
        }
        // Class.staticMethod(XXX)
        Method[] methods = clazz.getMethods();
        char prefix = name.charAt(0);
        String suffix = name.substring(1);
        boolean isLowerCase = Character.isLowerCase(prefix);
        String methodName1; // get Xxx
        String methodName2; // get xXX
        if (isLowerCase) {
            methodName1 = Character.toUpperCase(prefix) + suffix;
            methodName2 = prefix + suffix;
        } else {
            methodName1 = prefix + suffix;
            methodName2 = Character.toLowerCase(prefix) + suffix;
        }
        Method matchMethod1 = null;
        Method matchMethod2 = null;
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                String methodName = method.getName();
                if (name.equals(method.getName())) {
                    L.push(new JMethod(null, clazz, name));
                    return 1;
                } else if (matchMethod1 == null) {
                    if (methodName1.equals(methodName)) {
                        matchMethod1 = method;
                    } else if (methodName2.equals(methodName)) {
                        matchMethod2 = method;
                    }
                }
            }
        }
        if (matchMethod1 != null) {
            L.push(ClassUtils.getMethodField(matchMethod1));
            return 1;
        } else if (matchMethod2 != null) {
            L.push(ClassUtils.getMethodField(matchMethod2));
            return 1;
        }
        throw new LuaException(String.format("%s@%s is not a field or method", clazz.getName(), name));
    }

    @SuppressWarnings("unused")
    public static int jclassNew(long ptr, Class<?> clazz) throws LuaException {
        Lua L = Jua.get(ptr);
        LuaValue[] values = L.getAll(2);
        Constructor<?>[] constructors = clazz.getConstructors();
        L.push(callConstructor(constructors, values));
        return 1;
    }

    /* Java Object */
    public static int jobjectIndex(long ptr, Object instance, String name) throws IllegalAccessException, InvocationTargetException {
        Lua L = Jua.get(ptr);
        // Class.STATIC_FIELD
        Class<?> clazz = instance.getClass();
        Field field = ClassUtils.getPublicStaticField(clazz, name);
        if (field != null) {
            L.push(ClassUtils.getField(field));
            return 1;
        }
        // Class.staticMethod(XXX)
        Method[] methods = clazz.getMethods();
        char prefix = name.charAt(0);
        String suffix = name.substring(1);
        boolean isLowerCase = Character.isLowerCase(prefix);
        String methodName1; // get Xxx
        String methodName2; // get xXX
        if (isLowerCase) {
            methodName1 = Character.toUpperCase(prefix) + suffix;
            methodName2 = prefix + suffix;
        } else {
            methodName1 = prefix + suffix;
            methodName2 = Character.toLowerCase(prefix) + suffix;
        }
        Method matchedGetMethod1 = null;
        Method matchedGetMethod2 = null;
        Method matchedStaticGetMethod1 = null;
        Method matchedStaticGetMethod2 = null;
        Method matchedMethod = null;
        Method matchedStaticMethod = null;
        for (Method method : methods) {
            String methodName = method.getName();
            if (!Modifier.isStatic(method.getModifiers())) { // Instance method first
                if (name.equals(method.getName())) {
                    L.push(new JMethod(instance, clazz, name));
                    return 1;
                } else if (matchedGetMethod1 == null) {
                    if (methodName1.equals(methodName)) {
                        matchedGetMethod1 = method;
                    } else if (methodName2.equals(methodName)) {
                        matchedGetMethod2 = method;
                    }
                }
            } else {
                if (name.equals(method.getName())) {
                    matchedStaticMethod = method;
                } else if (matchedGetMethod1 == null) {
                    if (methodName1.equals(methodName)) {
                        matchedStaticGetMethod1 = method;
                    } else if (methodName2.equals(methodName)) {
                        matchedStaticGetMethod2 = method;
                    }
                }
            }
        }
        if (matchedStaticMethod != null) {
            L.push(new JMethod(null, clazz, name));
            return 1;
        } else if (matchedGetMethod1 != null) {
            L.push(ClassUtils.callMethod(instance, matchedGetMethod1, null));
            return 1;
        } else if (matchedGetMethod2 != null) {
            L.push(ClassUtils.callMethod(instance, matchedGetMethod2, null));
            return 1;
        } else if (matchedStaticGetMethod1 != null) {
            L.push(ClassUtils.callMethod(null, matchedStaticGetMethod1, null));
            return 1;
        } else if (matchedStaticGetMethod2 != null) {
            L.push(ClassUtils.callMethod(null, matchedStaticGetMethod2, null));
            return 1;
        }
        throw new LuaException(String.format("%s@%s is not a field or method", clazz.getName(), name));
    }

    public static int jobjectLength(long ptr, Object instance) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Lua L = Jua.get(ptr);
        Class<?> clazz = instance.getClass();
        Method method = clazz.getMethod("length");
        Object result = method.invoke(instance);
        L.push(result);
        return 1;
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
                    proxy.unwrap();
                    return 1;
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
                L.push(new CFunction() {
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
                return 1;
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
     * @param L the Lua state
     * @param i the stack position
     * @return a clazz converted from the value (jobject, jclass or string) at the stack position
     */
    private static @Nullable Class<?> looseGetClass(Lua L, int i) {
        if (L.isUserdata(i)) {
            Object o = L.toJavaObject(i);
            return o instanceof Class ? ((Class<?>) o) : null;
        } else {
            String name = L.toString(i);
            if (name != null) {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Pushes onto the stack a jclass or a package
     *
     * <p>
     * See the <code>java.import</code> API and <code>javaImport</code> in <code>jni/luajava/jualib.cpp</code>.
     * </p>
     *
     * @param id        the lua state id
     * @param className name of a clazz or a package
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int javaImport(long id, String className) {
        Lua L = Jua.get(id);
        try {
            L.push(Class.forName(className));
            return 1;
        } catch (ClassNotFoundException e) {
            return L.error(e);
        }
    }

    /**
     * Converts a value on top of the stack into a more Lua-style form
     *
     * @param id the state id
     * @return the number of return values, always 1
     */
    public static int luaify(long id) {
        Lua L = Jua.get(id);
        Object o = L.toJavaObject(-1);
        if (o != null) {
            L.push(o, Lua.Conversion.FULL);
        }
        return 1;
    }

    /**
     * Allocates an ID for a thread created on the Lua side
     *
     * @param ptr the pointer to the lua state who does not have an id
     * @return an allocated id
     */
    @SuppressWarnings("unused")
    public static int threadNewId(long ptr) {
//        return Lua.adopt(ptr);
        return 0;
    }

    /**
     * Closes a sub-thread
     *
     * @param id the thread id
     * @return 1
     */
    @SuppressWarnings("unused")
    public static int freeThreadId(long id) {
        Lua L = Jua.get(id);
        if (L.getMainState() != L) {
            L.close();
            return 0;
        } else {
            throw new LuaException(LuaException.LuaError.MEMORY, "unable to detach a main state");
        }
    }

    /**
     * Obtains the value of a certain field of an object
     *
     * <p>
     * This method is mainly used by the JNI side. See {@code jni/luajava/jua.cpp}.
     * </p>
     *
     * <p>
     * For static fields, use {@link #jclassIndex(long, Class, String)} instead.
     * </p>
     *
     * @param index  the id of {@link Jua} thread calling this method
     * @param object the object
     * @param name   the name of the field
     * @return {@code 1} if a field is found, {@code 2} otherwise
     * @see #fieldIndex(Lua, Class, Object, String)
     */
    @SuppressWarnings("unused")
    public static int objectIndex(long index, @NotNull Object object, String name) {
        return fieldIndex(Jua.get(index), object.getClass(), object, name);
    }

    /**
     * Invokes a member method of an object
     *
     * <p>
     * This method is mainly used by the JNI side. See {@code jni/luajava/jua.cpp}.
     * If {@code name} is {@code null}, we assume that the object is a {@link CFunction}.
     * </p>
     *
     * <p>
     * For static fields, use {@link #classIndex(long, Class, String)} instead.
     * </p>
     *
     * @param index      the id of {@link Jua} thread calling this method
     * @param obj        the object
     * @param name       the name of the field
     * @param paramCount number of parameters (on the lua stack)
     * @return the number result pushed on stack
     * @see #methodInvoke(Lua, Method, Object, Object[])
     */
    public static int objectInvoke(long index, @NotNull Object obj, @Nullable String name, int paramCount) {
        return methodInvoke(index, obj.getClass(), obj, name, paramCount);
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
     * Invokes a member method of an object
     *
     * @param index        the lua state index
     * @param obj          the object
     * @param name         the method name
     * @param notSignature the method signature, comma separated
     * @param paramCount   the parameter count
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int objectInvoke(long index, Object obj, String name,
                                   String notSignature, int paramCount) {
        int colon = name.indexOf(':');
        if (colon == -1) {
            return methodInvoke(index, obj.getClass(), obj, name, notSignature, paramCount);
        } else {
            String iClass = name.substring(0, colon);
            String method = name.substring(colon + 1);
            try {
                return methodInvoke(index, Class.forName(iClass), obj, method,
                        notSignature, paramCount);
            } catch (ClassNotFoundException e) {
                return Jua.get(index).error(e);
            }
        }
    }

    /**
     * Sets a field of an object
     *
     * @param index the lua state index
     * @param obj   the object
     * @param name  the field name
     * @return always 0
     */
    @SuppressWarnings("unused")
    public static int objectNewIndex(long index, Object obj, String name) {
        return fieldNewIndex(index, obj.getClass(), obj, name);
    }

    private final static LRUCacheFactory.LRUCacheSingle<Class<?>, Constructor<?>[]> CONSTRUCTORS_CACHE = new LRUCacheFactory.LRUCacheSingle<>(
            25,
            4
    );

    /**
     * Constructs an instance of a clazz
     *
     * @param index      the lua state index
     * @param oClazz     the clazz ({@link Object} typed to manually handle mismatched types)
     * @param paramCount the parameter count
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int classNew(long index, Object oClazz, int paramCount) {
        Class<?> clazz;
        Lua L = Jua.get(index);
        if (oClazz instanceof Class) {
            clazz = ((Class<?>) oClazz);
        } else {
            L.push("bad argument #1 to java.new (expecting Class<?>)");
            return -1;
        }
        if (clazz.isInterface()) {
            try {
                L.pushJavaObject(L.createProxy(-1, clazz, Lua.Conversion.SEMI));
                return 1;
            } catch (IllegalArgumentException e) {
                return L.error(e);
            }
        }
        Object[] objects = new Object[paramCount];
        Constructor<?>[] constructors = CONSTRUCTORS_CACHE.get(clazz);
        if (constructors == null) {
            constructors = clazz.getConstructors();
            CONSTRUCTORS_CACHE.put(clazz, constructors);
        }
        Constructor<?> constructor = matchMethod(L, constructors, CONSTRUCTOR_WRAPPER, objects);
        if (constructor != null) {
            return construct(L, objects, constructor);
        }
        L.push("no matching constructor found");
        return -1;
    }

    /**
     * Constructs an instance
     *
     * @param L           the lua state
     * @param objects     the parameters
     * @param constructor the constructor
     * @return the number of values pushed onto the stack
     */
    private static int construct(Lua L, Object[] objects, Constructor<?> constructor) {
        try {
            Object obj = constructor.newInstance(objects);
            L.pushJavaObject(obj);
            return 1;
        } catch (InstantiationException | IllegalAccessException e) {
            return L.error(e);
        } catch (InvocationTargetException e) {
            return L.error(e.getCause());
        }
    }

    /**
     * Pushes a static field of a clazz onto the stack
     *
     * <p>Used by <code>jobjectIndex</code> in <code>jni/luajava/luaapi.cpp</code></p>
     *
     * @param index the lua state index
     * @param clazz the clazz
     * @param name  the field name
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int classIndex(long index, Class<?> clazz, String name) {
        Lua L = Jua.get(index);
        if (name.equals("clazz")) {
            L.pushJavaObject(clazz);
            return 1;
        } else {
            int i = fieldIndex(L, clazz, null, name);
            if (i == 1) {
                return 1;
            } else {
                try {
                    L.pushJavaObject(Class.forName(clazz.getName() + '$' + name));
                    return 1;
                } catch (ClassNotFoundException e) {
                    return i;
                }
            }
        }
    }

    /**
     * Invokes a static method of a clazz
     *
     * @param index      the lua state index
     * @param clazz      the clazz
     * @param name       the method name
     * @param paramCount the parameter count
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int classInvoke(long index, Class<?> clazz, String name, int paramCount) {
        return methodInvoke(index, clazz, null, name, paramCount);
    }

    /**
     * Invokes a static method of a clazz
     *
     * @param index        the lua state index
     * @param clazz        the clazz
     * @param name         the method name
     * @param notSignature the signature, comma separated
     * @param paramCount   the parameter count
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int classInvoke(long index, Class<?> clazz, String name,
                                  String notSignature, int paramCount) {
        return methodInvoke(index, clazz, null, name, notSignature, paramCount);
    }

    /**
     * Assigns to a static field
     *
     * @param index the lua state index
     * @param clazz the clazz
     * @param name  the field name
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int classNewIndex(long index, Class<?> clazz, String name) {
        return fieldNewIndex(index, clazz, null, name);
    }

    /**
     * Pushes a created array onto the stack
     *
     * <p>
     * Use negative <code>size</code> to indicate that we should create a multi-dimensional array,
     * with the dimensions read from the stack.
     * </p>
     *
     * @param index  the lua state index
     * @param oClass the clazz
     * @param size   the array size
     * @return the number of values pushed onto the stack
     */
    @SuppressWarnings("unused")
    public static int arrayNew(long index, Object oClass, int size) {
        Class<?> clazz;
        Lua L = Jua.get(index);
        if (oClass instanceof Class && oClass != Void.TYPE) {
            clazz = ((Class<?>) oClass);
        } else {
            L.push("bad argument #1 to 'java.array' (expecting Class<?>)");
            return -1;
        }
        if (size >= 0) {
            L.pushJavaObject(Array.newInstance(clazz, size));
        } else {
            int depth = -size;
            int[] sizes = new int[depth];
            for (int i = size; i <= -1; i++) {
                if (!L.isNumber(i)) {
                    L.push("bad argument #" + (i - size + 2) + " to 'java.array' (expecting number)");
                    return -1;
                }
                int current = (int) L.toNumber(i);
                if (current < 0) {
                    L.push("bad argument #" + (i - size + 2) + " to 'java.array' (expecting non negative)");
                    return -1;
                }
                sizes[i - size] = current;
            }
            L.pushJavaObject(Array.newInstance(clazz, sizes));
        }
        return 1;
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

    private final static LRUCacheFactory.LRUCache<Class<?>, String, Method[]> MEMBER_METHOD_CACHE = new LRUCacheFactory.LRUCache<>(
            25,
            10,
            4
    );

    /**
     * Calls the given method <code>{obj}.{name}(... params from stack)</code>
     * <p>
     * Judge param types from the lua stack and pushes results afterwards.
     * The params are expected to be on stack positions from <code>-paramCount</code>
     * to <code>-1</code>. For example, if you have two params, then the first
     * is expected to be at -2, and the second be at -1.
     * </p>
     *
     * @param index      the index of Java-side {@link Jua}
     * @param clazz      the {@link Class}
     * @param obj        the object (nullable when calling static methods)
     * @param name       the method name
     * @param paramCount number of supplied params
     * @return the number result pushed on stack
     */
    public static int methodInvoke(long index, Class<?> clazz, @Nullable Object obj,
                                   String name, int paramCount) {
        Lua L = Jua.get(index);
        /* Storage of converted params */
        Object[] objects = new Object[paramCount];
        Method[] methods = MEMBER_METHOD_CACHE.get(clazz, name);
        if (methods == null) {
            List<Method> namedMethods = new ArrayList<>();
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(name)) {
                    namedMethods.add(method);
                }
            }
            methods = namedMethods.toArray(new Method[0]);
            MEMBER_METHOD_CACHE.put(clazz, name, methods);
        }
        Method method = matchMethod(L, methods, METHOD_WRAPPER, objects);
        if (method == null) {
            L.push("no matching method found");
            return -1;
        } else {
            return methodInvoke(L, method, obj, objects);
        }
    }

    /**
     * Invokes a method
     *
     * @param index        the lua state index
     * @param clazz        the clazz
     * @param obj          the object, {@code null} if calling a static method
     * @param name         the method name
     * @param notSignature method signature, comma separated
     * @param paramCount   the parameter count
     * @return the number of values pushed onto the stack
     */
    public static int methodInvoke(long index, Class<?> clazz, @Nullable Object obj,
                                   String name, String notSignature, int paramCount) {
        Lua L = Jua.get(index);
        if ("new".equals(name)) {
            if (obj == null) {
                Constructor<?> constructor = matchMethod(clazz, notSignature);
                if (constructor != null) {
                    Object[] objects = new Object[paramCount];
                    if (matchMethod(L, new Constructor[]{constructor}, CONSTRUCTOR_WRAPPER, objects) != null) {
                        return construct(L, objects, constructor);
                    }
                }
                L.push("no matching constructor found");
                return -1;
            }
            L.push("bad argument to constructor (Class<?> expected, got Object)");
            return -1;
        }
        Method method = matchMethod(clazz, name, notSignature);
        if (method != null) {
            Object[] objects = new Object[paramCount];
            if (matchMethod(L, new Method[]{method}, METHOD_WRAPPER, objects) != null) {
                if (clazz.isInterface()) {
                    return specialInvoke(L, method, obj, objects);
                } else {
                    return methodInvoke(L, method, obj, objects);
                }
            }
        }
        L.push("no matching method found");
        return -1;
    }

    /**
     * Invokes a method and pushes the result onto the stack
     *
     * @param L       the lua state
     * @param method  the method
     * @param obj     the object
     * @param objects the parameters
     * @return the number of values pushed onto the stack
     */
    private static int specialInvoke(Lua L, Method method, @Nullable Object obj, Object[] objects) {
        Object ret;
        try {
            ret = L.invokeSpecial(obj, method, objects);
        } catch (Throwable e) {
            return L.error(e);
        }
        if (ret == null) {
            return 0;
        } else {
            L.push(ret, Lua.Conversion.SEMI);
            return 1;
        }
    }

    /**
     * Invokes a method and pushes the result onto the stack
     *
     * @param L       the lua state
     * @param method  the method
     * @param obj     the object
     * @param objects the parameters
     * @return the number of values pushed onto the stack
     */
    public static int methodInvoke(Lua L, Method method, @Nullable Object obj, Object[] objects) {
        Object ret;
        try {
            ret = method.invoke(obj, objects);
        } catch (IllegalAccessException e) {
            return L.error(e);
        } catch (InvocationTargetException e) {
            return L.error(e.getCause());
        }
        if (ret == null) {
            return 0;
        } else {
            L.push(ret, Lua.Conversion.SEMI);
            return 1;
        }
    }

    /* TODO: Until we finally support varargs
    private static Object[] transformVarArgs(Executable executable, Object[] objects) {
        if (executable.isVarArgs()) {
            int count = executable.getParameterCount();
            Object[] transformed = new Object[count];

            Class<?>[] types = executable.getParameterTypes();
            Class<?> component = types[types.length - 1].getComponentType();
            // TODO: Handle primitive types
            Object[] args = (Object[]) Array.newInstance(component, objects.length - count + 1);

            System.arraycopy(objects, 0, transformed, 0, count - 1);
            System.arraycopy(objects, count - 1, args, 0, args.length);
            transformed[count - 1] = args;
            return transformed;
        } else {
            return objects;
        }
    }
    */

    private final static class OptionalField {
        @Nullable
        public final Field field;

        private OptionalField(@Nullable Field field) {
            this.field = field;
        }
    }

    private final static LRUCacheFactory.LRUCache<Class<?>, String, OptionalField> OBJECT_FIELD_CACHE = new LRUCacheFactory.LRUCache<>(
            25,
            10,
            4
    );

    /**
     * Tries to fetch field from an object
     * <p>
     * When a matching field is found, it is pushed to the corresponding lua stack
     * </p>
     *
     * @param L      the {@link Lua} state
     * @param clazz  the {@link Class}
     * @param object the object
     * @param name   the name of the field / method
     * @return 1 if a field is found, 2 otherwise
     */
    public static int fieldIndex(Lua L, Class<?> clazz, @Nullable Object object, String name) {
        try {
            OptionalField optionalField = OBJECT_FIELD_CACHE.get(clazz, name);
            Field field;
            if (optionalField == null) {
                field = clazz.getField(name);
                OBJECT_FIELD_CACHE.put(clazz, name, new OptionalField(field));
            } else {
                field = optionalField.field;
                if (field == null) {
                    return 2;
                }
            }
            Object obj = field.get(object);
            L.push(obj, Lua.Conversion.SEMI);
            return 1;
        } catch (NoSuchFieldException | IllegalAccessException | NullPointerException ignored) {
            OBJECT_FIELD_CACHE.put(clazz, name, new OptionalField(null));
            return 2;
        }
    }

    /**
     * Assigns to a field
     *
     * @param index  the lua state index
     * @param clazz  the clazz
     * @param object the object
     * @param name   the field name
     * @return 0
     */
    private static int fieldNewIndex(long index, Class<?> clazz, Object object, String name) {
        Lua L = Jua.get(index);
        try {
            OptionalField optionalField = OBJECT_FIELD_CACHE.get(clazz, name);
            Field field;
            if (optionalField == null) {
                field = clazz.getField(name);
                OBJECT_FIELD_CACHE.put(clazz, name, new OptionalField(field));
            } else {
                field = optionalField.field;
                if (field == null) {
                    return L.error(new NoSuchFieldException(name));
                }
            }
            Class<?> type = field.getType();
            Object o = convertFromLua(L, type, 3);
            field.set(object, o);
            return 0;
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            OBJECT_FIELD_CACHE.put(clazz, name, new OptionalField(null));
            return L.error(e);
        }
    }

    /**
     * Matches methods against values on stack
     *
     * @param L       the lua state
     * @param methods filtered methods that only differ in their parameters
     * @param params  an array to store converted parameters
     * @param <T>     either {@link Method} or {@link Constructor}
     * @return a match method
     */
    @Nullable
    private static <T> T matchMethod(Lua L, T[] methods,
                                     ExecutableWrapper<T> wrapper,
                                     Object[] params) {
        for (T method : methods) {
            /*
             * This is costly since it clones the internal array.
             * However, getParameterCount() is not available on Android 4.4
             *
             * {@code Call requires API level 24 (current min is 19): java.lang.reflect.Method#isDefault}
             */
            Class<?>[] classes = wrapper.getParameterTypes(method);
            if (classes.length == params.length) {
                try {
                    for (int i = 0; i != params.length; ++i) {
                        params[i] = convertFromLua(L, classes[i], -params.length + i);
                    }
                } catch (IllegalArgumentException e) {
                    continue;
                }
                return method;
            }
        }
        return null;
    }

    private final static LRUCacheFactory.LRUCache<Class<?>, String, Constructor<?>> CONSTRUCTOR_CACHE = new LRUCacheFactory.LRUCache<>(
            25,
            5,
            4
    );

    private final static LRUCacheFactory.LRUCache<Class<?>, String, Method> METHOD_CACHE = new LRUCacheFactory.LRUCache<>(
            25,
            50,
            4
    );

    /**
     * Find a certain constructor
     *
     * @param clazz        the clazz
     * @param notSignature the signature, comma separated
     * @return the matching constructor
     */
    @Nullable
    private static Constructor<?> matchMethod(Class<?> clazz, String notSignature) {
        Constructor<?> cached = CONSTRUCTOR_CACHE.get(clazz, notSignature);
        if (cached != null) {
            return cached;
        }
        Class<?>[] classes = getClasses(notSignature);
        try {
            return clazz.getConstructor(classes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Find a certain method
     *
     * @param clazz        the clazz
     * @param name         the name of the method
     * @param notSignature the signature, comma separated
     * @return the matching method
     */
    @Nullable
    private static Method matchMethod(Class<?> clazz, String name, String notSignature) {
        Method cached = METHOD_CACHE.get(clazz, name + ",," + notSignature);
        if (cached != null) {
            return cached;
        }
        Class<?>[] classes = getClasses(notSignature);
        try {
            return clazz.getMethod(name, classes);
        } catch (NoSuchMethodException e) {
            return null;
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

    private static final Pattern COMMA_SPLIT = Pattern.compile(",");

    public static Class<?>[] getClasses(String notSignature) {
        if (notSignature == null || notSignature.isEmpty()) {
            return new Class<?>[0];
        }
        String[] names = COMMA_SPLIT.split(notSignature);
        Class<?>[] classes = new Class[names.length];
        for (int i = 0; i < names.length; i++) {
            try {
                classes[i] = Class.forName(names[i]);
            } catch (ClassNotFoundException e) {
                classes[i] = null;
            }
        }
        return classes;
    }

    /**
     * A wrapper to extract common parts from {@link Constructor} and {@link Method},
     * since {@code Executable} is not introduced until Java 8.
     */
    interface ExecutableWrapper<T> {
        @Nullable
        String getName(T executable);

        Class<?>[] getParameterTypes(T executable);
    }

    final static ExecutableWrapper<Constructor<?>> CONSTRUCTOR_WRAPPER =
            new ExecutableWrapper<Constructor<?>>() {
                @Override
                public @Nullable String getName(Constructor<?> executable) {
                    return null;
                }

                @Override
                public Class<?>[] getParameterTypes(Constructor<?> executable) {
                    return executable.getParameterTypes();
                }
            };

    final static ExecutableWrapper<Method> METHOD_WRAPPER =
            new ExecutableWrapper<Method>() {
                @Override
                public String getName(Method executable) {
                    return executable.getName();
                }

                @Override
                public Class<?>[] getParameterTypes(Method executable) {
                    return executable.getParameterTypes();
                }
            };

    public static int toString(int stateIndex, Object obj) {
        Lua L = Jua.get(stateIndex);
        L.push(obj.toString());
        return 1;
    }

    public static int isInstanceOf(int stateIndex, Object obj, Class<?> clazz) {
        Lua L = Jua.get(stateIndex);
        L.push(clazz != null && clazz.isInstance(obj));
        return 1;
    }

    public static int coding(int stateIndex) {
        Lua L = Jua.get(stateIndex);
        try {
            String str = L.type(1) == LuaType.STRING ? L.toString(1) : null;
            String from = L.type(2) == LuaType.STRING ? L.toString(2) : "GBK";
            String to = L.type(3) == LuaType.STRING ? L.toString(3) : "UTF-8";
            if (str != null) {
                byte[] bytes = str.getBytes(from);
                String newStr = new String(bytes, to);
                L.push(newStr);
                return 1;
            }
        } catch (Exception e) {
            return L.error(e);
        }
        return 0;
    }

    public static int createArray(long stateIndex, Class<?> clazz, int index) {
        Lua L = Jua.get(stateIndex);
        // 判断是否为 ArrayList / List, 是的话则转换成 List
        if (clazz.isAssignableFrom(List.class)) {
            L.pushJavaObject(L.toList(index));
        } else if (clazz.isArray() && clazz.getComponentType() == Object.class) {
            // 只匹配 Object[]
            L.pushJavaObject(L.toList(index).toArray(new Object[0]));
        } else if (clazz.isAssignableFrom(Map.class)) {
            // 匹配 Map
            L.pushJavaObject(L.toMap(index));
        } else if (clazz.isInterface() && !clazz.isAnnotation()) {
            // 批量转换 View.OnClickListener 等接口
            L.pushValue(index);
            L.pushJavaObject(L.createProxy(-1, clazz, Lua.Conversion.SEMI));
        }
        return 1;
    }

    public static int asTable(int stateIndex, Object obj) {
        Lua L = Jua.get(stateIndex);
        if (obj == null) return 0;
        L.pushArray(obj);
        return 1;
    }
}
