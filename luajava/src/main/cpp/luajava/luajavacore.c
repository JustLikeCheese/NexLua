#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include "jnihelper.h"
#include "luakit.h"
#include "luajava.h"
#include "luajavaapi.h"
#include "luajavacore.h"
#include "luacomp.h"

lua_State *luaJ_newstate() {
    lua_State *L = luaL_newstate();
    luaopen_luajava(L);
    lua_atpanic(L, fatalError);
    return L;
}

// Java Custom Loader
static int jmoduleLoad(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    const char *name = luaL_checkstring(L, 1);
    jstring moduleName = ToString(name);
    int ret = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI, com_luajava_JuaAPI_jmoduleLoad,
                                          (jlong) L, moduleName);
    DeleteString(moduleName);
    return checkOrError(env, L, ret);
}

int luaJ_initloader(lua_State *L) {
    lua_getglobal(L, "package");
    if (lua_isnil(L, -1)) {
        lua_pop(L, 1);
        return -1;
    }
    lua_getfield(L, -1, "loaders");
    if (lua_istable(L, -1) == 0) {
        lua_pop(L, 2);
        return -1;
    }
    int len = (int) lua_objlen(L, -1);
    lua_pushcfunction(L, &jmoduleLoad);
    lua_rawseti(L, -2, len + 1);
    lua_pop(L, 2);
    return 0;
}

// For template usage
const char JAVA_CLASS_META_REGISTRY[] = "__jclass__";
const char JAVA_OBJECT_META_REGISTRY[] = "__jobject__";
const char JAVA_ARRAY_META_REGISTRY[] = "__jarray__";

/* Java Common API */

/* Push API */
int luaJ_pushclass(JNIEnv *env, lua_State *L, jclass obj) {
    jobject global = (*env)->NewGlobalRef(env, obj);
    if (global) {
        jobject *userdata = (jobject *) lua_newuserdata(L, sizeof(jclass));
        *userdata = global;
        luaL_setmetatable(L, JAVA_CLASS_META_REGISTRY);
        return 1;
    }
    return 0;
}

int luaJ_pushobject(JNIEnv *env, lua_State *L, jobject obj) {
    jobject global = (*env)->NewGlobalRef(env, obj);
    if (global) {
        jobject *userdata = (jobject *) lua_newuserdata(L, sizeof(jobject));
        *userdata = global;
        luaL_setmetatable(L, JAVA_OBJECT_META_REGISTRY);
        return 1;
    }
    return 0;
}

int luaJ_pusharray(JNIEnv *env, lua_State *L, jarray arr) {
    jobject global = (*env)->NewGlobalRef(env, arr);
    if (global) {
        jobject *userdata = (jobject *) lua_newuserdata(L, sizeof(jarray));
        *userdata = global;
        luaL_setmetatable(L, JAVA_ARRAY_META_REGISTRY);
        return 1;
    }
    return 0;
}

/* Check API */
jclass luaJ_checkclass(lua_State *L, int index) {
    return *(jobject *) luaL_checkudata(L, index, JAVA_CLASS_META_REGISTRY);
}

jobject luaJ_checkobject(lua_State *L, int index) {
    return *(jobject *) luaL_checkudata(L, index, JAVA_OBJECT_META_REGISTRY);
}

jarray luaJ_checkarray(lua_State *L, int index) {
    return *(jarray *) luaL_checkudata(L, index, JAVA_ARRAY_META_REGISTRY);
}

jclass luaJ_toclass(lua_State *L, int index) {
    jobject *userdata = (jobject *) luaL_testudata(L, index, JAVA_CLASS_META_REGISTRY);
    return userdata ? *userdata : NULL;
}

jobject luaJ_toobject(lua_State *L, int index) {
    jobject *userdata = (jobject *) luaL_testudata(L, index, JAVA_OBJECT_META_REGISTRY);
    return userdata ? *userdata : NULL;
}

jarray luaJ_toarray(lua_State *L, int index) {
    jobject *userdata = (jobject *) luaL_testudata(L, index, JAVA_ARRAY_META_REGISTRY);
    return userdata ? (jarray) *userdata : NULL;
}

// jfunction wrapper
static inline int jfunctionWrapper(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    jobject jfunc = luaJ_checkobject(L, lua_upvalueindex(1));
    jint result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                              com_luajava_JuaAPI_jfunctionCall,
                                              (jlong) L,
                                              jfunc);
    return checkOrError(env, L, result);
}

void luaJ_pushfunction(JNIEnv *env, lua_State *L, jobject func) {
    luaJ_pushobject(env, L, func);
    lua_pushcclosure(L, &jfunctionWrapper, 1);
}

void luaJ_pushcclosure(JNIEnv *env, lua_State *L, jobject func, int n) {
    luaJ_pushobject(env, L, func);
    lua_pushcclosure(L, &jfunctionWrapper, n + 1);
}

