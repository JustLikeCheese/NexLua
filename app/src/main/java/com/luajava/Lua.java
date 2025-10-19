/*
 * Copyright (C) 2022 the original author or authors.
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

import com.luajava.cleaner.LuaReferable;
import com.luajava.cleaner.LuaReference;
import com.luajava.util.ClassUtils;
import com.luajava.util.Type;
import com.luajava.value.*;

import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.luajava.value.LuaType;
import com.luajava.value.type.LuaBoolean;
import com.luajava.value.type.LuaCFunction;
import com.luajava.value.type.LuaFunction;
import com.luajava.value.type.LuaLightUserdata;
import com.luajava.value.type.LuaNil;
import com.luajava.value.type.LuaNumber;
import com.luajava.value.type.LuaString;
import com.luajava.value.type.LuaTable;
import com.luajava.value.type.LuaThread;
import com.luajava.value.type.LuaUnknown;
import com.luajava.value.type.LuaUserdata;

/**
 * An implementation that relies on {@link LuaNatives} for most of the features independent of Lua versions
 */
public class Lua {
    protected volatile ExternalLoader loader;
    protected final ReferenceQueue<LuaReferable> recyclableReferences;
    protected final ConcurrentHashMap<Integer, LuaReference<?>> recordedReferences;

    String GLOBAL_THROWABLE = "__jthrowable__";

    protected final long L;
    public static LuaNatives C;

    /**
     * Creates a new Lua (main) state
     */
    public Lua() {
        if (C == null) C = LuaNatives.getInstance();
        L = C.luaJ_newstate();
        loader = null;
        recyclableReferences = new ReferenceQueue<>();
        recordedReferences = new ConcurrentHashMap<>();
        Jua.add(this);
    }

    public static LuaNatives getNative() {
        if (C == null) C = LuaNatives.getInstance();
        return C;
    }

    // Push API
    public void pushLightUserdata(long ptr) {
        checkStack(1);
        C.lua_pushlightuserdata(L, ptr);
    }

    public void pushNil() {
        checkStack(1);
        C.lua_pushnil(L);
    }

    public void pushTraceback() {
        checkStack(1);
        C.luaJ_pushtraceback(L);
    }

    public void pushCClosure(LuaCFunction function, int n) {
        checkStack(1);
        C.lua_pushcclosure(L, function.getPointer(), n);
    }

    public void push(@NotNull Number number) {
        checkStack(1);
        C.lua_pushnumber(L, number.doubleValue());
    }

    public void push(long integer) {
        checkStack(1);
        C.lua_pushinteger(L, integer);
    }

    public void push(@NotNull String string) {
        checkStack(1);
        C.lua_pushstring(L, string);
    }

    public void push(boolean bool) {
        checkStack(1);
        C.lua_pushboolean(L, bool ? 1 : 0);
    }

    public void push(Buffer buffer) {
        checkStack(1);
        C.luaJ_pushbuffer(L, buffer, buffer.remaining());
    }

    public void push(Object object) {
        push(object, Conversion.NONE);
    }

    public void push(@Nullable Object object, Conversion degree) {
        checkStack(1);
        if (object == null) {
            pushNil();
        } else if (object instanceof LuaValue) {
            LuaValue value = (LuaValue) object;
            value.push(this);
        } else if (object instanceof JFunction) {
            push((JFunction) object);
        } else if (degree == Conversion.NONE) {
            pushJavaObjectOrArray(object);
        } else {
            if (object instanceof Boolean) {
                push((boolean) object);
            } else if (object instanceof String) {
                push((String) object);
            } else if (object instanceof Integer || object instanceof Byte || object instanceof Short) {
                push(((Number) object).intValue());
            } else if (object instanceof Character) {
                push(((int) (Character) object));
            } else if (object instanceof Long) {
                push((long) object);
            } else if (object instanceof Float || object instanceof Double) {
                push((Number) object);
            } else if (object instanceof CFunction) {
                push(((CFunction) object));
            } else if (degree == Conversion.SEMI) {
                pushJavaObjectOrArray(object);
            } else /* if (degree == Conversion.FULL) */ {
                if (object instanceof Class) {
                    pushJavaClass(((Class<?>) object));
                } else if (object instanceof Map) {
                    push((Map<?, ?>) object);
                } else if (object instanceof Collection) {
                    push((Collection<?>) object);
                } else if (object.getClass().isArray()) {
                    pushArray(object);
                } else {
                    pushJavaObject(object);
                }
            }
        }
    }

