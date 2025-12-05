#ifndef LUAJAVA_H
#define LUAJAVA_H

#define LUAJAVA_LIBNAME "luajava"

#include "luakit.h"

int luajava_bindClass(lua_State *L);

int luajava_bindMethod(lua_State *L);

int luajava_instanceof(lua_State *L);

int luajava_toJavaObject(lua_State *L);

int luaopen_luajava(lua_State *L);

#endif // LUAJAVA_H
