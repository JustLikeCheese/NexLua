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

#define LUA_JAVALIBNAME "luajava"
#define MEMORY_ALLOCATION_ERROR "Memory allocation failed"

/* Modules and functions */
static int bindClass(lua_State *L) {
    const char *name = luaL_checkstring(L, 1);
    // replace '.' to '/'
    size_t length = strlen(name);
    char *className = malloc(length + 1);
    if (className == NULL) return luaL_error(L, MEMORY_ALLOCATION_ERROR);
    for (size_t i = 0; i <= length; i++) {
        className[i] = (char) ((name[i] == '.') ? '/' : name[i]);
    }
    JNIEnv *env = getJNIEnv(L);
    jclass jclazz = bindJavaClass(env, className);
    LOG("%s", className);
    LOG("%p", jclazz);
    free(className);
    luaJ_pushclass(env, L, jclazz);
    return 1;
}

static const luaL_Reg javalib[] = {
        {"bindClass", bindClass},
        {NULL, NULL}
};

/* Initializes the library */
int luaopen_luajava(lua_State *L) {
    luaL_newlib(L, javalib);
    lua_setglobal(L, LUA_JAVALIBNAME);
    lua_atpanic(L, &fatalError);
    initMetaRegistry(L);
    return 1;
}
