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
    int result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                             com_luajava_JuaAPI_bindClass, (jlong) L, string);
                                             com_luajava_LuaJava_bindClass, (jlong) L, string);
    DeleteString(string);
    return checkOrError(env, L, result);
}

    DeleteString(string);
    return checkOrError(env, L, result);
}

static const luaL_Reg javalib[] = {
        {"bindClass", luajava_bindClass},
        {"bindClass",  luajava_bindClass},
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
