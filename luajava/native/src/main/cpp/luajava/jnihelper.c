#define JNI_HELPER_EXTERN

#include "jnihelper.h"
#include "invokespecial.c"

#pragma clang diagnostic push
#pragma ide diagnostic ignored "readability-misleading-indentation"

int initJNIBindings(JNIEnv *env) {
    if (updateJNIEnv(env) != 0) return -1;
    if (initBoxingBindings(env) != 0) return -1;
    // java.lang.Object
    java_lang_Object = bindJavaClass(env, "java/lang/Object");
    java_lang_Object_toString = bindJavaMethod(env, java_lang_Object,
                                               "toString", "()Ljava/lang/String;");
    java_lang_Object_equals = bindJavaMethod(env, java_lang_Object,
                                             "equals", "(Ljava/lang/Object;)Z");
    // java.lang.Class
    java_lang_Class = bindJavaClass(env, "java/lang/Class");
    java_lang_Class_forName = bindJavaStaticMethod(env, java_lang_Class,
                                                   "forName",
                                                   "(Ljava/lang/String;)Ljava/lang/Class;");
    java_lang_Class_toString = bindJavaMethod(env, java_lang_Class,
                                              "toString", "()Ljava/lang/String;");
    java_lang_Class_getName = bindJavaMethod(env, java_lang_Class,
                                             "getName", "()Ljava/lang/String;");
    // java.lang.Throwable
    java_lang_Throwable = bindJavaClass(env, "java/lang/Throwable");
    java_lang_Throwable_getMessage = bindJavaMethod(env, java_lang_Throwable,
                                                    "getMessage", "()Ljava/lang/String;");
    java_lang_Throwable_toString = bindJavaMethod(env, java_lang_Throwable,
                                                  "toString", "()Ljava/lang/String;");
    // com.luajava.JuaAPI
    com_luajava_JuaAPI = bindJavaClass(env, "com/luajava/JuaAPI");
    /* Java Class */
    com_luajava_JuaAPI_jclassIndex = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                          "jclassIndex",
                                                          "(JLjava/lang/Class;Ljava/lang/String;)I");
    com_luajava_JuaAPI_jclassNew = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                        "jclassNew",
                                                        "(JLjava/lang/Class;)I");
    com_luajava_JuaAPI_jclassNewIndex = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                             "jclassNewIndex",
                                                             "(JLjava/lang/Class;Ljava/lang/String;)I");
    /* Java Object */
    com_luajava_JuaAPI_jobjectIndex = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                           "jobjectIndex",
                                                           "(JLjava/lang/Object;Ljava/lang/String;)I");
    com_luajava_JuaAPI_jobjectLength = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                            "jobjectLength",
                                                            "(JLjava/lang/Object;)I");
    com_luajava_JuaAPI_jobjectNewIndex = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                              "jobjectNewIndex",
                                                              "(JLjava/lang/Object;Ljava/lang/String;)I");
    /* Java Array */
    com_luajava_JuaAPI_jarrayIndex = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                          "jarrayIndex",
                                                          "(JLjava/lang/Object;)I");
    com_luajava_JuaAPI_jarrayNewIndex = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                             "jarrayNewIndex",
                                                             "(JLjava/lang/Object;)I");
    com_luajava_JuaAPI_jarrayIpairsIterator = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                                   "jarrayIpairsIterator",
                                                                   "(JLjava/lang/Object;)I");
    /* LuaJava Library */
    com_luajava_LuaJava = bindJavaClass(env, "com/luajava/LuaJava");
    com_luajava_LuaJava_bindClass = bindJavaStaticMethod(env, com_luajava_LuaJava,
                                                         "bindClass",
                                                         "(JLjava/lang/String;)I");
    com_luajava_LuaJava_bindMethod = bindJavaStaticMethod(env, com_luajava_LuaJava,
                                                          "bindMethod",
                                                          "(JLjava/lang/Object;Ljava/lang/String;[Ljava/lang/Class;)I");
    com_luajava_LuaJava_toJavaObject = bindJavaStaticMethod(env, com_luajava_LuaJava,
                                                            "toJavaObject",
                                                            "(JLjava/lang/Class;)I");
    com_luajava_LuaJava_toJavaArray = bindJavaStaticMethod(env, com_luajava_LuaJava,
                                                           "toJavaArray",
                                                           "(JLjava/lang/Class;)I");
    com_luajava_LuaJava_toJavaMap = bindJavaStaticMethod(env, com_luajava_LuaJava,
                                                         "toJavaMap",
                                                         "(JLjava/lang/Class;Ljava/lang/Class;)I");
    com_luajava_LuaJava_asTable = bindJavaStaticMethod(env, com_luajava_LuaJava,
                                                       "asTable",
                                                       "(JLjava/lang/Object;)I");
    /* LuaJava Bridge API */
    com_luajava_JuaAPI_getStackTrace = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                            "getStackTrace",
                                                            "(Ljava/lang/Throwable;)Ljava/lang/String;");
    com_luajava_JuaAPI_allocateDirectBuffer = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                                   "allocateDirectBuffer",
                                                                   "(I)Ljava/nio/ByteBuffer;");
    /* Java CFunction */
    com_luajava_JuaAPI_jfunctionCall = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                            "jfunctionCall",
                                                            "(JLjava/lang/Object;)I");
    /* Java Module */
    com_luajava_JuaAPI_jmoduleLoad = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                          "jmoduleLoad",
                                                          "(JLjava/lang/String;)I");
    CHECK_NULL(
            java_lang_Object && java_lang_Object_toString && java_lang_Object_equals &&
            java_lang_Class && java_lang_Class_forName && java_lang_Class_toString &&
            java_lang_Class_getName &&
            java_lang_Throwable && java_lang_Throwable_getMessage && java_lang_Throwable_toString &&
            com_luajava_LuaJava &&
            com_luajava_LuaJava_bindClass && com_luajava_LuaJava_bindMethod &&
            com_luajava_LuaJava_toJavaObject && com_luajava_LuaJava_toJavaArray &&
            com_luajava_LuaJava_toJavaMap && com_luajava_LuaJava_asTable &&
            com_luajava_JuaAPI &&
            com_luajava_JuaAPI_jclassIndex && com_luajava_JuaAPI_jclassNew &&
            com_luajava_JuaAPI_jclassNewIndex &&
            com_luajava_JuaAPI_jobjectIndex && com_luajava_JuaAPI_jobjectLength &&
            com_luajava_JuaAPI_jobjectNewIndex &&
            com_luajava_JuaAPI_jarrayIndex && com_luajava_JuaAPI_jarrayNewIndex &&
            com_luajava_JuaAPI_jarrayIpairsIterator &&
            com_luajava_JuaAPI_getStackTrace &&
            com_luajava_JuaAPI_allocateDirectBuffer &&
            com_luajava_JuaAPI_jfunctionCall && com_luajava_JuaAPI_jmoduleLoad
    )
    return 0;
}

