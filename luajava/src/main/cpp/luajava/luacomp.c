#include <malloc.h>
#include <string.h>
#include <math.h>
#include <dlfcn.h>
#include <stdbool.h>
#include "luacomp.h"
#include "jnihelper.h"
#include "luajavacore.h"

// useful functions
static void dump_buffer_init(DumpBuffer *dump, size_t capacity) {
    dump->size = 0;
    dump->capacity = capacity;
    dump->buffer = (unsigned char *) malloc(dump->capacity);
}

static void dump_buffer_free(DumpBuffer *dump) {
    if (dump->buffer) {
        free(dump->buffer);
        dump->buffer = NULL;
    }
    dump->size = 0;
    dump->capacity = 0;
}

static size_t next_capacity(size_t capacity, size_t required_size) {
    while (capacity > 0 && capacity < required_size) {
        capacity <<= 1;
    }
    return capacity;
}

static int dump_buffer_writer(lua_State *L, const void *p, size_t sz, void *ud) {
    DumpBuffer *dump = (DumpBuffer *) ud;
    size_t new_size = dump->size + sz;
    if (new_size < dump->size)
        return luaL_error(L, "Buffer overflow in buffer writer"); // Check for overflow
    if (new_size > dump->capacity) { // Need to resize
        size_t new_capacity = next_capacity(dump->capacity, new_size);
        if (new_capacity < new_size)
            return luaL_error(L, "Buffer overflow in buffer writer");  // Check for overflow
        void *new_buffer = realloc(dump->buffer, new_capacity);
        if (new_buffer == NULL) return luaJ_error_memory(L);
        dump->capacity = new_capacity;
        dump->buffer = (unsigned char *) new_buffer;
    }
    memcpy(dump->buffer + dump->size, p, sz);
    dump->size = new_size;
    return 0;
}

int luaJ_dumptobuffer(lua_State *L, DumpBuffer *buffer) {
    dump_buffer_init(buffer, 4096);
    if (buffer->buffer == NULL) return -1; // Memory allocation failed
    int result = lua_dump(L, dump_buffer_writer, buffer);
    if (result != 0) {
        dump_buffer_free(buffer);
        return -2;  // Dump failed
    }
    return 0;
}

const char* luaJ_dumpstack(lua_State *L) {
    int top = lua_gettop(L);
    luaL_Buffer b;
    luaL_buffinit(L, &b);
    lua_pushfstring(L, "stack dump (%d values):\n", top);
    luaL_addvalue(&b);

    for (int idx = 1; idx <= top; idx++) {
        const char* str = luaJ_tostring(L, idx);
        lua_pushfstring(L, "  [%d]: (%s) %s\n", idx, luaL_typename(L, idx), str);
        lua_remove(L, -2);  // remove the string pushed by luaJ_tostring
        luaL_addvalue(&b);
    }
    luaL_pushresult(&b);
    const char* msg = lua_tostring(L, -1);
    lua_pop(L, 1);
    return msg;
}

// NOLINTNEXTLINE(misc-no-recursion)
int luaJ_copy(lua_State *from, lua_State *to, int index) {
    if (!lua_checkstack(to, 1)) return -1;
    index = luaJ_absindex(from, index);
    int type = lua_type(from, index);
    switch (type) {
        case LUA_TNONE:
        case LUA_TNIL:
            lua_pushnil(to);
            break;
        case LUA_TBOOLEAN:
            lua_pushboolean(to, lua_toboolean(from, index));
            break;
        case LUA_TNUMBER:
            lua_pushnumber(to, lua_tonumber(from, index));
            break;
        case LUA_TSTRING: {
            size_t len;
            const char *str = lua_tolstring(from, index, &len);
            lua_pushlstring(to, str, len);
            break;
        }
        case LUA_TTABLE: {
            lua_newtable(to);
            lua_pushnil(from);
            while (lua_next(from, index)) {
                if (luaJ_copy(from, to, lua_gettop(from) - 1) != 0) {
                    lua_pop(from, 2);
                    lua_pop(to, 2);
                    return -1;
                }
                if (luaJ_copy(from, to, lua_gettop(from)) != 0) {
                    lua_pop(from, 2);
                    lua_pop(to, 3);
                    return -1;
                }

                lua_settable(to, -3);
                lua_pop(from, 1);
            }
            if (lua_getmetatable(from, index)) {
                if (luaJ_copy(from, to, lua_gettop(from)) == 0) {
                    lua_setmetatable(to, -2);
                } else {
                    lua_pop(to, 1);
                }
                lua_pop(from, 1);
            }
            break;
        }
        case LUA_TFUNCTION:
            if (lua_iscfunction(from, index)) {
                lua_CFunction func = lua_tocfunction(from, index);
                lua_pushcfunction(to, func);
            } else {
                DumpBuffer buffer;
                lua_pushvalue(from, index);
                if (luaJ_dumptobuffer(from, &buffer) != 0) {
                    lua_pop(from, 1);
                    return -1;
                }
                lua_pop(from, 1);
                if (luaL_loadbuffer(to, (const char *) buffer.buffer,
                                    buffer.size, "=(copied)") != 0) {
                    free(buffer.buffer);
                    lua_pop(to, 1);
                    return -1;
                }
                free(buffer.buffer);
            }
            break;
        case LUA_TLIGHTUSERDATA:
            lua_pushlightuserdata(to, lua_touserdata(from, index));
            break;
        case LUA_TUSERDATA: {
            void *src_userdata = lua_touserdata(from, index);
            size_t size = lua_objlen(from, index);
            void *dst_userdata = lua_newuserdata(to, size);
            memcpy(dst_userdata, src_userdata, size);
            if (lua_getmetatable(from, index)) {
                if (luaJ_copy(from, to, lua_gettop(from)) == 0) {
                    lua_setmetatable(to, -2);
                } else {
                    lua_pop(to, 1);
                }
                lua_pop(from, 1);
            }
            break;
        }
        default:
            return -1;
    }
    return 0;
}

