//
// Created by Cheese on 2025/10/28.
//

#include <stdbool.h>
#include "jnihelper.h"
#include "luajavacore.h"
#include "luajavaapi.h"
#include "luacomp.h"
#include "luajava.h"

#define L ((lua_State *) ptr)
#define L1 ((lua_State *) ptr1)
#define JNIWRAP(RET, NAME, ...) \
    JNIEXPORT RET JNICALL Java_com_luajava_LuaNatives_##NAME(JNIEnv* env, jobject thiz, ##__VA_ARGS__)

#define JNIWRAP_STATIC(RET, NAME, ...) \
    JNIEXPORT RET JNICALL Java_com_luajava_LuaNatives_##NAME(JNIEnv* env, jclass thiz, ##__VA_ARGS__)


JNIWRAP(jlong, luaJ_1newstate) {
    return (jlong) luaJ_newstate();
}

JNIWRAP(jint, luaJ_1initloader, jlong ptr) {
    return luaJ_initloader(L);
}

/* Push API */
JNIWRAP(void, luaJ_1pushobject, jlong ptr, jobject obj) {
    luaJ_pushobject(env, L, obj);
}

JNIWRAP(void, luaJ_1pushclass, jlong ptr, jobject obj) {
    luaJ_pushclass(env, L, obj);
}

JNIWRAP(void, luaJ_1pusharray, jlong ptr, jobject array) {
    luaJ_pusharray(env, L, array);
}

JNIWRAP(void, luaJ_1pushfunction, jlong ptr, jobject func) {
    luaJ_pushfunction(env, L, func);
}

JNIWRAP(void, luaJ_1pushcclosure, jlong ptr, jobject func, jint n) {
    luaJ_pushcclosure(env, L, func, n);
}

JNIWRAP(void, luaJ_1pushbuffer, jlong ptr, jobject obj_buffer, jint size) {
    luaJ_pushbuffer(env, L, obj_buffer);
}

/* Type Check API */
JNIWRAP(jstring, luaJ_1tostring, jlong ptr, jint index) {
    return ToString(luaJ_tostring(L, index));
}

JNIWRAP(jobject, luaJ_1toobject, jlong ptr, jint index) {
    return (jobject) luaJ_toanyobject(L, (int) index);
}

JNIWRAP(jint, luaJ_1isobject, jlong ptr, jint index) {
    return luaJ_isanyobject(L, (int) index);
}

/* Buffer API */
JNIWRAP(jint, luaJ_1loadbuffer, jlong ptr, jobject obj_buffer, jint size, jstring obj_name) {
    const char *name = GetString(obj_name);
    unsigned char *buffer = obj_buffer ? (unsigned char *) (*env)->GetDirectBufferAddress(env,
                                                                                          obj_buffer)
                                       : NULL;
    jint result = luaL_loadbuffer(L, (char *) buffer, (int) size, name);
    ReleaseString(obj_name, name);
    return result;
}

JNIWRAP(jobject, luaJ_1dump, jlong ptr) {
    return luaJ_dump(env, L);
}

JNIWRAP(jobject, luaJ_1tobuffer, jlong ptr, jint index) {
    return luaJ_tojavabuffer(env, L, (int) index);
}

JNIWRAP(jobject, luaJ_1todirectbuffer, jlong ptr, jint index) {
    return luaJ_javadirectbuffer_new(env, L, (int) index);
}

JNIWRAP(void, luaJ_1openlib, jlong ptr, jstring j_name) {
    UseString(j_name, name, {
        luaJ_openlib(L, name);
    });
}

JNIWRAP(jint, luaJ_1compare, jlong ptr, jint idx1, jint idx2, jint opc) {
    return luaJ_compare(L, idx1, idx2, opc);
}

JNIWRAP(jlong, luaJ_1newthread, jlong ptr, jint lid) {
    lua_State *l = lua_newthread(L);
    luaopen_luajava(l);
    return (jlong) l;
}

JNIWRAP(jint, luaJ_1invokespecial, jlong ptr, jclass clazz, jstring obj_method, jstring obj_sig,
        jobject obj, jstring obj_params) {
    const char *method = GetString(obj_method);
    const char *sig = GetString(obj_sig);
    const char *params = GetString(obj_params);
    jint result = luaJ_invokespecial(env, L, clazz, method, sig, obj, params);
    ReleaseString(obj_method, method);
    ReleaseString(obj_sig, sig);
    ReleaseString(obj_params, params);
    return result;
}

JNIWRAP(void, luaJ_1gc, jlong ptr) {
    luaJ_gc(L);
}

JNIWRAP(jint, luaJ_1copy, jlong ptr, jlong ptr1, jint index) {
    return luaJ_copy(L, L1, index);
}

JNIWRAP(jint, luaJ_1xpcall, jlong ptr, jint nargs, jint nresults) {
    return luaJ_xpcall(L, nargs, nresults);
}

JNIWRAP(jint, luaJ_1pcall, jlong ptr, int nargs, int nresults, int errfunc) {
    return luaJ_pcall(L, nargs, nresults, errfunc);
}

JNIWRAP(jint, luaJ_1dofile, jlong ptr, jstring filename) {
    const char *c_filename = GetString(filename);
    int result = luaJ_dofile(L, c_filename);
    ReleaseString(filename, c_filename);
    return result;
}

JNIWRAP(jint, luaJ_1dostring, jlong ptr, jstring string) {
    const char *c_string = GetString(string);
    int result = luaJ_dostring(L, c_string);
    ReleaseString(string, c_string);
    return result;
}

JNIWRAP(jint, luaJ_1dobuffer, jlong ptr, jobject obj_buffer, jint size, jstring obj_name) {
    const char *name = GetString(obj_name);
    jint result = luaJ_dobuffer(L, obj_buffer, (int) size, name);
    ReleaseString(obj_name, name);
    return result;
}

JNIWRAP(jstring, luaJ_1dumpstack, jlong ptr) {
    return ToString(luaJ_dumpstack(L));
}

// NexLua's special function to improve jni performance
JNIWRAP(jint, luaJ_1refsafe, jlong ptr, jint idx) {
    return luaJ_refsafe(L, idx);
}

JNIWRAP(void, luaJ_1refGet, jlong ptr, jint ref) {
    luaJ_refGet(L, ref);
}

JNIWRAP(void, luaJ_1unRef, jlong ptr, jint ref) {
    luaJ_unRef(L, ref);
}

JNIWRAP(jint, luaJ_1ref, jlong ptr) {
    return luaJ_ref(L);
}

JNIWRAP(jint, luaJ_1typeRef, jlong ptr, jint ref) {
    lua_State *lua = L;
    luaJ_refGet(L, ref);
    int type = lua_type(lua, -1);
    lua_pop(lua, 1);
    return type;
}

#undef L
#undef L1
#undef JNIWRAP
#undef JNIWRAP_STATIC