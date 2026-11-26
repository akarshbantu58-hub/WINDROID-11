# WinDroid 11 — VM backend preparation

## Current milestone

The Android app currently provides the touchscreen-first UI, persistent document-picker access to a selected ISO, a floating expandable keyboard preview, and a touchpad pointer preview.

**Important:** the current app does not include QEMU, UEFI firmware, a virtual disk controller, or a running Windows guest. The keyboard and mouse are not yet connected to a guest OS. The VM configuration class is only a typed configuration model.

## Five development milestones

1. **Stabilize Android shell** — verify Compose build, persistent ISO URI access, floating keyboard overlay, and touchpad layout.
2. **Validate guest image** — identify whether the supplied ISO is Windows ARM64 or x64. A 3 GB file size does not reveal its CPU architecture. Windows ARM64 requires an ARM64 guest path; x64 requires a different emulation path.
3. **Select and integrate an Android-compatible VM engine** — evaluate QEMU user-mode/TCG and available host acceleration. Do not assume KVM is available on every Android device. Keep engine licensing and native ABI requirements documented.
4. **Create VM storage and firmware layer** — implement a user-selected virtual disk location, firmware boot configuration, and safe start/stop lifecycle. Do not silently create a large disk in internal storage.
5. **Connect input and display** — route keyboard scan/key events and touchpad pointer/button events to the VM; then test guest display scaling and lifecycle.

## Tablet resource limits

This project targets Android tablets with limited memory. Start with a conservative 1 GB guest-memory test only if the engine and Windows image permit it. A 1.5–2 GB Windows guest on a 4 GB Android device can cause severe memory pressure, slowdowns, or app termination. The settings screen currently saves preferences only; it does not reserve RAM.

## ISO and storage

Use Android's Storage Access Framework document URI. Keep the original ISO in user-selected external/shared storage when possible; do not copy it into app-private storage. Persisted URI permission can be revoked by Android or the user, so handle missing access gracefully.

## Current status

- [x] Touchscreen UI foundation
- [x] Persistent ISO file selection reference
- [x] Floating expandable on-screen keyboard preview
- [x] Touchpad pointer preview
- [x] VM configuration data model
- [ ] QEMU/native VM engine
- [ ] Firmware and virtual disk
- [ ] Real guest input bridge
- [ ] Windows boot/install verification
