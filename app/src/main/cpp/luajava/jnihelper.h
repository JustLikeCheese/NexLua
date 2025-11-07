#ifndef JNIHELPER_H
#define JNIHELPER_H

#include <jni.h>
#include <android/log.h>
#include "luakit.h"

// java class binding
#ifndef JNI_HELPER_EXTERN
#define JNI_HELPER_EXTERN extern
#endif

// java.lang.Object
JNI_HELPER_EXTERN jclass java_lang_Object;
JNI_HELPER_EXTERN jmethodID java_lang_Object_toString;

// java.lang.Class
JNI_HELPER_EXTERN jclass java_lang_Class;
JNI_HELPER_EXTERN jmethodID java_lang_Class_forName;
JNI_HELPER_EXTERN jmethodID java_lang_Class_toString;
JNI_HELPER_EXTERN jmethodID java_lang_Class_getName;

// java.lang.Throwable
JNI_HELPER_EXTERN jclass java_lang_Throwable;
JNI_HELPER_EXTERN jmethodID java_lang_Throwable_getMessage;
JNI_HELPER_EXTERN jmethodID java_lang_Throwable_toString;

// com.luajava.JuaAPI
JNI_HELPER_EXTERN jclass com_luajava_JuaAPI;
JNI_HELPER_EXTERN jmethodID com_luajava_JuaAPI_jclassIndex;
JNI_HELPER_EXTERN jmethodID com_luajava_JuaAPI_jclassNew;
JNI_HELPER_EXTERN jmethodID com_luajava_JuaAPI_jobjectIndex;
JNI_HELPER_EXTERN jmethodID com_luajava_JuaAPI_jfunctionCall;
JNI_HELPER_EXTERN jmethodID com_luajava_JuaAPI_jmoduleLoad;
JNI_HELPER_EXTERN jmethodID com_luajava_JuaAPI_allocateDirectBuffer;

/* some useful macros */
#define CHECK_NULL(...) if (!(__VA_ARGS__)) return -1;
#define ToString(c_string) (c_string ? (*env)->NewStringUTF(env, c_string) : NULL)
#define DeleteString(j_string) if (j_string) (*env)->DeleteLocalRef(env, j_string)
#define GetString(j_string) (j_string ? (*env)->GetStringUTFChars(env, j_string, 0) : NULL)
#define ReleaseString(j_string, c_string) if (c_string) (*env)->ReleaseStringUTFChars(env, j_string, c_string)
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_DEBUG, "LuaJava", fmt, ##__VA_ARGS__);
#define UseString(j_string, c_string, code) const char* c_string = GetString(j_string); code; ReleaseString(j_string, c_string)

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