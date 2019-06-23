cmake_minimum_required(VERSION 3.4.1)

file(GLOB bzip_source ${CMAKE_SOURCE_DIR}/src/main/cpp/bzip/*.c)

add_library(
        bspatcher

        SHARED

        src/main/cpp/native-lib.cpp
        src/main/cpp/bspatch.c
        ${bzip_source})

find_library(
        log-lib
        log)

target_link_libraries(
        bspatcher
        ${log-lib})