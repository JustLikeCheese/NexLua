#ifndef LUAJAVA_CORE_H
#define LUAJAVA_CORE_H

#include "luakit.h"
#include "jnihelper.h"
#include <stdbool.h>

extern const char JAVA_CLASS_META_REGISTRY[];
extern const char JAVA_ARRAY_META_REGISTRY[];

void initMetaRegistry(lua_State *L);

LUALIB_API lua_State *luaJ_newstate();
LUALIB_API int luaJ_initloader(lua_State* L);

LUALIB_API int luaJ_pushclass(JNIEnv *env, lua_State *L, jclass clazz);
LUALIB_API int luaJ_pushobject(JNIEnv *env, lua_State *L, jobject obj);
LUALIB_API int luaJ_pusharray(JNIEnv *env, lua_State *L, jarray arr);
LUALIB_API void luaJ_pushfunction(JNIEnv *env, lua_State *L, jobject func);
LUALIB_API void luaJ_pushcclosure(JNIEnv *env, lua_State *L, jobject func, int n);

LUALIB_API jclass luaJ_checkclass(lua_State *L, int index);
LUALIB_API jobject luaJ_checkobject(lua_State *L, int index);
LUALIB_API jarray luaJ_checkarray(lua_State *L, int index);

LUALIB_API jclass luaJ_toclass(lua_State *L, int index);
LUALIB_API jobject luaJ_toobject(lua_State *L, int index);
LUALIB_API jarray luaJ_toarray(lua_State *L, int index);

#define luaJ_isclass(L, index) (luaJ_toclass(L, index) != NULL)
#define luaJ_isobject(L, index) (luaJ_toobject(L, index) != NULL)
#define luaJ_isarray(L, index) (luaJ_toarray(L, index) != NULL)

LUALIB_API int luaJ_invokespecial(JNIEnv *env, lua_State *L, jclass clazz,
                                  const char *method, const char *sig,
                                  jobject obj, const char *params);

#define luaJ_toanyobject(L, index) \
    (luaJ_toobject(L, index) ? luaJ_toobject(L, index) : \
     (luaJ_toclass(L, index) ? luaJ_toclass(L, index) : \
      luaJ_toarray(L, index)))

#define luaJ_isanyobject(L, index) (luaJ_toanyobject(L, index) != NULL)

#endif