#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "luakit.h"
#include "jnihelper.h"

/*
 * JNIWRAP(returnType, methodName, methodArgs..)
 */

#define L ((lua_State *) ptr)
#define L1 ((lua_State *) ptr1)
#define JNIWRAP(RET, NAME, ...) \
    JNIEXPORT RET JNICALL Java_com_luajava_LuaNatives_##NAME(JNIEnv* env, jobject thiz, ##__VA_ARGS__)

#define JNIWRAP_STATIC(RET, NAME, ...) \
    JNIEXPORT RET JNICALL Java_com_luajava_LuaNatives_##NAME(JNIEnv* env, jclass thiz, ##__VA_ARGS__)

// luaJ_* extensions (unchanged)
JNIWRAP_STATIC(jint, initBindings) {
    return initJNIBindings(env);
}

// lua.h
// state manipulation
// #define lua_upvalueindex(i)	(LUA_GLOBALSINDEX-(i))
// LUA_API lua_State *(lua_newstate) (lua_Alloc f, void *ud);
JNIWRAP(void, lua_1close, jlong ptr) {
    lua_close(L);
}

JNIWRAP(jlong, lua_1newthread, jlong ptr) {
    return (jlong) lua_newthread(L);
}

// LUA_API lua_CFunction (lua_atpanic) (lua_State *L, lua_CFunction panicf);

// basic stack manipulation
JNIWRAP(jint, lua_1gettop, jlong ptr) {
    return (jint) lua_gettop(L);
}

JNIWRAP(void, lua_1settop, jlong ptr, jint idx) {
    lua_settop(L, idx);
}

JNIWRAP(void, lua_1pushvalue, jlong ptr, jint idx) {
    lua_pushvalue(L, idx);
}

JNIWRAP(void, lua_1remove, jlong ptr, jint idx) {
    lua_remove(L, idx);
}

JNIWRAP(void, lua_1insert, jlong ptr, jint idx) {
    lua_insert(L, idx);
}

JNIWRAP(void, lua_1replace, jlong ptr, jint idx) {
    lua_replace(L, idx);
}

JNIWRAP(jint, lua_1checkstack, jlong ptr, jint sz) {
    return lua_checkstack(L, sz);
}

JNIWRAP(void, lua_1xmove, jlong ptr, jlong ptr1, jint n) {
    lua_xmove(L, L1, n);
}

// access functions (stack -> C)
JNIWRAP(jint, lua_1isnumber, jlong ptr, jint idx) {
    return (jint) lua_isnumber(L, idx);
}

JNIWRAP(jint, lua_1isstring, jlong ptr, jint idx) {
    return (jint) lua_isstring(L, idx);
}

JNIWRAP(jint, lua_1iscfunction, jlong ptr, jint idx) {
    return (jint) lua_iscfunction(L, idx);
}

JNIWRAP(jint, lua_1isuserdata, jlong ptr, jint idx) {
    return (jint) lua_isuserdata(L, idx);
}

JNIWRAP(jint, lua_1type, jlong ptr, jint idx) {
    return (jint) lua_type(L, idx);
}

JNIWRAP(jstring, lua_1typename, jlong ptr, jint tp) {
    const char *name = lua_typename(L, tp);
    return ToString(name);
}

// equal functions
JNIWRAP(jint, lua_1equal, jlong ptr, jint idx1, jint idx2) {
    return (jint) lua_equal(L, idx1, idx2);
}

JNIWRAP(jint, lua_1rawequal, jlong ptr, jint idx1, jint idx2) {
    return (jint) lua_rawequal(L, idx1, idx2);
}

JNIWRAP(jint, lua_1lessthan, jlong ptr, jint idx1, jint idx2) {
    return (jint) lua_lessthan(L, idx1, idx2);
}

// convert functions
JNIWRAP(jdouble, lua_1tonumber, jlong ptr, jint idx) {
    return (jdouble) lua_tonumber(L, idx);
}

JNIWRAP(jlong, lua_1tointeger, jlong ptr, jint idx) {
    return (jlong) lua_tointeger(L, idx);
}

