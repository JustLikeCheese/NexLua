cmake_minimum_required(VERSION 3.10)

project(nexlua C)
set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3")
set(CMAKE_POSITION_INDEPENDENT_CODE ON)


# include
include_directories(
        luajava
        luajava/lj
        luajit-include
)

# add luajit library
add_library(luajit STATIC IMPORTED)
set_target_properties(luajit PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/luajit/${ANDROID_ABI}/libluajit.a
)

# add luajava sources
set(LUAJAVA_SRC
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/com_luajava_LuaNatives.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/jnihelper.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/luacomp.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/luajava.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/luajavaapi.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/luajavacore.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/luajava/nexlua.c"
)
add_library(luajava SHARED ${LUAJAVA_SRC})

# link options
target_link_options(luajava PRIVATE
        "-Wl,-z,max-page-size=16384"
)

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