#define bindMetaname(name) const char name[] = #name; // NOLINT(bugprone-reserved-identifier)
#define bindMetatable(name, func) \
    lua_pushcfunction(L, func); \
    lua_setfield(L, -2, name)

/*
 * LuaJIT2.1 ROLLING METAMETHOD
 * __index, __newindex
 * __concat, __len, __call
 * __pairs, __ipairs, __eq, __le, __lt
 * __gc, __mode, __tostring, __metatable
 * __add, __sub, __mul, __div, __mod, __pow, __unm */

static int commonConcat(lua_State *L) {
    luaJ_tostring(L, 1);
    luaJ_tostring(L, 2);
    lua_concat(L, 2);
    return 1;
}

/* Class Metatable */
static int classIndex(lua_State *L) {
    jclass clazz = luaJ_checkclass(L, 1);
    const char *name = luaL_checkstring(L, 2);
    JNIEnv *env = getJNIEnv(L);
    jstring fieldName = ToString(name);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                             com_luajava_JuaAPI_jclassIndex,
                                             (jlong) L, clazz, fieldName);
    DeleteString(fieldName);
    return checkOrError(env, L, result);
}

static int classNewIndex(lua_State *L) {
    jclass clazz = luaJ_checkclass(L, 1);
    const char *name = luaL_checkstring(L, 2);
    JNIEnv *env = getJNIEnv(L);
    if (lua_gettop(L) == 2) {
        jstring className = (*env)->CallStaticObjectMethod(env, java_lang_Class,
                                                            java_lang_Class_getName, clazz);
        luaJ_pushstring(env, L, className);
        lua_pushfstring(L, "@%s missing field value", name);
        lua_concat(L, 3);
        return lua_error(L);
    }
    jstring fieldName = ToString(name);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                             com_luajava_JuaAPI_jclassNewIndex,
                                             (jlong) L, clazz, fieldName);
    DeleteString(fieldName);
    return checkOrError(env, L, result);
}

static int classNew(lua_State *L) {
    jclass clazz = luaJ_checkclass(L, 1);
    JNIEnv *env = getJNIEnv(L);
    int top = lua_gettop(L);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                             com_luajava_JuaAPI_jclassNew,
                                             (jlong) L, clazz);
    return checkOrError(env, L, result);
}

static int classGC(lua_State *L) {
    jclass *obj = (jclass *) luaL_checkudata(L, 1, JAVA_CLASS_META_REGISTRY);
    JNIEnv *env = getJNIEnv(L);
    (*env)->DeleteGlobalRef(env, *obj);
    *obj = NULL;
    return 0;
}

static int classEquals(lua_State *L) {
    jclass class1 = luaJ_toclass(L, 1);
    jclass class2 = luaJ_toclass(L, 2);
    if (!class1 || !class2) {
        lua_pushboolean(L, false);
        return 1;
    }
    JNIEnv *env = getJNIEnv(L);
    jboolean same = (*env)->IsSameObject(env, class1, class2);
    lua_pushboolean(L, same ? true : false);
    return 1;
}

static int classToString(lua_State *L) {
    jclass clazz = luaJ_checkclass(L, 1);
    JNIEnv *env = getJNIEnv(L);
    jstring string = (*env)->CallObjectMethod(env, clazz, java_lang_Class_toString);
    if (checkIfError(env, L)) {
        return 0;
    }
    luaJ_pushstring(env, L, string);
    return 1;
}

static int className(lua_State *L) {
    jclass clazz = luaJ_checkclass(L, 1);
    JNIEnv *env = getJNIEnv(L);
    jstring name = (*env)->CallObjectMethod(env, clazz, java_lang_Class_getName);
    if (!checkIfError(env, L)) {
        luaJ_pushstring(env, L, name);
        return 1;
    }
    return lua_error(L);
}

/* Object Metatable */
static int objectGC(lua_State *L) {
    jobject *obj = (jobject *) luaL_checkudata(L, 1, JAVA_OBJECT_META_REGISTRY);
    if (*obj) {
        JNIEnv *env = getJNIEnv(L);
        (*env)->DeleteGlobalRef(env, *obj);
        *obj = NULL;
    }
    return 0;
}

static int objectIndex(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    jobject object = luaJ_checkobject(L, 1);
    const char *name = luaL_checkstring(L, 2);
    jstring methodName = ToString(name);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                             com_luajava_JuaAPI_jobjectIndex,
                                             (jlong) L, object, methodName);
    DeleteString(methodName);
    return checkOrError(env, L, result);
}

static int objectEquals(lua_State *L) {
    jobject obj1 = luaJ_toobject(L, 1);
    jobject obj2 = luaJ_toobject(L, 2);
    if (!obj1 || !obj2) {
        lua_pushboolean(L, false);
        return 1;
    }
    JNIEnv *env = getJNIEnv(L);
    jboolean same = (*env)->IsSameObject(env, obj1, obj2);
    lua_pushboolean(L, same ? true : false);
    return 1;
}

