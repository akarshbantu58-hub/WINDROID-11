package com.windroid11

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WinDroidApp() }
    }
}

@Composable
fun WinDroidApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("windroid_setup", 0) }
    var selectedUri by remember { mutableStateOf(prefs.getString("windows_iso_uri", null)?.let(Uri::parse)) }
    var selectedName by remember { mutableStateOf(prefs.getString("windows_iso_name", null) ?: "No ISO selected") }
    var selectedSize by remember { mutableStateOf(prefs.getString("windows_iso_size", "") ?: "") }
    var message by remember { mutableStateOf("Choose your Windows 11 ARM64 ISO now or later.") }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                var name = "Windows image"
                var sizeText = "Size unavailable"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
                        if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                            val bytes = cursor.getLong(sizeIndex)
                            sizeText = String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
                        }
                    }
                }
                if (!name.lowercase().endsWith(".iso")) {
                    message = "Please select a file ending in .iso"
                } else {
                    selectedUri = uri
                    selectedName = name
                    selectedSize = sizeText
                    prefs.edit()
                        .putString("windows_iso_uri", uri.toString())
                        .putString("windows_iso_name", name)
                        .putString("windows_iso_size", sizeText)
                        .apply()
                    message = "ISO selected. VM setup will be available after the QEMU engine is added."
                }
            } catch (e: Exception) {
                message = "Could not keep access to this file. Please select it again."
            }
        }
    }

    val blue = Color(0xFF4EA5FF)
    val background = Brush.verticalGradient(
        listOf(Color(0xFF071426), Color(0xFF101E35), Color(0xFF172A45))
    )
    MaterialTheme(colorScheme = darkColorScheme(primary = blue)) {
        Box(Modifier.fillMaxSize().background(background).padding(22.dp)) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("WinDroid 11", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Windows ARM64 virtual machine for Android", color = Color(0xFFB7C8DD), fontSize = 14.sp)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF203B5D)),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Windows 11 ARM64", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text("Installation image", color = Color(0xFFB7C8DD), fontSize = 13.sp)
                        Text(selectedName, color = Color.White, fontWeight = FontWeight.Medium)
                        if (selectedSize.isNotBlank()) {
                            Text(selectedSize, color = Color(0xFFB7C8DD), fontSize = 12.sp)
                        }
                        Button(
                            onClick = { picker.launch(arrayOf("application/octet-stream", "application/x-iso9660-image", "*/*")) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedUri == null) "Import Windows ISO" else "Choose another ISO")
                        }
                        Text(message, color = Color(0xFFFFD18A), fontSize = 12.sp)
                        Text(
                            "The selected file is referenced through Android's file picker. It is not copied into app storage.",
                            color = Color(0xFFD4E1F0), fontSize = 12.sp
                        )
                    }
                }

                Text("Setup progress", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                StatusCard("App foundation", "Ready", Color(0xFF8FE0B0))
                StatusCard("Windows ISO", if (selectedUri == null) "Waiting for file" else "Selected: $selectedName", if (selectedUri == null) Color(0xFFFFD18A) else Color(0xFF8FE0B0))
                StatusCard("QEMU virtual machine engine", "Not added yet", Color(0xFFFFD18A))
                StatusCard("Windows installation", "Not started", Color(0xFFFFD18A))

                Spacer(Modifier.weight(1f))
                Text(
                    "Touchscreen-first • Keyboard and mouse optional",
                    color = Color(0xFF9DB2CC), fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, status: String, statusColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13243A)),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Text(status, color = statusColor, fontSize = 12.sp)
        }
    }
}
