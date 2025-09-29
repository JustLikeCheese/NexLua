#include "jnihelper.h"

/* common class */
jclass java_lang_Class = NULL;
jmethodID java_lang_Class_forName = NULL;
jmethodID java_lang_Class_toString = NULL;
jclass java_lang_Throwable = NULL;
jmethodID java_lang_Throwable_getMessage = NULL;
jmethodID java_lang_Throwable_toString = NULL;
/* com.luajava.JuaAPI */
jclass com_luajava_JuaAPI = NULL;
jmethodID com_luajava_JuaAPI_jclassIndex = NULL;
jmethodID com_luajava_JuaAPI_jfunctionCall = NULL;
jmethodID com_luajava_JuaAPI_allocateDirectBuffer = NULL;

int initJNIBindings(JNIEnv *env) {
    if (updateJNIEnv(env) != 0) return -1;
    // if (initBoxingBindings != 0) return -1;
    // java.lang.Class
    java_lang_Class = bindJavaClass(env, "java/lang/Class");
    java_lang_Class_forName = bindJavaStaticMethod(env, java_lang_Class,
                                                   "forName",
                                                   "(Ljava/lang/String;)Ljava/lang/Class;");
    java_lang_Class_toString = bindJavaMethod(env, java_lang_Class,
                                              "toString", "()Ljava/lang/String;");
    // java.lang.Throwable
    java_lang_Throwable = bindJavaClass(env, "java/lang/Throwable");
    java_lang_Throwable_getMessage = bindJavaMethod(env, java_lang_Throwable,
                                                    "getMessage", "()Ljava/lang/String;");
    java_lang_Throwable_toString = bindJavaMethod(env, java_lang_Throwable,
                                                  "toString", "()Ljava/lang/String;");
    // com.luajava.JuaAPI
    com_luajava_JuaAPI = bindJavaClass(env, "com/luajava/JuaAPI");
    // jclassIndex
    com_luajava_JuaAPI_jclassIndex = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                          "jclassIndex",
                                                          "(JLjava/lang/Class;Ljava/lang/String;)I");
    // jfunctionCall
    com_luajava_JuaAPI_jfunctionCall = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                            "jfunctionCall",
                                                            "(JLjava/lang/Object;)I");
    // buffer
    com_luajava_JuaAPI_allocateDirectBuffer = bindJavaStaticMethod(env, com_luajava_JuaAPI,
                                                                   "allocateDirectBuffer",
                                                                   "(I)Ljava/nio/ByteBuffer;");
    CHECK_NULL(
            java_lang_Class && java_lang_Class_forName && java_lang_Class_toString &&
            java_lang_Throwable && java_lang_Throwable_getMessage && java_lang_Throwable_toString
    )
    return 0;
}

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

