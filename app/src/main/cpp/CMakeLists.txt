# app/src/main/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.10)

# project(nexlua C)
project(nexlua)

set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# set(CMAKE_C_STANDARD 99)
# set(CMAKE_C_STANDARD_REQUIRED ON)
# set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -flto")
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -flto")

# include
include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/luajava
        ${CMAKE_CURRENT_SOURCE_DIR}/luajava/lj
        ${CMAKE_CURRENT_SOURCE_DIR}/luajit-include
)

# add luajit library
add_library(luajit STATIC IMPORTED)
set_target_properties(luajit PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/luajit/${ANDROID_ABI}/libluajit.a
)

# add luajava sources
file(GLOB LUAJAVA_SRC
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/com_luajava_luajit_LuaJitNatives.cpp"
)
add_library(luajava SHARED ${LUAJAVA_SRC})

# link library
find_library(log-lib log)
target_link_libraries(
        luajava
        PRIVATE
        luajit                # Static linking luajit
        ${log-lib}            # Android Log Library
)

# target_compile_options(luajava PRIVATE -Wall -Wextra -Werror) # enable warnings
# target_compile_options(luajava PRIVATE -fno-rtti -fno-exceptions) # disable rtti and exceptions