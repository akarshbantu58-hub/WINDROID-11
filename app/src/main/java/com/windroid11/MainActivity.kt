package com.windroid11

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
    var selected by remember { mutableStateOf("Home") }
    val blue = Color(0xFF4EA5FF)
    val background = Brush.verticalGradient(
        listOf(Color(0xFF071426), Color(0xFF101E35), Color(0xFF172A45))
    )
    MaterialTheme(colorScheme = darkColorScheme(primary = blue)) {
        Box(Modifier.fillMaxSize().background(background).padding(22.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("WinDroid 11", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Windows ARM64 virtual machine for Android", color = Color(0xFFB7C8DD), fontSize = 14.sp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF203B5D)),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("Windows 11 ARM64", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text("Virtual machine engine not installed yet", color = Color(0xFFFFD18A), fontSize = 13.sp)
                        Text("This is the app foundation. Windows will not boot until the VM engine and a licensed installation image are added.", color = Color(0xFFD4E1F0), fontSize = 13.sp)
                        Button(onClick = { selected = "Engine setup" }, modifier = Modifier.fillMaxWidth()) {
                            Text("VM engine setup")
                        }
                    }
                }
                Text("Quick access", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile("Display", "Touch-first", Modifier.weight(1f)) { selected = "Display" }
                    QuickTile("Input", "Touch + optional mouse/keyboard", Modifier.weight(1f)) { selected = "Input" }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile("Storage", "Virtual disk", Modifier.weight(1f)) { selected = "Storage" }
                    QuickTile("Settings", "App settings", Modifier.weight(1f)) { selected = "Settings" }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF13243A)), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Current status", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("App foundation: ready", color = Color(0xFF8FE0B0), fontSize = 13.sp)
                        Text("QEMU engine: pending", color = Color(0xFFFFD18A), fontSize = 13.sp)
                        Text("Windows image: not added", color = Color(0xFFFFD18A), fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("Selected: $selected", color = Color(0xFF9DB2CC), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun QuickTile(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.heightIn(min = 100.dp).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF203B5D))
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFFB7C8DD), fontSize = 12.sp)
        }
    }
}
