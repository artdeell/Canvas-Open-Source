//
// Created by Lukas on 25/07/2022.
//

#ifndef SKY_MODLOADER_CANVAS_H
#define SKY_MODLOADER_CANVAS_H

#include <vector>
#include <jni.h>
#include "../cipher/Cipher.h"
#include "../KittyMemory/MemoryPatch.h"
#include "../imgui/imgui.h"

typedef struct Hook{
    uintptr_t address;
    uintptr_t hook;
    bool isLocked;
};

typedef struct Userlib{
    bool UIEnabled = false;
    bool UISelfManaged;
    const char *Name;
    void (*Draw)();
};


class Canvas {
private:
    inline static uintptr_t m_libBase;
    inline static unsigned long m_libSize;
    //inline static std::vector<Cipher *>m_Ciphers;
    inline static std::vector<Userlib> m_Userlibs;
    inline static const char *m_libName = "libBootloader.so";

public:
    inline static bool m_dev = false;
    static void push_Userlib(Userlib ulib);
    static bool isLibLoaded(const char *libName);
    static uintptr_t findLib(const char *libName);
    static void CanvasMenu();
    static void set_libBase(uintptr_t libBase);
    static uintptr_t get_libBase();
    static void set_libSize(unsigned long size);
    static unsigned long get_libSize();
    static const char *get_libName();
    static _Atomic uint32_t gameVersion;
    static bool isBeta;
    static inline bool framerateLimited;
    static inline JavaVM* javavm;
    static inline const char* configDirPath;
    //static void push_Cipher(Cipher *cipher);
};




#endif //SKY_MODLOADER_CANVAS_H