JNIWRAP(jint, lua_1toboolean, jlong ptr, jint idx) {
    return (jint) lua_toboolean(L, idx);
}

JNIWRAP(jint, lua_1objlen, jlong ptr, jint idx) {
    return (jint) lua_objlen(L, idx);
}

JNIWRAP(jlong, lua_1tocfunction, jlong ptr, jint idx) {
    return (jlong) lua_tocfunction(L, idx);
}

JNIWRAP(jlong, lua_1touserdata, jlong ptr, jint idx) {
    return (jlong) lua_touserdata(L, idx);
}

JNIWRAP(jlong, lua_1tothread, jlong ptr, jint idx) {
    return (jlong) lua_tothread(L, idx);
}

JNIWRAP(jlong, lua_1topointer, jlong ptr, jint idx) {
    return (jlong) lua_topointer(L, idx);
}

// push functions (C -> stack)
JNIWRAP(void, lua_1pushnil, jlong ptr) {
    lua_pushnil(L);
}

JNIWRAP(void, lua_1pushnumber, jlong ptr, jdouble n) {
    lua_pushnumber(L, n);
}

JNIWRAP(void, lua_1pushinteger, jlong ptr, jlong n) {
    lua_pushinteger(L, (lua_Integer) n);
}

JNIWRAP(void, lua_1pushstring, jlong ptr, jstring string) {
    const char *c_string = GetString(string);
    lua_pushstring(L, c_string);
    ReleaseString(string, c_string);
}

// LUA_API const char *(lua_pushvfstring) (lua_State *L, const char *fmt, va_list argp);
// LUA_API const char *(lua_pushfstring) (lua_State *L, const char *fmt, ...);

//JNIWRAP(void, lua_1pushlstring, jlong ptr, jstring string, jlong len) {
//    const char *c_string = GetString(string);
//    lua_pushlstring(L, c_string, len);
//    ReleaseString(string, c_string);
//}

JNIWRAP(void, lua_1pushcclosure, jlong ptr, jlong cfunc, jint n) {
    lua_pushcclosure(L, (lua_CFunction) cfunc, n);
}

JNIWRAP(void, lua_1pushboolean, jlong ptr, jint b) {
    lua_pushboolean(L, b);
}

JNIWRAP(void, lua_1pushlightuserdata, jlong ptr, jlong udata) {
    lua_pushlightuserdata(L, (void *) udata);
}

JNIWRAP(jint, lua_1pushthread, jlong ptr) {
    return (jint) lua_pushthread(L);
}

// get functions (Lua -> stack)
JNIWRAP(void, lua_1gettable, jlong ptr, jint idx) {
    lua_gettable(L, idx);
}

JNIWRAP(void, lua_1getfield, jlong ptr, jint idx, jstring k) {
    const char *c_k = GetString(k);
    lua_getfield(L, idx, c_k);
    ReleaseString(k, c_k);
}

JNIWRAP(void, lua_1rawget, jlong ptr, jint idx) {
    lua_rawget(L, idx);
}

JNIWRAP(void, lua_1rawgeti, jlong ptr, jint idx, jint n) {
    lua_rawgeti(L, idx, n);
}

JNIWRAP(void, lua_1createtable, jlong ptr, jint narr, jint nrec) {
    lua_createtable(L, narr, nrec);
}

JNIWRAP(jlong, lua_1newuserdata, jlong ptr, jint sz) {
    return (jlong) lua_newuserdata(L, sz);
}

JNIWRAP(jint, lua_1getmetatable, jlong ptr, jint objindex) {
    return lua_getmetatable(L, objindex);
}

JNIWRAP(void, lua_1getfenv, jlong ptr, jint idx) {
    lua_getfenv(L, idx);
}

// set functions (stack -> Lua)
JNIWRAP(void, lua_1settable, jlong ptr, jint idx) {
    lua_settable(L, idx);
}

JNIWRAP(void, lua_1setfield, jlong ptr, jint idx, jstring k) {
    const char *c_k = GetString(k);
    lua_setfield(L, idx, c_k);
    ReleaseString(k, c_k);
}

