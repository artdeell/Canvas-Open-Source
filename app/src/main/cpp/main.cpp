#include <android/log.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>
#include "include/misc/Logger.h"
#include "main.h"
#include "include/imgui/imgui.h"
#include "include/imgui/androidbk.h"
#include "include/misc/Vector3.h"
#include <vector>
#include "include/cipher/Cipher.h"
#include "include/canvas/Canvas.h"
#include "include/misc/visibility.h"
#include "include/iconloader/IconLoader.h"
#include "fileselector.h"
#include <android/asset_manager_jni.h>

void do_scroll();
void fsel_setup(JNIEnv*);

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

PRIVATE_API void SystemsTest() {
    ImGui::Begin("SystemsTest");
    ImGui::Text("Game version: %i",Cipher::getGameVersion());
    ImGui::Text("Is beta: %s", Cipher::isGameBeta() ? "Yes":"No");
    UIIcon icon;
    IconLoader::getUIIcon("UiOutfitPropAP10Hoop", &icon);
    if(icon.textureId != IL_NO_TEXTURE) {
        ImGui::Image((ImTextureID)icon.textureId, ImVec2(64,64), icon.uv0, icon.uv1, ImVec4(1,1,1,1));
    }else{
        ImGui::Text("Icon missing");
    }
    IconLoader::icon("UiOutfitPropAP10Hoop", 64);
    /*ImGui::InputText("Test content", test_file_buf, 511);
    if(ImGui::Button("Write test file")) {
        gfd = -3;
        requestFile("text/plain", &file_selector_cb, true);
    }
    if(ImGui::Button("Read file")) {
        gfd = -2;
        requestFile("text/plain", &file_selector_cb, false);
    }
    ImGui::Text("Fd: %i", gfd);
    ImGui::End();*/
}

PRIVATE_API void Canvas::CanvasMenu() {
        ImGui::Begin("Canvas Menu");
        for(auto & m_Userlib : Canvas::m_Userlibs){
            ImGui::Checkbox(m_Userlib.Name, &m_Userlib.UIEnabled);
            if(m_Userlib.UIEnabled) {
                if(m_Userlib.UISelfManaged) ImGui::PushID(m_Userlib.Name);
                else ImGui::Begin(m_Userlib.Name);
                m_Userlib.Draw();
                if(m_Userlib.UISelfManaged) ImGui::PopID();
                else ImGui::End();
            }
        }
        ImGui::Text("Application average %.3f ms/frame (%.1f FPS)", 1000.0f / ImGui::GetIO().Framerate, ImGui::GetIO().Framerate);
        ImGui::Checkbox("Limit FPS", &Canvas::framerateLimited);
        ImGui::End();
        //SystemsTest();
}

__unused __attribute__((constructor))
int main() {
    LOGI("Starting Sky ModMenu.. Build time: " __DATE__ " " __TIME__);

    do {
        sleep(1);
    } while (!Canvas::isLibLoaded(Canvas::get_libName()));
    KittyMemory::ProcMap procmap = KittyMemory::getLibraryMap(Canvas::get_libName());
    Canvas::set_libBase((uintptr_t)procmap.startAddr);
    Canvas::set_libSize((uintptr_t)procmap.length);
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_MainActivity_settle(JNIEnv *env, jclass clazz, jint gVersion, jboolean isBeta, jstring configPath,
                                                   jobject game_assets) {
    env->GetJavaVM(&Canvas::javavm);
    fsel_setup(env);
    Canvas::gameVersion = gVersion;
    Canvas::isBeta = isBeta;
    Canvas::configDirPath = (*env).GetStringUTFChars(configPath, NULL);
    IconLoader::aAssetManager = AAssetManager_fromJava(env, game_assets);
}


PRIVATE_API void *UserThread(void *Ulib){
    Userlib *userlib = (Userlib *)Ulib;
    func (*Start)() = (func(*)())userlib->Draw;
    userlib->Draw = Start();
    if(userlib->Name && userlib->Draw){
        Canvas::push_Userlib(*userlib);
    }
    delete userlib;
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
Java_git_artdeell_skymodloader_LibrarySelectorListener_onModLibrary(JNIEnv *env, jclass clazz, jstring path, jboolean isDraw, jstring name, jboolean dev, jboolean selfManagedUI) {
    Canvas::m_dev = dev;
    const char *temp = env->GetStringUTFChars(path, 0);
    void *dl_entry = dlopen(temp, RTLD_LOCAL);
    if(dl_entry == nullptr) {
        crash(env, dlerror());
        return;
    }
    func (*Start)() = (func (*)()) dlsym( dl_entry, "Start");
    env->ReleaseStringUTFChars(path, temp);
    if(Start == nullptr) {
        crash(env, dlerror());
        return;
    }
    //if(!Start) return;
    temp = env->GetStringUTFChars(name, 0);
    Userlib *userlib = new Userlib;
    if(!isDraw) temp = nullptr;
    userlib->Name = temp;
    userlib->Draw = (void (*)(void))(Start);
    userlib->UISelfManaged = selfManagedUI;
    pthread_t pid;
    pthread_create(&pid, nullptr, UserThread, (void *)userlib);
}