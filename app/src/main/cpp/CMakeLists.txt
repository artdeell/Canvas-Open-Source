# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

set(CMAKE_BUILD_TYPE RelWithDebInfo)

project("imgui")





add_library(ktx_read SHARED IMPORTED)
set_target_properties(ktx_read PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/include/ktx/libktx_read.so)
add_library(ciphered

        SHARED

        main.cpp
        scroll.cpp
        fileselector.cpp
        include/artpatch/procutils.c
        include/artpatch/artpatch.c
        include/canvas/Canvas.cpp
        include/cipher/Cipher.cpp
        include/cipher/CipherArm64.cpp
        include/And64InlineHook/And64InlineHook.cpp
        include/KittyMemory/KittyArm64.cpp
        include/KittyMemory/KittyScanner.cpp
        include/KittyMemory/MemoryBackup.cpp
        include/KittyMemory/KittyUtils.cpp
        include/KittyMemory/MemoryPatch.cpp
        include/KittyMemory/KittyMemory.cpp
        include/iconloader/IconLoader.cpp
        include/imgui/imgui.cpp
        include/imgui/androidbk.cpp
        include/imgui/imgui_demo.cpp
        include/imgui/imgui_draw.cpp
        include/imgui/imgui_tables.cpp
        include/imgui/imgui_widgets.cpp
        include/imgui/imgui_impl_opengl3.cpp

        )

target_compile_options(ciphered PUBLIC $<$<COMPILE_LANGUAGE:CXX>:-Werror -std=c++17 -Wno-error=c++11-narrowing  -Wall>)
target_compile_options(ciphered PUBLIC -w -s -Wno-error=format-security -fpermissive -fexceptions)
#target_link_options(cpihered PUBLIC -Wl,--gc-sections,--strip-all,-llog)

set(LINKED_LIBS log GLESv3 EGL android ktx_read)



target_link_libraries( # Specifies the target library.
        ciphered
        ${LINKED_LIBS}
)