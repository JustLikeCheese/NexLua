#include "lualib.h"
#include "lauxlib.h"

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