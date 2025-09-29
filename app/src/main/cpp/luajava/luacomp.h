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

// wrapper function
LUALIB_API int luaJ_compare(lua_State *L, int index1, int index2, int op);

LUALIB_API void luaJ_gc(lua_State *L);

// compatible function
LUALIB_API const char *luaJ_tolstring(lua_State *L, int idx, size_t *len); // from Lua 5.3

LUALIB_API int luaJ_pushtraceback(lua_State *L);

extern const luaL_Reg allAvailableLibs[];

LUALIB_API void luaJ_openlib_call(lua_State *L, const char *libName, lua_CFunction loader);

LUALIB_API void luaJ_openlib(lua_State *L, const char *libName);

LUALIB_API int luaJ_pcall(lua_State *L, int nargs, int nresults, int errfunc);

#endif // LUACOMP_H
