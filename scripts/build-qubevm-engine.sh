#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
QUBE="$ROOT/third_party/QubeVM"
JNI="$QUBE/qube-android-lib/src/main/jni"
NDK="${ANDROID_NDK_HOME:-${ANDROID_HOME}/ndk/30.0.16248370}"

if [[ ! -e "$QUBE/.git" || ! -f "$QUBE/README.developers" ]]; then
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
  fetch_extract "https://download.gnome.org/sources/glib/2.90/glib-2.90.1.tar.xz" "glib-2.90.1.tar.xz" "glib-2.90.1"
  mv glib-2.90.1 glib
fi
if [[ ! -d libffi ]]; then
  fetch_extract "https://github.com/libffi/libffi/releases/download/v3.8.0/libffi-3.8.0.tar.gz" "libffi-3.8.0.tar.gz" "libffi-3.8.0"
  mv libffi-3.8.0 libffi
fi
if [[ ! -d pixman ]]; then
  fetch_extract "https://gitlab.freedesktop.org/pixman/pixman/-/archive/pixman-0.46.4/pixman-pixman-0.46.4.tar.gz" "pixman-0.46.4.tar.gz" "pixman-pixman-0.46.4"
  mv pixman-pixman-0.46.4 pixman
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
# The upstream 1.3.0 tag has small Meson differences from the
# patch's original context. Apply all matching patch hunks, then enforce
# the two Android-specific Meson edits against the pinned 1.3.0 source.
if [[ ! -f virglrenderer/.qube-patch-applied ]]; then
  (cd virglrenderer && patch --batch --forward -p1 < ../patches/virglrenderer-1.3.0-android-egl.patch) || true
  python3 - <<'PY'
from pathlib import Path
p = Path("virglrenderer/meson.build")
s = p.read_text()
if "with_host_android =" not in s:
    needle = "with_host_darwin = host_machine.system() == 'darwin'"
    if needle not in s:
        raise SystemExit("Cannot locate virglrenderer host OS declarations")
    s = s.replace(needle, needle + "\nwith_host_android = host_machine.system() == 'android'", 1)
old_dep = "epoxy_dep = dependency('epoxy', version: '>= 1.5.4')"
if old_dep in s:
    s = s.replace(old_dep, """if with_host_android
  epoxy_dep = declare_dependency(
    compile_args: [],
    link_args: ['-lepoxy'],
    version: '1.5.10',\n    variables: {'epoxy_has_egl': '1'})
else
  epoxy_dep = dependency('epoxy', version: '>= 1.5.4')
endif""", 1)
if "epoxy_dep = declare_dependency(" not in s:
    raise SystemExit("Android epoxy dependency adaptation was not applied")
old_vtest = "if not with_host_windows\n   subdir('vtest')"
new_vtest = "if not with_host_windows and not with_host_android\n   subdir('vtest')"
if old_vtest in s:
    s = s.replace(old_vtest, new_vtest, 1)
if new_vtest not in s:
    raise SystemExit("Android vtest exclusion was not applied")
p.write_text(s)
PY
  touch virglrenderer/.qube-patch-applied
fi

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
