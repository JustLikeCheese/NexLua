/*
 * NexLuaJava Library
 * @Author: JustLikeCheese
 * @Date: 2025/9/9
 */

#include <string.h>
#include <malloc.h>

#include "luakit.h"
#include "luajavacore.h"
#include "luacomp.h"
#include "luajavaapi.h"
#include "luajava.h"
#include "luareg.h"

/* Modules and functions */
int luajava_bindClass(lua_State *L) {
    const char *name = luaL_checkstring(L, 1);
    JNIEnv *env = getJNIEnv(L);
    jstring string = ToString(name);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_LuaJava,
                                             com_luajava_LuaJava_bindClass, (jlong) L, string);
    DeleteString(string);
    return checkOrError(env, L, result);
}

int luajava_bindMethod(lua_State *L) {
    jobject object = luaJ_checkanyobject(L, 1);
    const char *methodName = luaL_checkstring(L, 2);
    JNIEnv *env = getJNIEnv(L);
    jstring string = ToString(methodName);
    int paramCount = lua_gettop(L) - 2;
    jobjectArray clazzArray = (*env)->NewObjectArray(env, paramCount, java_lang_Class, NULL);
    for (int i = 0; i < paramCount; i++) {
        jclass paramClass = luaJ_checkclass(L, i + 3);
        (*env)->SetObjectArrayElement(env, clazzArray, i, paramClass);
    }
    int result = (*env)->CallStaticIntMethod(env, com_luajava_LuaJava,
                                             com_luajava_LuaJava_bindMethod,
                                             (jlong) L, object, string, clazzArray);
    DeleteString(string);
    (*env)->DeleteLocalRef(env, clazzArray);
    return checkOrError(env, L, result);
}

int luajava_instanceof(lua_State *L) {
    jobject object = luaJ_checkanyobject(L, 1);
    jclass clazz = luaJ_checkclass(L, 2);
    JNIEnv *env = getJNIEnv(L);
    if ((*env)->IsInstanceOf(env, object, clazz)) {
        lua_pushboolean(L, 1);
    } else {
        lua_pushboolean(L, 0);
    }
    return 1;
}

int luajava_toJavaObject(lua_State *L) {
    luaL_checkany(L, 1);
    jclass clazz = luaJ_toclass(L, 2);
    JNIEnv *env = getJNIEnv(L);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_LuaJava,
                                             com_luajava_LuaJava_toJavaObject,
                                             (jlong) L, clazz);
    return checkOrError(env, L, result);
}

int luajava_toJavaArray(lua_State *L) {
    luaL_checkany(L, 1);
    jclass clazz = luaJ_toclass(L, 2);
    JNIEnv *env = getJNIEnv(L);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_LuaJava,
                                             com_luajava_LuaJava_toJavaArray,
                                             (jlong) L, clazz);
    return checkOrError(env, L, result);
}

int luajava_toJavaMap(lua_State *L) {
    luaL_checkany(L, 1);
    jclass keyClazz = luaJ_toclass(L, 2);
    jclass valueClazz = luaJ_toclass(L, 3);
    JNIEnv *env = getJNIEnv(L);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_LuaJava,
                                             com_luajava_LuaJava_toJavaMap,
                                             (jlong) L, keyClazz, valueClazz);
    return checkOrError(env, L, result);
}

int luajava_toString(lua_State *L) {
    luaL_checkany(L, 1);
    const char* string = luaJ_tostring(L, 1);
    if (string) {
        lua_pushstring(L, string);
        return 1;
    }
    return luaL_error(L, "luajava.toString failed");
}

static const luaL_Reg javalib[] = {
        {"bindClass", luajava_bindClass},
        {"bindMethod", luajava_bindMethod},
        {"instanceof", luajava_instanceof},
        {"toJavaObject", luajava_toJavaObject},
        {"toJavaArray", luajava_toJavaArray},
        {"toJavaMap", luajava_toJavaMap},
        {"toString", luajava_toString},
        {NULL, NULL}
};

/* Initializes the library */
REGISTER_MODULE(luajava, luaopen_luajava);

int luaopen_luajava(lua_State *L) {
    luaL_newlib(L, javalib);
    lua_pushvalue(L, -1);
    lua_setglobal(L, LUAJAVA_LIBNAME);
    lua_atpanic(L, &fatalError);
    initMetaRegistry(L);
    return 1;
}
