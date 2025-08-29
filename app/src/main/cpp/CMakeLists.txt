# app/src/main/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.10)

project(nexlua)

# 要编译成 PIE（Android 5.0+ 默认），并使用 Position Independent Code
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -flto")

# 确保通过 Gradle externalNativeBuild 调用：
if(NOT DEFINED ANDROID_ABI)
    message(FATAL_ERROR "请从 Gradle externalNativeBuild 调用此 CMakeLists.txt，并在 defaultConfig.abiFilters 中指定 ABI")
endif()

# 1. 包含头文件路径
include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/luajava          # juaapi.h, juaamalg.h 等
        ${CMAKE_CURRENT_SOURCE_DIR}/luajava/lj       # lj_arch.h 等（如有必要）
        ${CMAKE_CURRENT_SOURCE_DIR}/luajit-include
)

# 2. 导入对应 ABI 的 LuaJIT 静态库
add_library(luajit STATIC IMPORTED)

set_target_properties(luajit PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/luajit/${ANDROID_ABI}/libluajit.a
)

# 3. 编译 LuaJava JNI 层：把所有 .cpp 文件都编译进来
file(GLOB LUAJAVA_SRC
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/com_luajava_luajit_LuaJitNatives.cpp"
)

add_library(
        luajava               # 最终生成 libluajava.so
        SHARED
        ${LUAJAVA_SRC}
)

# 4. 链接依赖库
find_library(log-lib log)   # Android log 库

target_link_libraries(
        luajava
        PRIVATE
        luajit                # 静态链接 libluajit.a
        ${log-lib}            # log 输出
)

# 5. 编译选项（可酌情加入）
# 比如开启所有警告，或者禁止 RTTI/异常：
# target_compile_options(luajava PRIVATE -Wall -Wextra -Werror)
# target_compile_options(luajava PRIVATE -fno-rtti -fno-exceptions)
