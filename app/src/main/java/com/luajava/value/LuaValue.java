package com.luajava.value;

import com.luajava.Lua;

import java.nio.Buffer;
import java.util.List;
import java.util.Map;

public interface LuaValue {
    // stack
    Lua state();

    void push();

    void push(Lua L);

    LuaValue copyTo(Lua L);

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

    Map<LuaValue, LuaValue> toMap();

    List<LuaValue> toList();

    long length();

    // metatable
    LuaValue getMetatable();

    void setMetatable(String tname);

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

    LuaValue[] call(Object...args);

    LuaValue[] call(Lua.Conversion degree, Object...args);

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

    long toInteger();

    Object toJavaObject();

    Buffer toBuffer();

    Buffer dump();
}