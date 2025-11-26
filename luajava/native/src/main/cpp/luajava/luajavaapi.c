#include <string.h>
#include "luajavaapi.h"
#include "luajavacore.h"
#include "luacomp.h"

// error handler
int fatalError(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    (*env)->FatalError(env, lua_tostring(L, -1));
    return 0;
}

bool checkIfError(JNIEnv *env, lua_State *L) {
    jthrowable e = (*env)->ExceptionOccurred(env);
    if (e == NULL) {
        return 0;
    }
    (*env)->ExceptionClear(env);
    luaJ_pushobject(env, L, e);
    lua_setglobal(L, JAVA_GLOBAL_THROWABLE);
    jstring message = (jstring) (*env)->CallStaticObjectMethod(env, com_luajava_JuaAPI, com_luajava_JuaAPI_getStackTrace, e);
    luaJ_pushstring(env, L, message);
    // https://stackoverflow.com/q/33481144/17780636
    // env->DeleteLocalRef(e);
    return 1;
}

int checkOrError(JNIEnv *env, lua_State *L, jint ret) {
    if (!checkIfError(env, L) && ret >= 0) {
        return (int) ret;
    }
    return lua_error(L);
}

// buffer
jobject luaJ_dump(JNIEnv *env, lua_State *L) {
    DumpBuffer dumpBuffer;
    if (!luaJ_dumptobuffer(L, &dumpBuffer)) {
        return luaJ_javabuffer_new(env, L, dumpBuffer.buffer, (jint) dumpBuffer.size);
    }
    return NULL;
}

jobject luaJ_javabuffer_new(JNIEnv *env, lua_State *L, const void *ptr, jint size) {
    jobject buffer = (*env)->CallStaticObjectMethod(env, com_luajava_JuaAPI,
                                                    com_luajava_JuaAPI_allocateDirectBuffer,
                                                    (jint) size);
    if (checkIfError(env, L)) return NULL;
    void *bufferAddress = (*env)->GetDirectBufferAddress(env, buffer);
    memcpy(bufferAddress, ptr, size);
    return buffer;
}

jobject luaJ_javadirectbuffer_new(JNIEnv *env, lua_State *L, int i) {
    size_t len;
    const void *str = lua_tolstring(L, i, &len);
    if (str == NULL) return NULL;
    jobject buffer = (*env)->NewDirectByteBuffer(env, (void *) str, (jlong) len);
    if (checkIfError(env, L)) return NULL;
    return buffer;
}

jobject luaJ_tojavabuffer(JNIEnv *env, lua_State *L, int i) {
    size_t len;
    const char *str = lua_tolstring(L, i, &len);
    if (str == NULL) {
        return NULL;
    }
    return luaJ_javabuffer_new(env, L, str, (jint) len);
}

jobject luaJ_tojavadirectbuffer(JNIEnv *env, lua_State *L, int i) {
    size_t len;
    const char *str = lua_tolstring(L, i, &len);
    if (str == NULL) {
        return NULL;
    }
    return luaJ_javadirectbuffer_new(env, L, (jint) len);
}

int luaJ_dojavabuffer(JNIEnv *env, lua_State *L, jobject obj_buffer, int size, char *const name) {
    unsigned char *buffer = obj_buffer ? (*env)->GetDirectBufferAddress(env, obj_buffer) : NULL;
    return luaJ_dobuffer(L, buffer, size, name);
}

void luaJ_pushbuffer(JNIEnv *env, lua_State *L, jobject obj_buffer) {
    unsigned char *buffer = obj_buffer ? (unsigned char *) (*env)->GetDirectBufferAddress(
            env,
            obj_buffer)
                                       : NULL;
    if (buffer == NULL) return lua_pushnil(L);
    lua_pushstring(L, (char *) buffer);
}

void luaJ_pushstring(JNIEnv *env, lua_State *L, jstring string) {
    UseString(string, str, {
        lua_pushstring(L, str);
    });
}

int luaJ_getfield(JNIEnv *env, lua_State *L, jclass class, jfieldID field, char type) {
    switch (type) {
        case 'Z': // boolean
            lua_pushboolean(L, (jboolean) (*env)->GetStaticBooleanField(env, class, field));
            break;
        case 'C': // char
            lua_pushinteger(L, (jchar) (*env)->GetStaticCharField(env, class, field));
            break;
        case 'B': // byte
            lua_pushinteger(L, (jbyte) (*env)->GetStaticByteField(env, class, field));
            break;
        case 'S': // short
            lua_pushinteger(L, (jshort) (*env)->GetStaticShortField(env, class, field));
            break;
        case 'I': // int
            lua_pushinteger(L, (jint) (*env)->GetStaticIntField(env, class, field));
            break;
        case 'J': // long
            lua_pushnumber(L, (lua_Number) (jlong) (*env)->GetStaticLongField(env, class, field));
            break;
        case 'F': // float
            lua_pushnumber(L, (jfloat) (*env)->GetStaticFloatField(env, class, field));
            break;
        case 'D': // double
            lua_pushnumber(L, (jdouble) (*env)->GetStaticDoubleField(env, class, field));
            break;
        case 'L': // object
            luaJ_pushobject(env, L, (jobject) (*env)->GetStaticObjectField(env, class, field));
            break;
        default:
            return 0;
    }
    return 1;
}

int luaJ_callmethod(JNIEnv *env, lua_State *L, jclass class, jmethodID field, char type) {
    switch (type) {
        case 'Z': // boolean
            lua_pushboolean(L, (jboolean) (*env)->CallStaticBooleanMethod(env, class, field));
            break;
        case 'C': // char
            lua_pushinteger(L, (jchar) (*env)->CallStaticCharMethod(env, class, field));
            break;
        case 'B': // byte
            lua_pushinteger(L, (jbyte) (*env)->CallStaticByteMethod(env, class, field));
            break;
        case 'S': // short
            lua_pushinteger(L, (jshort) (*env)->CallStaticShortMethod(env, class, field));
            break;
        case 'I': // int
            lua_pushinteger(L, (jint) (*env)->CallStaticIntMethod(env, class, field));
            break;
        case 'J': // long
            lua_pushnumber(L, (lua_Number) (jlong) (*env)->CallStaticLongMethod(env, class, field));
            break;
        case 'F': // float
            lua_pushnumber(L, (jfloat) (*env)->CallStaticFloatMethod(env, class, field));
            break;
        case 'D': // double
            lua_pushnumber(L, (jdouble) (*env)->CallStaticDoubleMethod(env, class, field));
            break;
        case 'L': // object
            luaJ_pushobject(env, L, (jobject) (*env)->CallStaticObjectMethod(env, class, field));
            break;
        default:
            return 0;
    }
    return 1;
}
