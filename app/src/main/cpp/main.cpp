#include <android/log.h>
#include <dlfcn.h>
#include <pthread.h>
#include <thread>
#include <unistd.h>
#include "include/misc/Logger.h"
#include "main.h"
#include "Core/imgui/imgui.h"
#include "Utils/imgui_androidbk/androidbk.h"
#include "include/misc/Vector3.h"
#include <vector>
#include "Canvas/Canvas.h"
#include "Cipher/Cipher.h"
#include "include/misc/visibility.h"
#include "Iconloader/IconLoader.h"
#include "fileselector/fileselector.h"
#include <android/asset_manager_jni.h>
#include "shadowhook.h"


void do_scroll();
void fsel_setup(JNIEnv*);

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    Canvas::javaVM = vm;
    Canvas::javaVM->GetEnv((void **) &Canvas::jniEnv, JNI_VERSION_1_6);
    return JNI_VERSION_1_6;
}


char test_file_buf[512] = {0};
int gfd = -2;
void file_selector_cb(int fd) {
    if(fd != -1) {
        if(gfd == -3) {
            const size_t len = strlen(test_file_buf);
            write(fd, test_file_buf, len);
        }else{
            gfd = fd;
        }
        close(fd);
    }
}

void iAmHookable() {
    ImGui::Text("Hello, I am not hooked yet!");
}
static void (*iAmCallback)();
PRIVATE_API void iAmHook() {
    ImGui::Text("Hello, I'm the hook!");
    iAmCallback();
}
static CipherBase *hook = NULL;
PRIVATE_API void SystemsTest() {
    ImGui::Begin("System Tests");
    iAmHookable();
    if(ImGui::Button("Hook")) {
        if(hook != NULL) hook->Restore();
        delete hook;
        hook = (new CipherHook())
            ->set_Hook((uintptr_t)iAmHook)
            ->set_Callback((uintptr_t)&iAmCallback)
            ->set_Address((uintptr_t)iAmHookable, false)
            ->Fire();
    }
    ImGui::End();

}

PRIVATE_API static void HelpMarker(const char* desc)
{
    ImGui::TextDisabled("(?)");
    if (ImGui::BeginItemTooltip())
    {
        ImGui::PushTextWrapPos(ImGui::GetFontSize() * 35.0f);
        ImGui::TextWrapped("%s", desc);
        ImGui::PopTextWrapPos();
        ImGui::EndTooltip();
    }
}

PRIVATE_API void DrawMods() {
    for (auto &userLib: Canvas::userLibs) {
        if (userLib.UIEnabled) {
            if (userLib.UISelfManaged) {
                ImGui::PushID(userLib.Name.c_str());
            } else {
                ImGui::Begin(userLib.Name.c_str());
            }

            userLib.Draw(&userLib.UIEnabled);

            if (userLib.UISelfManaged) {
                ImGui::PopID();
            } else {
                ImGui::End();
            }
        }
    }
}

#include <sstream>
std::string formatUserLibInfo(const Canvas::UserLib& _userLib) {
    std::ostringstream oss;
    oss << _userLib.Name << ": " << _userLib.Version << "\n";
    if (!_userLib.Author.empty()) {
        oss << "Author: " << _userLib.Author << "\n";
    }

    if (!_userLib.Description.empty()) {
        oss << "---------\nDescription:\n" << _userLib.Description << "\n";
    }
    return oss.str();
}

PRIVATE_API void Canvas::CanvasMenu() {
    ImGui::Begin("Canvas Menu");
    if (!Canvas::userLibs.empty() && ImGui::BeginTable(
            "Mods##canvas_mods_table",
            2,
            ImGuiTableFlags_Borders
               | ImGuiTableFlags_RowBg
               | ImGuiTableFlags_BordersH
               | ImGuiTableFlags_BordersOuterH,
            ImVec2(-1.0f, 0.0f))
    ) {
        ImGui::TableSetupColumn("Mod", ImGuiTableColumnFlags_WidthStretch);
        ImGui::TableSetupColumn(
                "Info",
                ImGuiTableColumnFlags_WidthFixed,
                ImGui::CalcTextSize("Info").x + (20.0f / (24 / ImGui::GetFont()->FontSize))
        );
        ImGui::TableHeadersRow();
        for (auto &userLib: Canvas::userLibs) {
            ImGui::TableNextRow();
            ImGui::TableSetColumnIndex(0);
            ImGui::Checkbox(userLib.Name.c_str(), &userLib.UIEnabled);

            ImGui::TableSetColumnIndex(1);
            HelpMarker(formatUserLibInfo(userLib).c_str());

        }
        ImGui::EndTable();
    }

    DrawMods();
    ImGui::Text(
            "Application average %.3f ms/frame (%.1f FPS)",
            1000.0f / ImGui::GetIO().Framerate,
            ImGui::GetIO().Framerate
    );
    ImGui::Checkbox("Limit FPS", &Canvas::frameRateLimited);
    ImGui::End();

    SystemsTest();
}

void (*ImGuiEnd)();
void ImGuiEndHook() {
    do_scroll();
    return ImGuiEnd();
}

