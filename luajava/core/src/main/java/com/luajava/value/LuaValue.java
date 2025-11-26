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

import com.luajava.CFunction;
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
    public static final Object NONE = Lua.NONE;

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

    // Keep Object[]
    int call(Object[] args) throws LuaException;

    int call(Object[] args, int nResults) throws LuaException;

    // Keep Lua.Conversion
    int call(Object[] args, Lua.Conversion degree) throws LuaException;

    int call(Object[] args, Lua.Conversion degree, int nResults) throws LuaException;

    // Keep Class<?>
    int call(Object[] args, Class<?> clazz) throws LuaException;

    int call(Object[] args, Class<?> clazz, int nResults) throws LuaException;

    // Keep Class<?> & Lua.Conversion
    int call(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    int call(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException;

    int call() throws LuaException;

    int call(int nArgs) throws LuaException;

    int call(int nArgs, int nResults) throws LuaException;

    // Keep Object[]
    int pCall(Object[] args) throws LuaException;

    int pCall(Object[] args, int nResults) throws LuaException;

    int pCall(Object[] args, int nResults, int errfunc) throws LuaException;

    // Keep Lua.Conversion
    int pCall(Object[] args, Lua.Conversion degree) throws LuaException;

    int pCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException;

    int pCall(Object[] args, Lua.Conversion degree, int nResults, int errfunc) throws LuaException;

    // Keep Class<?>
    int pCall(Object[] args, Class<?> clazz) throws LuaException;

    int pCall(Object[] args, Class<?> clazz, int nResults) throws LuaException;

    int pCall(Object[] args, Class<?> clazz, int nResults, int errfunc) throws LuaException;

    // Keep Class<?> & Lua.Conversion
    int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException;

    int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, int errfunc) throws LuaException;

    int pCall() throws LuaException;

    int pCall(int nArgs) throws LuaException;

    int pCall(int nArgs, int nResults) throws LuaException;

    int pCall(int nArgs, int nResults, int errfunc) throws LuaException;

    // Keep Object[]
    int xpCall(Object[] args, CFunction handler) throws LuaException;

    int xpCall(Object[] args, int nResults, CFunction handler) throws LuaException;

    // Keep Lua.Conversion
    int xpCall(Object[] args, Lua.Conversion degree, CFunction handler) throws LuaException;

    int xpCall(Object[] args, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException;

    // Keep Class<?>
    int xpCall(Object[] args, Class<?> clazz, CFunction handler) throws LuaException;

    int xpCall(Object[] args, Class<?> clazz, int nResults, CFunction handler) throws LuaException;

    // Keep Class<?> & Lua.Conversion
    int xpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, CFunction handler) throws LuaException;

    int xpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException;

    int xpCall(CFunction handler) throws LuaException;

    int xpCall(int nArgs, CFunction handler) throws LuaException;

    int xpCall(int nArgs, int nResults, CFunction handler) throws LuaException;

    // Value Function Call
    // Keep Object[]
    LuaValue[] vCall(Object[] args) throws LuaException;

    LuaValue[] vCall(Object[] args, int nResults) throws LuaException;

    // Keep Lua.Conversion
    LuaValue[] vCall(Object[] args, Lua.Conversion degree) throws LuaException;

    LuaValue[] vCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException;

    // Keep Class<?>
    LuaValue[] vCall(Object[] args, Class<?> clazz) throws LuaException;

    LuaValue[] vCall(Object[] args, Class<?> clazz, int nResults) throws LuaException;

    // Keep Class<?> & Lua.Conversion
    LuaValue[] vCall(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    LuaValue[] vCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException;

    LuaValue[] vCall() throws LuaException;

    LuaValue[] vCall(int nArgs) throws LuaException;

    LuaValue[] vCall(int nArgs, int nResults) throws LuaException;

    // Keep Object[]
    LuaValue[] vpCall(Object[] args) throws LuaException;

    LuaValue[] vpCall(Object[] args, int nResults) throws LuaException;

    LuaValue[] vpCall(Object[] args, int nResults, int errfunc) throws LuaException;

    // Keep Lua.Conversion
    LuaValue[] vpCall(Object[] args, Lua.Conversion degree) throws LuaException;

    LuaValue[] vpCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException;

    LuaValue[] vpCall(Object[] args, Lua.Conversion degree, int nResults, int errfunc) throws LuaException;

    // Keep Class<?>
    LuaValue[] vpCall(Object[] args, Class<?> clazz) throws LuaException;

    LuaValue[] vpCall(Object[] args, Class<?> clazz, int nResults) throws LuaException;

    LuaValue[] vpCall(Object[] args, Class<?> clazz, int nResults, int errfunc) throws LuaException;

    // Keep Class<?> & Lua.Conversion
    LuaValue[] vpCall(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException;

    LuaValue[] vpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException;

    LuaValue[] vpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, int errfunc) throws LuaException;

    LuaValue[] vpCall() throws LuaException;

    LuaValue[] vpCall(int nArgs) throws LuaException;

    LuaValue[] vpCall(int nArgs, int nResults) throws LuaException;

    LuaValue[] vpCall(int nArgs, int nResults, int errfunc) throws LuaException;

    // Keep Object[]
    LuaValue[] vxpCall(Object[] args, CFunction handler) throws LuaException;

    LuaValue[] vxpCall(Object[] args, int nResults, CFunction handler) throws LuaException;

    // Keep Lua.Conversion
    LuaValue[] vxpCall(Object[] args, Lua.Conversion degree, CFunction handler) throws LuaException;

    LuaValue[] vxpCall(Object[] args, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException;

    // Keep Class<?>
    LuaValue[] vxpCall(Object[] args, Class<?> clazz, CFunction handler) throws LuaException;

    LuaValue[] vxpCall(Object[] args, Class<?> clazz, int nResults, CFunction handler) throws LuaException;

    // Keep Class<?> & Lua.Conversion
    LuaValue[] vxpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, CFunction handler) throws LuaException;

    LuaValue[] vxpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException;

    LuaValue[] vxpCall(CFunction handler) throws LuaException;

    LuaValue[] vxpCall(int nArgs, CFunction handler) throws LuaException;

    LuaValue[] vxpCall(int nArgs, int nResults, CFunction handler) throws LuaException;

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

    long toInteger() throws LuaException;

    double toNumber() throws LuaException;

    String toString();

    boolean LtoBoolean() throws LuaException;

    long LtoInteger() throws LuaException;

    double LtoNumber() throws LuaException;

    String LtoString() throws LuaException;

    boolean isJavaObject() throws LuaException;

    Object toJavaObject() throws LuaException;

    Buffer toBuffer() throws LuaException;

    Buffer dump() throws LuaException;

    long getPointer() throws LuaException;

    boolean isRef();

    int getRef() throws LuaException;

    void unRef() throws LuaException;

    LuaNil checkNil() throws LuaException;

    LuaBoolean checkBoolean() throws LuaException;

    LuaNumber checkNumber() throws LuaException;

    LuaString checkString() throws LuaException;

    LuaTable checkTable() throws LuaException;

    LuaFunction checkFunction() throws LuaException;

    LuaCFunction checkCFunction() throws LuaException;

    LuaLightUserdata checkLightUserdata() throws LuaException;

    LuaUserdata checkUserdata() throws LuaException;

    LuaThread checkThread() throws LuaException;

    LuaUnknown checkUnknown() throws LuaException;

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