#ifndef JNIHELPER_H
#define JNIHELPER_H

#include <jni.h>
#include <android/log.h>
#include "luakit.h"

// java class binding
extern jclass java_lang_Class;
extern jmethodID java_lang_Class_forName;
extern jmethodID java_lang_Class_toString;
extern jclass java_lang_Throwable;
extern jmethodID java_lang_Throwable_getMessage;
extern jmethodID java_lang_Throwable_toString;

extern jclass com_luajava_JuaAPI;
extern jmethodID com_luajava_JuaAPI_jclassIndex;
extern jmethodID com_luajava_JuaAPI_jfunctionCall;
extern jmethodID com_luajava_JuaAPI_allocateDirectBuffer;

/* some useful macros */
#define CHECK_NULL(...) if (!(__VA_ARGS__)) return -1;
#define JNIWRAP(RET, NAME, ...) \
    JNIEXPORT RET JNICALL Java_com_luajava_LuaNatives_##NAME(JNIEnv* env, jobject thiz, ##__VA_ARGS__)

#define JNIWRAP_STATIC(RET, NAME, ...) \
    JNIEXPORT RET JNICALL Java_com_luajava_LuaNatives_##NAME(JNIEnv* env, jclass thiz, ##__VA_ARGS__)

#define ToString(c_string) (c_string ? (*env)->NewStringUTF(env, c_string) : NULL)
#define GetString(j_string) (j_string ? (*env)->GetStringUTFChars(env, j_string, 0) : NULL)
#define ReleaseString(j_string, c_string) if (c_string) (*env)->ReleaseStringUTFChars(env, j_string, c_string)
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_DEBUG, "LuaJava", fmt, ##__VA_ARGS__)

#ifdef __GNUC__
#define DEPRECATED(msg) __attribute__((deprecated(msg)))
#else
#define DEPRECATED(msg)
#endif

int updateJNIEnv(JNIEnv *env);

JNIEnv *getJNIEnv(lua_State *luaState);

int initJNIBindings(JNIEnv *env);

jclass bindJavaClass(JNIEnv *env, const char *name);

jmethodID bindJavaStaticMethod(JNIEnv *env, jclass c, const char *name, const char *sig);

jmethodID bindJavaMethod(JNIEnv *env, jclass c, const char *name, const char *sig);

#endif //JNIHELPER_H