__unused __attribute__((constructor))
int main() {
    LOGI("Starting Sky ModMenu.. Build time: " __DATE__ " " __TIME__);
    Canvas::libName = "libBootloader.so";
    do {
        sleep(1);
    } while (!Canvas::isLibLoaded(Canvas::libName));
    auto elfScanner = ElfScanner::createWithPath(Canvas::libName);
    Canvas::libBase = elfScanner.baseSegment().startAddress;
    shadowhook_init(SHADOWHOOK_MODE_UNIQUE, false);
    (new CipherHook)->set_Hook((std::uintptr_t)ImGuiEndHook)
    ->set_Callback((std::uintptr_t)&ImGuiEnd)
    ->set_Address("_ZN5ImGui3EndEv")
    ->Fire();
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_MainActivity_settle(
        JNIEnv *env,
        jclass clazz,
        jint _gameVersion,
        jint _gameType,
        jstring _hostName,
        jstring _configDir,
        jobject _gameAssets
) {
    //env->GetJavaVM(&Canvas::javaVM);
    fsel_setup(env);
    Canvas::MainActivity = clazz;
    Canvas::gameVersion = _gameVersion;
    Canvas::gameType = _gameType;
    Canvas::configsPath = (*env).GetStringUTFChars(_configDir, NULL);
    Canvas::aAssetManager = AAssetManager_fromJava(env, _gameAssets);
}


typedef void (*func)();
PRIVATE_API void *UserThread(void *Ulib){
    Canvas::UserLib *pUserLib = (Canvas::UserLib *)Ulib;
    func (*Start)() = (func(*)())pUserLib->Start;
    pUserLib->Draw = (void (*)(bool *))Start();
    if (!pUserLib->Name.empty() && pUserLib->Draw){
        Canvas::pushUserLib(*pUserLib);
    }
    delete pUserLib;
    pthread_exit(nullptr);
}

PRIVATE_API void crash(JNIEnv *env, char* crashReason) {
    jclass exception =  env->FindClass("java/lang/Exception");
    if(exception != nullptr) {
        env->ThrowNew(exception, crashReason);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_LibrarySelectorListener_onModLibrary(
        JNIEnv *env,
        jclass clazz,
        jstring _path,
        jboolean _isDraw,
        jstring _displayName,
        jstring _author,
        jstring _description,
        jstring _version,
        jboolean _selfManagedUI
) {

    const char *temp = env->GetStringUTFChars(_path, 0);
    void *dl_entry = dlopen(temp, RTLD_LOCAL);
    if(dl_entry == nullptr) {
        crash(env, dlerror());
        return;
    }

    env->ReleaseStringUTFChars(_path, temp);

    func (*Start)() = (func (*)()) dlsym( dl_entry, "Start");
    if (Start == nullptr) {
        crash(env, dlerror());
        return;
    }

    Canvas::UserLib* pUserLib = new Canvas::UserLib;
    pUserLib->UISelfManaged = _selfManagedUI;
    pUserLib->Name = env->GetStringUTFChars(_displayName, 0);
    pUserLib->Author = env->GetStringUTFChars(_author, 0);
    pUserLib->Description = env->GetStringUTFChars(_description, 0);
    pUserLib->Version = env->GetStringUTFChars(_version, 0);
    pUserLib->Start = (void (*)(void))(Start);

    func (*InitLate)() = (func (*)()) dlsym( dl_entry, "InitLate");
    pUserLib->InitLate = (void (*)(void))InitLate;

    pthread_t pid;
    pthread_create(&pid, nullptr, UserThread, (void *)pUserLib);
}

extern "C"
JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_MainActivity_onKeyboardCompleteNative(
        JNIEnv *env,
        jclass clazz,
        jstring message
) {
    std::string msg = env->GetStringUTFChars(message, nullptr);
    for (auto& listener : Canvas::onKeyboardCompleteListeners) {
        listener(msg);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_MainActivity_setDeviceInfoNative(
        JNIEnv *env, jclass clazz,
        jfloat _xdpi,
        jfloat _ydpi,
        jfloat _density,
        jstring _deviceName,
        jstring _manufacturer,
        jstring _model
) {
    Canvas::deviceInfo.xdpi = _xdpi;
    Canvas::deviceInfo.ydpi = _ydpi;
    Canvas::deviceInfo.density = _density;
    Canvas::deviceInfo.deviceName = env->GetStringUTFChars(_deviceName, nullptr);
    Canvas::deviceInfo.deviceManufacturer = env->GetStringUTFChars(_manufacturer, nullptr);
    Canvas::deviceInfo.deviceModel = env->GetStringUTFChars(_model, nullptr);
}
extern "C"
JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_MainActivity_lateInitUserLibs(JNIEnv *env, jclass clazz) {
    std::thread lateInitThread([]() -> void {
        for (auto& userlib : Canvas::userLibs) {
            if (userlib.InitLate != nullptr) {
                userlib.InitLate();
                LOGI("InitLate() called for %s", userlib.Name.c_str());
            } else {
                LOGI("%s does not adopt a InitLate() function", userlib.Name.c_str());
            }
        }
    });
    lateInitThread.detach();
}