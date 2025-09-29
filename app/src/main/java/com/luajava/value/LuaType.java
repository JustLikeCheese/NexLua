package com.luajava.value;

import com.luajava.LuaConsts;
import com.luajava.LuaException;

/**
 * Lua data types
 */
public enum LuaType {
    NONE,
    NIL,
    BOOLEAN,
    LIGHTUSERDATA,
    NUMBER,
    STRING,
    TABLE,
    FUNCTION,
    USERDATA,
    THREAD;

    public static LuaType from(int code) {
        switch (code) {
            case LuaConsts.LUA_TNONE:
                return NONE;
            case LuaConsts.LUA_TNIL:
                return NIL;
            case LuaConsts.LUA_TBOOLEAN:
                return BOOLEAN;
            case LuaConsts.LUA_TLIGHTUSERDATA:
                return LIGHTUSERDATA;
            case LuaConsts.LUA_TNUMBER:
                return NUMBER;
            case LuaConsts.LUA_TSTRING:
                return STRING;
            case LuaConsts.LUA_TTABLE:
                return TABLE;
            case LuaConsts.LUA_TFUNCTION:
                return FUNCTION;
            case LuaConsts.LUA_TUSERDATA:
                return USERDATA;
            case LuaConsts.LUA_TTHREAD:
                return THREAD;
            default:
                throw new LuaException(LuaException.LuaError.RUNTIME, "Unrecognized type code: " + code);
        }
    }

    public int toInt() {
        switch (this) {
            case NONE:
                return LuaConsts.LUA_TNONE;
            case NIL:
                return LuaConsts.LUA_TNIL;
            case BOOLEAN:
                return LuaConsts.LUA_TBOOLEAN;
            case LIGHTUSERDATA:
                return LuaConsts.LUA_TLIGHTUSERDATA;
            case NUMBER:
                return LuaConsts.LUA_TNUMBER;
            case STRING:
                return LuaConsts.LUA_TSTRING;
            case TABLE:
                return LuaConsts.LUA_TTABLE;
            case FUNCTION:
                return LuaConsts.LUA_TFUNCTION;
            case USERDATA:
                return LuaConsts.LUA_TUSERDATA;
            case THREAD:
                return LuaConsts.LUA_TTHREAD;
            default:
                throw new LuaException(LuaException.LuaError.RUNTIME, "Unrecognized type: " + this);
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