JNIWRAP(void, lua_1rawset, jlong ptr, jint idx) {
    lua_rawset(L, idx);
}

JNIWRAP(void, lua_1rawseti, jlong ptr, jint idx, jint n) {
    lua_rawseti(L, idx, n);
}

JNIWRAP(jint, lua_1setmetatable, jlong ptr, jint idx) {
    return lua_setmetatable(L, idx);
}

JNIWRAP(jint, lua_1setfenv, jlong ptr, jint idx) {
    return lua_setfenv(L, idx);
}

JNIWRAP(void, lua_1call, jlong ptr, jint nargs, jint nresults) {
    lua_call(L, nargs, nresults);
}

JNIWRAP(jint, lua_1pcall, jlong ptr, jint nargs, jint nresults, jint errfunc) {
    return (jint) lua_pcall(L, nargs, nresults, errfunc);
}

JNIWRAP(jint, lua_1cpcall, jlong ptr, jlong cfunc, jlong ud) {
    return (jint) lua_cpcall(L, (lua_CFunction) cfunc, (void *) ud);
}

// LUA_API int   (lua_load) (lua_State *L, lua_Reader reader, void *dt, const char *chunkname);
// LUA_API int (lua_dump) (lua_State *L, lua_Writer writer, void *data);

// coroutine functions
JNIWRAP(jint, lua_1yield, jlong ptr, jint nresults) {
    return (jint) lua_yield(L, nresults);
}

JNIWRAP(jint, lua_1resume, jlong ptr, jint nargs) {
    return (jint) lua_resume(L, nargs);
}

JNIWRAP(jint, lua_1status, jlong ptr) {
    return (jint) lua_status(L);
}

// garbage-collection
JNIWRAP(jint, lua_1gc, jlong ptr, jint what, jint data) {
    return lua_gc(L, what, data);
}

// miscellaneous functions
JNIWRAP(jint, lua_1error, jlong ptr) {
    return (jint) lua_error(L);
}

JNIWRAP(jint, lua_1next, jlong ptr, jint idx) {
    return (jint) lua_next(L, idx);
}

JNIWRAP(void, lua_1concat, jlong ptr, jint n) {
    lua_concat(L, n);
}

// LUA_API lua_Alloc (lua_getallocf) (lua_State *L, void **ud);
// LUA_API void lua_setallocf (lua_State *L, lua_Alloc f, void *ud);

// some useful macros
JNIWRAP(void, lua_1pop, jlong ptr, jint n) {
    lua_pop(L, n);
}

JNIWRAP(void, lua_1newtable, jlong ptr) {
    lua_newtable(L);
}

JNIWRAP(void, lua_1register, jlong ptr, jstring name, jlong cfunction) {
    const char *c_name = GetString(name);
    lua_register(L, c_name, (lua_CFunction) cfunction);
    ReleaseString(name, c_name);
}

JNIWRAP(void, lua_1pushcfunction, jlong ptr, jlong cfunction) {
    lua_pushcfunction(L, (lua_CFunction) cfunction);
}

JNIWRAP(jlong, lua_1strlen, jlong ptr, jint idx) {
    return (jlong) lua_strlen(L, idx);
}

JNIWRAP(jint, lua_1isfunction, jlong ptr, jint idx) {
    return (jint) lua_isfunction(L, idx);
}

JNIWRAP(jint, lua_1istable, jlong ptr, jint idx) {
    return (jint) lua_istable(L, idx);
}

JNIWRAP(jint, lua_1islightuserdata, jlong ptr, jint idx) {
    return (jint) lua_islightuserdata(L, idx);
}

JNIWRAP(jint, lua_1isnil, jlong ptr, jint idx) {
    return (jint) lua_isnil(L, idx);
}

JNIWRAP(jint, lua_1isboolean, jlong ptr, jint idx) {
    return (jint) lua_isboolean(L, idx);
}

JNIWRAP(jint, lua_1isthread, jlong ptr, jint idx) {
    return (jint) lua_isthread(L, idx);
}

JNIWRAP(jint, lua_1isnone, jlong ptr, jint idx) {
    return (jint) lua_isnone(L, idx);
}

