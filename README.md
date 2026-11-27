# WinDroid 11 — Android virtual machine project

WinDroid 11 is a touchscreen-first Android front end for running guest operating systems in a virtual machine.

## Current implementation status

The app currently contains the Compose interface, persistent ISO document selection, a floating keyboard/touchpad preview, an ISO EFI boot-loader scanner, and a small JNI bridge build target.

**The QEMU engine is not yet wired into the app's Boot PC action.** The QubeVM source is included as a pinned Git submodule for the next integration work. A successful APK build at this stage proves only that the Android shell and its current native bridge compile; it does not prove that Windows boots.

## QEMU engine source

WinDroid includes [QubeVM](https://github.com/rhn-1k/QubeVM) as a Git submodule at `third_party/QubeVM`, pinned to commit `979d638e5d57ed0c01c4781957ddac5e9697e970`.

QubeVM is a QEMU-based Android VM project licensed under GPL-3.0. Its developer guide documents the Android ARM64 host build, guest-specific QEMU system libraries, native dependency builds, and Android modules. Read:
- `third_party/QubeVM/README.md`
- `third_party/QubeVM/README.developers`
- `third_party/QubeVM/COPYING`

The QubeVM build guide currently calls for Android SDK 37, NDK r30, JDK 17, Meson/Ninja, and a Linux build environment. It also describes building an ARM64 Android host with an x86_64 guest target. The existing WinDroid app uses a different Gradle/Android plugin setup, so the QubeVM modules and native libraries still need to be adapted and built before they can be called from the app.

## Build

Clone with submodules:

```bash
git clone --recurse-submodules https://github.com/akarshbantu58-hub/WINDROID-11.git
```

GitHub Actions checks out the QubeVM submodule. The current APK build remains a shell/bridge build, not a Windows VM release.

## Planned engine integration checks

1. Build QubeVM's ARM64-host x86_64 guest native libraries in CI using its documented toolchain.
2. Integrate the required QubeVM Android library and x86 guest module into WinDroid, resolving Android Gradle Plugin/SDK compatibility.
3. Replace the placeholder JNI status bridge with QubeVM's real VM lifecycle and display/input interfaces.
4. Test an x86_64 Linux guest first, then test the supplied Windows ISO and confirm firmware, storage, display, and keyboard/touch input.
5. Publish the exact corresponding source and notices with every distributed APK.

## Device constraints

The target Galaxy Tab A9 has 4 GB RAM. Stock Android generally does not expose `/dev/kvm` to apps, so expect software TCG emulation unless device-specific acceleration is verified. x86_64 Windows emulation may be very slow and can run out of memory. Start with a small test guest before attempting Windows.

## Licensing

WinDroid's combined project is intended to be distributed under GPL-3.0 when QubeVM code is integrated. QubeVM's complete license text and original notices are retained in the submodule at `third_party/QubeVM/COPYING`. Other QubeVM dependencies retain their own licenses; see their source directories and notices.
