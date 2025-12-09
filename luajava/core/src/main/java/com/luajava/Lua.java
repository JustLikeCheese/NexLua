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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.luajava.cleaner.LuaReferable;
import com.luajava.cleaner.LuaReference;
import com.luajava.util.ClassUtils;

import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.luajava.value.LuaIterator;
import com.luajava.value.LuaProxy;
import com.luajava.value.LuaType;
import com.luajava.value.LuaValue;
import com.luajava.value.immutable.LuaBoolean;
import com.luajava.value.referable.LuaCFunction;
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
@SuppressWarnings("unused")
public class Lua {
    protected volatile ExternalLoader loader;
    protected final ReferenceQueue<LuaReferable> recyclableReferences;
    protected final ConcurrentHashMap<Integer, LuaReference<?>> recordedReferences;
    public final LuaNil NIL;
    public final LuaBoolean TRUE, FALSE;
    public static final String JAVA_GLOBAL_THROWABLE = "__java_throwable__";
    public static final Object NONE = new Object();
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

    public void sendError(Exception e) {
        if (handler != null) {
            handler.sendError(e);
        } else {
            Log.i(TAG, LuaException.getFullMessage(e));
        }
    }

    public static void log(String str) {
        Log.i(TAG, str);
    }

    public static void logError(String str) {
        Log.e(TAG, str);
    }

    public static void logWarn(String str) {
        Log.w(TAG, str);
    }

    // Push API
    public int pushNil() {
        C.lua_pushnil(L);
        return 1;
    }

    public int push(boolean bool) throws LuaException {
        checkStack(1);
        C.lua_pushboolean(L, bool ? 1 : 0);
        return 1;
    }

    public int push(long integer) throws LuaException {
        checkStack(1);
        C.lua_pushinteger(L, integer);
        return 1;
    }

    public int push(int integer) throws LuaException {
        return push((long) integer);
    }

    public int push(short integer) throws LuaException {
        return push((long) integer);
    }

    public int push(byte integer) throws LuaException {
        return push((long) integer);
    }

    public int push(char ch) throws LuaException {
        return push((long) ch);
    }

    public int push(double number) throws LuaException {
        checkStack(1);
        C.lua_pushnumber(L, number);
        return 1;
    }

    public int push(float number) throws LuaException {
        return push((double) number);
    }

    public int push(Number number) throws LuaException {
        return push(number.doubleValue());
    }

    public int push(@NonNull String string) throws LuaException {
        C.lua_pushstring(L, string);
        return 1;
    }

    public int push(@NonNull Buffer buffer) throws LuaException {
        checkStack(1);
        C.luaJ_pushbuffer(L, buffer, buffer.remaining());
        return 1;
    }

    public int push(Class<?> clazz) throws LuaException {
        checkStack(1);
        C.luaJ_pushclass(L, clazz);
        return 1;
    }

    public int push(@NonNull LuaValue value) throws LuaException {
        checkStack(1);
        return value.push(this);
    }

    public int push(CFunction func) throws LuaException {
        checkStack(1);
        C.luaJ_pushfunction(L, func);
        return 1;
    }

    public int push(Object object) throws LuaException {
        return push(object, Conversion.NONE);
    }

    public int push(Object object, Conversion degree) throws LuaException {
        return push(object, null, degree);
    }

    public int push(Object object, Class<?> clazz) throws LuaException {
        return push(object, clazz, Conversion.NONE);
    }

    public int push(Object object, Class<?> clazz, Conversion degree) throws LuaException {
        if (object == null) return pushNil();
        Class<?> objClass = clazz != null ? clazz : object.getClass();
        switch (degree) {
            case FULL:
                if (objClass.isArray()) {
                    return pushArray(object);
                } else if (object instanceof Collection<?>) {
                    return pushCollection((Collection<?>) object);
                } else if (object instanceof Map<?, ?>) {
                    return pushMap((Map<?, ?>) object);
                }
            case SEMI:
                if (object instanceof Number) {
                    return push((Number) object);
                } else if (object instanceof Boolean) {
                    return push((boolean) object);
                } else if (object instanceof Character) {
                    return push((char) object);
                } else if (object instanceof String) {
                    return push((String) object);
                }
        }
        if (objClass.isPrimitive()) {
            if (objClass == boolean.class)
                return push((boolean) object);
            else if (objClass == char.class)
                return push((char) object);
            else if (objClass == byte.class)
                return push((byte) object);
            else if (objClass == short.class)
                return push((short) object);
            else if (objClass == int.class)
                return push((int) object);
            else if (objClass == long.class)
                return push((long) object);
            else if (objClass == float.class)
                return push((float) object);
            else if (objClass == double.class)
                return push((double) object);
            else if (objClass == void.class)
                return pushNil();
        }
        // fallback or none conversion
        return pushJavaObject(object, clazz);
    }

    public int pushJavaObject(Object object) throws LuaException {
        return pushJavaObject(object, null);
    }