JNIWRAP(jint, lua_1isnoneornil, jlong ptr, jint idx) {
    return (jint) lua_isnoneornil(L, idx);
}

JNIWRAP(void, lua_1setglobal, jlong ptr, jstring j_name) {
    const char *name = GetString(j_name);
    lua_setglobal(L, name);
    ReleaseString(j_name, name);
}

JNIWRAP(void, lua_1getglobal, jlong ptr, jstring j_name) {
    const char *name = GetString(j_name);
    lua_getglobal(L, name);
    ReleaseString(j_name, name);
}

JNIWRAP(jstring, lua_1tostring, jlong ptr, jint idx) {
    const char *s = lua_tostring(L, idx);
    return ToString(s);
}

JNIWRAP(jlong, lua_1open) {
    return (jlong) luaL_newstate();
}

JNIWRAP(void, lua_1getregistry, jlong ptr) {
    lua_getregistry(L);
}

JNIWRAP(jint, lua_1getgccount, jlong ptr) {
    return lua_getgccount(L);
}

// Debug API
// LUA_API int lua_getstack (lua_State *L, int level, lua_Debug *ar);
// LUA_API int lua_getinfo (lua_State *L, const char *what, lua_Debug *ar);
// LUA_API const char *lua_getlocal (lua_State *L, const lua_Debug *ar, int n);
// LUA_API const char *lua_setlocal (lua_State *L, const lua_Debug *ar, int n);
// LUA_API const char *lua_getupvalue (lua_State *L, int funcindex, int n);
// LUA_API const char *lua_setupvalue (lua_State *L, int funcindex, int n);
// LUA_API int lua_sethook (lua_State *L, lua_Hook func, int mask, int count);
// LUA_API lua_Hook lua_gethook (lua_State *L);
// LUA_API int lua_gethookmask (lua_State *L);
// LUA_API int lua_gethookcount (lua_State *L);

// From Lua 5.2+ (in LuaJIT)
JNIWRAP(jlong, lua_1upvalueid, jlong ptr, jint idx, jint n) {
    return (jlong) (intptr_t) lua_upvalueid(L, idx, n);
}

JNIWRAP(void, lua_1upvaluejoin, jlong ptr, jint idx1, jint n1, jint idx2, jint n2) {
    lua_upvaluejoin(L, idx1, n1, idx2, n2);
}

// LUA_API int lua_loadx (lua_State *L, lua_Reader reader, void *dt, const char *chunkname, const char *mode);

JNIWRAP(jdouble, lua_1version, jlong ptr) {
    const lua_Number *version = lua_version(L);
    return version ? *version : 0;
}

JNIWRAP(void, lua_1copy, jlong ptr, jint fromidx, jint toidx) {
    lua_copy(L, fromidx, toidx);
}

// LUA_API lua_Number lua_tonumberx (lua_State *L, int idx, int *isnum);
// LUA_API lua_Integer lua_tointegerx (lua_State *L, int idx, int *isnum);

JNIWRAP(jint, lua_1isyieldable, jlong ptr) {
    return (jint) lua_isyieldable(L);
}

// lauxlib.h
// LUALIB_API void (luaL_openlib) (lua_State *L, const char *libname, const luaL_Reg *l, int nup);
// LUALIB_API void (luaL_register) (lua_State *L, const char *libname, const luaL_Reg *l);
JNIWRAP(jint, luaL_1getmetafield, jlong ptr, jint obj, jstring e) {
    const char *c_e = GetString(e);
    int result = luaL_getmetafield(L, obj, c_e);
    ReleaseString(e, c_e);
    return result;
}

JNIWRAP(jint, luaL_1callmeta, jlong ptr, jint obj, jstring e) {
    const char *c_e = GetString(e);
    int result = luaL_callmeta(L, obj, c_e);
    ReleaseString(e, c_e);
    return result;
}

JNIWRAP(jint, luaL_1typerror, jlong ptr, jint narg, jstring tname) {
    const char *c_tname = GetString(tname);
    int result = luaL_typerror(L, narg, c_tname);
    ReleaseString(tname, c_tname);
    return result;
}

