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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.luajava.CFunction;
import com.luajava.Lua;
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
    public int push() throws LuaException {
        return push(L);
    }

    @Override
    public int copyTo(Lua L) throws LuaException {
        return push(L);
    }

    @Override
    public abstract int push(Lua L) throws LuaException;

    public LuaType type() {
        return TYPE;
    }

    public String typeName() {
        return TYPE.toString();
    }

    @Override
    public boolean containsKey(Object key) throws LuaException {
        return containsKey(key, Lua.Conversion.SEMI);
    }

    @Override
    public boolean containsKey(Object key, Lua.Conversion degree) throws LuaException {
        push();
        L.push(key, degree);
        L.getTable(-2);
        boolean result = !L.isNil(-1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean containsKey(Object key, Class<?> clazz) throws LuaException {
        return containsKey(key, clazz, Lua.Conversion.NONE);
    }

    @Override
    public boolean containsKey(Object key, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(key, clazz, degree);
        L.getTable(-2);
        boolean result = !L.isNil(-1);
        L.pop(2);
        return result;
    }

    @Override
    public LuaValue get(Object key) throws LuaException {
        return get(key, Lua.Conversion.NONE);
    }

    @Override
    public LuaValue get(Object key, Lua.Conversion degree) throws LuaException {
        push();
        L.push(key, degree);
        L.getTable(-2);
        LuaValue result = L.get();
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue get(Object key, Class<?> clazz) throws LuaException {
        return get(key, clazz, Lua.Conversion.NONE);
    }

    @Override
    public LuaValue get(Object key, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(key, clazz, degree);
        L.getTable(-2);
        LuaValue result = L.get();
        L.pop(1);
        return result;
    }

    @Override
    public void set(Object key, Object value) throws LuaException {
        set(key, value, Lua.Conversion.NONE);
    }

    @Override
    public void set(Object key, Object value, Lua.Conversion degree) throws LuaException {
        set(key, value, degree, degree);
    }

    @Override
    public void set(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2) throws LuaException {
        push();
        L.push(key, degree1);
        L.push(value, degree2);
        L.setTable(-3);
        L.pop(1);
    }

    // Keep Class<?>
    @Override
    public void set(Object key, Object value, Class<?> clazz) throws LuaException {
        set(key, value, clazz, clazz, Lua.Conversion.NONE);
    }

    @Override
    public void set(Object key, Object value, Class<?> clazz1, Class<?> clazz2) throws LuaException {
        set(key, value, clazz1, clazz2, Lua.Conversion.NONE);
    }

    @Override
    public void set(Object key, Object value, Class<?> clazz1, Class<?> clazz2, Lua.Conversion degree) throws LuaException {
        set(key, value, clazz1, clazz2, degree, degree);
    }

    @Override
    public void set(Object key, Object value, Class<?> clazz1, Class<?> clazz2, Lua.Conversion degree1, Lua.Conversion degree2) throws LuaException {
        push();
        L.push(key, clazz1, degree1);
        L.push(value, clazz2, degree2);
        L.setTable(-3);
        L.pop(1);
    }

    @Override
    public LuaValue rawget(Object key) throws LuaException {
        return rawget(key, Lua.Conversion.NONE);
    }

    @Override
    public LuaValue rawget(Object key, Lua.Conversion degree) throws LuaException {
        push();
        L.push(key, degree);
        L.rawGet(-2);
        LuaValue result = L.get();
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue rawget(Object key, Class<?> clazz) throws LuaException {
        return rawget(key, clazz, Lua.Conversion.NONE);
    }

    @Override
    public LuaValue rawget(Object key, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(key, clazz, degree);
        L.rawGet(-2);
        LuaValue result = L.get();
        L.pop(1);
        return result;
    }


    @Override
    public void rawset(Object key, Object value) throws LuaException {
        rawset(key, value, Lua.Conversion.NONE);
    }

    @Override
    public void rawset(Object key, Object value, Lua.Conversion degree) throws LuaException {
        rawset(key, value, degree, degree);
    }

    @Override
    public void rawset(Object key, Object value, Lua.Conversion degree1, Lua.Conversion degree2) throws LuaException {
        push();
        L.push(key, degree1);
        L.push(value, degree2);
        L.rawSet(-3);
        L.pop(1);
    }

    @Override
    public void rawset(Object key, Object value, Class<?> clazz) throws LuaException {
        rawset(key, value, clazz, clazz);
    }

    @Override
    public void rawset(Object key, Object value, Class<?> clazz1, Class<?> clazz2) throws LuaException {
        rawset(key, value, clazz1, clazz2, Lua.Conversion.NONE);
    }

    @Override
    public void rawset(Object key, Object value, Class<?> clazz1, Class<?> clazz2, Lua.Conversion degree) throws LuaException {
        rawset(key, value, clazz1, clazz2, degree, degree);
    }

    @Override
    public void rawset(Object key, Object value, Class<?> clazz1, Class<?> clazz2, Lua.Conversion degree1, Lua.Conversion degree2) throws LuaException {
        push();
        L.push(key, clazz1, degree1);
        L.push(value, clazz2, degree2);
        L.rawSet(-3);
        L.pop(1);
    }

    /**
     * Stack: -2 is key, -1 is value
     * if iterator returns true then pop and break
     */
    @Override
    public void pairs(LuaIterator.Pairs iterator) throws LuaException {
        push();
        L.pairs(-1, iterator);
        L.pop(1);
    }

    /**
     * Stack: -1 is value
     * if iterator returns true then pop and break
     */
    @Override
    public void ipairs(LuaIterator.Ipairs iterator) throws LuaException {
        push();
        L.ipairs(-1, iterator);
        L.pop(1);
    }

    @Override
    public int length() throws LuaException {
        push();
        int result = L.rawLength(-1);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue getMetatable() throws LuaException {
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
    public void setMetatable(String name) throws LuaException {
        push();
        L.setMetatable(name);
        L.pop(2);
    }

    @Override
    public void setMetatable(LuaTable metatable) throws LuaException {
        push(L);
        metatable.push(L);
        L.setMetatable(-2);
    }

    @Override
    public LuaValue callMetatable(String method) throws LuaException {
        push();
        if (L.callMetatable(-1, method)) {
            LuaValue result = L.get();
            L.pop(2);
            return result;
        }
        L.pop(1);
        return null;
    }

    @Override
    public boolean rawEqual(Object value) throws LuaException {
        return rawEqual(value, Lua.Conversion.NONE);
    }

    @Override
    public boolean rawEqual(Object value, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, degree);
        boolean result = L.rawEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean rawEqual(Object value, Class<?> clazz) throws LuaException {
        return rawEqual(value, clazz, Lua.Conversion.NONE);
    }

    @Override
    public boolean rawEqual(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, clazz, degree);
        boolean result = L.rawEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean equal(Object value) throws LuaException {
        return equal(value, Lua.Conversion.NONE);
    }

    @Override
    public boolean equal(Object value, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, degree);
        boolean result = L.equal(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean equal(Object value, Class<?> clazz) throws LuaException {
        return equal(value, clazz, Lua.Conversion.NONE);
    }

    @Override
    public boolean equal(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, clazz, degree);
        boolean result = L.equal(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean lessThan(Object value) throws LuaException {
        return lessThan(value, Lua.Conversion.NONE);
    }

    @Override
    public boolean lessThan(Object value, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, degree);
        boolean result = L.lessThan(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean lessThan(Object value, Class<?> clazz) throws LuaException {
        return lessThan(value, clazz, Lua.Conversion.NONE);
    }

    @Override
    public boolean lessThan(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, clazz, degree);
        boolean result = L.lessThan(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean lessThanOrEqual(Object value) throws LuaException {
        return lessThanOrEqual(value, Lua.Conversion.NONE);
    }

    @Override
    public boolean lessThanOrEqual(Object value, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, degree);
        boolean result = L.lessThanOrEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean lessThanOrEqual(Object value, Class<?> clazz) throws LuaException {
        return lessThanOrEqual(value, clazz, Lua.Conversion.NONE);
    }

    @Override
    public boolean lessThanOrEqual(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, clazz, degree);
        boolean result = L.lessThanOrEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean greaterThan(Object value) throws LuaException {
        return greaterThan(value, Lua.Conversion.NONE);
    }

    @Override
    public boolean greaterThan(Object value, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, degree);
        boolean result = L.greaterThan(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean greaterThan(Object value, Class<?> clazz) throws LuaException {
        return greaterThan(value, clazz, Lua.Conversion.NONE);
    }

    @Override
    public boolean greaterThan(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, clazz, degree);
        boolean result = L.greaterThan(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean greaterThanOrEqual(Object value) throws LuaException {
        return greaterThanOrEqual(value, Lua.Conversion.NONE);
    }

    @Override
    public boolean greaterThanOrEqual(Object value, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, degree);
        boolean result = L.greaterThanOrEqual(-2, -1);
        L.pop(2);
        return result;
    }

    @Override
    public boolean greaterThanOrEqual(Object value, Class<?> clazz) throws LuaException {
        return greaterThanOrEqual(value, clazz, Lua.Conversion.NONE);
    }

    @Override
    public boolean greaterThanOrEqual(Object value, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        L.push(value, clazz, degree);
        boolean result = L.greaterThanOrEqual(-2, -1);
        L.pop(2);
        return result;
    }

    // Keep Object[]
    @Override
    public int call(Object[] args) throws LuaException {
        push();
        return L.call(args);
    }

    @Override
    public int call(Object[] args, int nResults) throws LuaException {
        push();
        return L.call(args, nResults);
    }

    // Keep Lua.Conversion
    @Override
    public int call(Object[] args, Lua.Conversion degree) throws LuaException {
        push();
        return L.call(args, degree);
    }

    @Override
    public int call(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        return L.call(args, degree, nResults);
    }

    // Keep Class<?>
    @Override
    public int call(Object[] args, Class<?> clazz) throws LuaException {
        push();
        return L.call(args, clazz);
    }

    @Override
    public int call(Object[] args, Class<?> clazz, int nResults) throws LuaException {
        push();
        return L.call(args, clazz, nResults);
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public int call(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        return L.call(args, clazz, degree);
    }

    @Override
    public int call(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        return L.call(args, clazz, degree, nResults);
    }

    @Override
    public int call() throws LuaException {
        push();
        return L.call();
    }

    @Override
    public int call(int nArgs) throws LuaException {
        push();
        return L.call(nArgs);
    }

    @Override
    public int call(int nArgs, int nResults) throws LuaException {
        push();
        return L.call(nArgs, nResults);
    }

    // Keep Object[]
    @Override
    public int pCall(Object[] args) throws LuaException {
        push();
        return L.pCall(args);
    }

    @Override
    public int pCall(Object[] args, int nResults) throws LuaException {
        push();
        return L.pCall(args, nResults);
    }

    @Override
    public int pCall(Object[] args, int nResults, int errfunc) throws LuaException {
        push();
        return L.pCall(args, nResults, errfunc);
    }

    // Keep Lua.Conversion
    @Override
    public int pCall(Object[] args, Lua.Conversion degree) throws LuaException {
        push();
        return L.pCall(args, degree);
    }

    @Override
    public int pCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        return L.pCall(args, degree, nResults);
    }

    @Override
    public int pCall(Object[] args, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        push();
        return L.pCall(args, degree, nResults, errfunc);
    }

    // Keep Class<?>
    @Override
    public int pCall(Object[] args, Class<?> clazz) throws LuaException {
        push();
        return L.pCall(args, clazz);
    }

    @Override
    public int pCall(Object[] args, Class<?> clazz, int nResults) throws LuaException {
        push();
        return L.pCall(args, clazz, nResults);
    }

    @Override
    public int pCall(Object[] args, Class<?> clazz, int nResults, int errfunc) throws LuaException {
        push();
        return L.pCall(args, clazz, nResults, errfunc);
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        return L.pCall(args, clazz, degree);
    }

    @Override
    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        return L.pCall(args, clazz, degree, nResults);
    }

    @Override
    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        push();
        return L.pCall(args, clazz, degree, nResults, errfunc);
    }

    @Override
    public int pCall() throws LuaException {
        push();
        return L.pCall();
    }

    @Override
    public int pCall(int nArgs) throws LuaException {
        push();
        return L.pCall(nArgs);
    }

    @Override
    public int pCall(int nArgs, int nResults) throws LuaException {
        push();
        return L.pCall(nArgs, nResults);
    }

    @Override
    public int pCall(int nArgs, int nResults, int errfunc) throws LuaException {
        push();
        return L.pCall(nArgs, nResults, errfunc);
    }

    // Keep Object[]
    @Override
    public int xpCall(Object[] args, CFunction handler) throws LuaException {
        push();
        return L.xpCall(args, handler);
    }

    @Override
    public int xpCall(Object[] args, int nResults, CFunction handler) throws LuaException {
        push();
        return L.xpCall(args, nResults, handler);
    }

    // Keep Lua.Conversion
    @Override
    public int xpCall(Object[] args, Lua.Conversion degree, CFunction handler) throws LuaException {
        push();
        return L.xpCall(args, degree, handler);
    }

    @Override
    public int xpCall(Object[] args, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException {
        push();
        return L.xpCall(args, degree, nResults, handler);
    }

    // Keep Class<?>
    @Override
    public int xpCall(Object[] args, Class<?> clazz, CFunction handler) throws LuaException {
        push();
        return L.xpCall(args, clazz, handler);
    }

    @Override
    public int xpCall(Object[] args, Class<?> clazz, int nResults, CFunction handler) throws LuaException {
        push();
        return L.xpCall(args, clazz, nResults, handler);
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public int xpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, CFunction handler) throws LuaException {
        push();
        return L.xpCall(args, clazz, degree, handler);
    }

    @Override
    public int xpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException {
        push();
        return L.xpCall(args, clazz, degree, nResults, handler);
    }

    @Override
    public int xpCall(CFunction handler) throws LuaException {
        push();
        return L.xpCall(handler);
    }

    @Override
    public int xpCall(int nArgs, CFunction handler) throws LuaException {
        push();
        return L.xpCall(nArgs, handler);
    }

    @Override
    public int xpCall(int nArgs, int nResults, CFunction handler) throws LuaException {
        push();
        return L.xpCall(nArgs, nResults, handler);
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
    public boolean isCFunction() throws LuaException {
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
    public boolean toBoolean() throws LuaException {
        return true;
    }

    @Override
    public long toInteger() throws LuaException {
        return 0;
    }

    @Override
    public double toNumber() throws LuaException {
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        try {
            push();
            String result = L.toString(-1);
            L.pop(1);
            if (result != null) {
                return result;
            }
        } catch (LuaException ignored) {
        }
        return "";
    }

    @Override
    public String LtoString() throws LuaException {
        push();
        String result = L.LtoString(-1);
        L.pop(1);
        return result;
    }

    @Override
    public boolean isJavaObject() throws LuaException {
        push();
        boolean result = L.isJavaObject(-1);
        L.pop(1);
        return result;
    }

    @Override
    public Object toJavaObject() throws LuaException {
        push();
        Object result = L.toJavaObject(-1);
        L.pop(1);
        return result;
    }

    @Override
    public boolean isJavaFunction() throws LuaException {
        push();
        boolean result = L.isJavaFunction(-1);
        L.pop(1);
        return result;
    }

    @Override
    public CFunction toJavaFunction() throws LuaException {
        push();
        CFunction result = L.toJavaFunction(-1);
        L.pop(1);
        return result;
    }

    @Override
    public Buffer toBuffer() throws LuaException {
        push();
        Buffer result = L.toBuffer(-1);
        L.pop(1);
        return result;
    }

    @Override
    public Buffer dump() throws LuaException {
        push();
        Buffer result = L.dump();
        L.pop(1);
        return result;
    }

    @Override
    public long getPointer() throws LuaException {
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
    public int getRef() throws LuaException {
        throw new LuaException("Not a reference");
    }

    @Override
    public void unRef() throws LuaException {
        throw new LuaException("Not a reference");
    }

    @Override
    public LuaNil checkNil() throws LuaException {
        throw new LuaException("Not a nil");
    }

    @Override
    public LuaBoolean checkBoolean() throws LuaException {
        throw new LuaException("Not a boolean");
    }

    @Override
    public LuaNumber checkNumber() throws LuaException {
        throw new LuaException("Not a number");
    }

    @Override
    public LuaString checkString() throws LuaException {
        throw new LuaException("Not a string");
    }

    @Override
    public LuaTable checkTable() throws LuaException {
        throw new LuaException("Not a table");
    }

    @Override
    public LuaFunction checkFunction() throws LuaException {
        throw new LuaException("Not a function");
    }

    @Override
    public LuaFunction checkCFunction() throws LuaException {
        throw new LuaException("Not a C function");
    }

    @Override
    public LuaLightUserdata checkLightUserdata() throws LuaException {
        throw new LuaException("Not a light userdata");
    }

    @Override
    public LuaUserdata checkUserdata() throws LuaException {
        throw new LuaException("Not a userdata");
    }

    @Override
    public LuaThread checkThread() throws LuaException {
        throw new LuaException("Not a thread");
    }

    @Override
    public LuaUnknown checkUnknown() throws LuaException {
        throw new LuaException("Not a unknown");
    }

    @Override
    public Object checkJavaObject() throws LuaException {
        throw new LuaException("Not a java object");
    }

    @Override
    public CFunction checkJavaFunction() throws LuaException {
        throw new LuaException("Not a java function");
    }

    @Override
    public @Nullable Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) {
                return toBoolean();
            } else if (clazz == char.class) {
                return (char) toInteger();
            } else if (clazz == byte.class) {
                return (byte) toInteger();
            } else if (clazz == short.class) {
                return (short) toInteger();
            } else if (clazz == int.class) {
                return (int) toInteger();
            } else if (clazz == long.class) {
                return toInteger();
            } else if (clazz == float.class) {
                return (float) toNumber();
            } else if (clazz == double.class) {
                return toNumber();
            }
        }
        return null;
    }

    @Override
    public Object[] toJavaArray() throws LuaException {
        return (Object[]) toJavaArray(Object.class);
    }

    @Override
    public List<Object> toJavaList() throws LuaException {
        return toJavaList(Object.class);
    }

    @Override
    public Map<Object, Object> toJavaMap() throws LuaException {
        return toJavaMap(Object.class);
    }

    @Override
    public Object toJavaArray(Class<?> clazz) throws LuaException {
        push();
        int length = L.rawLength(-1);
        Object array = Array.newInstance(clazz, length);
        L.ipairs(-1, (L, index) -> {
            int arrayIndex = index - 1;
            Object element = L.toJavaObject(-1, clazz);
            Array.set(array, arrayIndex, element);
            return false;
        });
        L.pop(1);
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> toJavaList(Class<T> clazz) throws LuaException {
        List<T> list = new ArrayList<>();
        ipairs((L, index) -> {
            list.add((T) L.toJavaObject(-1, clazz));
            return false;
        });
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> toJavaMap(Class<K> keyClazz, Class<V> valueClazz) throws LuaException {
        Map<K, V> map = new LinkedHashMap<>();
        pairs((L) -> {
            map.put(
                    (K) L.toJavaObject(-2, keyClazz),
                    (V) L.toJavaObject(-1, valueClazz)
            );
            return true;
        });
        return map;
    }

    @Override
    public <T> Map<T, T> toJavaMap(Class<T> clazz) throws LuaException {
        return toJavaMap(clazz, clazz);
    }
}