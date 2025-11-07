# NexLuaJava

项目中所有 LuaJava 系列的 API 均以 **luaJ_** 开头

com_luajava_LuaNatives.c    JNI桥
com_luajava_LuaNatives.h    JNI桥的头文件

jnihelper.c                 提供了 JNIEnv, 所有的 jclass 和 jmethod 在此绑定
jnihelper.h                 提供了 JNIWRAP、JNIWRAP_STATIC、CHECK_NULL、ToString、GetString、ReleaseString、LOG、DEPRECATED 宏

luacomp.c                   提供了封装的 Lua 辅助性、兼容性的 API
luacomp.h                   luacomp.c 的头文件

luajava.c                   Lua 实际加载的 LuaJava 库
luajava.h                   luajava.c 的头文件

luajavaapi.c                提供了封装后的 JNI + Lua 的 API 函数
luajavaapi.h                luajavaapi.c 的头文件

luajavacore.c               NexLuaJava 核心, 包含了主要操作
luajavacore.h               luajavacore.c 的头文件

# Lua API 设计规范

Lua C API 主要使用 0 表示成功，非零表示错误 的约定

lua_CFunction 等都是返回推送到栈上的结果数量