static int objectToString(lua_State *L) {
    jobject object = luaJ_checkobject(L, 1);
    JNIEnv *env = getJNIEnv(L);
    jstring string = (*env)->CallObjectMethod(env, object, java_lang_Object_toString);
    if (checkIfError(env, L)) {
        return 0;
    }
    luaJ_pushstring(env, L, string);
    return 1;
}

static int objectLength(lua_State *L) {
    jobject object = luaJ_checkobject(L, 1);
    JNIEnv *env = getJNIEnv(L);
    int result = (*env)->CallStaticIntMethod(env, com_luajava_JuaAPI,
                                             com_luajava_JuaAPI_jobjectLength,
                                             (jlong) L, object);
    return checkOrError(env, L, result);
}

/* Array Metatable */
static int arrayGC(lua_State *L) {
    jarray *array = (jobject *) luaL_checkudata(L, 1, JAVA_ARRAY_META_REGISTRY);
    if (*array) {
        JNIEnv *env = getJNIEnv(L);
        (*env)->DeleteGlobalRef(env, *array);
        *array = NULL;
    }
    return 0;
}

static int arrayEquals(lua_State *L) {
    jarray obj1 = luaJ_toarray(L, 1);
    jarray obj2 = luaJ_toarray(L, 2);
    if (!obj1 || !obj2) {
        lua_pushboolean(L, false);
        return 1;
    }
    JNIEnv *env = getJNIEnv(L);
    jboolean same = (*env)->IsSameObject(env, obj1, obj2);
    lua_pushboolean(L, same ? true : false);
    return 1;
}

static int arrayToString(lua_State *L) {
    jobject object = luaJ_checkarray(L, 1);
    JNIEnv *env = getJNIEnv(L);
    jstring string = (*env)->CallObjectMethod(env, object, java_lang_Object_toString);
    if (checkIfError(env, L)) {
        return 0;
    }
    luaJ_pushstring(env, L, string);
    return 1;
}

static int arrayLength(lua_State *L) {
    jarray object = luaJ_checkarray(L, 1);
    JNIEnv *env = getJNIEnv(L);
    lua_pushinteger(L, (*env)->GetArrayLength(env, object));
    return 1;
}

/* Common Metatable */
bindMetaname(__index)
bindMetaname(__newindex)
bindMetaname(__concat)
bindMetaname(__call)
bindMetaname(__tostring)
bindMetaname(__len)
bindMetaname(__eq)
bindMetaname(__lt)
bindMetaname(__le)
bindMetaname(__pairs)
bindMetaname(__ipairs)
bindMetaname(__metatable)
bindMetaname(__gc)
bindMetaname(__mode)
bindMetaname(__add)
bindMetaname(__sub)
bindMetaname(__mul)
bindMetaname(__div)
bindMetaname(__mod)
bindMetaname(__pow)
bindMetaname(__unm)

/*
 * LuaJIT2.1 ROLLING METAMETHOD
 * __index, __newindex
 * __concat, __call, __tostring
 * __len, __eq, __lt, __le
 * __pairs, __ipairs, __metatable, __gc, __mode
 * __add, __sub, __mul, __div, __mod, __pow, __unm */

/* Initialize Metatable Registry */
void initMetaRegistry(lua_State *L) {
    // Java Class
    if (luaL_newmetatable(L, JAVA_CLASS_META_REGISTRY) == 1) {
        bindMetatable(__eq, &classEquals); // equal
        bindMetatable(__gc, &classGC); // gc
        bindMetatable(__index, &classIndex); // index
        bindMetatable(__newindex, &classNewIndex); // newindex
        bindMetatable(__call, &classNew); // call
        bindMetatable(__tostring, &classToString); // tostring
        bindMetatable(__len, &className); // length
        bindMetatable(__concat, &commonConcat); // concat
    }
    lua_pop(L, 1);
    // Java Object
    if (luaL_newmetatable(L, JAVA_OBJECT_META_REGISTRY) == 1) {
        bindMetatable(__eq, &objectEquals); // equal
        bindMetatable(__gc, &objectGC); // gc
        bindMetatable(__index, &objectIndex); // index
        bindMetatable(__tostring, &objectToString); // tostring
        bindMetatable(__len, &objectLength); // length
        bindMetatable(__concat, &commonConcat); // concat
    }
    lua_pop(L, 1);
    // Java Array
    if (luaL_newmetatable(L, JAVA_ARRAY_META_REGISTRY) == 1) {
        bindMetatable(__eq, &arrayEquals); // equal
        bindMetatable(__gc, &arrayGC); // gc
        bindMetatable(__tostring, &arrayToString); // tostring
        bindMetatable(__len, &arrayLength); // length
        bindMetatable(__concat, &commonConcat); // concat
    }
    lua_pop(L, 1);
}