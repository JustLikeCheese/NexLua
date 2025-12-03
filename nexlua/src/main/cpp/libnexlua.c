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
#include "luareg.h"

#include "lib/dump.h"
#include "lib/import.h"

/* Initializes the library */
REGISTER_MODULE(import, luaopen_import);
int luaopen_import(lua_State *L) {
    // import function
    lua_pushcfunction(L, import);
    lua_setglobal(L, IMPORT_LIBNAME);
    // dump function
    lua_pushcfunction(L, import_dump);
    lua_setglobal(L, "dump");
    return 1;
}

#undef IMPORT
#undef IMPORT_PACKAGES
#undef IMPORT_INJECTOR