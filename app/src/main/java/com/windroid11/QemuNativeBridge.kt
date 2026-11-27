package com.windroid11

/**
 * Native boundary for the future QEMU system-emulator integration.
 * This bridge deliberately reports unavailable until a real QEMU core is packaged.
 */
internal object QemuNativeBridge {
    init {
        System.loadLibrary("windroid_qemu_bridge")
    }

    external fun nativeStatus(): String

    fun status(): String = try {
        nativeStatus()
    } catch (error: UnsatisfiedLinkError) {
        "Native bridge unavailable: ${error.message ?: "library load failed"}"
    }
}
