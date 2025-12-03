//
// Created by Cheese on 2025/12/3.
//

#include <string.h>
#include <malloc.h>
#include "import.h"
#include "luacomp.h"
#include "luajava.h"
#include "luajavaapi.h"

static const char* get_simple_name(const char* name) {
    const char *simpleName = name;
    for (const char *p = name; *p; p++) {
        if (*p == '.' || *p == '$') {
            simpleName = p + 1;
        }
    }
    return simpleName;
}

static int import_mt_index(lua_State *L) {
    lua_pushvalue(L, 2);
    lua_rawget(L, 1);
    if (!lua_isnil(L, -1)) { // [_G, key, value]
        return 1;
    }
    lua_pop(L, 1);
    if (!lua_isstring(L, 2)) {
        return 0;
    }
    // check string
    const char *key = lua_tostring(L, 2);
    // check jni env
    JNIEnv *env = getJNIEnv(L);
    // push __import
    lua_getfield(L, 1, "__import");
    if (!lua_istable(L, -1)) {
        lua_pop(L, 1);
        lua_createtable(L, 0, 2);
        lua_pushvalue(L, -1);
        lua_setfield(L, -3, "__import");
    }
    // push __import.packages
    lua_getfield(L, -1, "packages");
    if (!lua_istable(L, -1)) {
        lua_pop(L, 1);
        lua_createtable(L, 0, 1);
        lua_pushvalue(L, -1);
        lua_setfield(L, -3, "packages");
    }
    int len = (int) lua_objlen(L, -1);
    for (int i = 1; i <= len; i++) {
        // push __import.packages[i]
        lua_rawgeti(L, -1, i);
        if (lua_type(L, -1) == LUA_TSTRING) {
            const char *pkgName = lua_tostring(L, -1);
            size_t pkgLen = strlen(pkgName);
            size_t keyLen = strlen(key);
            char *fullName = (char*) malloc(pkgLen + keyLen + 2);
            if (fullName) {
                memcpy(fullName, pkgName, pkgLen);
                fullName[pkgLen] = '.';
                memcpy(fullName + pkgLen + 1, key, keyLen);
                fullName[pkgLen + keyLen + 1] = '\0';
            }
            lua_pop(L, 1);
            int top = lua_gettop(L);
            jstring string = ToString(fullName);
            int result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                                     com_luajava_JuaAPI_bindClass, (jlong) L, string);
            DeleteString(string);
            if (!checkIfError(env, L) && result) {
                char *simpleName = strdup(get_simple_name(fullName));
                lua_pushvalue(L, -1);
                lua_setglobal(L, simpleName);
                free(fullName);
                free(simpleName);
                return 1;
            } else {
                lua_pushnil(L);
                lua_setglobal(L, JAVA_GLOBAL_THROWABLE);
                lua_settop(L, top);
            }
        }
        // pop packages[i]
        lua_pop(L, 1);
    }
    lua_pop(L, 2); // pop __import and __import.packages
    return 0;
}

static int local_import(lua_State *L, int idx) {
    const char* name = luaL_checkstring(L, idx);
    size_t length = strlen(name);
    if (length > 2 && name[length - 2] == '.' && name[length - 1] == '*') { // import package
        // push _G
        lua_pushvalue(L, LUA_GLOBALSINDEX);
        luaL_checktype(L, -1, LUA_TTABLE);
        // push __import
        lua_getfield(L, -1, "__import");
        if (!lua_istable(L, -1)) {
            lua_pop(L, 1);
            lua_createtable(L, 0, 2);
            lua_pushvalue(L, -1);
            lua_setfield(L, -3, "__import"); // [_G, new_table]
        }
        // check __import.packages
        lua_getfield(L, -1, "packages");
        if (!lua_istable(L, -1)) {
            lua_pop(L, 1);
            lua_createtable(L, 0, 1);
            lua_pushvalue(L, -1);
            lua_setfield(L, -3, "packages"); // [_G, __import, new_table, new_table2]
        }
        // update __import.packages
        int size = (int) lua_objlen(L, -1); // [_G, __import, __import.packages]
        lua_pushlstring(L, name, length - 2);
        lua_rawseti(L, -2, size + 1);
        lua_pop(L, 1);
        // update __import.injected
        lua_getfield(L, -1, "injected");
        if (!lua_toboolean(L, -1)) {
            lua_pop(L, 1);
            // check metatable
            if (!lua_getmetatable(L, -2))
                lua_createtable(L, 0, 1);
            lua_pushcfunction(L, import_mt_index);
            lua_setfield(L, -2, "__index"); // [_G, __import, metatable, func, __index]
            // set metatable
            lua_setmetatable(L, -3); // [_G, __import, metatable]
            // update import.injected to true
            lua_pushboolean(L, 1);
            lua_setfield(L, -2, "injected");
        }
        lua_pop(L, 2); // pop _G and cache
        return 0;
    }
    // local require
    int err = luaJ_require(L, idx);
    if (err == LUA_OK)
        return 1;
    else
        err = (err != -1);
    // import class
    int top = lua_gettop(L);
    JNIEnv *env = getJNIEnv(L);
    jstring string = ToString(name);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                             com_luajava_JuaAPI_bindClass, (jlong) L, string);
    DeleteString(string);
    if (!checkIfError(env, L) && result) {
        char *simpleName = strdup(get_simple_name(name));
        lua_pushvalue(L, -1);
        lua_setglobal(L, simpleName);
        free(simpleName);
        return 1;
    } else if (err) {
        lua_pushnil(L);
        lua_setglobal(L, JAVA_GLOBAL_THROWABLE);
        lua_settop(L, err);
    }
    return lua_error(L);
}

/* Modules and functions */
int import(lua_State *L) {
    luaL_checktype(L, 1, LUA_TSTRING);
    return local_import(L, 1);
}