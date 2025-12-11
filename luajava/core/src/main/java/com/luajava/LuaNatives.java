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

import java.nio.Buffer;

/**
 * Provides raw JNI bindings to the Lua C API, including Lua, LuaJIT, and LuaJava-specific extensions.
 * <p>
 * This class contains a comprehensive set of native methods that map directly to functions
 * in the Lua C library. It is responsible for loading the native library and serves as the
 * lowest-level interface between Java and the Lua state.
 * <p>
 * All methods that operate on a Lua state take a {@code long ptr} as their first argument, which is
 * the memory address of the {@code lua_State} struct.
 * <p>
 * This class is not intended to be instantiated.
 */
public class LuaNatives {
    private static LuaNatives INSTANCE;

    private LuaNatives() {
        System.loadLibrary("luajava");
    }

    public static LuaNatives getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LuaNatives();
            initBindings();
        }
        return INSTANCE;
    }

    public static native int initBindings();

    // luaJ_* extensions (custom LuaJava functions)
    public native long luaJ_newstate();

    public native int luaJ_initloader(long ptr);

    public native void luaJ_pushbuffer(long ptr, Object obj_buffer, int size);

    public native void luaJ_pushobject(long ptr, Object obj);

    public native int luaJ_isobject(long ptr, int index);

    public native Object luaJ_toobject(long ptr, int index);

    public native Object luaJ_checkobject(long ptr, int index);

    public native void luaJ_pushclass(long ptr, Object obj);

    public native void luaJ_pusharray(long ptr, Object array);

    public native void luaJ_pushfunction(long ptr, Object func);

    public native void luaJ_pushcclosure(long ptr, Object func, int n);

    public native String luaJ_tostring(long ptr, int index);

    public native int luaJ_loadbuffer(long ptr, Buffer buffer, int size, String name);

    public native Buffer luaJ_dump(long ptr);

    public native Object luaJ_tobuffer(long ptr, int index);

    public native Object luaJ_todirectbuffer(long ptr, int index);

    public native void luaJ_openlib(long ptr, String lib);

    public native int luaJ_compare(long ptr, int index1, int index2, int opc);

    public native long luaJ_newthread(long ptr, int lid);

    public native int luaJ_invokespecial(long ptr, Class<?> clazz, String method, String sig, Object obj, String params);

    public native void luaJ_gc(long ptr);

    public native int luaJ_copy(long ptr, long ptr1);

    public native int luaJ_xpcall(long ptr, int nargs, int nresults);

    public native int luaJ_pcall(long ptr, int nargs, int nresults, int errfunc);

    public native int luaJ_dofile(long ptr, String filename);

    public native int luaJ_dostring(long ptr, String string);

    public native int luaJ_dobuffer(long ptr, Buffer buffer, int size, String name);

    public native String luaJ_dumpstack(long ptr);

    // nexlua.h binding
    public native int luaJ_refsafe(long ptr, int idx);

    public native void luaJ_refGet(long ptr, int ref);

    public native void luaJ_unRef(long ptr, int ref);

    public native int luaJ_ref(long ptr);

    public native int luaJ_refType(long ptr, int ref);

    public native int luaJ_refLength(long ptr, int ref);

    public native void luaJ_refSetMetatable(long ptr, int ref, String name);

    public native String luaJ_refLtoString(long ptr, int ref);

    public native String luaJ_refToString(long ptr, int ref);

    public native int luaJ_refCallMeta(long ptr, int ref, String name);

    public native long luaJ_refGetPointer(long ptr, int ref);

    public native void luaJ_refCopyTo(long ptr, long ptr1, int ref);

    public native int luaJ_loadstringbuffer(long ptr, byte[] buff, long sz, String name);

    // lua.h bindings
    public native void lua_close(long ptr);

    public native long lua_newthread(long ptr);

    public native int lua_gettop(long ptr);

    public native void lua_settop(long ptr, int idx);

    public native void lua_pushvalue(long ptr, int idx);

    public native void lua_remove(long ptr, int idx);

    public native void lua_insert(long ptr, int idx);

    public native void lua_replace(long ptr, int idx);

    public native int lua_checkstack(long ptr, int sz);

    public native void lua_xmove(long ptr, long ptr1, int n);

    public native int lua_isnumber(long ptr, int idx);

    public native int lua_isstring(long ptr, int idx);

    public native int lua_iscfunction(long ptr, int idx);

    public native int lua_isuserdata(long ptr, int idx);

    public native int lua_type(long ptr, int idx);

    public native String lua_typename(long ptr, int tp);

    public native int lua_equal(long ptr, int idx1, int idx2);

    public native int lua_rawequal(long ptr, int idx1, int idx2);

    public native int lua_lessthan(long ptr, int idx1, int idx2);

    public native double lua_tonumber(long ptr, int idx);

    public native long lua_tointeger(long ptr, int idx);

    public native int lua_toboolean(long ptr, int idx);

    public native int lua_objlen(long ptr, int idx);

    public native long lua_tocfunction(long ptr, int idx);

    public native long lua_touserdata(long ptr, int idx);

    public native long lua_tothread(long ptr, int idx);

    public native long lua_topointer(long ptr, int idx);

    public native void lua_pushnil(long ptr);

    public native void lua_pushnumber(long ptr, double n);

    public native void lua_pushinteger(long ptr, long n);

    public native void lua_pushstring(long ptr, String string);

    // use lua_pushstring instead
    // public native void lua_pushlstring(long ptr, String string, long len);

    public native void lua_pushcclosure(long ptr, long cfunc, int n);

    public native void lua_pushboolean(long ptr, int b);

    public native void lua_pushlightuserdata(long ptr, long udata);

    public native int lua_pushthread(long ptr);

    public native void lua_gettable(long ptr, int idx);

    public native void lua_getfield(long ptr, int idx, String k);

    public native void lua_rawget(long ptr, int idx);

    public native void lua_rawgeti(long ptr, int idx, int n);

    public native void lua_createtable(long ptr, int narr, int nrec);

    public native long lua_newuserdata(long ptr, int sz);

    public native int lua_getmetatable(long ptr, int objindex);

    public native void lua_getfenv(long ptr, int idx);

    public native void lua_settable(long ptr, int idx);

    public native void lua_setfield(long ptr, int idx, String k);

    public native void lua_rawset(long ptr, int idx);

    public native void lua_rawseti(long ptr, int idx, int n);

    public native int lua_setmetatable(long ptr, int idx);

    public native int lua_setfenv(long ptr, int idx);

    public native void lua_call(long ptr, int nargs, int nresults);

    // Use luaJ_pcall instead
    public native int lua_pcall(long ptr, int nargs, int nresults, int errfunc);

    public native int lua_cpcall(long ptr, long cfunc, long ud);

    public native int lua_yield(long ptr, int nresults);

    public native int lua_resume(long ptr, int nargs);

    public native int lua_status(long ptr);

    public native int lua_gc(long ptr, int what, int data);

    public native int lua_error(long ptr);

    public native int lua_next(long ptr, int idx);

    public native void lua_concat(long ptr, int n);

    public native void lua_pop(long ptr, int n);

    public native void lua_newtable(long ptr);

    public native void lua_register(long ptr, String name, long cfunction);

    public native void lua_pushcfunction(long ptr, long cfunction);

    public native long lua_strlen(long ptr, int idx);

    public native int lua_isfunction(long ptr, int idx);

    public native int lua_istable(long ptr, int idx);

    public native int lua_islightuserdata(long ptr, int idx);

    public native int lua_isnil(long ptr, int idx);

    public native int lua_isboolean(long ptr, int idx);

    public native int lua_isthread(long ptr, int idx);

    public native int lua_isnone(long ptr, int idx);

    public native int lua_isnoneornil(long ptr, int idx);

    public native void lua_setglobal(long ptr, String name);

    public native void lua_getglobal(long ptr, String name);

    public native String lua_tostring(long ptr, int idx);

    public native long lua_open();

    public native void lua_getregistry(long ptr);

    public native int lua_getgccount(long ptr);

    public native long lua_upvalueid(long ptr, int idx, int n);

    public native void lua_upvaluejoin(long ptr, int idx1, int n1, int idx2, int n2);

    public native double lua_version(long ptr);

    public native void lua_copy(long ptr, int fromidx, int toidx);

    public native int lua_isyieldable(long ptr);

    // lauxlib.h bindings
    public native int luaL_getmetafield(long ptr, int obj, String e);

    public native int luaL_callmeta(long ptr, int obj, String e);

    public native int luaL_typerror(long ptr, int narg, String tname);

    public native int luaL_argerror(long ptr, int numarg, String extramsg);

    public native double luaL_checknumber(long ptr, int numArg);

    public native double luaL_optnumber(long ptr, int nArg, double def);

    public native long luaL_checkinteger(long ptr, int numArg);

    public native long luaL_optinteger(long ptr, int nArg, long def);

    public native void luaL_checkstack(long ptr, int sz, String msg);

    public native void luaL_checktype(long ptr, int narg, int t);

    public native void luaL_checkany(long ptr, int narg);

    public native int luaL_newmetatable(long ptr, String tname);

    public native long luaL_checkudata(long ptr, int ud, String tname);

    public native void luaL_where(long ptr, int lvl);

    public native int luaL_ref(long ptr, int t);

    public native void luaL_unref(long ptr, int t, int ref);

    public native int luaL_loadfile(long ptr, String filename);

    // Recommend using luaJ_loadbuffer instead
    public native int luaL_loadbuffer(long ptr, String buff, long sz, String name);

    public native int luaL_loadstring(long ptr, String s);

    public native long luaL_newstate();

    public native String luaL_gsub(long ptr, String s, String p, String r);

    public native String luaL_findtable(long ptr, int idx, String fname, int szhint);

    public native int luaL_fileresult(long ptr, int stat, String fname);

    public native int luaL_execresult(long ptr, int stat);

    public native int luaL_loadfilex(long ptr, String filename, String mode);

    public native int luaL_loadbufferx(long ptr, String buff, long sz, String name, String mode);

    public native void luaL_traceback(long ptr, long ptr1, String msg, int level);

    public native long luaL_testudata(long ptr, int ud, String tname);

    public native void luaL_setmetatable(long ptr, String tname);

    public native String luaL_checkstring(long ptr, int n);

    public native String luaL_optstring(long ptr, int n, String def);

    public native int luaL_checkint(long ptr, int n);

    public native int luaL_optint(long ptr, int n, int d);

    public native long luaL_checklong(long ptr, int n);

    public native long luaL_optlong(long ptr, int n, long d);

    public native String luaL_typename(long ptr, int i);

    public native int luaL_dofile(long ptr, String filename);

    public native int luaL_dostring(long ptr, String s);

    public native void luaL_getmetatable(long ptr, String n);

    // lualib.h bindings
    public native int luaopen_base(long ptr);

    public native int luaopen_math(long ptr);

    public native int luaopen_string(long ptr);

    public native int luaopen_table(long ptr);

    public native int luaopen_io(long ptr);

    public native int luaopen_os(long ptr);

    public native int luaopen_package(long ptr);

    public native int luaopen_debug(long ptr);

    public native int luaopen_bit(long ptr);

    public native int luaopen_jit(long ptr);

    public native int luaopen_ffi(long ptr);

    public native int luaopen_string_buffer(long ptr);

    public native void luaL_openlibs(long ptr);

    // luajit.h bindings
    public native int luaJIT_setmode(long ptr, int idx, int mode);

//    public native void luaJIT_profile_stop(long ptr);
//
//    public native String luaJIT_profile_dumpstack(long ptr, String format, int depth);
}