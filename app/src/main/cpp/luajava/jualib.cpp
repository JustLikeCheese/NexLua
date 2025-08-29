#include "jua.h"
#include "juaapi.h"
#include "jualib.h"

#include <cstddef>
#include <cstring>

static int javaMethod(lua_State *L) {
    if (luaL_testudata(L, 1, JAVA_OBJECT_META_REGISTRY) != NULL) {
        return jobjectSigCall(L);
    }

    if (luaL_testudata(L, 1, JAVA_CLASS_META_REGISTRY) != NULL) {
        return jclassSigCall(L);
    }

    return luaL_error(L, "bad argument #1 to 'java.method': %s or %s expected",
                      JAVA_CLASS_META_REGISTRY, JAVA_OBJECT_META_REGISTRY);
}

static int javaNew(lua_State *L) {
    if (luaL_testudata(L, 1, JAVA_CLASS_META_REGISTRY) != NULL
        || luaL_testudata(L, 1, JAVA_OBJECT_META_REGISTRY) != NULL) {
        return jclassCall(L);
    } else {
        return luaL_error(L, "bad argument #1 to 'java.new': %s or %s expected",
                          JAVA_CLASS_META_REGISTRY, JAVA_OBJECT_META_REGISTRY);
    }
}

static int javaLuaify(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);
    return checkOrError(env, L,
                        env->CallStaticIntMethod(juaapi_class, juaapi_luaify, (jint) stateIndex));
}

/**
 * @brief Counts the number of trailing ".*"
 */
static std::size_t countDepth(const char *str, std::size_t length) {
    int depth = 0;
    for (int i = length - 2; i >= 0; i -= 2) {
        if (str[i] == '.' && str[i + 1] == '*') {
            depth++;
        } else {
            return depth;
        }
    }
    return depth;
}

int javaImport(lua_State *L) {
    const char *className = luaL_checkstring(L, 1);

    std::size_t length = std::strlen(className);
    std::size_t depth = countDepth(className, length);

    if (depth > 0) {
        /* Pre-allocates two extra slots */
        lua_createtable(L, 0, 4);
        lua_pushinteger(L, depth);
        lua_rawseti(L, -2, 1);
        lua_pushlstring(L, className, length - 2 * depth + 1);
        lua_rawseti(L, -2, 2);
        luaL_getmetatable(L, JAVA_PACKAGE_META_REGISTRY);
        lua_setmetatable(L, -2);
        return 1;
    }

    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);

    jstring str = env->NewStringUTF(className);
    int ret = env->CallStaticIntMethod(juaapi_class, juaapi_import, (jint) stateIndex,
                                       str);
    env->DeleteLocalRef(str);
    return checkOrError(env, L, ret);
}

static int javaProxy(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);
    return checkOrError(env, L,
                        env->CallStaticIntMethod(juaapi_class, juaapi_proxy, (jint) stateIndex));
}

static int javaUnwrap(lua_State *L) {
    jobject *data = (jobject *) luaL_checkudata(L, 1, JAVA_OBJECT_META_REGISTRY);

    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);

    return checkOrError(env, L,
                        env->CallStaticIntMethod(juaapi_class, juaapi_unwrap, (jint) stateIndex,
                                                 *data));
}

static int javaArray(lua_State *L) {
    if (luaL_testudata(L, 1, JAVA_CLASS_META_REGISTRY) != NULL
        || luaL_testudata(L, 1, JAVA_OBJECT_META_REGISTRY) != NULL) {
        JNIEnv *env = getJNIEnv(L);
        int stateIndex = getStateIndex(L);
        int top = lua_gettop(L);
        jobject *data = (jobject *) lua_touserdata(L, 1);
        if (top == 2) {
            return checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_arraynew,
                                                                 (jint) stateIndex, *data,
                                                                 (jint) lua_tointeger(L, 2)));
        }
        if (top > 2) {
            return checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_arraynew,
                                                                 (jint) stateIndex, *data,
                                                                 (jint) (1 - top)));
        }
        return luaL_error(L, "bad argument #2 to 'java.array': number expected, got none");
    }
    return luaL_error(L, "bad argument #1 to 'java.array': %s or %s expected",
                      JAVA_CLASS_META_REGISTRY, JAVA_OBJECT_META_REGISTRY);
}

static int javaCaught(lua_State *L) {
    lua_getglobal(L, GLOBAL_THROWABLE);
    return 1;
}

static int javaDetach(lua_State *L) {
    luaL_checktype(L, 1, LUA_TTHREAD);
    lua_State *thread = lua_tothread(L, 1);
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(thread);
    return checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_freethreadid,
                                                         (jint) stateIndex));
}

