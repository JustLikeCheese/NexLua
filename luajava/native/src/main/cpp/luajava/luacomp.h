// Lua Compatible Header

#ifndef LUACOMP_H
#define LUACOMP_H

#include "luakit.h"
#include "jnihelper.h"


// dump buffer
typedef struct DumpBuffer {
    unsigned char *buffer;
    size_t size;
    size_t capacity;
} DumpBuffer;

// useful function
LUALIB_API int luaJ_dumptobuffer(lua_State *L, DumpBuffer *buffer);

LUALIB_API int luaJ_dobuffer(lua_State *L, unsigned char *buffer, int size, const char *name);

LUALIB_API int luaJ_copy(lua_State *from, lua_State *to, int index);

LUALIB_API int luaJ_xpcall(lua_State *L, int nargs, int nresults);

LUALIB_API const char* luaJ_dumpstack(lua_State *L);

// wrapper function to improve performance
LUALIB_API int luaJ_compare(lua_State *L, int idx1, int idx2, int opc);

// wrapper function to improve reference performance
#define luaJ_refGet(L, ref) lua_rawgeti(L, LUA_REGISTRYINDEX, ref)
#define luaJ_unRef(L, ref) luaL_unref(L, LUA_REGISTRYINDEX, ref)
#define luaJ_ref(L) luaL_ref(L, LUA_REGISTRYINDEX)
#define luaJ_refsafe(L, idx) (lua_pushvalue(L, idx), luaJ_ref(L))

// compatible function
LUALIB_API int luaJ_absindex(lua_State *L, int idx); // from Lua 5.3
LUALIB_API const char *luaJ_tolstring(lua_State *L, int idx, size_t *len); // from Lua 5.3
LUALIB_API int luaJ_traceback(lua_State *L); // from LuaJIT

LUALIB_API int luaJ_pcall(lua_State *L, int nargs, int nresults, int errfunc);

LUALIB_API int luaJ_require(lua_State *L, int idx);

LUALIB_API void luaJ_openlib(lua_State *L, const char *libName);
LUALIB_API void luaJ_openlibs(lua_State *L);

#define luaJ_dofile(L, fn) \
	(luaL_loadfile(L, fn) || luaJ_pcall(L, 0, LUA_MULTRET, 0))

#define luaJ_dostring(L, s) \
	(luaL_loadstring(L, s) || luaJ_pcall(L, 0, LUA_MULTRET, 0))

#define luaJ_dobuffer(L, buffer, size, name) \
    (luaL_loadbuffer(L, (const char *) buffer, size, name) || luaJ_pcall(L, 0, LUA_MULTRET, 0))

#define luaJ_error_memory(L) luaL_error(L, "Not enough memory")
#define luaJ_tostring(L, index) luaJ_tolstring(L, index, NULL)
#define luaJ_gc(L) lua_gc(L, LUA_GCCOLLECT, 0)

#endif // LUACOMP_H
