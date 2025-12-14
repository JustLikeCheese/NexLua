#include "nexlua.h"
#include "string.h"

static void add_quoted(luaL_Buffer *b, const char *s, size_t len) {
    luaL_addchar(b, '"');
    while (len--) {
        uint32_t c = (uint32_t)(uint8_t)*s++;
        if (c == '"' || c == '\\') {
            luaL_addchar(b, '\\');
            luaL_addchar(b, (char)c);
        } else if (c == '\n') {
            luaL_addchar(b, '\\');
            luaL_addchar(b, 'n');
        } else if (c < 32 || c == 127) {
            uint32_t d;
            luaL_addchar(b, '\\');
            if (c >= 100 || (len > 0 && *s >= '0' && *s <= '9')) {
                luaL_addchar(b, (char)('0' + (c >= 100)));
                if (c >= 100) c -= 100;
                goto tens;
            } else if (c >= 10) {
                tens:
                d = (c * 205) >> 11;
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

static void to_string(lua_State *L, int idx, luaL_Buffer *buffer, int *deep, int visited, int current_path) {
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
            int abs_idx = idx;
            if (idx < 0) {
                abs_idx = lua_gettop(L) + idx + 1;
            }

            // Check circular reference
            lua_pushvalue(L, abs_idx);
            lua_rawget(L, visited);
            if (!lua_isnil(L, -1)) {
                size_t path_len;
                const char *path = lua_tolstring(L, -1, &path_len);

                lua_pushvalue(L, current_path);
                size_t current_len;
                const char *current = lua_tolstring(L, -1, &current_len);

                int is_circular = 0;
                if (path_len <= current_len) {
                    if (strncmp(path, current, path_len) == 0) {
                        if (current_len == path_len || current[path_len] == '[') {
                            is_circular = 1;
                        }
                    }
                }

                lua_pop(L, 1); // pop current path string

                if (path_len == 0) {
                    luaL_addstring(buffer, "<root>");
                } else {
                    luaL_addlstring(buffer, path, path_len);
                }

                if (is_circular) {
                    luaL_addstring(buffer, " -- circular reference");
                }

                lua_pop(L, 1);
                return;
            }
            lua_pop(L, 1);

            // Mark as visited with current path
            lua_pushvalue(L, abs_idx);
            lua_pushvalue(L, current_path);
            lua_rawset(L, visited);

            *deep += 2;
            luaL_addchar(buffer, '{');

            lua_pushnil(L);
            int first = 1;

            while (lua_next(L, abs_idx)) {
                if (!first) {
                    luaL_addchar(buffer, ',');
                }
                first = 0;

                luaL_addstring(buffer, "\n");
                for (int i = 0; i < *deep; i++) {
                    luaL_addchar(buffer, ' ');
                }

                // Build key string for new path
                const char *key_str = NULL;
                size_t key_str_len = 0;

                if (lua_type(L, -2) == LUA_TSTRING) {
                    key_str = lua_tolstring(L, -2, &key_str_len);
                    lua_pushfstring(L, "[\"%s\"]", key_str);
                } else if (lua_type(L, -2) == LUA_TNUMBER) {
                    lua_pushfstring(L, "[%d]", (int)lua_tonumber(L, -2));
                } else {
                    lua_pushstring(L, "[?]");
                }
                int key_part_idx = lua_gettop(L);

                // Add key to output
                if (lua_type(L, -3) == LUA_TSTRING) {
                    size_t klen;
                    const char *kstr = lua_tolstring(L, -3, &klen);
                    luaL_addchar(buffer, '[');
                    add_quoted(buffer, kstr, klen);
                    luaL_addchar(buffer, ']');
                } else if (lua_type(L, -3) == LUA_TNUMBER) {
                    luaL_addchar(buffer, '[');
                    lua_pushvalue(L, -3);
                    luaL_addvalue(buffer);
                    luaL_addchar(buffer, ']');
                } else {
                    luaL_addchar(buffer, '[');
                    size_t klen;
                    const char *kstr = luaJ_tolstring(L, -3, &klen);
                    if (kstr) {
                        luaL_addlstring(buffer, kstr, klen);
                        lua_pop(L, 1);
                    }
                    luaL_addchar(buffer, ']');
                }

                luaL_addstring(buffer, " = ");

                // Build new path for nested tables
                lua_pushvalue(L, current_path);
                lua_pushvalue(L, key_part_idx);
                lua_concat(L, 2);
                int new_path_idx = lua_gettop(L);

                // Add value
                to_string(L, -3, buffer, deep, visited, new_path_idx);

                lua_pop(L, 2); // pop new_path and key_part
                lua_pop(L, 1); // pop value
            }

            // Unmark as visited
            lua_pushvalue(L, abs_idx);
            lua_pushnil(L);
            lua_rawset(L, visited);

            if (!first) {
                luaL_addstring(buffer, "\n");
                for (int i = 0; i < *deep - 2; i++) {
                    luaL_addchar(buffer, ' ');
                }
            }
            luaL_addchar(buffer, '}');
            *deep -= 2;
            break;
        }

        default: {
            size_t len;
            const char *str = luaJ_tolstring(L, idx, &len);
            if (str) {
                luaL_addlstring(buffer, str, len);
                lua_pop(L, 1);
            }
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

    lua_pushstring(L, "");
    int path_idx = lua_gettop(L);

    int deep = 0;
    to_string(L, 1, &b, &deep, visited, path_idx);

    luaL_pushresult(&b);
    return 1;
}

REGISTER_MODULE(dump, luaopen_dump);
int luaopen_dump(lua_State *L) {
    lua_pushcfunction(L, import_dump);
    lua_pushvalue(L, -1);
    lua_setglobal(L, "dump");
    return 1;
}