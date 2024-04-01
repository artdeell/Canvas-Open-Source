#pragma once

#include <vector>
#include <string>
#include <jni.h>
#include <android/asset_manager_jni.h>

#include <KittyMemory/KittyInclude.hpp>

namespace Canvas {
    struct UserLib {
        bool UIEnabled = false;
        bool UISelfManaged;
        const char* Name;
        void (*Draw)();
    };


    extern const char* libName;
    extern std::uintptr_t libBase;

    extern std::vector<UserLib> userLibs;
    extern void pushUserLib(UserLib _userLib);

    extern AAssetManager *aAssetManager;
    extern bool dev;

    extern bool isLibLoaded(const char* _elfName);
    extern std::uintptr_t findLib(const char* _elfName);
    extern void CanvasMenu();


    extern _Atomic std::uint32_t gameVersion;
    extern int gameType;
    extern bool frameRateLimited;
    extern JavaVM *javaVM;
    extern const char *configsPath;

    extern std::vector<void (*)(std::string)> onKeyboardCompleteListeners;
}