#pragma clang diagnostic pop

static JavaVM *javaVm = NULL;
static jint jniEnvVersion;

int updateJNIEnv(JNIEnv *env) {
    if ((*env)->GetJavaVM(env, &javaVm) == 0) {
        jniEnvVersion = (*env)->GetVersion(env);
        return 0;
    } else {
        return -1;
    }
}


JNIEnv *getJNIEnv(lua_State *L) {
    if (javaVm != NULL) {
        JNIEnv *env = NULL;
        int result = (*javaVm)->GetEnv(javaVm, (void **) &env, jniEnvVersion);
        if (result == JNI_OK) {
            return env;
        } else if (result == JNI_EDETACHED) {
            JavaVMAttachArgs args;
            args.version = jniEnvVersion;
            args.name = NULL;  // thread name
            args.group = NULL; // thread group
            result = (*javaVm)->AttachCurrentThread(javaVm, &env, &args);
            if (result == JNI_OK) {
                return env;
            }
        }
    }
    if (L)
        luaL_error(L, "Failed to get JNIEnv. JNI Version: %d, ", jniEnvVersion);
    return NULL;
}

/**
 * Returns a global reference to the class matching the name
 *
 * Exceptions on the Java side is not cleared (NoClassDefFoundError, for example).
 */
jclass bindJavaClass(JNIEnv *env, const char *name) {
    jclass tempClass;
    tempClass = (*env)->FindClass(env, name);
    if (tempClass == NULL) {
        return NULL;
    } else {
        jclass classRef = (jclass) (*env)->NewGlobalRef(env, tempClass);
        // https://stackoverflow.com/q/33481144/17780636
        // env->DeleteLocalRef(tempClass);
        if (classRef == NULL) {
            return NULL;
        } else {
            return classRef;
        }
    }
}

/**
 * Returns the methodID
 */
jmethodID bindJavaStaticMethod(JNIEnv *env, jclass c, const char *name, const char *sig) {
    jmethodID id = (*env)->GetStaticMethodID(env, c, name, sig);
    if (id == NULL) {
        return NULL;
    }
    return id;
}

/**
 * Returns the methodID
 */
jmethodID bindJavaMethod(JNIEnv *env, jclass c, const char *name, const char *sig) {
    jmethodID id = (*env)->GetMethodID(env, c, name, sig);
    if (id == NULL) {
        return NULL;
    }
    return id;
}