    public int pushJavaObject(Object object, Class<?> clazz) throws LuaException {
        checkStack(1);
        if (object == null) {
            C.lua_pushnil(L);
        } else {
            Class<?> objClass = clazz != null ? clazz : object.getClass();
            if (objClass.isArray()) {
                C.luaJ_pusharray(L, object);
            } else if (object instanceof Class<?>) {
                C.luaJ_pushclass(L, object);
            } else if (object instanceof CFunction) {
                C.luaJ_pushfunction(L, object);
            } else if (object instanceof LuaValue) {
                return push((LuaValue) object);
            } else {
                C.luaJ_pushobject(L, object);
            }
        }
        return 1;
    }

    public int pushArray(@NonNull Object array) throws IllegalArgumentException, LuaException {
        Class<?> clazz = array.getClass();
        if (!clazz.isArray()) {
            throw new IllegalArgumentException("Not an array");
        }
        checkStack(2);
        Class<?> type = clazz.getComponentType();
        int length = Array.getLength(array);
        C.lua_createtable(L, length, 0);
        for (int index = 0; index < length; ++index) {
            push(Array.get(array, index), type, Conversion.FULL);
            C.lua_rawseti(L, -2, index + 1);
        }
        return 1;
    }

    public int pushCollection(@NonNull Collection<?> collection) throws LuaException {
        checkStack(2);
        C.lua_createtable(L, collection.size(), 0);
        int index = 1;
        for (Object object : collection) {
            push(object, Conversion.FULL);
            C.lua_rawseti(L, -2, index);
            index++;
        }
        return 1;
    }

    public int pushMap(@NonNull Map<?, ?> map) throws LuaException {
        checkStack(3);
        C.lua_createtable(L, 0, map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            push(entry.getKey(), Conversion.FULL);
            push(entry.getValue(), Conversion.FULL);
            C.lua_rawset(L, -3);
        }
        return 1;
    }

    public int pushAll(Object[] objects) throws LuaException {
        return pushAll(objects, Conversion.NONE);
    }

    public int pushAll(Object[] objects, Lua.Conversion degree) throws LuaException {
        int length = 0;
        if (objects == null) return length;
        for (Object object : objects) {
            length = length + push(object, degree);
        }
        return length;
    }

    public int pushAll(Object[] objects, Class<?> clazz) throws LuaException {
        return pushAll(objects, clazz, Conversion.NONE);
    }

    public int pushAll(Object[] objects, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        int length = 0;
        if (objects == null) return length;
        for (Object object : objects) {
            length = length + push(object, clazz, degree);
        }
        return length;
    }

    // Push Global API
    public int pushGlobal(Object object, Class<?> clazz, Conversion degree, @NonNull String... names) throws LuaException {
        int length = names.length;
        if (length < 1) {
            throw new LuaException("Invalid number of arguments");
        }
        length--;
        push(object, clazz, degree);
        for (int i = 0; i < length; i++) {
            pushValue(-1);
            setGlobal(names[i]);
        }
        setGlobal(names[length]);
        return 0;
    }

    public int pushGlobal(Object object, Class<?> clazz, String... names) throws LuaException {
        return pushGlobal(object, clazz, Conversion.NONE, names);
    }

    public int pushGlobal(Object object, Conversion degree, @NonNull String... names) throws LuaException {
        int length = names.length;
        if (length < 1) {
            throw new LuaException("Invalid number of arguments");
        }
        length--;
        push(object, degree);
        for (int i = 0; i < length; i++) {
            pushValue(-1);
            setGlobal(names[i]);
        }
        setGlobal(names[length]);
        return 0;
    }

    public int pushGlobal(Object object, String... names) throws LuaException {
        return pushGlobal(object, Conversion.NONE, names);
    }

    // Get API
    public LuaValue get(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaValue value = get();
        pop(1);
        return value;
    }

    public void set(String key, Object value) throws LuaException {
        set(key, value, Conversion.NONE);
    }

    public void set(String key, Object value, Conversion degree) throws LuaException {
        push(value, degree);
        setGlobal(key);
    }

    public void set(String key, Object value, Class<?> clazz) throws LuaException {
        push(value, clazz, Conversion.NONE);
        setGlobal(key);
    }

    public void set(String key, Object value, Class<?> clazz, Conversion degree) throws LuaException {
        push(value, clazz, degree);
        setGlobal(key);
    }

    private static final LuaValue[] EMPTY_LUA_VALUES = new LuaValue[0];

    public LuaValue[] getArgs(int length) throws LuaException {
        if (length == 0) {
            return EMPTY_LUA_VALUES;
        }
        return getAll(-length);
    }

    public LuaValue[] getArgs(int startIdx, int length) throws LuaException {
        if (length == 0) {
            return EMPTY_LUA_VALUES;
        }
        int top = getTop();
        startIdx = getAbsoluteIndex(top, startIdx);
        LuaValue[] values = new LuaValue[length];
        for (int i = 0; i < length; i++) {
            values[i] = get(startIdx + i);
        }
        return values;
    }

    public LuaValue[] getAll() throws LuaException {
        int top = getTop();
        LuaValue[] values = new LuaValue[top];
        for (int i = 0; i < top; i++) {
            values[i] = get(i + 1);
        }
        return values;
    }

