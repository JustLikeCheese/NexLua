#include <string.h>
#include "luajavaapi.h"
#include "luajavacore.h"

// error handler
int fatalError(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    (*env)->FatalError(env, lua_tostring(L, -1));
    return 0;
}

bool checkIfError(JNIEnv *env, lua_State *L) {
    jthrowable e = (*env)->ExceptionOccurred(env);
    if (e == NULL) {
        return false;
    }
    (*env)->ExceptionClear(env);
    jstring message = (jstring) (*env)->CallObjectMethod(env, e, java_lang_Throwable_toString);
    const char *str = (*env)->GetStringUTFChars(env, message, NULL);
    lua_pushstring(L, str);
    (*env)->ReleaseStringUTFChars(env, message, str);
    (*env)->DeleteLocalRef(env, (jobject) message);
    luaJ_pushobject(env, L, (jobject) e);
    lua_setglobal(L, GLOBAL_THROWABLE);
    // https://stackoverflow.com/q/33481144/17780636
    // env->DeleteLocalRef(e);
    return true;
}

int checkOrError(JNIEnv *env, lua_State *L, jint ret) {
    if (!checkIfError(env, L) && ret >= 0) {
        lua_pushnil(L);
        lua_setglobal(L, GLOBAL_THROWABLE);
        return (int) ret;
    }
    return lua_error(L);
}

// buffer
jobject luaJ_javabuffer(JNIEnv *env, lua_State *L, const void *ptr, jint size) {
    jobject buffer = (*env)->CallStaticObjectMethod(env, com_luajava_JuaAPI,
                                                    com_luajava_JuaAPI_allocateDirectBuffer,
                                                    (jint) size);
    if (checkIfError(env, L)) {
        return NULL;
    }
    void *bufferAddress = (*env)->GetDirectBufferAddress(env, buffer);
    memcpy(bufferAddress, ptr, size);
    return buffer;
}

jobject luaJ_tojavabuffer(JNIEnv *env, lua_State *L, int i) {
    size_t len;
    const char *str = lua_tolstring(L, i, &len);
    if (str == NULL) {
        return NULL;
    }
    return luaJ_javabuffer(env, L, str, (jint) len);
}

jobject luaJ_todirectbuffer(JNIEnv *env, lua_State *L, int i) {
    size_t len;
    const void *str = lua_tolstring(L, i, &len);
    if (str == NULL) {
        return NULL;
    }
    jobject buffer = (*env)->NewDirectByteBuffer(env, (void *) str, (jlong) len);
    if (checkIfError(env, L)) {
        return NULL;
    }
    return buffer;
}

void luaJ_pushbuffer(JNIEnv *env, lua_State *L, jobject obj_buffer) {
    unsigned char *buffer = obj_buffer ? (unsigned char *) (*env)->GetDirectBufferAddress(
            env,
            obj_buffer)
                                       : NULL;
    if (buffer == NULL) return lua_pushnil(L);
    lua_pushstring(L, (char *) buffer);
}