    public void pushAll(Object[] objects) {
        pushAll(objects, Conversion.NONE);
    }

    public void pushAll(Object[] objects, Lua.Conversion degree) {
        for (Object object : objects) {
            push(object, degree);
        }
    }

    protected void pushJavaObjectOrArray(Object object) {
        checkStack(1);
        if (object.getClass().isArray()) {
            pushJavaArray(object);
        } else {
            pushJavaObject(object);
        }
    }


    public void push(@NotNull Map<?, ?> map) {
        checkStack(3);
        C.lua_createtable(L, 0, map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            push(entry.getKey(), Conversion.FULL);
            push(entry.getValue(), Conversion.FULL);
            C.lua_rawset(L, -3);
        }
    }


    public void push(@NotNull Collection<?> collection) {
        checkStack(2);
        C.lua_createtable(L, collection.size(), 0);
        int i = 1;
        for (Object o : collection) {
            push(o, Conversion.FULL);
            C.lua_rawseti(L, -2, i);
            i++;
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


    public void push(@NotNull CFunction function) {
        checkStack(1);
        C.luaJ_pushfunction(L, function);
    }

    public void push(@NotNull LuaValue value) {
        checkStack(1);
        value.push(this);
    }


    public void push(@NotNull JFunction function) {
        checkStack(1);
        push(new JFunctionWrapper(function));
    }


    public void pushJavaObject(@NotNull Object object) throws IllegalArgumentException {
        if (object.getClass().isArray()) {
            throw new IllegalArgumentException("Expecting non-array argument");
        } else {
            checkStack(1);
            C.luaJ_pushobject(L, object);
        }
    }


    public void pushJavaArray(@NotNull Object array) throws IllegalArgumentException {
        if (array.getClass().isArray()) {
            checkStack(1);
            C.luaJ_pusharray(L, array);
        } else {
            throw new IllegalArgumentException("Expecting non-array argument");
        }
    }


    public void pushJavaClass(@NotNull Class<?> clazz) {
        checkStack(1);
        C.luaJ_pushclass(L, clazz);
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
        if (n < 0 || getTop() < n) {
            throw new LuaException(
                    LuaException.LuaError.MEMORY,
                    "invalid number of items to pop"
            );
        }
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

    public void xMove(Lua other, int n) {
        other.checkStack(n);
        C.lua_xmove(L, other.getPointer(), n);
    }

    // LuaValue API
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

    public LuaType type(int index) {
        return LuaType.from(C.lua_type(L, index));
    }

    public String typeName(LuaType type) {
        return type.toString();
    }

    public String typeName(int index) {
        return C.luaL_typename(L, index);
    }

    public boolean equal(int idx1, int idx2) {
        return C.lua_equal(L, idx1, idx2) != 0;
    }

    public boolean rawEqual(int i1, int i2) {
        return C.lua_rawequal(L, i1, i2) != 0;
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

    public double toNumber(int idx) {
        return C.lua_tonumber(L, idx);
    }

    public long toInteger(int index) {
        return C.lua_tointeger(L, index);
    }

    public boolean toBoolean(int index) {
        return C.lua_toboolean(L, index) != 0;
    }

    public @Nullable String toString(int index) {
        return C.lua_tostring(L, index);
    }

    public @Nullable String ltoString(int index) {
        return C.luaJ_tostring(L, index);
    }

    public @Nullable LuaCFunction toCFunction(int index) {
        long ptr = C.lua_tocfunction(L, index);
        return (ptr != 0)
                ? new LuaCFunction(ptr, this)
                : null;
    }

    public @Nullable LuaUserdata toUserdata(int index) {
        long ptr = C.lua_touserdata(L, index);
        return (ptr != 0)
                ? new LuaUserdata(this, index)
                : null;
    }

    public @Nullable LuaThread toThread(int index) {
        long ptr = C.lua_tothread(L, index);
        return (ptr != 0)
                ? new LuaThread(this, index)
                : null;
    }

    public long rawLength(int index) {
        return C.lua_objlen(L, index);
    }

    // LuaTable API
    public void getTable(int index) {
        C.lua_gettable(L, index);
    }

    public void getField(int index, String key) {
        checkStack(1);
        C.lua_getfield(L, index, key);
    }

    public void setTable(int index) {
        C.lua_settable(L, index);
    }

    public void setField(int index, String key) {
        C.lua_setfield(L, index, key);
    }

    public void rawGet(int index) {
        C.lua_rawget(L, index);
    }

    public void rawGetI(int index, int n) {
        checkStack(1);
        C.lua_rawgeti(L, index, n);
    }

    public void rawSet(int index) {
        C.lua_rawset(L, index);
    }

    public void rawSetI(int index, int n) {
        C.lua_rawseti(L, index, n);
    }

    public void createTable(int nArr, int nRec) {
        checkStack(1);
        C.lua_createtable(L, nArr, nRec);
    }

    // public native long lua_newuserdata(long ptr, int sz);

    // LuaMetatable API
    public boolean getMetatable(int index) {
        checkStack(1);
        return C.lua_getmetatable(L, index) != 0;
    }

    public int getMetaField(int index, String field) {
        checkStack(1);
        return C.luaL_getmetafield(L, index, field);
    }

    public void setMetatable(int index) {
        C.lua_setmetatable(L, index);
    }

    public void setMetatable(String tname) {
        C.luaL_setmetatable(L, tname);
    }

    public boolean callMeta(int obj, String e) {
        return C.luaL_callmeta(L, obj, e) != 0;
    }

    // Function Environment API
    public void getFenv(int index) {
        checkStack(1);
        C.lua_getfenv(L, index);
    }

    public void setFenv(int index) {
        C.lua_setfenv(L, index);
    }

    // Function API
    public void call(int nArgs, int nResults) {
        C.lua_call(L, nArgs, nResults);
    }

    public void pCall(int nArgs, int nResults) throws LuaException {
        pCall(nArgs, nResults, 0);
    }

    public void pCall(int nArgs, int nResults, int errfunc) throws LuaException {
        checkStack(Math.max(nResults - nArgs - 1, 0));
        checkError(C.luaJ_pcall(L, nArgs, nResults, errfunc), false);
    }

    public void pCall(Object[] args, int nResults, int errfunc) throws LuaException {
        pCall(args, Conversion.NONE, nResults, errfunc);
    }

    public void pCall(Object[] args, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        int nArgs;
        if (args != null) {
            nArgs = args.length;
            pushAll(args, degree);
        } else {
            nArgs = 0;
        }
        checkStack(Math.max(nResults - nArgs - 1, 0));
        checkError(C.luaJ_pcall(L, nArgs, nResults, errfunc), false);
    }


    public void cpCall(LuaCFunction cfunc, LuaLightUserdata ud) {
        int result = C.lua_cpcall(L, cfunc.getPointer(), ud.getPointer());
        checkError(result, false);
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

    public void where(int level) {
        C.luaL_where(L, level);
    }

    public void error() {
        C.lua_error(L);
    }

    public void error(String message) {
        throw new RuntimeException(message);
    }

    public int error(@Nullable Throwable e) {
        if (e == null) {
            pushNil();
            setGlobal(GLOBAL_THROWABLE);
            return 0;
        }
        pushJavaObject(e);
        setGlobal(GLOBAL_THROWABLE);
        push(e.toString());
        return -1;
    }

    public void register(String name, LuaCFunction function) {
        C.lua_register(L, name, function.getPointer());
    }

    public int typeError(int nArg, String tname) {
        return C.luaL_typerror(L, nArg, tname);
    }

    public int argError(int numArg, String extraMsg) {
        return C.luaL_argerror(L, numArg, extraMsg);
    }

    // type check
    public void checkType(int nArg, LuaType type) {
        C.luaL_checktype(L, nArg, type.toInt());
    }

    public void checkAny(int nArg) {
        C.luaL_checkany(L, nArg);
    }

    public LuaUserdata checkUserdata(int index, String tname) {
        C.luaL_checkudata(L, index, tname);
        return new LuaUserdata(this);
    }

    public @Nullable LuaUserdata testUserdata(int ud, String tname) {
        long ptr = C.luaL_testudata(L, ud, tname);
        if (ptr == 0)
            return null;
        return new LuaUserdata(this);
    }

    public double checkNumber(int numArg) {
        return C.luaL_checknumber(L, numArg);
    }

    public double optNumber(int nArg, double def) {
        return C.luaL_optnumber(L, nArg, def);
    }

    public long checkInteger(int numArg) {
        return C.luaL_checkinteger(L, numArg);
    }

    public long optInteger(int nArg, long def) {
        return C.luaL_optinteger(L, nArg, def);
    }

    public String checkString(int numArg) {
        return C.luaL_checkstring(L, numArg);
    }

    public String optString(int nArg, String def) {
        return C.luaL_optstring(L, nArg, def);
    }

    public int checkInt(int numArg) {
        return C.luaL_checkint(L, numArg);
    }

    public int optInt(int nArg, int def) {
        return C.luaL_optint(L, nArg, def);
    }

    public long checkLong(int numArg) {
        return C.luaL_checklong(L, numArg);
    }

    public long optLong(int nArg, long def) {
        return C.luaL_optlong(L, nArg, def);
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

    // table.concat
    public void concat(int n) {
        if (n == 0) {
            checkStack(1);
        }
        C.lua_concat(L, n);
    }

    public void newTable() {
        C.lua_newtable(L);
    }

    // public native void lua_register(long ptr, String name, long cfunction);

    public long strlen(int index) {
        return C.lua_strlen(L, index);
    }

    // Lua Global API
    public void getGlobal(String name) {
        checkStack(1);
        C.lua_getglobal(L, name);
    }

    public void setGlobal(String name) {
        C.lua_setglobal(L, name);
    }

    public long toPointer(int index) {
        return C.lua_topointer(L, index);
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


    // TODO: HHHHHHHHHHHHHHHHHHHHH


    /**
     * Converts a stack index into an absolute index.
     *
     * @param index a stack index
     * @return an absolute positive stack index
     */
    public int toAbsoluteIndex(int index) {
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
        pushValue(index);
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
        index = toAbsoluteIndex(index);
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


    public boolean isJavaObject(int index) {
        return C.luaJ_isobject(L, index) != 0;
    }


    public void pushThread() {
        checkStack(1);
        C.lua_pushthread(L);
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
        checkError(C.luaL_dofile(L, filename), true);
    }

    public void doString(String script) throws LuaException {
        checkStack(1);
        checkError(C.luaL_dostring(L, script), true);
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
        return (ByteBuffer) C.luaJ_dumptobuffer(L);
    }


//    public Lua newThread() {
//        checkStack(1);
//        LuaInstances.Token<Lua> token = instances.add();
//        long L2 = C.luaJ_newthread(L, token.id);
//        Lua lua = newThread(L2, token.id, this.mainThread);
//        mainThread.addSubThread(lua);
//        token.setter.accept(lua);
//        return lua;
//    }
//
//    protected void addSubThread(Lua lua) {
//        synchronized (subThreads) {
//            subThreads.add(lua);
//        }
//    }

//    protected Lua newThread(long L, int id, Lua mainThread) {
//        return new Lua(L, id, mainThread);
//    }


    public int ref(int index) {
        return C.luaL_ref(L, index);
    }


    public void unRef(int index, int ref) {
        C.luaL_unref(L, index, ref);
    }


    public void getRegisteredMetatable(String typeName) {
        checkStack(1);
        C.luaL_getmetatable(L, typeName);
    }


    public int newRegisteredMetatable(String typeName) {
        checkStack(1);
        return C.luaL_newmetatable(L, typeName);
    }


    public void openLibraries() {
        checkStack(1);
        C.luaL_openlibs(L);
//        C.luaJ_initloader(L);
    }


    public void openLibrary(String name) {
        checkStack(1);
        C.luaJ_openlib(L, name);
        if ("package".equals(name)) {
//            C.luaJ_initloader(L);
        }
    }

    public void openLibrary(String... name) {
        for (String n : name) {
            openLibrary(n);
        }
    }

    public Object createProxy(Class<?> interfaces, Conversion degree) throws IllegalArgumentException {
        switch (Objects.requireNonNull(type(-1))) {
            case FUNCTION:
                String name = ClassUtils.getSingleInterfaceMethodName(interfaces);
                if (name == null) {
                    pop(1);
                    throw new IllegalArgumentException("Unable to merge interfaces into a functional one");
                }
                createTable(0, 1);
                insert(getTop() - 1);
                setField(-2, name);
                // Fall through
            case TABLE:
                try {
                    LuaProxy proxy = new LuaProxy(ref(), this, degree, interfaces);
                    recordedReferences.put(proxy.getRef(), new LuaReference<>(proxy, recyclableReferences));
                    return Proxy.newProxyInstance(interfaces.getClassLoader(), new Class[]{interfaces}, proxy);
                } catch (Throwable e) {
                    throw new IllegalArgumentException(e);
                }
            default:
                break;
        }
        pop(1);
        throw new IllegalArgumentException("Expecting a table / function and interfaces");
    }


    public void register(String name, LuaFunction function) {
        push(function);
        setGlobal(name);
    }


    public void setExternalLoader(ExternalLoader loader) {
        this.loader = loader;
    }


    public void loadExternal(String module) throws LuaException {
        ExternalLoader loader = this.loader;
        if (loader == null) {
            throw new LuaException(LuaException.LuaError.RUNTIME, "External loader not set");
        }
        Buffer buffer = loader.load(module, this);
        if (buffer == null) {
            throw new LuaException(LuaException.LuaError.FILE, "Loader returned null");
        }
        loadBuffer(buffer, module);
    }

    public Lua getMainState() {
        return this;
    }


    public long getPointer() {
        return L;
    }


    public @Nullable Throwable getJavaError() {
        getGlobal(GLOBAL_THROWABLE);
        Object o = toJavaObject(-1);
        pop(1);
        if (o instanceof Throwable) {
            return (Throwable) o;
        } else {
            return null;
        }
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
    protected @Nullable Object invokeSpecial(Object object, Method method, @Nullable Object[] params) throws
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
            Throwable javaError = getJavaError();
            pop(1);
            throw Objects.requireNonNull(javaError);
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


    public int ref() {
        return ref(LuaConsts.LUA_REGISTRYINDEX);
    }


    public void refGet(int ref) {
        rawGetI(LuaConsts.LUA_REGISTRYINDEX, ref);
    }


    public void unRef(int ref) {
        unRef(LuaConsts.LUA_REGISTRYINDEX, ref);
    }

    /**
     * Throws {@link LuaException} if the code is not {@link LuaException.LuaError#OK}.
     *
     * <p>
     * Most Lua C API functions attaches along an error message on the stack.
     * If this method finds a string on the top of the stack, it pops the string
     * and uses it as the exception message.
     * </p>
     *
     * @param code    the error code returned by Lua C API
     * @param runtime if {@code true}, treat non-zero code values as runtime errors
     */
    protected void checkError(int code, boolean runtime) throws LuaException {
        LuaException.LuaError error = runtime
                ? (code == 0 ? LuaException.LuaError.OK : LuaException.LuaError.RUNTIME)
                : LuaException.LuaError.from(code);
        if (error == LuaException.LuaError.OK) {
            return;
        }
        String message;
        if (type(-1) == LuaType.STRING) {
            message = toString(-1);
            pop(1);
        } else {
            message = "Lua-side error";
        }
        LuaException e = new LuaException(error, message);
        Throwable javaError = getJavaError();
        if (javaError != null) {
            e.initCause(javaError);
            error((Throwable) null);
        }
        throw e;
    }

    public LuaValue get(String globalName) {
        getGlobal(globalName);
        return get();
    }


    public void set(String key, Object value) {
        push(value, Conversion.SEMI);
        setGlobal(key);
    }


    public LuaValue getFunction(String funcName) {
        getGlobal(funcName);
        if (isFunction(-1)) {
            return get();
        }
        pop(1);
        return null;
    }


    public LuaValue[] eval(String command) throws LuaException {
        loadString(command);
        return get().call();
    }

    public LuaValue get(int index) {
        pushValue(index);
        LuaValue value = get();
        pop(1);
        return value;
    }

    public LuaValue get() {
        LuaType type = type(-1);
        LuaValue value;
        switch (Objects.requireNonNull(type)) {
            case NIL:
            case NONE:
                value = new LuaNil(this);
                break;
            case BOOLEAN:
                value = new LuaBoolean(this);
                break;
            case NUMBER:
                value = new LuaNumber(this);
                break;
            case STRING:
                value = new LuaString(this);
                break;
            case TABLE:
                value = new LuaTable(this);
                break;
            case FUNCTION:
                value = new LuaFunction(this);
                break;
            case LIGHTUSERDATA:
                value = new LuaLightUserdata(this);
                break;
            case USERDATA:
                value = new LuaUserdata(this);
                break;
            case THREAD:
                value = new LuaThread(this);
                break;
            default:
                value = new LuaUnknown(this, type);
        }
        if (value instanceof AbstractLuaRefValue) {
            AbstractLuaRefValue refValue = (AbstractLuaRefValue) value;
            recordedReferences.put(refValue.getRef(),
                    new LuaReference<>(refValue, recyclableReferences));
        }
        return value;
    }


    public LuaNil fromNull() {
        return new LuaNil(this);
    }


    public LuaBoolean from(boolean bool) {
        return new LuaBoolean(bool, this);
    }


    public LuaNumber from(Number number) {
        return new LuaNumber(number, this);
    }

    public LuaString from(String str) {
        return new LuaString(str, this);
    }

    public LuaString from(Buffer buffer) {
        return new LuaString(buffer, this);
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

    private static class JFunctionWrapper implements CFunction {
        private final @NotNull JFunction function;

        public JFunctionWrapper(@NotNull JFunction function) {
            this.function = function;
        }

        public int __call(Lua L) {
            LuaValue[] args = new LuaValue[L.getTop()];
            for (int i = 0; i < args.length; i++) {
                args[args.length - i - 1] = L.get();
            }
            LuaValue[] results = function.call(L, args);
            if (results != null) {
                for (LuaValue result : results) {
                    L.push(result);
                }
            }
            return results == null ? 0 : results.length;
        }
    }

    public boolean copyFunction(Lua L1) {
        return C.luaJ_copyfunction(L, L1.getPointer()) != 0;
    }

    public boolean copyString(Lua L1) {
        return C.luaJ_copystring(L, L1.getPointer()) != 0;
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
         *     Arrays are pushed with {@link Lua#pushJavaArray(Object)}.
         * </p>
         */
        SEMI,
        /**
         * All objects, including {@link Integer}, for example, are pushed as either
         * Java objects (with {@link Lua#pushJavaObject(Object)}) or Java arrays
         * (with {@link Lua#pushJavaArray(Object)}).
         */
        NONE
    }
}
