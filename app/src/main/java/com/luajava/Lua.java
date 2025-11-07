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

import android.util.Log;

import com.luajava.cleaner.LuaReferable;
import com.luajava.cleaner.LuaReference;
import com.luajava.util.ClassUtils;
import com.luajava.util.Type;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.luajava.value.LuaIpairsIterator;
import com.luajava.value.LuaPairsIterator;
import com.luajava.value.LuaProxy;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;
import com.luajava.value.immutable.LuaBoolean;
import com.luajava.value.referable.LuaFunction;
import com.luajava.value.referable.LuaLightUserdata;
import com.luajava.value.immutable.LuaNil;
import com.luajava.value.immutable.LuaNumber;
import com.luajava.value.referable.LuaString;
import com.luajava.value.referable.LuaTable;
import com.luajava.value.referable.LuaThread;
import com.luajava.value.referable.LuaUnknown;
import com.luajava.value.referable.LuaUserdata;

/**
 * An implementation that relies on {@link LuaNatives} for most of the features independent of Lua versions
 */
public class Lua {
    protected volatile ExternalLoader loader;
    protected final ReferenceQueue<LuaReferable> recyclableReferences;
    protected final ConcurrentHashMap<Integer, LuaReference<?>> recordedReferences;
    public final LuaNil NIL;
    public final LuaBoolean TRUE, FALSE;
    public static LuaNatives C;
    protected final long L;
    protected static final String TAG = "LuaJava";
    protected LuaHandler handler;

    /**
     * Creates a new Lua (main) state
     */
    public Lua() {
        if (C == null) C = LuaNatives.getInstance();
        L = C.luaJ_newstate();
        loader = null;
        recyclableReferences = new ReferenceQueue<>();
        recordedReferences = new ConcurrentHashMap<>();
        NIL = LuaNil.from(this);
        TRUE = LuaBoolean.from(this, true);
        FALSE = LuaBoolean.from(this, false);
        Jua.add(this);
    }

    public Lua(LuaHandler handler) {
        this();
        this.handler = handler;
    }

    public static LuaNatives getNative() {
        if (C == null) C = LuaNatives.getInstance();
        return C;
    }

    public void setHandler(LuaHandler handler) {
        this.handler = handler;
    }

    public void onError(Exception e) {
        if (handler != null) {
            handler.onError(e);
        } else {
            Log.i(TAG, e.getMessage());
        }
    }

    public static void log(String str) {
        Log.i(TAG, str);
    }

    // Push API
    public void pushNil() {
        checkStack(1);
        C.lua_pushnil(L);
    }

    public void push(boolean bool) {
        checkStack(1);
        C.lua_pushboolean(L, bool ? 1 : 0);
    }

    public void push(long integer) {
        checkStack(1);
        C.lua_pushinteger(L, integer);
    }

    public void push(float number) {
        checkStack(1);
        C.lua_pushnumber(L, number);
    }

    public void push(double number) {
        checkStack(1);
        C.lua_pushnumber(L, number);
    }

    public void push(@NotNull String string) {
        checkStack(1);
        C.lua_pushstring(L, string);
    }

    public void push(Buffer buffer) {
        checkStack(1);
        C.luaJ_pushbuffer(L, buffer, buffer.remaining());
    }

    public void push(@NotNull CFunction function) {
        checkStack(1);
        C.luaJ_pushfunction(L, function);
    }

    public void push(@NotNull CFunction function, int n) {
        checkStack(1);
        C.luaJ_pushcclosure(L, function, n);
    }

    public void push(@NotNull LuaValue value) {
        checkStack(1);
        value.push(this);
    }

    public void push(Object object) {
        push(object, Conversion.SEMI);
    }

    public void push(@Nullable Object object, Conversion degree) {
        checkStack(1);
        switch (degree) {
            case SEMI:
                if (object instanceof Boolean) {
                    push((boolean) object);
                    return;
                } else if (object instanceof String) {
                    push((String) object);
                    return;
                } else if (object instanceof Long) {
                    push((long) object);
                    return;
                } else if (object instanceof Integer) {
                    push((int) object);
                    return;
                } else if (object instanceof Short) {
                    push((short) object);
                    return;
                } else if (object instanceof Byte) {
                    push((byte) object);
                    return;
                } else if (object instanceof Character) {
                    push((char) object);
                    return;
                } else if (object instanceof Float || object instanceof Double) {
                    push((Number) object);
                    return;
                }
                break;
            case FULL:
                if (object.getClass().isArray()) {
                    pushArray(object);
                    return;
                } else if (object instanceof Collection<?>) {
                    pushCollection((Collection<?>) object);
                    return;
                } else if (object instanceof Map<?, ?>) {
                    pushMap((Map<?, ?>) object);
                    return;
                }
                break;
        }
        // fallback or none conversion
        pushJavaObject(object);
    }

