#ifndef LUAJAVAAPI_H
#define LUAJAVAAPI_H

#include <stdbool.h>
#include "jnihelper.h"
#include "luakit.h"

// error handler
#define GLOBAL_THROWABLE "__jthrowable__"

int fatalError(lua_State *L);

bool checkIfError(JNIEnv *env, lua_State *L);

int checkOrError(JNIEnv *env, lua_State *L, jint ret);

// buffer
jobject luaJ_javabuffer(JNIEnv *env, lua_State *L, const void *ptr, jint size);

jobject luaJ_tojavabuffer(JNIEnv *env, lua_State *L, int i);

jobject luaJ_todirectbuffer(JNIEnv *env, lua_State *L, int i);

void luaJ_pushbuffer(JNIEnv *env, lua_State *L, jobject obj_buffer);

#endif // LUAJAVAAPI_H
