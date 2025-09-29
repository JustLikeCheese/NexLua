# Nex LuaJava 源码介绍

## JNI 桥文件

com_luajava_LuaNatives.c 包含 Lua 的所有方法

com_luajava_LuaJava.c 包含了 luaJ_* 的方法（nxluajava 的扩展方法会以 luaJ_ 开头）

# LuaJava

luajava.c LuaJava 核心, Java 来的对象都要靠它

luajavaapi.c 封装了 LuaJava 会用到的 API

luajavalib.c & luacustom.h 万恶之源, 提供了 luajava 库给 Lua

## Lua 扩展文件

luakit.h 包含了 Lua 中的所有可用头文件

luacomp.h 兼容原版的 openlib 函数, 并扩展了 luaJ_openlib、luaJ_openlib_call 方法

lj/lj_arch.h & mobile-nosys.h 兼容不同环境
