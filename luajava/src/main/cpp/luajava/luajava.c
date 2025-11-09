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

/* Modules and functions */
static int bindClass(lua_State *L) {
    const char *name = luaL_checkstring(L, 1);
    JNIEnv *env = getJNIEnv(L);
    jstring string = ToString(name);
    jclass clazz = (*env)->CallStaticObjectMethod(env, java_lang_Class,
                                                  java_lang_Class_forName, string);
    DeleteString(string);
    if (!checkIfError(env, L)) {
        luaJ_pushclass(env, L, clazz);
        return 1;
    }
    return lua_error(L);
}

static const luaL_Reg javalib[] = {
        {"bindClass", bindClass},
        {NULL, NULL}
};

/* Initializes the library */
int luaopen_luajava(lua_State *L) {
    luaL_newlib(L, javalib);
    lua_setglobal(L, LUAJAVA_LIBNAME);
    lua_atpanic(L, &fatalError);
    initMetaRegistry(L);
    return 1;
}
