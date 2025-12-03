#include "dump.h"
#include "luacomp.h"

static void add_quoted(luaL_Buffer *b, const char *s, size_t len) {
    luaL_addchar(b, '"');
    while (len--) {
        uint32_t c = (uint32_t)(uint8_t)*s++;
        if (c == '"' || c == '\\' || c == '\n') {
            luaL_addchar(b, '\\');
            luaL_addchar(b, (char)c);
        } else if (c < 32 || c == 127) {  /* Control Char */
            uint32_t d;
            luaL_addchar(b, '\\');
            if (c >= 100 || (len > 0 && *s >= '0' && *s <= '9')) {
                luaL_addchar(b, (char)('0' + (c >= 100)));
                if (c >= 100) c -= 100;
                goto tens;
            } else if (c >= 10) {
                tens:
                d = (c * 205) >> 11;  /* d = c / 10 */
                c -= d * 10;
                luaL_addchar(b, (char)('0' + d));
            }
            c += '0';
            luaL_addchar(b, (char)c);
        } else {
            luaL_addchar(b, (char)c);
        }
    }
    luaL_addchar(b, '"');
}

static void to_string(lua_State *L, int idx, luaL_Buffer *buffer, int *deep, int visited, int path_idx) {
    int type = lua_type(L, idx);
    switch (type) {
        case LUA_TNUMBER:
            lua_pushvalue(L, idx);
            luaL_addvalue(buffer);
            break;

        case LUA_TSTRING: {
            size_t len;
            const char *s = lua_tolstring(L, idx, &len);
            add_quoted(buffer, s, len);
            break;
        }

        case LUA_TTABLE: {
            if (luaL_callmeta(L, idx, "__tostring")) {
                if (!lua_isstring(L, -1))
                    luaL_error(L, "'__tostring' must return a string");
                luaL_addvalue(buffer);
            } else {
                // Normalize index to absolute
                int abs_idx = idx;
                if (idx < 0) {
                    abs_idx = lua_gettop(L) + idx + 1;
                }

                // Check if already visited
                lua_pushvalue(L, abs_idx);
                lua_rawget(L, visited);
                if (!lua_isnil(L, -1)) {
                    luaL_addvalue(buffer);
                    return;
                }
                lua_pop(L, 1);

                // Mark as visited
                lua_pushvalue(L, abs_idx);
                lua_pushvalue(L, path_idx);
                lua_rawset(L, visited);

                *deep += 2;
                luaL_addchar(buffer, '{');

                lua_pushnil(L);
                int first = 1;
                while (lua_next(L, abs_idx)) {
                    if (!first) {
                        luaL_addstring(buffer, ",");
                    }
                    first = 0;

                    luaL_addstring(buffer, "\r\n");
                    for (int i = 0; i < *deep - 1; i++) {
                        luaL_addstring(buffer, " ");
                    }

                    // Add key
                    if (lua_type(L, -2) == LUA_TSTRING) {
                        size_t klen;
                        const char *kstr = lua_tolstring(L, -2, &klen);
                        luaL_addstring(buffer, "[\"");
                        luaL_addlstring(buffer, kstr, klen);
                        luaL_addstring(buffer, "\"]");
                    } else if (lua_type(L, -2) == LUA_TNUMBER) {
                        luaL_addchar(buffer, '[');
                        lua_pushvalue(L, -2);
                        luaL_addvalue(buffer);
                        luaL_addchar(buffer, ']');
                    } else {
                        luaL_addstring(buffer, "[\"");
                        to_string(L, -2, buffer, deep, visited, path_idx);
                        luaL_addstring(buffer, "\"]");
                    }

                    luaL_addstring(buffer, " = ");

                    // Add value
                    if (lua_type(L, -1) == LUA_TTABLE) {
                        lua_pushvalue(L, -1);
                        lua_rawget(L, visited);
                        if (lua_isnil(L, -1)) {
                            lua_pop(L, 1);
                            to_string(L, -1, buffer, deep, visited, path_idx);
                        } else {
                            luaL_addvalue(buffer);
                        }
                    } else {
                        to_string(L, -1, buffer, deep, visited, path_idx);
                    }

                    lua_pop(L, 1); // pop value
                }

                luaL_addstring(buffer, "\r\n");
                for (int i = 0; i < *deep - 2; i++) {
                    luaL_addstring(buffer, " ");
                }
                luaL_addchar(buffer, '}');
                *deep -= 2;
            }
            break;
        }

        default: {
            const char *str = luaJ_tolstring(L, idx, NULL);
            luaL_addstring(buffer, str);
            break;
        }
    }
}

int import_dump(lua_State *L) {
    luaL_checkany(L, 1);

    luaL_Buffer b;
    luaL_buffinit(L, &b);

    lua_newtable(L);
    int visited = lua_gettop(L);

    lua_pushliteral(L, "  ");
    int path_idx = lua_gettop(L);

    int deep = 0;
    to_string(L, 1, &b, &deep, visited, path_idx);

    luaL_pushresult(&b);
    return 1;
}