static int javaLoadlib(lua_State *L) {
    const char *className = luaL_checkstring(L, 1);
    const char *methodName = luaL_checkstring(L, 2);

    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);

    jstring classNameStr = env->NewStringUTF(className);
    jstring methodNameStr = env->NewStringUTF(methodName);
    int ret = env->CallStaticIntMethod(juaapi_class, juaapi_loadlib, (jint) stateIndex,
                                       classNameStr, methodNameStr);
    env->DeleteLocalRef(classNameStr);
    env->DeleteLocalRef(methodNameStr);
    return checkOrError(env, L, ret);
}

// Luajava API Compatibility
static int javaNewInstance(lua_State *L) {
    const char *className = luaL_checkstring(L, 1);
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);
    jstring str = env->NewStringUTF(className);
    int ret = env->CallStaticIntMethod(juaapi_class, juaapi_import, (jint) stateIndex,
                                       str);
    env->DeleteLocalRef(str);
    if (checkOrError(env, L, ret) != 1) {
        return luaL_error(L, "class not found: %s", className);
    }
    lua_replace(L, 1);
    return jclassCall(L);
}

static int javaIsInstanceOf(lua_State *L) {
    luaL_checkudata(L, 1, JAVA_OBJECT_META_REGISTRY);
    luaL_checkudata(L, 2, JAVA_CLASS_META_REGISTRY);
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);
    jobject *obj = (jobject *)lua_touserdata(L, 1);
    jobject *clazz = (jobject *)lua_touserdata(L, 2);
    return checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_isinstanceof,
                                                         (jint)stateIndex, *obj, *clazz));
}


//static int javaOverride(lua_State *L) {
//    luaL_checkudata(L, 1, JAVA_CLASS_META_REGISTRY);
//    luaL_checktype(L, 2, LUA_TTABLE);
//    JNIEnv *env = getJNIEnv(L);
//    int stateIndex = getStateIndex(L);
//    jobject *clazz = (jobject *)lua_touserdata(L, 1);
//    int tableIndex = 2;
//    return checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_override,
//                                                         (jint)stateIndex, *clazz, tableIndex));
//}

static int javaCreateArray(lua_State *L) {
    const char *className = luaL_checkstring(L, 1);
    luaL_checktype(L, 2, LUA_TTABLE);
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);
    jstring jClassName = env->NewStringUTF(className);
    int tableIndex = 2;
    int result = checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_createarray,
                                                               (jint)stateIndex, jClassName, tableIndex));
    env->DeleteLocalRef(jClassName); // 释放 JNI 局部引用
    return result;
}

// 实现: java.astable(javaObject)
static int javaAsTable(lua_State *L) {
    luaL_checkudata(L, 1, JAVA_OBJECT_META_REGISTRY);
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);
    jobject *obj = (jobject *)lua_touserdata(L, 1);
    return checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_astable,
                                                         (jint)stateIndex, *obj));
}

// 实现: java.tostring(object)
static int javaToString(lua_State *L) {
    // 如果不是 Java 对象，则行为同 Lua 原生 tostring
    if (!luaJ_isobject(L, 1)) {
        lua_tolstring(L, 1, NULL);
        return 1;
    }
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);
    jobject *obj = (jobject *)lua_touserdata(L, 1);
    return checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_tostring,
                                                         (jint)stateIndex, *obj));
}

static int javaCoding(lua_State *L) {
    JNIEnv *env = getJNIEnv(L);
    int stateIndex = getStateIndex(L);
    return checkOrError(env, L, env->CallStaticIntMethod(juaapi_class, juaapi_coding, (jint)stateIndex));
}

const luaL_Reg javalib[] = {
        // NewAPI
        {"bindClass", javaImport},
        {"bindMethod", javaMethod},
        {"luaify", javaLuaify},
        {"newInstance", javaNew},
        {"new", javaNew},
        {"createArray", javaCreateArray},
        {"createProxy", javaProxy},
        {"unwrap",  javaUnwrap},
        {"caught",  javaCaught},
        {"detach",  javaDetach},
//
//        {"method",  javaMethod},
//        {"new",     javaNew},
//        {"luaify",  javaLuaify},
//        {"import",  javaImport},
//
//        {"proxy",   javaProxy},
//        {"array",   javaArray},
//
//        {"catched", javaCaught},
//
//        {"loadlib", javaLoadlib},
        // Luajava API Compatibility
//        {"bindClass",   javaImport},
//        // {"new",         javaNew},
//        {"newInstance", javaNewInstance},
//        {"loadLib",     javaLoadlib},
//
//        {"newArray",    javaArray},
//        {"createArray", javaCreateArray},
//        {"astable",     javaAsTable},
//        {"tostring",    javaToString},
//        {"coding",      javaCoding},
        // {"clear",       gc},
//        {"instanceof",  javaIsInstanceOf},
        // {"override",    javaOverride},
        {NULL, NULL}
};