    public void pushJavaObject(@NotNull Object object) throws IllegalArgumentException {
        checkStack(1);
        if (object == null) {
            C.lua_pushnil(L);
        } else if (object instanceof Class<?>) {
            C.luaJ_pushclass(L, object);
        } else if (object.getClass().isArray()) {
            C.luaJ_pusharray(L, object);
        } else if (object instanceof LuaValue) {
            ((LuaValue) object).push(this);
        } else if (object instanceof JFunction) {
            push((JFunction) object);
        } else if (object instanceof CFunction) {
            push((CFunction) object);
        } else {
            C.luaJ_pushobject(L, object);
        }
    }

    public void pushArray(@NotNull Object array) throws IllegalArgumentException {
        checkStack(2);
        if (array.getClass().isArray()) {
            int len = Array.getLength(array);
            C.lua_createtable(L, len, 0);
            for (int i = 0; i != len; ++i) {
                push(Array.get(array, i), Conversion.FULL);
                C.lua_rawseti(L, -2, i + 1);
            }
        } else {
            throw new IllegalArgumentException("Not an array");
        }
    }

    public void pushCollection(@NotNull Collection<?> collection) {
        checkStack(2);
        C.lua_createtable(L, collection.size(), 0);
        int i = 1;
        for (Object o : collection) {
            push(o, Conversion.FULL);
            C.lua_rawseti(L, -2, i);
            i++;
        }
    }

    public void pushMap(@NotNull Map<?, ?> map) {
        checkStack(3);
        C.lua_createtable(L, 0, map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            push(entry.getKey(), Conversion.FULL);
            push(entry.getValue(), Conversion.FULL);
            C.lua_rawset(L, -3);
        }
    }

    public int pushAll(Object[] objects) {
        return pushAll(objects, Conversion.NONE);
    }

    public int pushAll(Object[] objects, Lua.Conversion degree) {
        if (objects == null) return 0;
        for (Object object : objects) {
            push(object, degree);
        }
        return objects.length;
    }

    // Get API
    public LuaValue get(String globalName) {
        getGlobal(globalName);
        return get();
    }

    public void set(String key, Object value) {
        set(key, value, Conversion.NONE);
    }

    public void set(String key, Object value, Conversion degree) {
        push(value, degree);
        setGlobal(key);
    }


    public LuaValue[] getArgs(int length) {
        return getAll(-length);
    }

    public LuaValue[] getArgs(int startIdx, int length) {
        int top = getTop();
        startIdx = getAbsoluteIndex(top, startIdx);
        LuaValue[] values = new LuaValue[length];
        for (int i = 0; i < length; i++) {
            values[i] = get(startIdx + i);
        }
        return values;
    }

    public LuaValue[] getAll() {
        int top = getTop();
        LuaValue[] values = new LuaValue[top];
        for (int i = 0; i < top; i++) {
            values[i] = get(i + 1);
        }
        return values;
    }

    public LuaValue[] getAll(int startIdx) {
        int targetIdx = getTop();
        startIdx = getAbsoluteIndex(targetIdx, startIdx);
        int length = targetIdx - startIdx + 1;
        LuaValue[] values = new LuaValue[length];
        for (int i = 0; i < length; i++) {
            values[i] = get(startIdx + i);
        }
        return values;
    }

    public LuaValue[] getAll(int startIdx, int targetIdx) {
        int top = getTop();
        startIdx = getAbsoluteIndex(top, startIdx);
        targetIdx = getAbsoluteIndex(top, targetIdx);
        int length = targetIdx - startIdx + 1;
        LuaValue[] values = new LuaValue[length];
        for (int i = 0; i < length; i++) {
            values[i] = get(startIdx + i);
        }
        return values;
    }

    public LuaValue get() {
        return get(-1);
    }

    public LuaValue get(int index) {
        return get(index, type(index));
    }

