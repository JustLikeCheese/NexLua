#ifndef LUAKIT_H
#define LUAKIT_H

#include "lua.h"
#include "lauxlib.h"
#include "lualib.h"
#include "luajit.h"

#define CFunction(body) \
({ \
    static int _func(lua_State *L) body \
    _func; \
})

#endif // LUAKIT_H