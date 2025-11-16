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
    public void push() throws LuaException {
        push(L);
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

    @Override
    public void pairs(LuaPairsIterator iterator) throws LuaException {
        push();
        L.pairs(iterator);
        L.pop(1);
    }

    @Override
    public void ipairs(LuaIpairsIterator iterator) throws LuaException {
        push();
        L.ipairs(iterator);
        L.pop(1);
    }

    @Override
    public LuaValue[] toArray() throws LuaException {
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
    public List<LuaValue> toList() throws LuaException {
        List<LuaValue> list = new ArrayList<>();
        ipairs((index, value) -> {
            list.add(value);
            return true;
        });
        return list;
    }

    @Override
    public Map<LuaValue, LuaValue> toMap() throws LuaException {
        Map<LuaValue, LuaValue> map = new LinkedHashMap<>();
        pairs((key, value) -> {
            map.put(key, value);
            return true; // continue iteration
        });
        return map;
    }

    @Override
    public long length() throws LuaException {
        push();
        long result = L.rawLength(-1);
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
    public boolean setMetatable(String tname) throws LuaException {
        push();
        L.setMetatable(tname);
        L.pop(2);
        return false;
    }

    @Override
    public LuaValue callMetatable(String method) throws LuaException {
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
        int result = L.call(args);
        L.pop(1);
        return result;
    }

    @Override
    public int call(Object[] args, int nResults) throws LuaException {
        push();
        int result = L.call(args, nResults);
        L.pop(1);
        return result;
    }

    // Keep Lua.Conversion
    @Override
    public int call(Object[] args, Lua.Conversion degree) throws LuaException {
        push();
        int result = L.call(args, degree);
        L.pop(1);
        return result;
    }

    @Override
    public int call(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        int result = L.call(args, degree, nResults);
        L.pop(1);
        return result;
    }

    // Keep Class<?>
    @Override
    public int call(Object[] args, Class<?> clazz) throws LuaException {
        push();
        int result = L.call(args, clazz);
        L.pop(1);
        return result;
    }

    @Override
    public int call(Object[] args, Class<?> clazz, int nResults) throws LuaException {
        push();
        int result = L.call(args, clazz, nResults);
        L.pop(1);
        return result;
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public int call(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        int result = L.call(args, clazz, degree);
        L.pop(1);
        return result;
    }

    @Override
    public int call(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        int result = L.call(args, clazz, degree, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public int call() {
        push();
        int result = L.call();
        L.pop(1);
        return result;
    }

    @Override
    public int call(int nArgs) {
        push();
        int result = L.call(nArgs);
        L.pop(1);
        return result;
    }

    @Override
    public int call(int nArgs, int nResults) {
        push();
        int result = L.call(nArgs, nResults);
        L.pop(1);
        return result;
    }
    // Keep Object[]
    @Override
    public int pCall(Object[] args) throws LuaException {
        push();
        int result = L.pCall(args);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(Object[] args, int nResults) throws LuaException {
        push();
        int result = L.pCall(args, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(Object[] args, int nResults, int errfunc) throws LuaException {
        push();
        int result = L.pCall(args, nResults, errfunc);
        L.pop(1);
        return result;
    }

    // Keep Lua.Conversion
    @Override
    public int pCall(Object[] args, Lua.Conversion degree) throws LuaException {
        push();
        int result = L.pCall(args, degree);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        int result = L.pCall(args, degree, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(Object[] args, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        push();
        int result = L.pCall(args, degree, nResults, errfunc);
        L.pop(1);
        return result;
    }

    // Keep Class<?>
    @Override
    public int pCall(Object[] args, Class<?> clazz) throws LuaException {
        push();
        int result = L.pCall(args, clazz);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(Object[] args, Class<?> clazz, int nResults) throws LuaException {
        push();
        int result = L.pCall(args, clazz, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(Object[] args, Class<?> clazz, int nResults, int errfunc) throws LuaException {
        push();
        int result = L.pCall(args, clazz, nResults, errfunc);
        L.pop(1);
        return result;
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        int result = L.pCall(args, clazz, degree);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        int result = L.pCall(args, clazz, degree, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        push();
        int result = L.pCall(args, clazz, degree, nResults, errfunc);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall() throws LuaException {
        push();
        int result = L.pCall();
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(int nArgs) throws LuaException {
        push();
        int result = L.pCall(nArgs);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(int nArgs, int nResults) throws LuaException {
        push();
        int result = L.pCall(nArgs, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public int pCall(int nArgs, int nResults, int errfunc) throws LuaException {
        push();
        int result = L.pCall(nArgs, nResults, errfunc);
        L.pop(1);
        return result;
    }

    // Keep Object[]
    @Override
    public int xpCall(Object[] args, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(args, handler);
        L.pop(1);
        return result;
    }

    @Override
    public int xpCall(Object[] args, int nResults, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(args, nResults, handler);
        L.pop(1);
        return result;
    }

    // Keep Lua.Conversion
    @Override
    public int xpCall(Object[] args, Lua.Conversion degree, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(args, degree, handler);
        L.pop(1);
        return result;
    }

    @Override
    public int xpCall(Object[] args, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(args, degree, nResults, handler);
        L.pop(1);
        return result;
    }

    // Keep Class<?>
    @Override
    public int xpCall(Object[] args, Class<?> clazz, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(args, clazz, handler);
        L.pop(1);
        return result;
    }

    @Override
    public int xpCall(Object[] args, Class<?> clazz, int nResults, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(args, clazz, nResults, handler);
        L.pop(1);
        return result;
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public int xpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(args, clazz, degree, handler);
        L.pop(1);
        return result;
    }

    @Override
    public int xpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(args, clazz, degree, nResults, handler);
        L.pop(1);
        return result;
    }

    @Override
    public int xpCall(CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(handler);
        L.pop(1);
        return result;
    }

    @Override
    public int xpCall(int nArgs, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(nArgs, handler);
        L.pop(1);
        return result;
    }

    @Override
    public int xpCall(int nArgs, int nResults, CFunction handler) throws LuaException {
        push();
        int result = L.xpCall(nArgs, nResults, handler);
        L.pop(1);
        return result;
    }

    // Value Function Call
    // Keep Object[]
    @Override
    public LuaValue[] vCall(Object[] args) throws LuaException {
        push();
        LuaValue[] result = L.vCall(args);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vCall(Object[] args, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vCall(args, nResults);
        L.pop(1);
        return result;
    }

    // Keep Lua.Conversion
    @Override
    public LuaValue[] vCall(Object[] args, Lua.Conversion degree) throws LuaException {
        push();
        LuaValue[] result = L.vCall(args, degree);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vCall(args, degree, nResults);
        L.pop(1);
        return result;
    }

    // Keep Class<?>
    @Override
    public LuaValue[] vCall(Object[] args, Class<?> clazz) throws LuaException {
        push();
        LuaValue[] result = L.vCall(args, clazz);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vCall(Object[] args, Class<?> clazz, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vCall(args, clazz, nResults);
        L.pop(1);
        return result;
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public LuaValue[] vCall(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        LuaValue[] result = L.vCall(args, clazz, degree);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vCall(args, clazz, degree, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vCall() {
        push();
        LuaValue[] result = L.vCall();
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vCall(int nArgs) {
        push();
        LuaValue[] result = L.vCall(nArgs);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vCall(int nArgs, int nResults) {
        push();
        LuaValue[] result = L.vCall(nArgs, nResults);
        L.pop(1);
        return result;
    }

    // Keep Object[]
    @Override
    public LuaValue[] vpCall(Object[] args) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(Object[] args, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(Object[] args, int nResults, int errfunc) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, nResults, errfunc);
        L.pop(1);
        return result;
    }

    // Keep Lua.Conversion
    @Override
    public LuaValue[] vpCall(Object[] args, Lua.Conversion degree) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, degree);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, degree, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(Object[] args, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, degree, nResults, errfunc);
        L.pop(1);
        return result;
    }

    // Keep Class<?>
    @Override
    public LuaValue[] vpCall(Object[] args, Class<?> clazz) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, clazz);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(Object[] args, Class<?> clazz, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, clazz, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(Object[] args, Class<?> clazz, int nResults, int errfunc) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, clazz, nResults, errfunc);
        L.pop(1);
        return result;
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public LuaValue[] vpCall(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, clazz, degree);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, clazz, degree, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(args, clazz, degree, nResults, errfunc);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall() throws LuaException {
        push();
        LuaValue[] result = L.vpCall();
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(int nArgs) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(nArgs);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(int nArgs, int nResults) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(nArgs, nResults);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vpCall(int nArgs, int nResults, int errfunc) throws LuaException {
        push();
        LuaValue[] result = L.vpCall(nArgs, nResults, errfunc);
        L.pop(1);
        return result;
    }

    // Keep Object[]
    @Override
    public LuaValue[] vxpCall(Object[] args, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(args, handler);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vxpCall(Object[] args, int nResults, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(args, nResults, handler);
        L.pop(1);
        return result;
    }

    // Keep Lua.Conversion
    @Override
    public LuaValue[] vxpCall(Object[] args, Lua.Conversion degree, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(args, degree, handler);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vxpCall(Object[] args, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(args, degree, nResults, handler);
        L.pop(1);
        return result;
    }

    // Keep Class<?>
    @Override
    public LuaValue[] vxpCall(Object[] args, Class<?> clazz, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(args, clazz, handler);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vxpCall(Object[] args, Class<?> clazz, int nResults, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(args, clazz, nResults, handler);
        L.pop(1);
        return result;
    }

    // Keep Class<?> & Lua.Conversion
    @Override
    public LuaValue[] vxpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(args, clazz, degree, handler);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vxpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(args, clazz, degree, nResults, handler);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vxpCall(CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(handler);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vxpCall(int nArgs, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(nArgs, handler);
        L.pop(1);
        return result;
    }

    @Override
    public LuaValue[] vxpCall(int nArgs, int nResults, CFunction handler) throws LuaException {
        push();
        LuaValue[] result = L.vxpCall(nArgs, nResults, handler);
        L.pop(1);
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
    public boolean toBoolean() throws LuaException {
        push();
        boolean result = L.toBoolean(-1);
        L.pop(1);
        return result;
    }

    @Override
    public long toInteger() throws LuaException {
        push();
        long result = L.toInteger(-1);
        L.pop(1);
        return result;
    }

    @Override
    public double toNumber() throws LuaException {
        push();
        double result = L.toNumber(-1);
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
    public String toString() {
        try {
            push();
            String result = L.toString(-1);
            L.pop(1);
            return result;
        } catch (LuaException e) {
            return super.toString();
        }
    }

    @Override
    public String ltoString() throws LuaException {
        push();
        String result = L.ltoString(-1);
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
    public LuaCFunction checkCFunction() {
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
    public Object checkJavaObject() throws LuaException {
        throw new IllegalArgumentException("Not a java object");
    }

    @Override
    public Object toJavaObject(Class<?> clazz) throws LuaException {
        if (clazz == Void.class || clazz == void.class)
            return null;
        else if (clazz == boolean.class || clazz == Boolean.class)
            return true;
        throw new LuaException(String.format("Could not convert %s to %s", typeName(), clazz.getName()));
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
    public <T> List<T> toJavaList(Class<T> clazz) throws LuaException {
        List<T> list = new ArrayList<>();
        ipairs((index, value) -> {
            list.add((T) value.toJavaObject(clazz));
            return true;
        });
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> toJavaMap(Class<K> keyClazz, Class<V> valueClazz) throws LuaException {
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
    public <T> Map<T, T> toJavaMap(Class<T> clazz) throws LuaException {
        return toJavaMap(clazz, clazz);
    }
}