JNIWRAP(jint, luaL_1argerror, jlong ptr, jint numarg, jstring extramsg) {
    const char *c_extramsg = GetString(extramsg);
    int result = luaL_argerror(L, numarg, c_extramsg);
    ReleaseString(extramsg, c_extramsg);
    return result;
}

// LUALIB_API const char *(luaL_checklstring) (lua_State *L, int numArg, size_t *l);
// LUALIB_API const char *(luaL_optlstring) (lua_State *L, int numArg, const char *def, size_t *l);

// number
JNIWRAP(jdouble, luaL_1checknumber, jlong ptr, jint numArg) {
    return (jdouble) luaL_checknumber(L, numArg);
}

JNIWRAP(jdouble, luaL_1optnumber, jlong ptr, jint nArg, jdouble def) {
    return (jdouble) luaL_optnumber(L, nArg, def);
}

// integer
JNIWRAP(jlong, luaL_1checkinteger, jlong ptr, jint numArg) {
    return (jlong) luaL_checkinteger(L, numArg);
}

JNIWRAP(jlong, luaL_1optinteger, jlong ptr, jint nArg, jlong def) {
    return (jlong) luaL_optinteger(L, nArg, (lua_Integer) def);
}

// check
JNIWRAP(void, luaL_1checkstack, jlong ptr, jint sz, jstring msg) {
    const char *c_msg = GetString(msg);
    luaL_checkstack(L, sz, c_msg);
    ReleaseString(msg, c_msg);
}

JNIWRAP(void, luaL_1checktype, jlong ptr, jint narg, jint t) {
    luaL_checktype(L, narg, t);
}

JNIWRAP(void, luaL_1checkany, jlong ptr, jint narg) {
    luaL_checkany(L, narg);
}

JNIWRAP(jint, luaL_1newmetatable, jlong ptr, jstring tname) {
    const char *c_tname = GetString(tname);
    jint result = luaL_newmetatable(L, c_tname);
    ReleaseString(tname, c_tname);
    return result;
}

JNIWRAP(jlong, luaL_1checkudata, jlong ptr, jint ud, jstring tname) {
    const char *c_tname = GetString(tname);
    void *result = luaL_checkudata(L, ud, c_tname);
    ReleaseString(tname, c_tname);
    return (jlong) (intptr_t) result;
}

JNIWRAP(void, luaL_1where, jlong ptr, jint lvl) {
    luaL_where(L, lvl);
}

// LUALIB_API int (luaL_checkoption) (lua_State *L, int narg, const char *def, const char *const lst[]);

JNIWRAP(jint, luaL_1ref, jlong ptr, jint t) {
    return (jint) luaL_ref(L, t);
}

JNIWRAP(void, luaL_1unref, jlong ptr, jint t, jint ref) {
    luaL_unref(L, t, ref);
}

JNIWRAP(jint, luaL_1loadfile, jlong ptr, jstring filename) {
    const char *c_filename = GetString(filename);
    int result = luaL_loadfile(L, c_filename);
    ReleaseString(filename, c_filename);
    return result;
}

// NOTE: recommend use luaJ_loadbuffer instead
JNIWRAP(jint, luaL_1loadbuffer, jlong ptr, jstring buff, jlong sz, jstring name) {
    const char *c_buff = GetString(buff);
    const char *c_name = GetString(name);
    int result = luaL_loadbuffer(L, c_buff, (size_t) sz, c_name);
    ReleaseString(buff, c_buff);
    ReleaseString(name, c_name);
    return result;
}

JNIWRAP(jint, luaL_1loadstring, jlong ptr, jstring s) {
    const char *c_s = GetString(s);
    int result = luaL_loadstring(L, c_s);
    ReleaseString(s, c_s);
    return result;
}

JNIWRAP(jlong, luaL_1newstate) {
    return (jlong) luaL_newstate();
}

JNIWRAP(jstring, luaL_1gsub, jlong ptr, jstring s, jstring p, jstring r) {
    const char *c_s = GetString(s);
    const char *c_p = GetString(p);
    const char *c_r = GetString(r);
    const char *result = luaL_gsub(L, c_s, c_p, c_r);
    ReleaseString(s, c_s);
    ReleaseString(p, c_p);
    ReleaseString(r, c_r);
    return ToString(result);
}

