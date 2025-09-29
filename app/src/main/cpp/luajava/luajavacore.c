#include <stdbool.h>
#include <string.h>
#include <malloc.h>
#include <math.h>
#include "jnihelper.h"
#include "luakit.h"
#include "luajava.h"
#include "luajavaapi.h"
#include "luajavacore.h"

lua_State *luaJ_newstate() {
    lua_State *L = luaL_newstate();
    luaopen_luajava(L);
    lua_atpanic(L, fatalError);
    return L;
}

// For template usage
const char JAVA_CLASS_META_REGISTRY[] = "__jclass__";
const char JAVA_OBJECT_META_REGISTRY[] = "__jobject__";
const char JAVA_ARRAY_META_REGISTRY[] = "__jarray__";

/* Java Common API */
static inline int gc_common(lua_State *L, const char *meta_registry) {
    jobject *udata = (jobject *) luaL_checkudata(L, 1, meta_registry);
    if (*udata) {
        JNIEnv *env = getJNIEnv(L);
        (*env)->DeleteGlobalRef(env, *udata);
        *udata = NULL;
    }
    return 0;
}

static inline int pushJ(lua_State *L, jobject obj, const char *metatable) {
    jobject *data = (jobject *) lua_newuserdata(L, sizeof(jobject));
    *data = obj;
    luaL_setmetatable(L, metatable);
    return 1;
}

/* Push API */
int luaJ_pushclass(JNIEnv *env, lua_State *L, jobject obj) {
    jobject global = (*env)->NewGlobalRef(env, obj);
    if (global != NULL) {
        return pushJ(L, global, JAVA_CLASS_META_REGISTRY);
    }
    return 0;
}

int luaJ_pushobject(JNIEnv *env, lua_State *L, jobject obj) {
    jobject global = (*env)->NewGlobalRef(env, obj);
    if (global != NULL) {
        return pushJ(L, global, JAVA_OBJECT_META_REGISTRY);
    }
    return 0;
}

int luaJ_pusharray(JNIEnv *env, lua_State *L, jobject obj) {
    jobject global = (*env)->NewGlobalRef(env, obj);
    if (global != NULL) {
        return pushJ(L, global, JAVA_ARRAY_META_REGISTRY);
    }
    return 0;
}

/* Check API */
jclass luaJ_checkclass(lua_State *L, int index) {
    return *(jclass *) luaL_checkudata(L, index, JAVA_CLASS_META_REGISTRY);
}

jobject luaJ_checkobject(lua_State *L, int index) {
    return (jobject *) luaL_checkudata(L, index, JAVA_OBJECT_META_REGISTRY);
}

jarray luaJ_checkarray(lua_State *L, int index) {
    return (jarray *) luaL_checkudata(L, index, JAVA_ARRAY_META_REGISTRY);
}

jclass luaJ_toclass(lua_State *L, int index) {
    return (jclass *) luaL_testudata(L, index, JAVA_CLASS_META_REGISTRY);
}

jobject luaJ_toobject(lua_State *L, int index) {
    return (jobject *) luaL_testudata(L, index, JAVA_OBJECT_META_REGISTRY);
}

jarray luaJ_toarray(lua_State *L, int index) {
    return (jarray *) luaL_testudata(L, index, JAVA_ARRAY_META_REGISTRY);
}

// jfunction wrapper
static inline int jfunctionWrapper(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    jobject *udata = (jobject *) lua_touserdata(L, lua_upvalueindex(1));
    jint result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                              com_luajava_JuaAPI_jfunctionCall,
                                              L,
                                              *udata);
    checkOrError(env, L, result);
    return result;
}

void luaJ_pushfunction(JNIEnv *env, lua_State *L, jobject func) {
    luaJ_pushobject(env, L, func);
    lua_pushcclosure(L, &jfunctionWrapper, 1);
}

//int luaJ_initloader(lua_State *L) {
//    return luaJ_insertloader(L, "loaders");
//}
#define Metatable(name, tname, body) static int name ## tname (lua_State *L) body
#define bindMetatable(name, tname) \
    lua_pushcfunction(L, name ## tname); \
    lua_setfield(L, -2, #tname)

/* Class Metatable */
Metatable(class, __gc, {
    return gc_common(L, JAVA_CLASS_META_REGISTRY);
})

Metatable(class, __eq, {
    jobject udata1 = luaJ_toobject(L, 1);
    jobject udata2 = luaJ_toobject(L, 2);
    if (udata1 == NULL || udata2 == NULL) {
        return 0;
    }
    JNIEnv *env = getJNIEnv(L);
    jboolean same = (*env)->IsSameObject(env, udata1, udata2);
    lua_pushboolean(L, same ? true : false);
    return 1;
})

Metatable(class, __index, {
    jclass clazz = luaJ_checkclass(L, 1);
    const char *name = luaL_checkstring(L, 2);
    JNIEnv *env = getJNIEnv(L);
    jstring jstr = (*env)->NewStringUTF(env, name);
    jint returnValue = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                                   com_luajava_JuaAPI_jclassIndex, jstr);
    (*env)->DeleteLocalRef(env, jstr);
    return checkOrError(env, L, returnValue);
})

/* Object Metatable */
Metatable(object, __gc, {
    return gc_common(L, JAVA_OBJECT_META_REGISTRY);
})

/* Array Metatable */
Metatable(array, __gc, {
    return gc_common(L, JAVA_ARRAY_META_REGISTRY);
})

/* Common Metatable */
Metatable(common, __tostring, {
    jobject *udata = (jobject *) lua_touserdata(L, 1);
    if (*udata) {
        JNIEnv *env = getJNIEnv(L);
        jstring string = (*env)->CallObjectMethod(env, *udata, java_lang_Class_toString);
        if (checkIfError(env, L)) {
            return 0;
        }
        const char *c_string = GetString(string);
        lua_pushstring(L, c_string);
        ReleaseString(string, c_string);
        return 1;
    }
    lua_pushnil(L);
    return 1;
})

/*
 * LuaJIT2.1 ROLLING METAMETHOD
 * __index, __newindex
 * __concat, __len, __call
 * __pairs, __ipairs
 * __eq, __le, __le
 * __add, __sub, __mul, __div, __mod, __pow, __unm */
void initMetaRegistry(lua_State *L) {
    // Java Class
    if (luaL_newmetatable(L, JAVA_CLASS_META_REGISTRY) == 1) {
        bindMetatable(class, __eq); // equal
        bindMetatable(class, __gc); // gc
        bindMetatable(common, __tostring); // tostring
    }
    lua_pop(L, 1);
    // Java Object
    if (luaL_newmetatable(L, JAVA_OBJECT_META_REGISTRY) == 1) {
        bindMetatable(object, __gc); // gc
        bindMetatable(common, __tostring); // tostring
    }
    lua_pop(L, 1);
    // Java Array
    if (luaL_newmetatable(L, JAVA_ARRAY_META_REGISTRY) == 1) {
        bindMetatable(array, __gc); // gc
        bindMetatable(common, __tostring); // tostring
    }
    lua_pop(L, 1);
}