    public LuaValue get(int index, LuaType type) {
        LuaValue value;
        switch (Objects.requireNonNull(type)) {
            case NONE:
            case NIL:
                value = fromNull();
                break;
            case BOOLEAN:
                value = from(toBoolean(index));
                break;
            case NUMBER:
                value = new LuaNumber(this, index);
                break;
            case STRING:
                value = new LuaString(this, index);
                break;
            case TABLE:
                value = new LuaTable(this, index);
                break;
            case FUNCTION:
                value = new LuaFunction(this, index);
                break;
            case LIGHTUSERDATA:
                value = new LuaLightUserdata(this, index);
                break;
            case USERDATA:
                value = new LuaUserdata(this, index);
                break;
            case THREAD:
                value = new LuaThread(this, index);
                break;
            default:
                value = new LuaUnknown(this, type, index);
        }
        return value;
    }

    // To API
    public boolean toBoolean(int index) {
        return C.lua_toboolean(L, index) != 0;
    }

    public long toInteger(int index) {
        return C.lua_tointeger(L, index);
    }

    public double toNumber(int idx) {
        return C.lua_tonumber(L, idx);
    }

    public @Nullable String toString(int index) {
        return C.lua_tostring(L, index);
    }

    public @Nullable String ltoString(int index) {
        return C.luaJ_tostring(L, index);
    }

    public @Nullable ByteBuffer toBuffer(int index) {
        return (ByteBuffer) C.luaJ_tobuffer(L, index);
    }

    public @Nullable ByteBuffer toDirectBuffer(int index) {
        ByteBuffer buffer = (ByteBuffer) C.luaJ_todirectbuffer(L, index);
        if (buffer == null) {
            return null;
        } else {
            return buffer.asReadOnlyBuffer();
        }
    }

    public @Nullable Object toJavaObject(int index) {
        return C.luaJ_toobject(L, index);
    }