JNIWRAP(jstring, luaL_1findtable, jlong ptr, jint idx, jstring fname, jint szhint) {
    const char *c_fname = GetString(fname);
    const char *result = luaL_findtable(L, idx, c_fname, szhint);
    ReleaseString(fname, c_fname);
    return ToString(result);
}

// From Lua 5.2.
JNIWRAP(jint, luaL_1fileresult, jlong ptr, jint stat, jstring fname) {
    const char *c_fname = GetString(fname);
    int result = luaL_fileresult(L, stat, c_fname);
    ReleaseString(fname, c_fname);
    return (jint) result;
}

JNIWRAP(jint, luaL_1execresult, jlong ptr, jint stat) {
    return (jint) luaL_execresult(L, stat);
}

JNIWRAP(jint, luaL_1loadfilex, jlong ptr, jstring filename, jstring mode) {
    const char *c_filename = GetString(filename);
    const char *c_mode = GetString(mode);
    int result = luaL_loadfilex(L, c_filename, c_mode);
    ReleaseString(filename, c_filename);
    ReleaseString(mode, c_mode);
    return result;
}

// NOTE: recommend use luaJ_loadbufferx instead
JNIWRAP(jint, luaL_1loadbufferx, jlong ptr, jstring buff, jlong sz, jstring name, jstring mode) {
    const char *c_buff = GetString(buff);
    const char *c_name = GetString(name);
    const char *c_mode = GetString(mode);
    int result = luaL_loadbufferx(L, c_buff, (size_t) sz, c_name, c_mode);
    ReleaseString(buff, c_buff);
    ReleaseString(name, c_name);
    ReleaseString(mode, c_mode);
    return result;
}

JNIWRAP(void, luaL_1traceback, jlong ptr, jlong ptr1, jstring msg, jint level) {
    const char *c_msg = GetString(msg);
    luaL_traceback(L, L1, c_msg, level);
    ReleaseString(msg, c_msg);
}

// LUALIB_API void (luaL_setfuncs) (lua_State *L, const luaL_Reg *l, int nup);
// LUALIB_API void (luaL_pushmodule) (lua_State *L, const char *modname, int sizehint);

JNIWRAP(jlong, luaL_1testudata, jlong ptr, jint ud, jstring tname) {
    const char *c_tname = GetString(tname);
    void *res = luaL_testudata(L, ud, c_tname);
    ReleaseString(tname, c_tname);
    return (jlong) (intptr_t) res;
}

JNIWRAP(void, luaL_1setmetatable, jlong ptr, jstring tname) {
    const char *c_tname = GetString(tname);
    luaL_setmetatable(L, c_tname);
    ReleaseString(tname, c_tname);
}

// some useful macros
// #define luaL_argcheck(L, cond,numarg,extramsg)	\
//		 ((void)((cond) || luaL_argerror(L, (numarg), (extramsg))))
JNIWRAP(jstring, luaL_1checkstring, jlong ptr, jint n) {
    const char *s = luaL_checkstring(L, n);
    return ToString(s);
}

JNIWRAP(jstring, luaL_1optstring, jlong ptr, jint n, jstring def) {
    const char *c_def = GetString(def);
    const char *s = luaL_optstring(L, n, c_def);
    ReleaseString(def, c_def);
    return ToString(s);
}

JNIWRAP(jint, luaL_1checkint, jlong ptr, jint n) {
    return (jint) luaL_checkint(L, n);
}

JNIWRAP(jint, luaL_1optint, jlong ptr, jint n, jint d) {
    return (jint) luaL_optint(L, n, d);
}

JNIWRAP(jlong, luaL_1checklong, jlong ptr, jint n) {
    return (jlong) luaL_checklong(L, n);
}

JNIWRAP(jlong, luaL_1optlong, jlong ptr, jint n, jlong d) {
    return (jlong) luaL_optlong(L, n, d);
}

