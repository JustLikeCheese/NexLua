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
        if (new_buffer == NULL) return luaL_error(L, "Not enough memory in buffer writer");
        dump->capacity = new_capacity;
        dump->buffer = (unsigned char *) new_buffer;
    }
    memcpy(dump->buffer + dump->size, p, sz);
    dump->size = new_size;
    return 0;
}

int luaJ_dumptobuffer(lua_State *L, DumpBuffer *buffer) {
    dump_buffer_init(buffer, 4096);
    if (buffer->buffer == NULL) {
        lua_pop(L, 1);
        return -2;  // Memory allocation failed
    }
    int result = lua_dump(L, dump_buffer_writer, buffer);
    lua_pop(L, 1);
    if (result != 0) {
        dump_buffer_free(buffer);
        return -3;  // Dump failed
    }
    return 0;
}

int luaJ_dobuffer(lua_State *L, unsigned char *buffer, int size, const char *name) {
    return (luaL_loadbuffer(L, (const char *) buffer, size, name) ||
            lua_pcall(L, 0, LUA_MULTRET, 0));
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

void luaJ_gc(lua_State *L) {
    lua_gc(L, LUA_GCCOLLECT, 0);
}

// some compatible functions
const char *luaJ_tolstring(lua_State *L, int idx, size_t *len) { // from Lua5.3
    if (luaL_callmeta(L, idx, "__tostring")) {  /* metafield? */
        if (!lua_isstring(L, -1))
            luaL_error(L, "'__tostring' must return a string");
    } else {
        switch (lua_type(L, idx)) {
            case LUA_TNUMBER: {
                lua_Number n = lua_tonumber(L, idx);
                if (n == floor(n)) {
                    lua_pushfstring(L, "%I", n);
                } else {
                    lua_pushfstring(L, "%f", n);
                }
                break;
            }
            case LUA_TSTRING:
                lua_pushvalue(L, idx);
                break;
            case LUA_TBOOLEAN:
                lua_pushstring(L, (lua_toboolean(L, idx) ? "true" : "false"));
                break;
            case LUA_TNIL:
                lua_pushliteral(L, "nil");
                break;
            default: {
                int tt = luaL_getmetafield(L, idx, "__name");  /* try name */
                const char *kind = (tt == LUA_TSTRING) ? lua_tostring(L, -1) :
                                   luaL_typename(L, idx);
                lua_pushfstring(L, "%s: %p", kind, lua_topointer(L, idx));
                if (tt != LUA_TNIL)
                    lua_remove(L, -2);  /* remove '__name' */
                break;
            }
        }
    }
    return lua_tolstring(L, -1, len);
}

// traceback
static int LUA_TRACEBACK = 0;

int luaJ_pushtraceback(lua_State *L) {
    lua_pushlightuserdata(L, &LUA_TRACEBACK);
    lua_rawget(L, LUA_REGISTRYINDEX);
    int type = lua_type(L, -1);
    switch (type) {
        case LUA_TNIL:
            lua_pop(L, 1); // pop nil
            lua_getglobal(L, "debug");
            if (lua_isnil(L, -1)) {
                lua_pop(L, 1); // pop nil
                return false;
            }
            lua_getfield(L, -1, "traceback");
            lua_remove(L, -2); // pop debug table
            if (!lua_isfunction(L, -1)) {
                lua_pop(L, 1);
                return false;
            }
            lua_pushlightuserdata(L, &LUA_TRACEBACK);
            lua_pushvalue(L, -2);
            lua_rawset(L, LUA_REGISTRYINDEX);
            break;
        case LUA_TFUNCTION:
            // Already on stack: [key, func]
            break;
        default:
            lua_pop(L, 1);
            lua_getglobal(L, "debug");
            if (lua_isnil(L, -1)) {
                lua_pop(L, 1);
                return 0;
            }
            lua_getfield(L, -1, "traceback");
            lua_remove(L, -2); // pop debug
            if (!lua_isfunction(L, -1)) {
                lua_pop(L, 1);
                return false;
            }
            break;
    }
    lua_remove(L, -2); // pop key
    return true;
}

int luaJ_pcall(lua_State *L, int nargs, int nresults, int errfunc) {
    if (errfunc == 0 && luaJ_pushtraceback(L)) {
        errfunc = lua_gettop(L);
        int result = lua_pcall(L, nargs, nresults, errfunc);
        lua_remove(L, errfunc - 1);
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

