#include <string.h>
#include "jnihelper.h"
#include "luajavaloader.h"
#include "luajavaapi.h"
#include "luareg.h"

// Java Custom Loader
static int jmoduleLoad(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    const char *name = luaL_checkstring(L, 1);
    jstring moduleName = ToString(name);
    int ret = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI, com_luajava_JuaAPI_jmoduleLoad,
                                          (jlong) L, moduleName);
    DeleteString(moduleName);
    if((*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionClear(env);
        return 0;
    }
    return 1;
}

int luaJ_initloader(lua_State *L) {
    // push package
    lua_getglobal(L, "package");
    if (lua_isnil(L, -1)) {
        lua_pop(L, 1);
        return 1;
    }
    // package.nexluajava = true
    lua_getfield(L, -1, "nexluajava");
    if (lua_toboolean(L, -1)) {
        lua_pop(L, 2); // pop package, package.nexluajava
        return 0;
    }
    lua_pop(L, 1); // pop package.nexluajava
    lua_pushboolean(L, 1);
    lua_setfield(L, -2, "nexluajava");
    // package.loaders
    lua_getfield(L, -1, "loaders");
    if (!lua_istable(L, -1)) {
        lua_pop(L, 2);
        return 1;
    }
    int len = (int) lua_objlen(L, -1);
    for (int i = len; i >= 1; i--) {
        lua_rawgeti(L, -1, i);      // loaders[i]
        lua_rawseti(L, -2, i + 1);  // loaders[i+1] = loaders[i]
    }
    lua_pushcfunction(L, &jmoduleLoad);
    lua_rawseti(L, -2, 1);  // loaders[1] = jmoduleLoad
    lua_pop(L, 1);  // pop loaders
    // package.preload
    lua_getfield(L, -1, "preload");
    // register modules
    const ModuleEntry *iter = &__start_extra_modules;
    const ModuleEntry *end = &__stop_extra_modules;
    for (; iter < end; iter++) {
        if (iter->name && iter->func) {
            lua_pushcfunction(L, iter->func);
            lua_setfield(L, -2, iter->name);
        }
    }
    lua_pop(L, 2); // pop preload, package
    return 0;
}