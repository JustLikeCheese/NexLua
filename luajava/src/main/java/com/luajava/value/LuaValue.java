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

import com.luajava.Lua;
import com.luajava.LuaException;
import com.luajava.value.immutable.LuaBoolean;
import com.luajava.value.immutable.LuaNil;
import com.luajava.value.immutable.LuaNumber;
import com.luajava.value.referable.LuaCFunction;
import com.luajava.value.referable.LuaFunction;
import com.luajava.value.referable.LuaLightUserdata;
import com.luajava.value.referable.LuaString;
import com.luajava.value.referable.LuaTable;
import com.luajava.value.referable.LuaThread;
import com.luajava.value.referable.LuaUnknown;
import com.luajava.value.referable.LuaUserdata;

import java.nio.Buffer;
import java.util.List;
import java.util.Map;

public interface LuaValue {
    // stack
    Lua state();

    void push() throws LuaException;

    int push(Lua L) throws LuaException;

    LuaType type();

    String typeName();

    boolean containsKey(Object key) throws LuaException;

    boolean containsKey(Object key, Lua.Conversion degree) throws LuaException;

    boolean containsKey(Object key, Class<?> clazz) throws LuaException;

    boolean containsKey(Object key, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    // table
    LuaValue get(Object key) throws LuaException;

    LuaValue get(Object key, Lua.Conversion degree) throws LuaException;

    LuaValue get(Object key, Class<?> clazz) throws LuaException;

    LuaValue get(Object key, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    void set(Object key, Object value) throws LuaException;

    // Keep Lua.Conversion
    void set(Object key, Object value, Lua.Conversion degree) throws LuaException;

    void set(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2) throws LuaException;

    // Keep Class<?>
    void set(Object key, Object value, Class<?> clazz) throws LuaException;

    void set(Object key, Object value, Class<?> clazz1, Class<?> clazz2) throws LuaException;

    // Keep Class<?> & Lua.Conversion
    void set(Object key, Object value, Class<?> clazz1, Class<?> clazz2, Lua.Conversion degree) throws LuaException;

    void set(Object key, Object value, Class<?> clazz1, Class<?> clazz2, Lua.Conversion degree1, Lua.Conversion degree2) throws LuaException;

    LuaValue rawget(Object key) throws LuaException;

    LuaValue rawget(Object key, Lua.Conversion degree) throws LuaException;

    LuaValue rawget(Object key, Class<?> clazz) throws LuaException;

    LuaValue rawget(Object key, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    void rawset(Object key, Object value) throws LuaException;

    // Keep Lua.Conversion
    void rawset(Object key, Object value, Lua.Conversion degree) throws LuaException;

    void rawset(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2) throws LuaException;

    // Keep Class<?>
    void rawset(Object key, Object value, Class<?> clazz) throws LuaException;

    void rawset(Object key, Object value, Class<?> clazz1, Class<?> clazz2) throws LuaException;

    // Keep Class<?> & Lua.Conversion
    void rawset(Object key, Object value, Class<?> clazz1, Class<?> clazz2, Lua.Conversion degree) throws LuaException;

    void rawset(Object key, Object value, Class<?> clazz1, Class<?> clazz2, Lua.Conversion degree1, Lua.Conversion degree2) throws LuaException;

    void pairs(LuaPairsIterator iterator) throws LuaException;

    void ipairs(LuaIpairsIterator iterator) throws LuaException;

    LuaValue[] toArray() throws LuaException;

    List<LuaValue> toList() throws LuaException;

    Map<LuaValue, LuaValue> toMap() throws LuaException;

    long length() throws LuaException;

    // metatable
    LuaValue getMetatable() throws LuaException;

    boolean setMetatable(String tname) throws LuaException;

    LuaValue callMetatable(String method) throws LuaException;

    // compare
    boolean rawEqual(Object value) throws LuaException;

    boolean rawEqual(Object value, Lua.Conversion degree) throws LuaException;

    boolean rawEqual(Object value, Class<?> clazz) throws LuaException;

    boolean rawEqual(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    boolean equal(Object value) throws LuaException;

    boolean equal(Object value, Lua.Conversion degree) throws LuaException;

    boolean equal(Object value, Class<?> clazz) throws LuaException;

    boolean equal(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    boolean lessThan(Object value) throws LuaException;

    boolean lessThan(Object value, Lua.Conversion degree) throws LuaException;

    boolean lessThan(Object value, Class<?> clazz) throws LuaException;

    boolean lessThan(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    boolean lessThanOrEqual(Object value) throws LuaException;

    boolean lessThanOrEqual(Object value, Lua.Conversion degree) throws LuaException;

    boolean lessThanOrEqual(Object value, Class<?> clazz) throws LuaException;

    boolean lessThanOrEqual(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    boolean greaterThan(Object value) throws LuaException;

    boolean greaterThan(Object value, Lua.Conversion degree) throws LuaException;

    boolean greaterThan(Object value, Class<?> clazz) throws LuaException;

    boolean greaterThan(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    boolean greaterThanOrEqual(Object value) throws LuaException;

    boolean greaterThanOrEqual(Object value, Lua.Conversion degree) throws LuaException;

    boolean greaterThanOrEqual(Object value, Class<?> clazz) throws LuaException;

    boolean greaterThanOrEqual(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    // call
    LuaValue[] call() throws LuaException;

    LuaValue[] call(Object... args) throws LuaException;

    LuaValue[] call(Lua.Conversion degree, Object... args) throws LuaException;

    LuaValue[] call(Class<?> clazz, Object... args) throws LuaException;

    LuaValue[] call(Class<?> clazz, Lua.Conversion degree, Object... args) throws LuaException;

    LuaValue[] pCall() throws LuaException;

    LuaValue[] pCall(Object... args) throws LuaException;

    LuaValue[] pCall(Lua.Conversion degree, Object... args) throws LuaException;

    LuaValue[] pCall(Class<?> clazz, Object... args) throws LuaException;

    LuaValue[] pCall(Class<?> clazz, Lua.Conversion degree, Object... args) throws LuaException;

    // sugar
    boolean isNone();

    boolean isNil();

    boolean isBoolean();

    boolean isLightUserdata();

    boolean isNumber();

    boolean isString();

    boolean isTable();

    boolean isCFunction();

    boolean isFunction();

    boolean isUserdata();

    boolean isThread();

    // to object
    boolean toBoolean() throws LuaException;

    double toNumber() throws LuaException;

    String toString();

    String ltoString() throws LuaException;

    long toInteger() throws LuaException;

    boolean isJavaObject() throws LuaException;

    Object toJavaObject() throws LuaException;

    Buffer toBuffer() throws LuaException;

    Buffer dump() throws LuaException;

    long getPointer() throws LuaException;

    boolean isRef();

    int getRef();

    void unRef();

    LuaNil checkNil();

    LuaBoolean checkBoolean();

    LuaNumber checkNumber();

    LuaString checkString();

    LuaTable checkTable();

    LuaFunction checkFunction();

    LuaCFunction checkCFunction();

    LuaLightUserdata checkLightUserdata();

    LuaUserdata checkUserdata();

    LuaThread checkThread();

    LuaUnknown checkUnknown();

    Object checkJavaObject() throws LuaException;

    boolean isJavaObject(Class<?> clazz) throws LuaException;

    Object toJavaObject(Class<?> clazz) throws IllegalArgumentException, LuaException;

    Object[] toJavaArray() throws LuaException;

    List<Object> toJavaList() throws LuaException;

    Map<Object, Object> toJavaMap() throws LuaException;

    Object toJavaArray(Class<?> clazz) throws LuaException;

    <T> List<T> toJavaList(Class<T> clazz) throws LuaException;

    <K, V> Map<K, V> toJavaMap(Class<K> keyClazz, Class<V> valueClazz) throws LuaException;

    <T> Map<T, T> toJavaMap(Class<T> clazz) throws LuaException;
}