JNIWRAP(jstring, luaL_1typename, jlong ptr, jint i) {
    return ToString(luaL_typename(L, i));
}

JNIWRAP(jint, luaL_1dofile, jlong ptr, jstring filename) {
    const char *c_filename = GetString(filename);
    int result = luaL_dofile(L, c_filename);
    ReleaseString(filename, c_filename);
    return result;
}

JNIWRAP(jint, luaL_1dostring, jlong ptr, jstring string) {
    const char *c_string = GetString(string);
    int result = luaL_dostring(L, c_string);
    ReleaseString(string, c_string);
    return result;
}

JNIWRAP(void, luaL_1getmetatable, jlong ptr, jstring name) {
    const char *c_name = GetString(name);
    luaL_getmetatable(L, c_name);
    ReleaseString(name, c_name);
}

/*#define luaL_opt(L,f,n,d)	(lua_isnoneornil(L,(n)) ? (d) : f(L,(n)))
// From Lua 5.2.
#define luaL_newlibtable(L, l) \
	 lua_createtable(L, 0, sizeof(l)/sizeof((l)[0]) - 1)
#define luaL_newlib(L, l)	(luaL_newlibtable(L, l), luaL_setfuncs(L, l, 0))*/

/*#define luaL_addchar(B,c) \
  ((void)((B)->p < ((B)->buffer+LUAL_BUFFERSIZE) || luaL_prepbuffer(B)), \
   (*(B)->p++ = (char)(c)))
#define luaL_putchar(B,c)	luaL_addchar(B,c)
#define luaL_addsize(B,n)	((B)->p += (n))
LUALIB_API void (luaL_buffinit) (lua_State *L, luaL_Buffer *B);
LUALIB_API char *(luaL_prepbuffer) (luaL_Buffer *B);
LUALIB_API void (luaL_addlstring) (luaL_Buffer *B, const char *s, size_t l);
LUALIB_API void (luaL_addstring) (luaL_Buffer *B, const char *s);
LUALIB_API void (luaL_addvalue) (luaL_Buffer *B);
LUALIB_API void (luaL_pushresult) (luaL_Buffer *B);*/

// lualib.h
#define JNIWRAP_LUALIB_WRAP(libname) \
    JNIWRAP(jint, luaopen_1##libname, jlong ptr) { \
        return (jint) luaopen_##libname(L); \
    }

#define JNIWRAP_LUALIB(name, libname) \
    JNIWRAP(jint, luaopen_1##name, jlong ptr) { \
        return (jint) luaopen_##libname(L); \
    }

JNIWRAP_LUALIB_WRAP(base)

JNIWRAP_LUALIB_WRAP(math)

JNIWRAP_LUALIB_WRAP(string)

JNIWRAP_LUALIB_WRAP(table)

JNIWRAP_LUALIB_WRAP(io)

JNIWRAP_LUALIB_WRAP(os)

JNIWRAP_LUALIB_WRAP(package)

JNIWRAP_LUALIB_WRAP(debug)

JNIWRAP_LUALIB_WRAP(bit)

JNIWRAP_LUALIB_WRAP(jit)

JNIWRAP_LUALIB_WRAP(ffi)

JNIWRAP_LUALIB(string_1buffer, string_buffer)

#undef JNIWRAP_LUALIB
#undef JNIWRAP_LUALIB2

// luajit.h
JNIWRAP(jint, luaJIT_1setmode, jlong ptr, jint idx, jint mode) {
    return (jint) luaJIT_setmode(L, idx, mode);
}

//JNIWRAP(void, luaJIT_1profile_1stop, jlong ptr) {
//    luaJIT_profile_stop(L);
//}
//
//JNIWRAP(jstring, luaJIT_1profile_1dumpstack, jlong ptr, jstring format, jint depth) {
//    const char *c_format = GetString(format);
//    size_t len = 0;
//    const char *result = luaJIT_profile_dumpstack(L, c_format, depth, &len);
//    ReleaseString(format, c_format);
//    return ToString(result);
//}

#undef L
#undef L1
#undef JNIWRAP
#undef JNIWRAP_STATIC