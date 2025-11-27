#ifndef LUAJAVA_CORE_H
#define LUAJAVA_CORE_H

#include "luakit.h"
#include "jnihelper.h"
#include <stdbool.h>

extern const char JAVA_CLASS_META_REGISTRY[];
extern const char JAVA_OBJECT_META_REGISTRY[];
extern const char JAVA_ARRAY_META_REGISTRY[];

extern const char JAVA_OBJECT_ID[];
#define JAVA_TYPE_OBJECT  1
#define JAVA_TYPE_CLASS   2
#define JAVA_TYPE_ARRAY   3

void initMetaRegistry(lua_State *L);

LUALIB_API lua_State *luaJ_newstate();
LUALIB_API int luaJ_initloader(lua_State* L);

LUALIB_API int luaJ_pushclass(JNIEnv *env, lua_State *L, jclass clazz);
LUALIB_API int luaJ_pushobject(JNIEnv *env, lua_State *L, jobject obj);
LUALIB_API int luaJ_pusharray(JNIEnv *env, lua_State *L, jarray arr);
LUALIB_API void luaJ_pushfunction(JNIEnv *env, lua_State *L, jobject func);
LUALIB_API void luaJ_pushcclosure(JNIEnv *env, lua_State *L, jobject func, int n);

LUALIB_API int luaJ_getjavatype(lua_State *L, int index);
LUALIB_API int luaJ_isanyobject(lua_State *L, int index);
LUALIB_API jobject luaJ_toanyobject(lua_State *L, int index);
LUALIB_API jobject luaJ_checkanyobject(lua_State *L, int index);

LUALIB_API int luaJ_isclass(lua_State *L, int index);
LUALIB_API int luaJ_isobject(lua_State *L, int index);
LUALIB_API int luaJ_isarray(lua_State *L, int index);

LUALIB_API jclass luaJ_checkclass(lua_State *L, int index);
LUALIB_API jobject luaJ_checkobject(lua_State *L, int index);
LUALIB_API jarray luaJ_checkarray(lua_State *L, int index);

LUALIB_API jclass luaJ_toclass(lua_State *L, int index);
LUALIB_API jobject luaJ_toobject(lua_State *L, int index);
LUALIB_API jarray luaJ_toarray(lua_State *L, int index);

LUALIB_API int luaJ_invokespecial(JNIEnv *env, lua_State *L, jclass clazz,
                                  const char *method, const char *sig,
                                  jobject obj, const char *params);

#endif