    public LuaValue[] getAll(int startIdx) throws LuaException {
        int targetIdx = getTop();
        startIdx = getAbsoluteIndex(targetIdx, startIdx);
        int length = targetIdx - startIdx + 1;
        LuaValue[] values = new LuaValue[length];
        for (int i = 0; i < length; i++) {
            values[i] = get(startIdx + i);
        }
        return values;
    }

    public LuaValue[] getAll(int startIdx, int targetIdx) throws LuaException {
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

    public LuaValue get() throws LuaException {
        return get(-1);
    }

    public LuaValue get(int index) throws LuaException {
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

    public @Nullable String LtoString(int index) {
        return C.luaJ_tostring(L, index);
    }

    public @Nullable ByteBuffer toBuffer(int index) {
        return (ByteBuffer) C.luaJ_tobuffer(L, index);
    }

    public @Nullable ByteBuffer toDirectBuffer(int index) {
        ByteBuffer buffer = (ByteBuffer) C.luaJ_todirectbuffer(L, index);
        return (buffer != null) ? buffer.asReadOnlyBuffer() : null;
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

    public void pushValue(int index) throws LuaException {
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

    public void xMove(@NonNull Lua to, int n) throws LuaException {
        to.checkStack(n);
        C.lua_xmove(L, to.L, n);
    }

    public void copyTo(@NonNull Lua to) {
        C.luaJ_copy(this.L, to.L);
    }

    public String dumpStack() {
        return C.luaJ_dumpstack(L);
    }

    // LuaValue API
    // Type
    public LuaType type(int index) throws LuaException {
        return LuaType.from(C.lua_type(L, index));
    }

    public String typeName(int index) {
        return C.luaL_typename(L, index);
    }

    public String typeName(@NonNull LuaType type) {
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

    // Check API
    public int checkOption(int arg, String def, @NonNull String[] options) {
        String str = (isString(arg)) ? toString(arg) : def;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(str)) {
                return i;
            }
        }
        return argError(arg, String.format("Invalid option '%s'", str));
    }

    public void checkType(int nArg, @NonNull LuaType type) throws LuaException {
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

    public int rawLength(int index) {
        return C.lua_objlen(L, index);
    }

    public long strlen(int index) {
        return C.lua_strlen(L, index);
    }

    public void concat(int n) throws LuaException {
        if (n == 0) checkStack(1);
        C.lua_concat(L, n);
    }

    // Lua Table API
    public void newTable() {
        C.lua_newtable(L);
    }

    public void createTable(int nArr, int nRec) {
        C.lua_createtable(L, nArr, nRec);
    }

    public void getTable(int index) throws LuaException {
        checkStack(1);
        C.lua_gettable(L, index);
    }

    public void setTable(int index) {
        C.lua_settable(L, index);
    }

    public void getField(int index, String key) {
        C.lua_getfield(L, index, key);
    }

    public void setField(int index, String key) {
        C.lua_setfield(L, index, key);
    }

    public void setField(int index, String key, String value) throws LuaException {
        push(value);
        setField(index, key);
    }

    public void rawGet(int index) throws LuaException {
        checkStack(1);
        C.lua_rawget(L, index);
    }

    public void rawSet(int index) {
        C.lua_rawset(L, index);
    }

    public void rawGetI(int index, int n) throws LuaException {
        checkStack(1);
        C.lua_rawgeti(L, index, n);
    }

    public void rawSetI(int index, int n) {
        C.lua_rawseti(L, index, n);
    }

    /**
     * Stack: -2 is key, -1 is value
     * if iterator returns true then pop and break
     */
    public void pairs(int tableIndex, LuaIterator.Pairs iterator) throws LuaException {
        tableIndex = getAbsoluteIndex(tableIndex);
        pushNil();
        while (next(tableIndex)) {
            if (iterator.iterate(this)) {
                pop(2); // pop key and value
                break;
            }
            pop(1); // pop value
        }
    }

    /**
     * Stack: -1 is value
     * if iterator returns true then pop and break
     */
    public void ipairs(int tableIndex, LuaIterator.Ipairs iterator) throws LuaException {
        tableIndex = getAbsoluteIndex(tableIndex);
        int index = 1;
        while (true) {
            push(index);
            getTable(tableIndex);
            if (isNil(-1)) {
                pop(1); // pop nil
                break;
            }
            if (iterator.iterate(this, index)) {
                pop(1); // pop value
                break;
            }
            pop(1); // pop value
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
        return C.lua_getmetatable(L, index) != 0;
    }

    public void setMetatable(int index) {
        C.lua_setmetatable(L, index);
    }

    public int getMetaField(int index, String field) throws LuaException {
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

    // Keep Object[]
    public int call(Object[] args) throws LuaException {
        return call(pushAll(args));
    }

    public int call(Object[] args, int nResults) throws LuaException {
        return call(pushAll(args), nResults);
    }

    // Keep Lua.Conversion
    public int call(Object[] args, Lua.Conversion degree) throws LuaException {
        return call(pushAll(args, degree));
    }

    public int call(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        return call(pushAll(args, degree), nResults);
    }

    // Keep Class<?>
    public int call(Object[] args, Class<?> clazz) throws LuaException {
        return call(pushAll(args, clazz));
    }

    public int call(Object[] args, Class<?> clazz, int nResults) throws LuaException {
        return call(pushAll(args, clazz), nResults);
    }

    // Keep Class<?> & Lua.Conversion
    public int call(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        return call(pushAll(args, clazz, degree));
    }

    public int call(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException {
        return call(pushAll(args, clazz, degree), nResults);
    }

    public int call() {
        C.lua_call(L, 0, 0);
        return 0;
    }

    public int call(int nArgs) {
        C.lua_call(L, nArgs, 0);
        return 0;
    }

    public int call(int nArgs, int nResults) {
        if (nResults == -1) {
            int base = C.lua_gettop(L);
            C.lua_call(L, nArgs, nResults);
            return C.lua_gettop(L) - base + nArgs + 1;
        }
        C.lua_call(L, nArgs, nResults);
        return nResults;
    }

    // Keep Object[]
    public int pCall(Object[] args) throws LuaException {
        return pCall(pushAll(args));
    }

    public int pCall(Object[] args, int nResults) throws LuaException {
        return pCall(pushAll(args), nResults);
    }

    public int pCall(Object[] args, int nResults, int errfunc) throws LuaException {
        return pCall(pushAll(args), nResults, errfunc);
    }

    // Keep Lua.Conversion
    public int pCall(Object[] args, Lua.Conversion degree) throws LuaException {
        return pCall(pushAll(args, degree));
    }

    public int pCall(Object[] args, Lua.Conversion degree, int nResults) throws LuaException {
        return pCall(pushAll(args, degree), nResults);
    }

    public int pCall(Object[] args, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        return pCall(pushAll(args, degree), nResults, errfunc);
    }

    // Keep Class<?>
    public int pCall(Object[] args, Class<?> clazz) throws LuaException {
        return pCall(pushAll(args, clazz));
    }

    public int pCall(Object[] args, Class<?> clazz, int nResults) throws LuaException {
        return pCall(pushAll(args, clazz), nResults);
    }

    public int pCall(Object[] args, Class<?> clazz, int nResults, int errfunc) throws LuaException {
        return pCall(pushAll(args, clazz), nResults, errfunc);
    }

    // Keep Class<?> & Lua.Conversion
    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree) throws LuaException {
        return pCall(pushAll(args, clazz, degree));
    }

    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults) throws LuaException {
        return pCall(pushAll(args, clazz, degree), nResults);
    }

    public int pCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, int errfunc) throws LuaException {
        return pCall(pushAll(args, clazz, degree), nResults, errfunc);
    }

    public int pCall() throws LuaException {
        return pCall(0, 0, 0);
    }

    public int pCall(int nArgs) throws LuaException {
        return pCall(nArgs, 0, 0);
    }

    public int pCall(int nArgs, int nResults) throws LuaException {
        return pCall(nArgs, nResults, 0);
    }

    public int pCall(int nArgs, int nResults, int errfunc) throws LuaException {
        if (nResults == -1) {
            int base = C.lua_gettop(L);
            checkError(C.luaJ_pcall(L, nArgs, nResults, errfunc), false);
            return C.lua_gettop(L) - base + nArgs + 1;
        }
        checkError(C.luaJ_pcall(L, nArgs, nResults, errfunc), false);
        return nResults;
    }

    // xpCall
    // Keep Object[]
    public int xpCall(Object[] args, CFunction handler) throws LuaException {
        return xpCall(pushAll(args), handler);
    }

    public int xpCall(Object[] args, int nResults, CFunction handler) throws LuaException {
        return xpCall(pushAll(args), nResults, handler);
    }

    // Keep Lua.Conversion
    public int xpCall(Object[] args, Lua.Conversion degree, CFunction handler) throws LuaException {
        return xpCall(pushAll(args, degree), handler);
    }

    public int xpCall(Object[] args, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException {
        return xpCall(pushAll(args, degree), nResults, handler);
    }

    // Keep Class<?>
    public int xpCall(Object[] args, Class<?> clazz, CFunction handler) throws LuaException {
        return xpCall(pushAll(args, clazz), handler);
    }

    public int xpCall(Object[] args, Class<?> clazz, int nResults, CFunction handler) throws LuaException {
        return xpCall(pushAll(args, clazz), nResults, handler);
    }

    // Keep Class<?> & Lua.Conversion

    public int xpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, CFunction handler) throws LuaException {
        return xpCall(pushAll(args, clazz, degree), handler);
    }

    public int xpCall(Object[] args, Class<?> clazz, Lua.Conversion degree, int nResults, CFunction handler) throws LuaException {
        return xpCall(pushAll(args, clazz, degree), nResults, handler);
    }

    public int xpCall(CFunction handler) throws LuaException {
        return xpCall(0, 0, handler);
    }

    public int xpCall(int nArgs, CFunction handler) throws LuaException {
        return xpCall(nArgs, 0, handler);
    }

    public int xpCall(int nArgs, int nResults, CFunction handler) throws LuaException {
        push(handler);
        if (nResults == -1) {
            int base = C.lua_gettop(L) - nArgs - 1;
            checkError(C.luaJ_xpcall(L, nArgs, nResults), false);
            return C.lua_gettop(L) - base;
        }
        checkError(C.luaJ_xpcall(L, nArgs, nResults), false);
        return nResults;
    }

    public void cpCall(@NonNull LuaCFunction func, @NonNull LuaLightUserdata ud) throws LuaException {
        int result = C.lua_cpcall(L, func.getPointer(), ud.getPointer());
        checkError(result, false);
    }

    // Function Environment API
    public void getFenv(int index) throws LuaException {
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

    public LuaException.LuaError status() throws LuaException {
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

    public void error(String message) throws LuaException {
        push(message);
        C.lua_error(L);
    }

    public int error(@NonNull Throwable e) throws LuaException {
        error(e.toString());
        return 0;
    }

    public void register(String name, @NonNull LuaCFunction function) {
        C.lua_register(L, name, function.getPointer());
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
    public boolean next(int n) throws LuaException {
        checkStack(1);
        return C.lua_next(L, n) != 0;
    }

    // Lua Global API
    public void getGlobal(String name) throws LuaException {
        C.lua_getglobal(L, name);
    }

    public void setGlobal(String name) {
        C.lua_setglobal(L, name);
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

    public void traceback(@NonNull Lua L1, String msg, int level) {
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

    /* Java Object */
    public boolean isJavaObject(int index) {
        return C.luaJ_isobject(L, index) != 0;
    }

    public Object toJavaObject(int index) {
        return C.luaJ_toobject(L, index);
    }

    public Object checkJavaObject(int index) {
        return C.luaJ_checkobject(L, index);
    }

    /* Java Object Cast */
    public boolean isJavaObject(int index, Class<?> clazz) throws LuaException {
        LuaType type = type(index);
        Class<?> wrapperType = ClassUtils.getWrapperType(clazz);
        switch (type) {
            case NIL:
            case NONE:
                return (clazz == Object.class
                        || clazz == LuaValue.class
                        || clazz == LuaNil.class
                        || !clazz.isPrimitive());
            case BOOLEAN:
                return clazz == Object.class
                        || clazz == LuaValue.class
                        || clazz == LuaBoolean.class
                        || clazz == Boolean.class || clazz == boolean.class;
            case NUMBER:
                return clazz == Object.class
                        || clazz == LuaValue.class
                        || clazz == LuaNumber.class
                        || clazz == byte.class || clazz == Byte.class
                        || clazz == short.class || clazz == Short.class
                        || clazz == int.class || clazz == Integer.class
                        || clazz == long.class || clazz == Long.class
                        || clazz == float.class || clazz == Float.class
                        || clazz == double.class || clazz == Double.class
                        || clazz == char.class || clazz == Character.class
                        || clazz == Number.class;
            case STRING:
                return clazz == Object.class
                        || clazz == LuaValue.class
                        || clazz == LuaString.class
                        || CharSequence.class.isAssignableFrom(clazz);
            case TABLE:
                return clazz == Object.class || clazz == LuaValue.class || clazz == LuaTable.class
                        || clazz.isArray() || List.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)
                        || clazz.isInterface();
            case FUNCTION:
                return clazz == Object.class
                        || clazz == LuaValue.class
                        || clazz == LuaFunction.class
                        || clazz.isInterface();
            case THREAD:
                return clazz == Object.class
                        || clazz == LuaValue.class
                        || clazz == LuaThread.class;
            case LIGHTUSERDATA:
                return clazz == Object.class
                        || clazz == LuaValue.class
                        || clazz == LuaLightUserdata.class;
            case USERDATA:
                if (clazz == Object.class || clazz == LuaValue.class || clazz == LuaUserdata.class) {
                    return true;
                }
                if (isJavaObject(index)) {
                    Object object = toJavaObject(index);
                    Class<?> wrapperClass = ClassUtils.getWrapperType(clazz);
                    Class<?> objectClass = ClassUtils.getWrapperType(object.getClass());
                    return wrapperClass.isAssignableFrom(objectClass);
                }
                return false;
        }
        return false;
    }

    public @Nullable Object toJavaObject(int index, Class<?> clazz) throws LuaException {
        LuaType type = type(index);
        switch (type) {
            case NIL:
            case NONE:
                if (clazz == LuaValue.class || clazz == LuaNil.class)
                    return NIL;
                else if (clazz == Objects.class)
                    return null;
                break;
            case BOOLEAN:
                if (clazz == LuaValue.class || clazz == LuaBoolean.class)
                    return from(toBoolean(index));
                else if (clazz == Object.class || clazz == Boolean.class)
                    return toBoolean(index);
                break;
            case NUMBER:
                if (clazz == LuaValue.class || clazz == LuaNumber.class)
                    return new LuaNumber(this, index);
                else if (clazz == Object.class)
                    return toNumber(index);
                else if (clazz == Number.class)
                    return toNumber(index);
                else if (clazz == Character.class)
                    return (char) toInteger(index);
                else if (clazz == Byte.class)
                    return (byte) toInteger(index);
                else if (clazz == Integer.class)
                    return (int) toInteger(index);
                else if (clazz == Short.class)
                    return (short) toInteger(index);
                else if (clazz == Long.class)
                    return toInteger(index);
                else if (clazz == Float.class)
                    return (float) toNumber(index);
                else if (clazz == Double.class)
                    return toNumber(index);
            case STRING:
                if (clazz == LuaValue.class || clazz == LuaString.class)
                    return new LuaString(this, index);
                else if (clazz == Object.class)
                    return toString(index);
                else if (CharSequence.class.isAssignableFrom(clazz))
                    return toString(index);
                break;
            case TABLE:
                if (clazz == LuaValue.class || clazz == LuaTable.class)
                    return new LuaTable(this, index);
                else if (clazz.isArray())
                    return toJavaArray(index);
                else if (clazz.isAssignableFrom(Collection.class))
                    return toJavaList(index);
                else if (clazz == Object.class || Map.class.isAssignableFrom(clazz))
                    return toJavaMap(index);
                else if (clazz.isInterface())
                    return LuaProxy.newInstance(new LuaTable(this, index), clazz, Lua.Conversion.SEMI).toProxy();
                break;
            case FUNCTION:
                if (clazz == LuaValue.class || clazz == LuaFunction.class)
                    return new LuaFunction(this, index);
                else if (clazz == Object.class || clazz.isInterface())
                    return LuaProxy.newInstance(new LuaFunction(this, index), clazz, Lua.Conversion.SEMI).toProxy();
            case THREAD:
                if (clazz == LuaValue.class || clazz == LuaThread.class)
                    return new LuaThread(this, index);
                else if (clazz == Object.class)
                    return null;
            case LIGHTUSERDATA:
                if (clazz == LuaValue.class || clazz == LuaLightUserdata.class)
                    return this;
                else if (clazz == Object.class)
                    return null;
            case USERDATA:
                if (clazz == LuaValue.class || clazz == LuaUserdata.class) {
                    return this;
                }
                if (isJavaObject(index)) {
                    Object object = toJavaObject(index);
                    Class<?> objClass = ClassUtils.getWrapperType(object.getClass());
                    Class<?> wrapperClass = ClassUtils.getWrapperType(clazz);
                    if (clazz == Object.class || wrapperClass.isAssignableFrom(objClass)) {
                        return object;
                    }
                }
        }
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) {
                return toBoolean(index);
            } else if (clazz == char.class) {
                return (char) toInteger(index);
            } else if (clazz == byte.class) {
                return (byte) toInteger(index);
            } else if (clazz == short.class) {
                return (short) toInteger(index);
            } else if (clazz == int.class) {
                return (int) toInteger(index);
            } else if (clazz == long.class) {
                return toInteger(index);
            } else if (clazz == float.class) {
                return (float) toNumber(index);
            } else if (clazz == double.class) {
                return toNumber(index);
            }
        }
        return null;
    }

    public Object toJavaArray(int idx) throws LuaException {
        return toJavaArray(idx, Object.class);
    }

    public List<Object> toJavaList(int idx) throws LuaException {
        return toJavaList(idx, Object.class);
    }

    public Map<Object, Object> toJavaMap(int index) throws LuaException {
        return toJavaMap(index, Object.class);
    }

    public Object toJavaArray(int index, Class<?> clazz) throws LuaException {
        int length = rawLength(index);
        Object array = Array.newInstance(clazz, length);
        ipairs(index, (L, value) -> {
            int javaIndex = value - 1;
            Object element = toJavaObject(-1, clazz);
            Array.set(array, javaIndex, element);
            return false;
        });
        return array;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> toJavaList(int index, Class<T> clazz) throws LuaException {
        List<T> list = new ArrayList<>();
        ipairs(index, (L, value) -> {
            list.set(value - 1, (T) toJavaObject(-1, clazz));
            return false;
        });
        return list;
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> toJavaMap(int index, Class<K> keyClazz, Class<V> valueClazz) throws LuaException {
        Map<K, V> map = new LinkedHashMap<>();
        pairs(index, (L) -> {
            map.put(
                    (K) toJavaObject(-2, keyClazz),
                    (V) toJavaObject(-1, valueClazz)
            );
            return false;
        });
        return map;
    }

    public <T> Map<T, T> toJavaMap(int index, Class<T> clazz) throws LuaException {
        return toJavaMap(index, clazz, clazz);
    }

    public void loadString(String script) throws LuaException {
        checkError(C.luaL_loadstring(L, script), false);
    }

    public void loadBuffer(@NonNull Buffer buffer, String name) throws LuaException {
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

    public void loadStringBuffer(String script, String name) throws LuaException {
        checkStack(1);
        checkError(C.luaL_loadbuffer(L, script, script.length(), name), false);
    }

    public void doFile(String filename) throws LuaException {
        checkStack(1);
        checkError(C.luaJ_dofile(L, filename), true);
    }

    public void doString(String script) throws LuaException {
        checkStack(1);
        checkError(C.luaJ_dostring(L, script), true);
    }

    public void doBuffer(@NonNull Buffer buffer, String name) throws LuaException {
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

    public void getMetatable(String typeName) throws LuaException {
        checkStack(1);
        C.luaL_getmetatable(L, typeName);
    }

    public boolean newMetatable(String typeName) throws LuaException {
        checkStack(1);
        return C.luaL_newmetatable(L, typeName) != 0;
    }

    public void openLibraries() throws LuaException {
        checkStack(1);
        C.luaL_openlibs(L);
        C.luaJ_initloader(L);
    }

    public void openLibrary(String name) throws LuaException {
        checkStack(1);
        C.luaJ_openlib(L, name);
    }

    public void openLibrary(@NonNull String... name) throws LuaException {
        for (String n : name) {
            openLibrary(n);
        }
    }

    public Object createProxy(int index, Class<?> interfaces, Conversion degree) throws IllegalArgumentException, LuaException {
        return LuaProxy.newInstance(this, index, interfaces, degree).toProxy();
    }

    public void setExternalLoader(ExternalLoader loader) {
        this.loader = loader;
    }

    public int loadExternal(String module) throws Exception {
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
     */
    public @Nullable Object invokeSpecial(Object object, Method method, @Nullable Object[] params) throws RuntimeException, LuaException {
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
                ClassUtils.getMethodDescriptor(method),
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

    private void appendCustomDescriptor(@NonNull Class<?> type, StringBuilder customSignature) {
        if (type.isPrimitive()) {
            customSignature.append(ClassUtils.getPrimitiveDescriptor(type));
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

    public int refGet(int ref) throws LuaException {
        checkStack(1);
        C.luaJ_refGet(L, ref);
        return 1;
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

    public LuaType refType(int ref) throws LuaException {
        return LuaType.from(C.luaJ_refType(L, ref));
    }

    public int refLength(int ref) {
        return C.luaJ_refLength(L, ref);
    }

    public void refSetMetatable(int ref, String name) {
        C.luaJ_refSetMetatable(L, ref, name);
    }

    public String refLtoString(int ref) {
        return C.luaJ_refLtoString(L, ref);
    }

    public String refToString(int ref) {
        return C.luaJ_refToString(L, ref);
    }

    public boolean refCallMeta(int ref, String name) {
        return C.luaJ_refCallMeta(L, ref, name) != 0;
    }

    public long refGetPointer(int ref) {
        return C.luaJ_refGetPointer(L, ref);
    }

    public int refCopyTo(@NonNull Lua to, int ref) throws LuaException {
        C.luaJ_refCopyTo(L, to.L, ref);
        return 1;
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

    public void registerReference(@NonNull LuaReferable referable) {
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

    // Lua Value
    public LuaNil getLuaNil(int idx) throws LuaException {
        if (type(idx) == LuaType.NIL) {
            return fromNull();
        }
        return null;
    }

    public LuaNil getLuaNil() throws LuaException {
        return getLuaNil(-1);
    }

    public LuaNil getLuaNil(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaNil value = getLuaNil();
        pop(1);
        return value;
    }

    public LuaBoolean getLuaBoolean(int idx) throws LuaException {
        if (type(idx) == LuaType.BOOLEAN) {
            return from(toBoolean(idx));
        }
        return null;
    }

    public LuaBoolean getLuaBoolean() throws LuaException {
        return getLuaBoolean(-1);
    }

    public LuaBoolean getLuaBoolean(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaBoolean value = getLuaBoolean();
        pop(1);
        return value;
    }

    public LuaUserdata getLuaUserdata(int idx) throws LuaException {
        if (type(idx) == LuaType.USERDATA) {
            return new LuaUserdata(this, idx);
        }
        return null;
    }

    public LuaUserdata getLuaUserdata() throws LuaException {
        return getLuaUserdata(-1);
    }

    public LuaUserdata getLuaUserdata(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaUserdata value = getLuaUserdata();
        pop(1);
        return value;
    }

    public LuaNumber getLuaNumber(int idx) throws LuaException {
        if (type(idx) == LuaType.NUMBER) {
            return new LuaNumber(this, idx);
        }
        return null;
    }

    public LuaNumber getLuaNumber() throws LuaException {
        return getLuaNumber(-1);
    }

    public LuaNumber getLuaNumber(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaNumber value = getLuaNumber();
        pop(1);
        return value;
    }

    public LuaString getLuaString(int idx) throws LuaException {
        if (type(idx) == LuaType.STRING) {
            return new LuaString(this, idx);
        }
        return null;
    }

    public LuaString getLuaString() throws LuaException {
        return getLuaString(-1);
    }

    public LuaString getLuaString(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaString value = getLuaString();
        pop(1);
        return value;
    }

    public LuaTable getLuaTable(int idx) throws LuaException {
        if (type(idx) == LuaType.TABLE) {
            return new LuaTable(this, idx);
        }
        return null;
    }

    public LuaTable getLuaTable() throws LuaException {
        return getLuaTable(-1);
    }

    public LuaTable getLuaTable(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaTable value = getLuaTable();
        pop(1);
        return value;
    }

    public LuaFunction getLuaFunction(int idx) throws LuaException {
        if (type(idx) == LuaType.FUNCTION) {
            return new LuaFunction(this, idx);
        }
        return null;
    }

    public LuaFunction getLuaFunction() throws LuaException {
        return getLuaFunction(-1);
    }

    public LuaFunction getLuaFunction(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaFunction value = getLuaFunction();
        pop(1);
        return value;
    }

    public LuaCFunction getLuaCFunction(int idx) {
        if (isCFunction(idx)) {
            return new LuaCFunction(this, idx);
        }
        return null;
    }

    public LuaCFunction getLuaCFunction() {
        return getLuaCFunction(-1);
    }

    public LuaCFunction getLuaCFunction(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaCFunction value = getLuaCFunction();
        pop(1);
        return value;
    }

    public LuaLightUserdata getLuaLightUserdata(int idx) throws LuaException {
        if (type(idx) == LuaType.LIGHTUSERDATA) {
            return new LuaLightUserdata(this, idx);
        }
        return null;
    }

    public LuaLightUserdata getLuaLightUserdata() throws LuaException {
        return getLuaLightUserdata(-1);
    }

    public LuaLightUserdata getLuaLightUserdata(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaLightUserdata value = getLuaLightUserdata();
        pop(1);
        return value;
    }

    public LuaThread getLuaThread(int idx) throws LuaException {
        if (type(idx) == LuaType.THREAD) {
            return new LuaThread(this, idx);
        }
        return null;
    }

    public LuaThread getLuaThread() throws LuaException {
        return getLuaThread(-1);
    }

    public LuaThread getLuaThread(String globalName) throws LuaException {
        getGlobal(globalName);
        LuaThread value = getLuaThread();
        pop(1);
        return value;
    }

    // Check API
    public LuaNil checkLuaNil(int idx) throws LuaException {
        LuaNil value = getLuaNil();
        if (value == null)
            throw new LuaException("Not a nil");
        return value;
    }

    public LuaNil checkLuaNil() throws LuaException {
        return checkLuaNil(-1);
    }

    public LuaNil checkLuaNil(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaNil();
    }

    public LuaBoolean checkLuaBoolean(int idx) throws LuaException {
        LuaBoolean value = getLuaBoolean();
        if (value == null)
            throw new LuaException("Not a boolean");
        return value;
    }

    public LuaBoolean checkLuaBoolean() throws LuaException {
        return checkLuaBoolean(-1);
    }

    public LuaBoolean checkLuaBoolean(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaBoolean();
    }

    public LuaUserdata checkLuaUserdata(int idx) throws LuaException {
        LuaUserdata value = getLuaUserdata();
        if (value == null)
            throw new LuaException("Not a userdata");
        return value;
    }

    public LuaUserdata checkLuaUserdata() throws LuaException {
        return checkLuaUserdata(-1);
    }

    public LuaUserdata checkLuaUserdata(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaUserdata();
    }

    public LuaNumber checkLuaNumber(int idx) throws LuaException {
        LuaNumber value = getLuaNumber();
        if (value == null)
            throw new LuaException("Not a number");
        return value;
    }

    public LuaNumber checkLuaNumber() throws LuaException {
        return checkLuaNumber(-1);
    }

    public LuaNumber checkLuaNumber(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaNumber();
    }

    public LuaString checkLuaString(int idx) throws LuaException {
        LuaString value = getLuaString();
        if (value == null)
            throw new LuaException("Not a string");
        return value;
    }

    public LuaString checkLuaString() throws LuaException {
        return checkLuaString(-1);
    }

    public LuaString checkLuaString(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaString();
    }

    public LuaTable checkLuaTable(int idx) throws LuaException {
        LuaTable value = getLuaTable();
        if (value == null)
            throw new LuaException("Not a table");
        return value;
    }

    public LuaTable checkLuaTable() throws LuaException {
        return checkLuaTable(-1);
    }

    public LuaTable checkLuaTable(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaTable();
    }

    public LuaFunction checkLuaFunction(int idx) throws LuaException {
        LuaFunction value = getLuaFunction();
        if (value == null)
            throw new LuaException("Not a function");
        return value;
    }

    public LuaFunction checkLuaFunction() throws LuaException {
        return checkLuaFunction(-1);
    }

    public LuaFunction checkLuaFunction(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaFunction();
    }

    public LuaCFunction checkLuaCFunction(int idx) throws LuaException {
        LuaCFunction value = getLuaCFunction();
        if (value == null)
            throw new LuaException("Not a C function");
        return value;
    }

    public LuaCFunction checkLuaCFunction() throws LuaException {
        return checkLuaCFunction(-1);
    }

    public LuaCFunction checkLuaCFunction(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaCFunction();
    }

    public LuaLightUserdata checkLuaLightUserdata(int idx) throws LuaException {
        LuaLightUserdata value = getLuaLightUserdata();
        if (value == null)
            throw new LuaException("Not a light userdata");
        return value;
    }

    public LuaLightUserdata checkLuaLightUserdata() throws LuaException {
        return checkLuaLightUserdata(-1);
    }

    public LuaLightUserdata checkLuaLightUserdata(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaLightUserdata();
    }

    public LuaThread checkLuaThread(int idx) throws LuaException {
        LuaThread value = getLuaThread();
        if (value == null)
            throw new LuaException("Not a thread");
        return value;
    }

    public LuaThread checkLuaThread() throws LuaException {
        return checkLuaThread(-1);
    }

    public LuaThread checkLuaThread(String globalName) throws LuaException {
        getGlobal(globalName);
        return checkLuaThread();
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
