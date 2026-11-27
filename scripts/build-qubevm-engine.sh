#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
QUBE="$ROOT/third_party/QubeVM"
JNI="$QUBE/qube-android-lib/src/main/jni"
NDK="${ANDROID_NDK_HOME:-${ANDROID_HOME}/ndk/30.0.16248370}"

if [[ ! -d "$QUBE/.git" || ! -f "$QUBE/README.developers" ]]; then
  echo "QubeVM submodule is missing. Run checkout with submodules: recursive." >&2
  exit 2
fi
if [[ ! -d "$NDK/toolchains/llvm/prebuilt/linux-x86_64" ]]; then
  echo "Android NDK r30 not found at $NDK" >&2
  exit 2
fi

export NDK_ROOT="$NDK"
export BUILD_HOST=arm64-v8a
export BUILD_GUEST=x86_64-softmmu
export USE_QEMU_VERSION=11.1.3
export USE_KVM=false
export NDK_ENV=linux-x86_64
export BUILD_THREADS="${BUILD_THREADS:-2}"
export PATH="$NDK:$PATH"

cd "$JNI"

fetch_extract() {
  local url="$1" archive="$2" destination="$3"
  if [[ ! -d "$destination" ]]; then
    curl --fail --location --retry 3 "$url" --output "$archive"
    case "$archive" in
      *.tar.xz) tar -xJf "$archive" ;;
      *.tar.gz) tar -xzf "$archive" ;;
      *.zip) unzip -q "$archive" ;;
      *) echo "Unsupported archive: $archive" >&2; exit 2 ;;
    esac
  fi
}

# Versions and patches are pinned to the QubeVM source revision recorded in this repo.
if [[ ! -d qemu ]]; then
  fetch_extract "https://download.qemu.org/qemu-11.1.3.tar.xz" "qemu-11.1.3.tar.xz" "qemu-11.1.3"
  mv qemu-11.1.3 qemu
fi
if [[ ! -d glib ]]; then
  fetch_extract "https://download.gnome.org/sources/glib/2.88/glib-2.90.1.tar.xz" "glib-2.90.1.tar.xz" "glib-2.90.1"
  mv glib-2.90.1 glib
fi
fetch_extract "https://github.com/libffi/libffi/releases/download/v3.8.0/libffi-3.8.0.tar.gz" "libffi-3.8.0.tar.gz" "libffi-3.8.0"
if [[ -d libffi-3.8.0 ]]; then mv libffi-3.8.0 libffi; fi
if [[ ! -d pixman ]]; then
  fetch_extract "https://gitlab.freedesktop.org/pixman/pixman/-/archive/pixman-0.46.4/pixman-pixman-0.46.4.tar.gz" "pixman-0.46.4.tar.gz" "pixman-pixman-0.46.4"
  mv pixman-pixman-0.46.4 pixman
  (cd pixman && bash autogen.sh)
fi
if [[ ! -d qemu/subprojects/libslirp ]]; then
  fetch_extract "https://gitlab.freedesktop.org/slirp/libslirp/-/archive/v4.9.5/libslirp-v4.9.5.zip" "libslirp-4.9.5.zip" "libslirp-4.9.5"
  mkdir -p qemu/subprojects
  mv libslirp-v4.9.5 qemu/subprojects/libslirp
fi
if [[ ! -d libepoxy ]]; then
  fetch_extract "https://github.com/anholt/libepoxy/archive/refs/tags/1.5.10.zip" "libepoxy-1.5.10.zip" "libepoxy-1.5.10"
  mv libepoxy-1.5.10 libepoxy
fi
if [[ ! -d virglrenderer ]]; then
  git clone --depth 1 --branch 1.3.0 https://gitlab.freedesktop.org/virgl/virglrenderer.git virglrenderer
fi

apply_once() {
  local marker="$1" directory="$2" patch_file="$3"
  if [[ ! -f "$marker" ]]; then
    (cd "$directory" && patch --forward -p1 < "$patch_file")
    touch "$marker"
  fi
}
apply_once qemu/.qube-patch-applied qemu ../patches/qemu-11.1.3.patch
apply_once glib/.qube-patch-applied glib ../patches/glib-2.90.1.patch
apply_once qemu/subprojects/libslirp/.qube-patch-applied qemu/subprojects/libslirp ../../../patches/libslirp-4.9.5.patch
apply_once virglrenderer/.qube-patch-applied virglrenderer ../patches/virglrenderer-1.3.0-android-egl.patch

make qube

LIBDIR="$QUBE/qube-android-lib/src/main/jniLibs/arm64-v8a"
X86DIR="$QUBE/qube-android-x86/src/main/jniLibs/arm64-v8a"
test -s "$LIBDIR/libqube.so"
test -s "$LIBDIR/libcompat-musl.so"
test -s "$LIBDIR/libglib-2.0.so"
test -s "$X86DIR/libqemu-system-x86_64.so"
test -d "$QUBE/qube-android-x86/src/main/assets/roms"

echo "QEMU engine build passed for ARM64 Android host / x86_64 guest."
find "$LIBDIR" "$X86DIR" -maxdepth 1 -type f -name '*.so' -printf '%p %s bytes\n'
