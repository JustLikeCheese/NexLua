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
import com.luajava.value.immutable.LuaBoolean;
import com.luajava.value.immutable.LuaNil;
import com.luajava.value.immutable.LuaNumber;
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

    void push();

    void push(Lua L);

    LuaType type();

    String typeName();

    boolean containsKey(Object key);

    boolean containsKey(Object key, Lua.Conversion degree);

    // table
    LuaValue get(Object key);

    LuaValue get(Object key, Lua.Conversion degree);

    void set(Object key, Object value);

    void set(Object key, Object value, Lua.Conversion degree);

    void set(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2);

    LuaValue rawget(Object key);

    LuaValue rawget(Object key, Lua.Conversion degree);

    void rawset(Object key, Object value);

    void rawset(Object key, Object value, Lua.Conversion degree);

    void rawset(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2);

    void pairs(LuaPairsIterator iterator);

    void ipairs(LuaIpairsIterator iterator);

    LuaValue[] toArray();

    List<LuaValue> toList();

    Map<LuaValue, LuaValue> toMap();

    long length();

    // metatable
    LuaValue getMetatable();

    boolean setMetatable(String tname);

    LuaValue callMetatable(String method);

    // compare
    boolean rawEqual(Object value);

    boolean rawEqual(Object value, Lua.Conversion degree);

    boolean equal(Object value);

    boolean equal(Object value, Lua.Conversion degree);

    boolean lessThan(Object value);

    boolean lessThan(Object value, Lua.Conversion degree);

    boolean lessThanOrEqual(Object value);

    boolean lessThanOrEqual(Object value, Lua.Conversion degree);

    boolean greaterThan(Object value);

    boolean greaterThan(Object value, Lua.Conversion degree);

    boolean greaterThanOrEqual(Object value);

    boolean greaterThanOrEqual(Object value, Lua.Conversion degree);

    // call
    LuaValue[] call();

    LuaValue[] call(Object... args);

    LuaValue[] call(Lua.Conversion degree, Object... args);

    LuaValue[] pCall();

    LuaValue[] pCall(Object... args);

    LuaValue[] pCall(Lua.Conversion degree, Object... args);

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
    boolean toBoolean();

    double toNumber();

    String toString();

    String ltoString();

    long toInteger();

    boolean isJavaObject();

    Object toJavaObject();

    Buffer toBuffer();

    Buffer dump();

    long getPointer();

    boolean isRef();

    int getRef();

    void unRef();

    LuaNil checkNil();

    LuaBoolean checkBoolean();

    LuaNumber checkNumber();

    LuaString checkString();

    LuaTable checkTable();

    LuaFunction checkFunction();

    LuaFunction checkCFunction();

    LuaLightUserdata checkLightUserdata();

    LuaUserdata checkUserdata();

    LuaThread checkThread();

    LuaUnknown checkUnknown();

    Object checkJavaObject();

    boolean isJavaObject(Class<?> clazz);

    Object toJavaObject(Class<?> clazz) throws IllegalArgumentException;

    Object[] toJavaArray();

    List<Object> toJavaList();

    Map<Object, Object> toJavaMap();
}