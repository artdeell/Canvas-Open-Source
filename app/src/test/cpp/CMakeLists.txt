cmake_minimum_required(VERSION 3.18)

project(canvas_native_tests CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

enable_testing()

add_executable(chat_flag_fix_test ChatFlagFixTest.cpp)
target_include_directories(chat_flag_fix_test PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/../../main/cpp/Core)
target_compile_options(chat_flag_fix_test PRIVATE -Wall -Wextra -Werror)

add_test(NAME chat_flag_fix_test COMMAND chat_flag_fix_test)
