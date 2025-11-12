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

    public static LuaType from(int code) throws LuaException {
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

    public int toInt() throws LuaException {
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
