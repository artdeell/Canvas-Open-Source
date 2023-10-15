//
// Created by maks on 11.12.2022.
//

#include "fileselector.h"
#include "include/canvas/Canvas.h"
#include "include/misc/visibility.h"
#include <jni.h>
#include <android/log.h>

jclass class_FileSelector;
jmethodID method_nselectFile;


PRIVATE_API void fsel_setup(JNIEnv* env) {
    class_FileSelector = (jclass)env->NewGlobalRef(env->FindClass("git/artdeell/skymodloader/FileSelector"));
    method_nselectFile = env->GetStaticMethodID(class_FileSelector, "nselectFile", "(Ljava/lang/String;JZ)Z");
}
extern "C"
JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_FileSelector_callbackFunctionCall(JNIEnv *env, jclass clazz,
                                                                 jlong cb, jint fd) {
    auto callbackFunction = (callback_function)cb;
    callbackFunction(fd);
}

bool requestFile(const char* mime_type, callback_function callback, bool save) {
    JNIEnv *env;
    if(Canvas::javavm->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_EDETACHED) {
        __android_log_print(ANDROID_LOG_ERROR, "fileselector", "Not on ImGui UI thread????");
        abort();
    }
    return env->CallStaticBooleanMethod(class_FileSelector, method_nselectFile, env->NewStringUTF(mime_type), (jlong)callback, save) == JNI_OK;
}