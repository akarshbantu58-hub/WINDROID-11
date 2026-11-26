package com.windroid11

/**
 * Configuration model for the future VM backend.
 * This does not start a virtual machine or allocate guest RAM yet.
 */
data class VmConfiguration(
    val architecture: GuestArchitecture = GuestArchitecture.ARM64,
    val memoryMb: Int = 1536,
    val displayWidth: Int = 1280,
    val displayHeight: Int = 800,
    val isoUri: String? = null,
    val diskUri: String? = null
) {
    init {
        require(memoryMb in listOf(1024, 1536, 2048)) { "Unsupported guest memory preset" }
        require(displayWidth > 0 && displayHeight > 0) { "Display dimensions must be positive" }
    }
}

enum class GuestArchitecture { ARM64, X86_64 }

enum class VmEngineState {
    NOT_INSTALLED,
    READY,
    STARTING,
    RUNNING,
    PAUSED,
    STOPPED,
    ERROR
}
