/*
 * NexLua Enhance Import Library
 * @Author: JustLikeCheese
 * @Date: 2025/9/9
 */

#include <string.h>
#include <malloc.h>

#include "luakit.h"
#include "libnexlua.h"
#include "luacomp.h"
#include "luajava.h"

static const char *IMPORT_CACHE = "__import";
static const char *IMPORT_PACKAGES = "packages";

static int local_require(lua_State *L) {
    int top = lua_gettop(L);
    lua_getglobal(L, "require");
    lua_insert(L, top);
    if (lua_pcall(L, 1, 1, 0) != LUA_OK) {
        if (lua_type(L, -1) != LUA_TSTRING) {
            return lua_error(L);
        }
        const char *err = lua_tostring(L, -1);
        if (!err || strstr(err, "no file") == NULL) {
            return lua_error(L);
        }
        lua_pop(L, 1);
        return 0;
    }
    lua_settop(L, top);
    return 1;
}

static int local_import(lua_State *L, int idx) {
    const char *name = luaL_checkstring(L, idx);
    size_t length = strlen(name);
    if (length < 1) return 0;
    if (name[length - 1] == '*') {
        lua_getfenv(L, LUA_GLOBALSINDEX);
        if (!lua_istable(L, -1)) { // type(_G) != "table"
            lua_pop(L, 1);
            return 0;
        }
        lua_getfield(L, -1, IMPORT_CACHE);
        if (lua_isnil(L, -1)) { // _G.packages doesn't exist
            lua_pop(L, 1);
            lua_createtable(L, 1, 0);
            lua_pushvalue(L, -1);
            lua_setfield(L, -3, IMPORT_CACHE);
        }
        if (!lua_istable(L, -1)) {
            lua_pop(L, 2);
            return 0;
        }
        lua_pushlstring(L, name, length - 1);
        int size = (int) lua_objlen(L, -2);
        lua_rawseti(L, -2, size + 1);
        lua_pop(L, 2);
        return 0;
    } else {
        int result = local_require(L);
        if (result == 0) {
            return luajava_bindClass(L);
        }
        return result;
    }
}

/* Modules and functions */
static int import(lua_State *L) {
    int type = lua_type(L, 1);
    switch (type) {
        case LUA_TSTRING:
            return local_import(L, -1);
        case LUA_TTABLE: {
            int len = (int) lua_objlen(L, 1);
            lua_createtable(L, len, 0);
            lua_pushnil(L);
            // [table, new_table, nil]
            while (lua_next(L, 1) != 0) {
                // [table, new_table, key, value]
                int key_type = lua_type(L, -2);
                int value_type = lua_type(L, -1);
                lua_pop(L, 1); // pop value
            }
            lua_pop(L, 1); // pop value
        }
        default:
            return luaL_typerror(L, 1, "string or table");
    }
}

/* Initializes the library */
int luaopen_import(lua_State *L) {
    lua_newtable(L);
    lua_setglobal(L, IMPORT_CACHE);
    lua_pushcfunction(L, import);
    lua_setglobal(L, IMPORT_LIBNAME);
    return 1;
}
