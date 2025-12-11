#ifndef LIB_NEXLUA_H
#define LIB_NEXLUA_H

#define IMPORT_LIBNAME "import"

#include "luajava.h"
#include "luajavaapi.h"
#include "luajavacore.h"
#include "luacomp.h"
#include "luareg.h"

int luaopen_import(lua_State *L);
int luaopen_dump(lua_State *L);

#endif