//
// Created by Lukas on 25/07/2022.
//

#include "Canvas.h"
#include "../misc/Utils.h"
#include "../misc/visibility.h"

PRIVATE_API _Atomic uint32_t Canvas::gameVersion;
PRIVATE_API bool Canvas::isBeta;
 PRIVATE_API uintptr_t Canvas::findLib(const char *libName) {
    return findLibrary(libName);
}

PRIVATE_API bool Canvas::isLibLoaded(const char *libName) {
    return isLibraryLoaded(libName);
}

PRIVATE_API void Canvas::push_Userlib(Userlib ulib) {
    m_Userlibs.push_back(ulib);
}



PRIVATE_API void Canvas::set_libBase(uintptr_t libBase) {
    Canvas::m_libBase = libBase;
}

PRIVATE_API uintptr_t Canvas::get_libBase() {
    return Canvas::m_libBase;
}

PRIVATE_API void Canvas::set_libSize(unsigned long libBase) {
    Canvas::m_libSize = libBase;
}

PRIVATE_API unsigned long Canvas::get_libSize() {
    return Canvas::m_libSize;
}



PRIVATE_API const char *Canvas::get_libName() {
    return m_libName;
}


