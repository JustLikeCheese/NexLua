cmake_minimum_required(VERSION 3.18.1)

project(nexlua)

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3")
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(LUAJAVA_REL_DIR "../../../../../luajava/native/src/main/cpp")
get_filename_component(LUAJAVA_DIR "${LUAJAVA_REL_DIR}" ABSOLUTE BASE_DIR ${CMAKE_CURRENT_SOURCE_DIR})

# include
include_directories(
        ${LUAJAVA_DIR}/luajava
        ${LUAJAVA_DIR}/luajava/lj
        ${LUAJAVA_DIR}/luajit-include
)

# add luajit library
add_library(luajit STATIC IMPORTED)
set_target_properties(luajit PROPERTIES
        IMPORTED_LOCATION
        ${LUAJAVA_DIR}/luajit/${ANDROID_ABI}/libluajit.a
)

# add luajava sources
set(LUAJAVA_SRC
        "${LUAJAVA_DIR}/luajava/com_luajava_LuaNatives.c"
        "${LUAJAVA_DIR}/luajava/jnihelper.c"
        "${LUAJAVA_DIR}/luajava/luacomp.c"
        "${LUAJAVA_DIR}/luajava/luajava.c"
        "${LUAJAVA_DIR}/luajava/luajavaapi.c"
        "${LUAJAVA_DIR}/luajava/luajavacore.c"
        "${LUAJAVA_DIR}/luajava/luajavaloader.c"
        "${LUAJAVA_DIR}/luajava/nexlua.c"
        # libnexlua.c
        "${CMAKE_CURRENT_SOURCE_DIR}/nexlua.c"
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