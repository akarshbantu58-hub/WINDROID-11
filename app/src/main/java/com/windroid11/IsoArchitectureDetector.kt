package com.windroid11

import android.content.ContentResolver
import android.net.Uri

/**
 * Lightweight ISO boot-loader probe.
 * It scans at most 96 MiB and identifies common UEFI boot-loader names.
 * This is a preliminary hint, not a complete ISO validation.
 */
object IsoArchitectureDetector {
    fun detect(resolver: ContentResolver, uri: Uri): String {
        val aa64 = "BOOTAA64.EFI"
        val x64 = "BOOTX64.EFI"
        return try {
            resolver.openInputStream(uri)?.use { input ->
                val chunk = ByteArray(64 * 1024)
                var carry = ""
                var total = 0L
                var foundArm = false
                var foundX64 = false
                while (total < 96L * 1024L * 1024L) {
                    val count = input.read(chunk)
                    if (count <= 0) break
                    total += count
                    val part = carry + String(chunk, 0, count, Charsets.ISO_8859_1)
                    val upper = part.uppercase()
                    if (upper.contains(aa64)) foundArm = true
                    if (upper.contains(x64)) foundX64 = true
                    if (foundArm && foundX64) break
                    carry = part.takeLast(32)
                }
                when {
                    foundArm && !foundX64 -> "ARM64 (AArch64) boot loader detected"
                    foundX64 && !foundArm -> "x64 (AMD64) boot loader detected"
                    foundArm && foundX64 -> "Both ARM64 and x64 boot loaders found — needs deeper inspection"
                    else -> "Unknown — EFI boot loader not found in first 96 MiB"
                }
            } ?: "Could not open the selected ISO"
        } catch (e: Exception) {
            "Scan failed: ${e.message ?: "file access error"}"
        }
    }

    fun detect(context: android.content.Context, uri: Uri): String =
        detect(context.contentResolver, uri)
}