    public @Nullable Map<?, ?> toMap(int index) {
        Object obj = toJavaObject(index);
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj);
        }
        checkStack(2);
        index = getAbsoluteIndex(index);
        if (C.lua_istable(L, index) == 1) {
            C.lua_pushnil(L);
            Map<Object, Object> map = new HashMap<>();
            while (C.lua_next(L, index) != 0) {
                Object k = toObject(-2);
                Object v = toObject(-1);
                map.put(k, v);
                pop(1);
            }
            return map;
        }
        return null;
    }

    public @Nullable List<?> toList(int index) {
        Object obj = toJavaObject(index);
        if (obj instanceof List) {
            return ((List<?>) obj);
        }
        checkStack(1);
        if (C.lua_istable(L, index) == 1) {
            long length = rawLength(index);
            ArrayList<Object> list = new ArrayList<>();
            list.ensureCapacity((int) length);
            for (int i = 1; i <= length; i++) {
                C.lua_rawgeti(L, index, i);
                list.add(toObject(-1));
                pop(1);
            }
            return list;
        }
        return null;
    }

    // LuaState
    public void close() {
        C.lua_close(L);
        Jua.remove(L);
    }

    // TODO: support lua_newthread

    // LuaStack API
    public int getTop() {
        return C.lua_gettop(L);
    }

    public void setTop(int index) {
        C.lua_settop(L, index);
    }

    public void pop(int n) {
        C.lua_pop(L, n);
    }

    public void pushValue(int index) {
        checkStack(1);
        C.lua_pushvalue(L, index);
    }

    public void remove(int index) {
        C.lua_remove(L, index);
    }

    public void insert(int index) {
        C.lua_insert(L, index);
    }

    public void replace(int index) {
        C.lua_replace(L, index);
    }

    public void checkStack(int extra) throws LuaException {
        recycleReferences();
        if (C.lua_checkstack(L, extra) == 0) {
            throw new LuaException(LuaException.LuaError.MEMORY, "No more stack space available");
        }
    }

    public void checkStack(int extra, String msg) {
        recycleReferences();
        C.luaL_checkstack(L, extra, msg);
    }

    public void checkError(int code, boolean runtime) throws LuaException {
        LuaException exception = LuaException.from(this, code, runtime);
        if (exception != null) throw exception;
    }

    public void xMove(Lua to, int n) {
        to.checkStack(n);
        C.lua_xmove(L, to.L, n);
    }

    public void copy(Lua to) {
        C.luaJ_copy(this.L, to.L);
    }

    public String dumpStack() {
        return C.luaJ_dumpstack(L);
    }

    // LuaValue API
    // Type
    public LuaType type(int index) {
        return LuaType.from(C.lua_type(L, index));
    }

    public String typeName(int index) {
        return C.luaL_typename(L, index);
    }

    public String typeName(LuaType type) {
        return type.toString();
    }

    // Type Check
    public boolean isNoneOrNil(int index) {
        return C.lua_isnoneornil(L, index) != 0;
    }

    public boolean isNone(int index) {
        return C.lua_isnone(L, index) != 0;
    }

    public boolean isNil(int index) {
        return C.lua_isnil(L, index) != 0;
    }

    public boolean isBoolean(int index) {
        return C.lua_isboolean(L, index) != 0;
    }

    public boolean isNumber(int index) {
        return C.lua_isnumber(L, index) != 0;
    }

    public boolean isString(int index) {
        return C.lua_isstring(L, index) != 0;
    }

    public boolean isTable(int index) {
        return C.lua_istable(L, index) != 0;
    }

    public boolean isFunction(int index) {
        return C.lua_isfunction(L, index) != 0;
    }

    public boolean isCFunction(int index) {
        return C.lua_iscfunction(L, index) != 0;
    }

    public boolean isLightUserdata(int index) {
        return C.lua_islightuserdata(L, index) != 0;
    }

    public boolean isUserdata(int index) {
        return C.lua_isuserdata(L, index) != 0;
    }

    public boolean isThread(int index) {
        return C.lua_isthread(L, index) != 0;
    }

    public boolean isJavaObject(int index) {
        return C.luaJ_isobject(L, index) != 0;
    }

    // Check API
    public int checkOption(int arg, String def, String[] options) {
        String str = (isString(arg)) ? toString(arg) : def;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(str)) {
                return i;
            }
        }
        return argError(arg, String.format("Invalid option '%s'", str));
    }

    public void checkType(int nArg, LuaType type) {
        C.luaL_checktype(L, nArg, type.toInt());
    }

    public void checkAny(int nArg) {
        C.luaL_checkany(L, nArg);
    }

    public int checkInt(int numArg) {
        return C.luaL_checkint(L, numArg);
    }

    public long checkInteger(int numArg) {
        return C.luaL_checkinteger(L, numArg);
    }

    public long checkLong(int numArg) {
        return C.luaL_checklong(L, numArg);
    }

    public double checkNumber(int numArg) {
        return C.luaL_checknumber(L, numArg);
    }

    public String checkString(int numArg) {
        return C.luaL_checkstring(L, numArg);
    }

    // Optional API
    public int optInt(int nArg, int def) {
        return C.luaL_optint(L, nArg, def);
    }

    public long optInteger(int nArg, long def) {
        return C.luaL_optinteger(L, nArg, def);
    }

    public long optLong(int nArg, long def) {
        return C.luaL_optlong(L, nArg, def);
    }

    public double optNumber(int nArg, double def) {
        return C.luaL_optnumber(L, nArg, def);
    }

    public String optString(int nArg, String def) {
        return C.luaL_optstring(L, nArg, def);
    }

    // Compare API
    public boolean equal(int idx1, int idx2) {
        return C.lua_equal(L, idx1, idx2) != 0;
    }

    public boolean rawEqual(int idx1, int idx2) {
        return C.lua_rawequal(L, idx1, idx2) != 0;
    }

    public boolean lessThan(int idx1, int idx2) {
        return C.lua_lessthan(L, idx1, idx2) != 0;
    }

    public boolean lessThanOrEqual(int idx1, int idx2) {
        return C.luaJ_compare(L, idx1, idx2, 1) != 0;
    }

    public boolean greaterThan(int idx1, int idx2) {
        return C.luaJ_compare(L, idx1, idx2, 3) != 0;
    }

    public boolean greaterThanOrEqual(int idx1, int idx2) {
        return C.luaJ_compare(L, idx1, idx2, 4) != 0;
    }

    // Basic API
    public long getPointer(int index) {
        return C.lua_topointer(L, index);
    }

    public long rawLength(int index) {
        return C.lua_objlen(L, index);
    }

    public long strlen(int index) {
        return C.lua_strlen(L, index);
    }

    public void concat(int n) {
        if (n == 0) checkStack(1);
        C.lua_concat(L, n);
    }

    // Lua Table API
    public void newTable() {
        C.lua_newtable(L);
    }

    public void createTable(int nArr, int nRec) {
        checkStack(1);
        C.lua_createtable(L, nArr, nRec);
    }

    public void getTable(int index) {
        checkStack(1);
        C.lua_gettable(L, index);
    }

    public void setTable(int index) {
        C.lua_settable(L, index);
    }

    public void getField(int index, String key) {
        checkStack(1);
        C.lua_getfield(L, index, key);
    }

    public void setField(int index, String key) {
        C.lua_setfield(L, index, key);
    }

    public void rawGet(int index) {
        checkStack(1);
        C.lua_rawget(L, index);
    }

    public void rawSet(int index) {
        C.lua_rawset(L, index);
    }

    public void rawGetI(int index, int n) {
        checkStack(1);
        C.lua_rawgeti(L, index, n);
    }

    public void rawSetI(int index, int n) {
        C.lua_rawseti(L, index, n);
    }

    public void pairs(LuaPairsIterator iterator) {
        pushNil();
        while (next(-2)) {
            LuaValue key = get(-2); // key
            LuaValue value = get(-1); // value
            pop(1); // pop value
            boolean shouldContinue = iterator.iterate(key, value);
            if (!shouldContinue) {
                pop(1); // pop key
                break;
            }
        }
    }

    public void ipairs(LuaIpairsIterator iterator) {
        long index = 1;
        while (true) {
            push(index);
            getTable(-2);
            if (isNil(-1)) {
                pop(1); // pop nil value
                break;
            }
            LuaValue value = get(-1);
            pop(1); // pop value
            boolean shouldContinue = iterator.iterate(index, value);
            if (!shouldContinue) {
                break;
            }
            index++;
        }
    }

    // Metatable API
    public LuaUserdata newUserdata(int size) {
        long ptr = C.lua_newuserdata(L, size);
        if (ptr != 0) {
            return new LuaUserdata(this);
        }
        return null;
    }

    public boolean getMetatable(int index) {
        checkStack(1);
        return C.lua_getmetatable(L, index) != 0;
    }

    public void setMetatable(int index) {
        C.lua_setmetatable(L, index);
    }

    public int getMetaField(int index, String field) {
        checkStack(1);
        return C.luaL_getmetafield(L, index, field);
    }

    public void setMetatable(String tname) {
        C.luaL_setmetatable(L, tname);
    }

    public boolean callMetatable(int obj, String meta) {
        return C.luaL_callmeta(L, obj, meta) != 0;
    }

    // Function API
    public void call(Object[] args) {
        call(pushAll(args));
    }

    public void call(Object[] args, int nResults) {
        call(pushAll(args), nResults);
    }

    public void call(Object[] args, Lua.Conversion degree) {
        call(pushAll(args, degree));
    }

    public void call(Object[] args, Lua.Conversion degree, int nResults) {
        call(pushAll(args, degree), nResults);
    }

    public void call() {
        C.lua_call(L, 0, 0);
    }

    public void call(int nArgs) {
        C.lua_call(L, nArgs, 0);
    }

    public void call(int nArgs, int nResults) {
        C.lua_call(L, nArgs, nResults);
    }

    public void pCall(Object[] args) throws LuaException {
        pCall(args, Lua.Conversion.NONE);
    }

    public void pCall(Object[] args, int nResults) throws LuaException {
        pCall(args, Lua.Conversion.NONE, nResults, 0);
    }

    public void pCall(Object[] args, int nResults, int errfunc) throws LuaException {
        pCall(args, Lua.Conversion.NONE, nResults, errfunc);
    }

    public void pCall(Object[] args, Lua.Conversion degree) throws LuaException {
        pCall(pushAll(args, degree));
    }

    public void pCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        pCall(pushAll(args, degree), nResults);
    }

    public void pCall(Object[] args, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        pCall(pushAll(args, degree), nResults, errfunc);
    }

    public void pCall() throws LuaException {
        checkError(C.luaJ_pcall(L, 0, 0, 0), false);
    }

    public void pCall(int nArgs) throws LuaException {
        checkError(C.luaJ_pcall(L, nArgs, 0, 0), false);
    }

    public void pCall(int nArgs, int nResults) throws LuaException {
        checkError(C.luaJ_pcall(L, nArgs, nResults, 0), false);
    }

    public void pCall(int nArgs, int nResults, int errfunc) throws LuaException {
        checkError(C.luaJ_pcall(L, nArgs, nResults, errfunc), false);
    }

    // xpCall
    public void xpCall(CFunction handler) throws LuaException {
        push(handler);
        insert(-2);
        pCall(0, 0, -2);
    }

    public void xpCall(int nArgs, CFunction handler) throws LuaException {
        push(handler);
        int errfunc = -nArgs - 2;
        insert(errfunc);
        pCall(nArgs, 0, errfunc);
    }

    public void xpCall(int nArgs, int nResults, CFunction handler) throws LuaException {
        push(handler);
        int errfunc = -nArgs - 2;
        insert(errfunc);
        pCall(nArgs, nResults, errfunc);
    }

    public void cpCall(LuaFunction cfunc, LuaLightUserdata ud) {
        LuaFunction func = cfunc.checkCFunction();
        int result = C.lua_cpcall(L, func.getPointer(), ud.getPointer());
        checkError(result, false);
    }

    // Function Environment API
    public void getFenv(int index) {
        checkStack(1);
        C.lua_getfenv(L, index);
    }

    public void setFenv(int index) {
        C.lua_setfenv(L, index);
    }

    // Thread API
    public int yield(int nResults) {
        return C.lua_yield(L, nResults);
    }

    public boolean yieldable() {
        return C.lua_isyieldable(L) != 0;
    }

    public boolean resume(int nArgs) throws LuaException {
        int code = C.lua_resume(L, nArgs);
        if (LuaException.LuaError.from(code) == LuaException.LuaError.YIELD) {
            return true;
        }
        checkError(code, false);
        return false;
    }

    public LuaException.LuaError status() {
        return LuaException.LuaError.from(C.lua_status(L));
    }

    // LuaStatues
    public void gc() {
        recycleReferences();
        C.luaJ_gc(L);
    }

    public void gc(int what, int data) {
        recycleReferences();
        C.lua_gc(L, what, data);
    }

    public void where(int level) {
        C.luaL_where(L, level);
    }

    public void error() {
        C.lua_error(L);
    }

    public void error(String message) {
        push(message);
        C.lua_error(L);
    }

    public int error(@Nullable Throwable e) {
        error(e.toString());
        return 0;
    }

    public void register(String name, LuaFunction function) {
        if (function.isCFunction()) {
            C.lua_register(L, name, function.getPointer());
        } else {
            push(function);
            setGlobal(name);
        }
    }

    public int typeError(int nArg, String tname) {
        return C.luaL_typerror(L, nArg, tname);
    }

    public int argError(int numArg, String extraMsg) {
        return C.luaL_argerror(L, numArg, extraMsg);
    }

    // LuaJIT
    public boolean setJITMode(int idx, int mode) {
        return C.luaJIT_setmode(L, idx, mode) != 0;
    }

    // table pairs
    public boolean next(int n) {
        checkStack(1);
        return C.lua_next(L, n) != 0;
    }

    // Lua Global API
    public void getGlobal(String name) {
        checkStack(1);
        C.lua_getglobal(L, name);
    }

    public void setGlobal(String name) {
        C.lua_setglobal(L, name);
    }

    public LuaFunction getFunction(String funcName) {
        getGlobal(funcName);
        if (isFunction(-1)) {
            LuaFunction func = new LuaFunction(this);
            pop(1);
            return func;
        }
        pop(1);
        return null;
    }

    public void getRegistry() {
        C.lua_getregistry(L);
    }

    public int getRegistryIndex() {
        return LuaConsts.LUA_REGISTRYINDEX;
    }

    public int getGcCount() {
        return C.lua_getgccount(L);
    }

    public long upvalueid(int idx, int n) {
        return C.lua_upvalueid(L, idx, n);
    }

    public void upvaluejoin(int idx1, int n1, int idx2, int n2) {
        C.lua_upvaluejoin(L, idx1, n1, idx2, n2);
    }

    public double version() {
        return C.lua_version(L);
    }

    public void copy(int fromidx, int toidx) {
        C.lua_copy(L, fromidx, toidx);
    }

    public String gsub(String s, String p, String r) {
        return C.luaL_gsub(L, s, p, r);
    }

    public String findTable(int idx, String fname, int szhint) {
        return C.luaL_findtable(L, idx, fname, szhint);
    }

    public int fileResult(int stat, String fname) {
        return C.luaL_fileresult(L, stat, fname);
    }

    public int execResult(int stat) {
        return C.luaL_execresult(L, stat);
    }

    public int loadFileX(String filename, String mode) {
        return C.luaL_loadfilex(L, filename, mode);
    }

    public int loadBufferX(String buff, long sz, String name, String mode) {
        return C.luaL_loadbufferx(L, buff, sz, name, mode);
    }

    public void traceback(Lua L1, String msg, int level) {
        C.luaL_traceback(L, L1.getPointer(), msg, level);
    }

    /**
     * Converts a stack index into an absolute index.
     *
     * @param index a stack index
     * @return an absolute positive stack index
     */
    public int getAbsoluteIndex(int index) {
        if (index > 0) {
            return index;
        }
        if (index <= LuaConsts.LUA_REGISTRYINDEX) {
            return index;
        }
        if (index == 0) {
            throw new IllegalArgumentException("Stack index should not be 0");
        }
        return getTop() + 1 + index;
    }

    public int getAbsoluteIndex(int top, int index) {
        if (index > 0) {
            return index;
        }
        if (index <= LuaConsts.LUA_REGISTRYINDEX) {
            return index;
        }
        if (index == 0) {
            throw new IllegalArgumentException("Stack index should not be 0");
        }
        return top + 1 + index;
    }


    public @Nullable Object toObject(int index) {
        LuaType type = type(index);
        if (type == null) {
            return null;
        }
        switch (type) {
            case NIL:
            case NONE:
                return null;
            case BOOLEAN:
                return toBoolean(index);
            case NUMBER:
                return toNumber(index);
            case STRING:
                return toString(index);
            case TABLE:
                return toMap(index);
            case USERDATA:
                return toJavaObject(index);
        }
        return get();
    }


    public @Nullable Object toObject(int index, Class<?> type) {
        Object converted = toObject(index);
        if (converted == null) {
            return null;
        } else if (type.isAssignableFrom(converted.getClass())) {
            return converted;
        } else if (Number.class.isAssignableFrom(converted.getClass())) {
            Number number = ((Number) converted);
            if (type == byte.class || type == Byte.class) {
                return number.byteValue();
            }
            if (type == short.class || type == Short.class) {
                return number.shortValue();
            }
            if (type == int.class || type == Integer.class) {
                return number.intValue();
            }
            if (type == long.class || type == Long.class) {
                return number.longValue();
            }
            if (type == float.class || type == Float.class) {
                return number.floatValue();
            }
            if (type == double.class || type == Double.class) {
                return number.doubleValue();
            }
        }
        return null;
    }


    public void loadString(String script) throws LuaException {
        checkStack(1);
        checkError(C.luaL_loadstring(L, script), false);
    }

    public void loadBuffer(Buffer buffer, String name) throws LuaException {
        if (buffer.isDirect()) {
            checkStack(1);
            checkError(C.luaJ_loadbuffer(L, buffer, buffer.limit(), name), false);
        } else {
            throw new LuaException(LuaException.LuaError.MEMORY, "Expecting a direct buffer");
        }
    }

    public void loadFile(String filename) throws LuaException {
        checkStack(1);
        checkError(C.luaL_loadfile(L, filename), false);
    }

    public void doFile(String filename) throws LuaException {
        checkStack(1);
        checkError(C.luaJ_dofile(L, filename), true);
    }

    public void doString(String script) throws LuaException {
        checkStack(1);
        checkError(C.luaJ_dostring(L, script), true);
    }

    public void doBuffer(Buffer buffer, String name) throws LuaException {
        if (buffer.isDirect()) {
            checkStack(1);
            checkError(C.luaJ_dobuffer(L, buffer, buffer.limit(), name), true);
        } else {
            throw new LuaException(LuaException.LuaError.MEMORY, "Expecting a direct buffer");
        }
    }


    public ByteBuffer dump() {
        return (ByteBuffer) C.luaJ_dump(L);
    }

    public void getMetatable(String typeName) {
        checkStack(1);
        C.luaL_getmetatable(L, typeName);
    }

    public int newMetatable(String typeName) {
        checkStack(1);
        return C.luaL_newmetatable(L, typeName);
    }

    public void openLibraries() {
        checkStack(1);
        C.luaL_openlibs(L);
        C.luaJ_initloader(L);
    }

    public void openLibrary(String name) {
        checkStack(1);
        C.luaJ_openlib(L, name);
        if ("package".equals(name)) {
            C.luaJ_initloader(L);
        }
    }

    public void openLibrary(String... name) {
        for (String n : name) {
            openLibrary(n);
        }
    }

    public Object createProxy(int index, Class<?> interfaces, Conversion degree) throws IllegalArgumentException {
        return LuaProxy.newInstance(this, index, interfaces, degree).toProxy();
    }

    public void setExternalLoader(ExternalLoader loader) {
        this.loader = loader;
    }

    public int loadExternal(String module) throws LuaException {
        synchronized (this) {
            ExternalLoader loader = this.loader;
            if (loader != null) {
                return loader.load(this, module);
            }
            return 0;
        }
    }

    public Lua getMainState() {
        return this;
    }

    public long getPointer() {
        return L;
    }

    /**
     * Calls a method on an object, equivalent to <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokespecial">invokespecial</a>
     *
     * <p>
     * Internally it uses {@link LuaNatives#luaJ_invokespecial(long, Class, String, String, Object, String)} which then uses
     * {@code CallNonvirtual<Type>MethodA} functions to avoid tons of restrictions imposed by the JVM.
     * </p>
     *
     * @param object the {@code this} object
     * @param method the method
     * @param params the parameters
     * @return the return value
     * @throws Throwable whenever the method call throw exceptions
     */
    public @Nullable Object invokeSpecial(Object object, Method method, @Nullable Object[] params) throws
            Throwable {
        if (!ClassUtils.isDefault(method)) {
            throw new IncompatibleClassChangeError("Unable to invoke non-default method");
        }
        if (params == null) {
            params = new Object[0];
        }
        for (int i = params.length - 1; i >= 0; i--) {
            Object param = params[i];
            if (param == null) {
                pushNil();
            } else {
                pushJavaObject(param);
            }
        }
        StringBuilder customSignature = new StringBuilder(params.length + 1);
        for (Class<?> type : method.getParameterTypes()) {
            appendCustomDescriptor(type, customSignature);
        }
        appendCustomDescriptor(method.getReturnType(), customSignature);
        if (C.luaJ_invokespecial(
                L,
                method.getDeclaringClass(),
                method.getName(),
                Type.getMethodDescriptor(method),
                object,
                customSignature.toString()
        ) == -1) {
            String error = toString(-1);
            pop(1);
            throw new LuaException(LuaException.LuaError.RUNTIME, error);
        }
        if (method.getReturnType() == Void.TYPE) {
            return null;
        }
        Object ret = toJavaObject(-1);
        pop(1);
        return ret;
    }

    private void appendCustomDescriptor(Class<?> type, StringBuilder customSignature) {
        if (type.isPrimitive()) {
            customSignature.append(Type.getPrimitiveDescriptor(type));
        } else {
            customSignature.append("_");
        }
    }

    // Reference API
    public int refSafe() {
        return refSafe(-1);
    }

    public int refSafe(int idx) {
        return C.luaJ_refsafe(L, idx);
    }

    public int ref() {
        return C.luaJ_ref(L);
    }

    public void refGet(int ref) {
        C.luaJ_refGet(L, ref);
    }

    public void unRef(int ref) {
        C.luaJ_unRef(L, ref);
    }

    public int ref(int index) {
        return C.luaL_ref(L, index);
    }

    public void unRef(int index, int ref) {
        C.luaL_unref(L, index, ref);
    }

    public LuaType typeRef(int ref) {
        return LuaType.from(C.luaJ_typeRef(L, ref));
    }

    public LuaNil fromNull() {
        return NIL;
    }

    public LuaBoolean from(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public LuaNumber from(Number number) {
        return LuaNumber.from(this, number);
    }

    public LuaString from(String str) {
        return LuaString.from(this, str);
    }

    public LuaString from(Buffer buffer) {
        return LuaString.from(this, buffer);
    }

    public void registerReference(LuaReferable referable) {
        recordedReferences.putIfAbsent(referable.getRef(), new LuaReference<>(referable, recyclableReferences));
    }

    /**
     * Do {@link #unRef(int)} on all references in {@link #recyclableReferences}
     */
    private void recycleReferences() {
        LuaReference<?> ref = (LuaReference<?>) recyclableReferences.poll();
        while (ref != null) {
            recordedReferences.remove(ref.getReference());
            unRef(ref.getReference());
            ref = (LuaReference<?>) recyclableReferences.poll();
        }
    }

    public enum Conversion {
        /**
         * Converts everything possible, including the following classes:
         *
         * <ul>
         *     <li>Boolean -&gt; boolean</li>
         *     <li>String -&gt; string</li>
         *     <li>Number -&gt; lua_Number</li>
         *     <li>Map / Collection / Array -&gt; table (recursive)</li>
         *     <li>Object -&gt; Java object wrapped by a metatable {@link Lua#pushJavaObject}</li>
         * </ul>
         *
         * <p>
         * Note that this means luatable changes on the lua side will not get reflected
         * to the Java side.
         * </p>
         */
        FULL,
        /**
         * Converts immutable types, including:
         * <ul>
         *     <li>Boolean</li>
         *     <li>String</li>
         *     <li>Number</li>
         * </ul>
         *
         * <p>
         *     {@link Map}, {@link Collection}, etc. are pushed with {@link Lua#pushJavaObject(Object)}.
         * </p>
         */
        SEMI,
        /**
         * All objects, including {@link Integer}, for example, are pushed as either
         * Java objects (with {@link Lua#pushJavaObject(Object)})
         */
        NONE
    }
}
