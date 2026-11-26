# WinDroid 11

An Android application project intended to host a Windows 11 ARM64 virtual machine.

## Current stage

Step 1: Android project foundation.

Included:
- Kotlin and Jetpack Compose application
- Touch-first starter interface
- Optional Android-connected mouse and keyboard input planned
- GitHub Actions debug APK workflow

Not yet included:
- QEMU system emulator binary or Android integration
- Virtual machine hardware configuration
- UEFI firmware and virtual TPM
- Windows installation media or virtual disk

The current app is only a foundation UI; it does not boot Windows yet.

## Build

Run the GitHub Actions workflow named **Build WinDroid 11 APK**, or build locally with JDK 17 and Gradle 8.9.

The debug APK is created at app/build/outputs/apk/debug/app-debug.apk.

## Important

Windows installation media must be obtained from Microsoft and used in accordance with its license. VM performance depends on the Android device's memory, CPU and available virtualization support.
