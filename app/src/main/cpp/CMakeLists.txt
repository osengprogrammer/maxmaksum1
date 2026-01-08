cmake_minimum_required(VERSION 3.10)

project(azura_face_lib)

# Use modern C++
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Include current directory for headers
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

# Build shared library: ONLY preprocessing + JNI bridge
add_library(
    azura_face_lib
    SHARED
    jni_bridge.cpp
    image_processor.c
)

# Android log library
find_library(
    log-lib
    log
)

# Link required system libraries
target_link_libraries(
    azura_face_lib
    ${log-lib}
    atomic
    m
)
