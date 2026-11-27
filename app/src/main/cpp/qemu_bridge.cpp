#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_windroid11_QemuNativeBridge_nativeStatus(JNIEnv *env, jobject /* thiz */) {
    return env->NewStringUTF(
        "Android ARM64 JNI bridge is loaded. QEMU system-emulator core and firmware are not bundled yet."
    );
}
