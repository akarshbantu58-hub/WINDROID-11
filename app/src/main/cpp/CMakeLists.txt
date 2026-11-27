cmake_minimum_required(VERSION 3.22.1)
project(windroid_qemu_bridge LANGUAGES C CXX)

add_library(windroid_qemu_bridge SHARED qemu_bridge.cpp)

find_library(log-lib log)
target_link_libraries(windroid_qemu_bridge ${log-lib})
