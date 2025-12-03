#ifndef LUAJAVA_H
#define LUAJAVA_H

#define LUAJAVA_LIBNAME "luajava"

#include "luakit.h"

extern int luajava_bindClass(lua_State *L);
extern int luaopen_luajava(lua_State *L);

#endif // LUAJAVA_H
