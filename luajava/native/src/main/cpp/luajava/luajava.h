#ifndef LUAJAVA_H
#define LUAJAVA_H

#define LUAJAVA_LIBNAME "luajava"
#define LUAJAVA_VERSION "NexLuaJava 1.0.0"
#define LUAJAVA_COPYRIGHT "Copyright (C) 2025, JustLikeCheese"

#include "luakit.h"

extern int luajava_bindClass(lua_State *L);
extern int luaopen_luajava(lua_State *L);

#endif // LUAJAVA_H
