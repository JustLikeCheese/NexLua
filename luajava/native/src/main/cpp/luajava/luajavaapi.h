#ifndef LUAJAVAAPI_H
#define LUAJAVAAPI_H

#include <stdbool.h>
#include "jnihelper.h"
#include "luakit.h"

// error handler
#define JAVA_GLOBAL_THROWABLE "__java_throwable__"

int fatalError(lua_State *L);

bool checkIfError(JNIEnv *env, lua_State *L);

int checkOrError(JNIEnv *env, lua_State *L, jint ret);

// buffer
jobject luaJ_dump(JNIEnv* env, lua_State* L);

jobject luaJ_javabuffer_new(JNIEnv *env, lua_State *L, const void *ptr, jint size);

jobject luaJ_tojavabuffer(JNIEnv *env, lua_State *L, int i);

jobject luaJ_javadirectbuffer_new(JNIEnv *env, lua_State *L, int i);

void luaJ_pushbuffer(JNIEnv *env, lua_State *L, jobject obj_buffer);

void luaJ_pushstring(JNIEnv *env, lua_State *L, jstring string);

int luaJ_getfield(JNIEnv *env, lua_State *L, jclass class, jfieldID field, char type);

int luaJ_callmethod(JNIEnv *env, lua_State *L, jclass class, jmethodID field, char type);

#endif // LUAJAVAAPI_H
