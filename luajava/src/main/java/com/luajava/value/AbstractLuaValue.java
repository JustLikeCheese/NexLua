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
import com.luajava.LuaConsts;
import com.luajava.LuaException;
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

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractLuaValue implements LuaValue {

    protected final Lua L;
    protected final LuaType TYPE;

    public AbstractLuaValue(Lua L, LuaType type) {
        this.L = L;
        this.TYPE = type;
    }

    @Override
    public Lua state() {
        return L;
    }

    @Override
    public void push() {
        push(L);
    }

    @Override
    public abstract void push(Lua L);

    public LuaType type() {
        return TYPE;
    }

    public String typeName() {
        return TYPE.toString();
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(key, Lua.Conversion.SEMI);
    }

    @Override
    public boolean containsKey(Object key, Lua.Conversion degree) {
        push();
        L.push(key, degree);
        L.getTable(-2);
        boolean result = !L.isNil(-1);
        L.pop(2);
        return result;
    }

    @Override
    public LuaValue get(Object key) {
        return get(key, Lua.Conversion.SEMI);
    }

    @Override
    public LuaValue get(Object key, Lua.Conversion degree) {
        push();
        L.push(key, degree);
        L.getTable(-2);
        LuaValue result = L.get();
        L.pop(1);
        return result;
    }

    @Override
    public void set(Object key, Object value) {
        set(key, value, Lua.Conversion.SEMI);
    }

    @Override
    public void set(Object key, Object value, Lua.Conversion degree) {
        set(key, value, degree, degree);
    }

    @Override
    public void set(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2) {
        push();
        L.push(key, degree1);
        L.push(value, degree2);
        L.setTable(-3);
        L.pop(1);
    }

    @Override
    public LuaValue rawget(Object key) {
        return rawget(key, Lua.Conversion.SEMI);
    }

    @Override
    public LuaValue rawget(Object key, Lua.Conversion degree) {
        push();
        L.push(key, degree);
        L.rawGet(-2);
        LuaValue result = L.get();
        L.pop(1);
        return result;
    }


    @Override
    public void rawset(Object key, Object value) {
        rawset(key, value, Lua.Conversion.SEMI);
    }

    @Override
    public void rawset(Object key, Object value, Lua.Conversion degree) {
        rawset(key, value, degree, degree);
    }

    @Override
    public void rawset(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2) {
        push();
        L.push(key, degree1);
        L.push(value, degree2);
        L.rawSet(-3);
        L.pop(1);
    }

    @Override
    public void pairs(LuaPairsIterator iterator) {
        push();
        L.pairs(iterator);
        L.pop(1);
    }

    @Override
    public void ipairs(LuaIpairsIterator iterator) {
        push();
        L.ipairs(iterator);
        L.pop(1);
    }

    @Override
    public LuaValue[] toArray() {
        push();
        int length = (int) L.rawLength(-1);
        LuaValue[] array = new LuaValue[length];
        L.ipairs((index, value) -> {
            array[(int) index - 1] = value;
            return true;
        });
        L.pop(1);
        return array;
    }

    @Override
    public List<LuaValue> toList() {
        List<LuaValue> list = new ArrayList<>();
        ipairs((index, value) -> {
            list.add(value);
            return true;
        });
        return list;
    }

    @Override
    public Map<LuaValue, LuaValue> toMap() {
        Map<LuaValue, LuaValue> map = new LinkedHashMap<>();
        pairs((key, value) -> {
            map.put(key, value);
            return true; // continue iteration
        });
        return map;
    }

    @Override
    public long length() {
        push();
        long result = L.rawLength(-1);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue getMetatable() {
        push();
        if (L.getMetatable(-1)) {
            LuaValue result = L.get();
            L.pop(1);
            return result;
        }
        L.pop(1);
        return null;
    }

    @Override
    public boolean setMetatable(String tname) {
        push();
        L.setMetatable(tname);
        L.pop(2);
        return false;
    }

    @Override
    public LuaValue callMetatable(String method) {
        push();
        if (L.callMetatable(-1, method)) {
            LuaValue result = L.get();
            L.pop(1);
            return result;
        }
        L.pop(1);
        return null;
    }

    @Override
    public boolean rawEqual(Object value) {
        return rawEqual(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean rawEqual(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.rawEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean equal(Object value) {
        return equal(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean equal(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.equal(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean lessThan(Object value) {
        return lessThan(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean lessThan(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.lessThan(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean lessThanOrEqual(Object value) {
        return lessThanOrEqual(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean lessThanOrEqual(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.lessThanOrEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean greaterThan(Object value) {
        return greaterThan(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean greaterThan(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.greaterThan(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean greaterThanOrEqual(Object value) {
        return greaterThanOrEqual(value, Lua.Conversion.SEMI);
    }

    @Override
    public boolean greaterThanOrEqual(Object value, Lua.Conversion degree) {
        push();
        L.push(value, degree);
        boolean result = L.greaterThanOrEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public LuaValue[] call() {
        push();
        int oldTop = L.getTop();
        L.call(0, LuaConsts.LUA_MULTRET);
        int nResult = L.getTop() - oldTop;
        LuaValue[] result = L.getArgs(nResult);
        L.pop(nResult);
        return result;
    }

    @Override
    public LuaValue[] call(Object... args) {
        return call(Lua.Conversion.SEMI, args);
    }

    @Override
    public LuaValue[] call(Lua.Conversion degree, Object... args) {
        push();
        int oldTop = L.getTop();
        L.call(args, degree, LuaConsts.LUA_MULTRET);
        int nResult = L.getTop() - oldTop;
        LuaValue[] result = L.getArgs(nResult);
        L.pop(nResult);
        return result;
    }

    @Override
    public LuaValue[] pCall() {
        push();
        int oldTop = L.getTop();
        L.pCall(0, LuaConsts.LUA_MULTRET);
        int nResult = L.getTop() - oldTop;
        LuaValue[] result = L.getArgs(nResult);
        L.pop(nResult);
        return result;
    }

    @Override
    public LuaValue[] pCall(Object... args) {
        return pCall(Lua.Conversion.SEMI, args);
    }

    @Override
    public LuaValue[] pCall(Lua.Conversion degree, Object... args) {
        push();
        int oldTop = L.getTop();
        L.pCall(args, degree, LuaConsts.LUA_MULTRET);
        int nResult = L.getTop() - oldTop;
        LuaValue[] result = L.getArgs(nResult);
        L.pop(nResult);
        return result;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public boolean isNil() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isLightUserdata() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isTable() {
        return false;
    }

    @Override
    public boolean isCFunction() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return false;
    }

    @Override
    public boolean isUserdata() {
        return false;
    }

    @Override
    public boolean isThread() {
        return false;
    }

    @Override
    public boolean toBoolean() {
        push();
        boolean result = L.toBoolean(-1);
        L.pop(1);
        return result;
    }

    @Override
    public long toInteger() {
        push();
        long result = L.toInteger(-1);
        L.pop(1);
        return result;
    }

    @Override
    public double toNumber() {
        push();
        double result = L.toNumber(-1);
        L.pop(1);
        return result;
    }

    @Override
    public boolean isJavaObject() {
        /*push();
        boolean result = L.isJavaObject(-1);
        L.pop(1);
        return result;*/
        return false;
    }

    @Override
    public Object toJavaObject() {
        push();
        Object result = L.toJavaObject(-1);
        L.pop(1);
        return result;
    }

    @Override
    public String toString() {
        push();
        String result = L.toString(-1);
        L.pop(1);
        return result;
    }

    @Override
    public String ltoString() {
        push();
        String result = L.ltoString(-1);
        L.pop(1);
        return result;
    }

    @Override
    public Buffer toBuffer() {
        push();
        Buffer result = L.toBuffer(-1);
        L.pop(1);
        return result;
    }

    @Override
    public Buffer dump() {
        push();
        Buffer result = L.dump();
        L.pop(1);
        return result;
    }

    @Override
    public long getPointer() {
        push();
        long result = L.getPointer(-1);
        L.pop(1);
        return result;
    }

    @Override
    public boolean isRef() {
        return false;
    }

    @Override
    public int getRef() {
        throw new IllegalArgumentException("Not a reference");
    }

    @Override
    public void unRef() {
        throw new IllegalArgumentException("Not a reference");
    }

    @Override
    public LuaNil checkNil() {
        throw new IllegalArgumentException("Not a nil");
    }

    @Override
    public LuaBoolean checkBoolean() {
        throw new IllegalArgumentException("Not a boolean");
    }

    @Override
    public LuaNumber checkNumber() {
        throw new IllegalArgumentException("Not a number");
    }

    @Override
    public LuaString checkString() {
        throw new IllegalArgumentException("Not a string");
    }

    @Override
    public LuaTable checkTable() {
        throw new IllegalArgumentException("Not a table");
    }

    @Override
    public LuaFunction checkFunction() {
        throw new IllegalArgumentException("Not a function");
    }

    @Override
    public LuaFunction checkCFunction() {
        throw new IllegalArgumentException("Not a C function");
    }

    @Override
    public LuaLightUserdata checkLightUserdata() {
        throw new IllegalArgumentException("Not a light userdata");
    }

    @Override
    public LuaUserdata checkUserdata() {
        throw new IllegalArgumentException("Not a userdata");
    }

    @Override
    public LuaThread checkThread() {
        throw new IllegalArgumentException("Not a thread");
    }

    @Override
    public LuaUnknown checkUnknown() {
        throw new IllegalArgumentException("Not a unknown");
    }

    @Override
    public Object checkJavaObject() {
        throw new IllegalArgumentException("Not a java object");
    }

    @Override
    public Object toJavaObject(Class<?> clazz) {
        throw new LuaException(String.format("Could not convert %s to %s", typeName(), clazz.getName()));
    }

    @Override
    public Object[] toJavaArray() {
        return (Object[]) toJavaArray(Object.class);
    }

    @Override
    public List<Object> toJavaList() {
        return toJavaList(Object.class);
    }

    @Override
    public Map<Object, Object> toJavaMap() {
        return toJavaMap(Object.class);
    }

    @Override
    public Object toJavaArray(Class<?> clazz) {
        push();
        int length = (int) L.rawLength(-1);
        Object array = Array.newInstance(clazz, length);
        L.ipairs((index, value) -> {
            int arrayIndex = (int) (index - 1);
            Object element = value.toJavaObject(clazz);
            Array.set(array, arrayIndex, element);
            return true;
        });
        L.pop(1);
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> toJavaList(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        ipairs((index, value) -> {
            list.add((T) value.toJavaObject(clazz));
            return true;
        });
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> toJavaMap(Class<K> keyClazz, Class<V> valueClazz) {
        Map<K, V> map = new LinkedHashMap<>();
        pairs((key, value) -> {
            map.put(
                    (K) key.toJavaObject(keyClazz),
                    (V) value.toJavaObject(valueClazz)
            );
            return true;
        });
        return map;
    }

    @Override
    public <T> Map<T, T> toJavaMap(Class<T> clazz) {
        return toJavaMap(clazz, clazz);
    }
}