// some functions to improve performance
int luaJ_compare(lua_State *L, int idx1, int idx2, int opc) {
    switch (opc) {
        case 0: // lessthan
            return lua_lessthan(L, idx1, idx2);
        case 1: // lessthan and equal
            return lua_lessthan(L, idx1, idx2) || lua_equal(L, idx1, idx2);
        case 2: // equal
            return lua_equal(L, idx1, idx2);
        case 3: // morethan
            return (!lua_lessthan(L, idx1, idx2)) && (!lua_equal(L, idx1, idx2));
        case 4: // morethan and equal
            return !lua_lessthan(L, idx1, idx2);
        default:
            return 0; // unsupported opcode
    }
}

// some functions to improve ref performance



// some compatible functions
int luaJ_absindex(lua_State *L, int index) {
    return (index > 0 || index <= LUA_REGISTRYINDEX) ? index : lua_gettop(L) + index + 1;
}

const char *luaJ_tolstring(lua_State *L, int idx, size_t *len) {
    if (luaL_callmeta(L, idx, "__tostring")) {
        if (!lua_isstring(L, -1))
            luaL_error(L, "'__tostring' must return a string");
    } else {
        int type = lua_type(L, idx);
        switch (type) {
            case LUA_TNUMBER:
            case LUA_TSTRING:
                lua_pushvalue(L, idx);
                break;
            case LUA_TBOOLEAN:
                lua_pushstring(L, lua_toboolean(L, idx) ? "true" : "false");
                break;
            case LUA_TNIL:
            case LUA_TNONE:
                lua_pushliteral(L, "nil");
                break;
            default: {
                int tt = luaL_getmetafield(L, idx, "__name");
                const char *kind = (tt == LUA_TSTRING) ? lua_tostring(L, -1) : lua_typename(L,
                                                                                            type);
                lua_pushfstring(L, "%s: %p", kind, lua_topointer(L, idx));
                if (tt != LUA_TNIL) lua_remove(L, -2);
                break;
            }
        }
    }
    return lua_tolstring(L, -1, len);
}

// traceback
int luaJ_traceback(lua_State *L) { // From LuaJIT/src/luajit.c:115
    if (!lua_isstring(L, 1)) { /* Non-string error object? Try metamethod. */
        if (lua_isnoneornil(L, 1) ||
            !luaL_callmeta(L, 1, "__tostring") ||
            !lua_isstring(L, -1))
            return 1;  /* Return non-string error object. */
        lua_remove(L, 1);  /* Replace object by result of __tostring metamethod. */
    }
    luaL_traceback(L, L, lua_tostring(L, 1), 1);
    return 1;
}

int luaJ_pcall(lua_State *L, int nargs, int nresults, int errfunc) {
    if (errfunc == 0) {
        errfunc = lua_gettop(L) - nargs;
        lua_pushcfunction(L, luaJ_traceback);
        lua_insert(L, errfunc);
        int result = lua_pcall(L, nargs, nresults, errfunc);
        lua_remove(L, errfunc);
        return result;
    }
    return lua_pcall(L, nargs, nresults, errfunc);
}

const luaL_Reg allAvailableLibs[] = {
        {"",              luaopen_base},
        {"package",       luaopen_package},
        {"string",        luaopen_string},
        {"table",         luaopen_table},
        {"math",          luaopen_math},
        {"io",            luaopen_io},
        {"os",            luaopen_os},
        {"debug",         luaopen_debug},
        {"ffi",           luaopen_ffi},
        {"jit",           luaopen_jit},
        {"bit",           luaopen_bit},
        {"string.buffer", luaopen_string_buffer},
        {NULL, NULL},
};

void luaJ_openlib_call(lua_State *L, const char *libName, lua_CFunction loader) {
    lua_pushcfunction(L, loader);
    lua_pushstring(L, libName);
    lua_call(L, 1, 0);
}

void luaJ_openlib(lua_State *L, const char *libName) {
    const luaL_Reg *lib = allAvailableLibs;
    for (; lib->func != NULL; lib++) {
        if (strcmp(lib->name, libName) == 0) {
            luaJ_openlib_call(L, lib->name, lib->func);
            return;
        }
    }
}

