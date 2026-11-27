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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val bg = Color(0xFF081321)
private val panel = Color(0xFF142941)
private val blue = Color(0xFF65B9FF)
private val muted = Color(0xFFA8BCD2)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { Windroid() } }
}

@Composable
fun Windroid() {
 val ctx = androidx.compose.ui.platform.LocalContext.current
 val prefs = remember { ctx.getSharedPreferences("windroid_setup", 0) }
 var uri by remember { mutableStateOf(prefs.getString("windows_iso_uri", null)?.let(Uri::parse)) }
 var fileName by remember { mutableStateOf(prefs.getString("windows_iso_name", "No ISO selected") ?: "No ISO selected") }
 var fileSize by remember { mutableStateOf(prefs.getString("windows_iso_size", "") ?: "") }
 var notice by remember { mutableStateOf("Choose your Windows 11 ARM64 ISO now or later.") }
 var tab by remember { mutableStateOf("Home") }
 var keyboardEnabled by remember { mutableStateOf(true) }
 var keyboardExpanded by remember { mutableStateOf(false) }
 var mouse by remember { mutableStateOf(true) }
 var fit by remember { mutableStateOf(true) }
 var haptics by remember { mutableStateOf(false) }
 var resolution by remember { mutableStateOf(prefs.getString("resolution", "1280 × 800") ?: "1280 × 800") }
 var ram by remember { mutableStateOf(prefs.getString("ram", "1.5 GB") ?: "1.5 GB") }
 var typed by remember { mutableStateOf("") }

 val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
  if (u != null) try {
   ctx.contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION)
   var n = "Windows image"; var s = ""
   ctx.contentResolver.query(u, null, null, null, null)?.use { c ->
    val ni=c.getColumnIndex(OpenableColumns.DISPLAY_NAME); val si=c.getColumnIndex(OpenableColumns.SIZE)
    if(c.moveToFirst()){ if(ni>=0)n=c.getString(ni)?:n; if(si>=0&&!c.isNull(si))s=String.format("%.2f GB",c.getLong(si)/1073741824.0) }
   }
   if(!n.lowercase().endsWith(".iso")) notice="Please select a .iso file."
   else { uri=u; fileName=n; fileSize=s; prefs.edit().putString("windows_iso_uri",u.toString()).putString("windows_iso_name",n).putString("windows_iso_size",s).apply(); notice="ISO reference saved. VM engine is still required before installation." }
  } catch (_:Exception) { notice="Could not save file access. Please select it again." }
 }

 MaterialTheme(colorScheme=darkColorScheme(primary=blue, background=bg, surface=panel)) {
  Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(bg,Color(0xFF10243A),Color(0xFF183552))))) {
  Column(Modifier.fillMaxSize()) {
   Row(Modifier.fillMaxWidth().padding(20.dp),verticalAlignment=Alignment.CenterVertically) {
    Column(Modifier.weight(1f)){ Text("WINDROID",color=muted,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=3.sp); Text("11",color=Color.White,fontSize=32.sp,fontWeight=FontWeight.ExtraBold) }
    Text("ARM64  •  TOUCH PC",color=blue,fontSize=10.sp,fontWeight=FontWeight.Bold)
   }
   Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
    listOf("Home","Controls","Settings").forEach { t ->
     Surface(color=if(tab==t)blue.copy(alpha=.18f) else panel,shape=RoundedCornerShape(12.dp),modifier=Modifier.weight(1f).clickable{tab=t}) { Text(t,color=if(tab==t)blue else muted,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(13.dp)) }
    }
   }
   when(tab) {
    "Home" -> LazyColumn(Modifier.weight(1f).padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)) {
     item { Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF1B3B5A)),shape=RoundedCornerShape(22.dp)){ Column(Modifier.padding(20.dp)){ Text("YOUR PC. REIMAGINED.",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold); Text("A touchscreen-first Windows ARM64 virtual machine.",color=muted,fontSize=12.sp); Spacer(Modifier.height(12.dp)); Text("GEN-PC KEYBOARD  •  VIRTUAL MOUSE  •  PC SETTINGS",color=blue,fontSize=10.sp) } } }
     item { InfoCard("WINDOWS 11 ARM64 ISO",fileName+(if(fileSize.isNotBlank())"  •  $fileSize" else "")){ Button(onClick={filePicker.launch(arrayOf("application/octet-stream","application/x-iso9660-image","*/*"))},modifier=Modifier.fillMaxWidth()){Text(if(uri==null)"Choose ISO file" else "Change ISO file")} ; Text(notice,color=Color(0xFFFFD28B),fontSize=11.sp); Text("File is linked, not copied into internal storage. Add it later when ready.",color=muted,fontSize=10.sp) } }
     item { InfoCard("SYSTEM STATUS","App interface: READY\nISO: "+if(uri==null)"NOT ADDED" else "LINKED"+"\nQEMU engine: NOT INTEGRATED\nWindows VM: NOT STARTED") { } }
     item { Text("The mouse, keyboard and settings screens are prepared as UI. They will control Windows after the QEMU input bridge is implemented.",color=muted,fontSize=11.sp) }
    }
    "Controls" -> LazyColumn(Modifier.weight(1f).padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
     item { InfoCard("VIRTUAL MOUSE / TOUCHPAD","Drag inside the pad to move the on-screen pointer.") {
      TouchPad()
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ Button(onClick={},modifier=Modifier.weight(1f)){Text("LEFT CLICK")}; OutlinedButton(onClick={},modifier=Modifier.weight(1f)){Text("RIGHT CLICK")} }
      Text("Pointer and click events are a preview until connected to QEMU.",color=muted,fontSize=10.sp)
     } }
     item { InfoCard("GEN-PC KEYBOARD","Tap the floating keyboard button at the bottom-right to unfold or hide it.") {
      Text("Preview only. Actual Windows key input will be connected after VM engine integration.",color=muted,fontSize=10.sp)
     } }
     item { Row(verticalAlignment=Alignment.CenterVertically){Text("Enable virtual mouse",color=Color.White,modifier=Modifier.weight(1f));Switch(mouse,{mouse=it})}; Row(verticalAlignment=Alignment.CenterVertically){Text("Show keyboard",color=Color.White,modifier=Modifier.weight(1f));Switch(keyboardEnabled,{keyboardEnabled=it; if(!it) keyboardExpanded=false})} }
    }
    else -> LazyColumn(Modifier.weight(1f).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
     item { Text("PC SETTINGS",color=blue,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp) }
     item { InfoCard("DISPLAY RESOLUTION","Choose the planned Windows desktop size.") { listOf("960 × 600","1280 × 800","1366 × 768").forEach{v->Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().clickable{resolution=v;prefs.edit().putString("resolution",v).apply()}){RadioButton(resolution==v,{resolution=v;prefs.edit().putString("resolution",v).apply()});Text(v,color=Color.White)}} } }
     item { InfoCard("VIRTUAL MEMORY","Low memory settings for a 4 GB tablet.") { listOf("1 GB","1.5 GB","2 GB").forEach{v->Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().clickable{ram=v;prefs.edit().putString("ram",v).apply()}){RadioButton(ram==v,{ram=v;prefs.edit().putString("ram",v).apply()});Text(v,color=Color.White)}};Text("Saved preference only. Actual VM memory is not allocated yet.",color=muted,fontSize=10.sp) } }
     item { Toggle("Fit Windows screen to tablet", "Scale guest display to available screen",fit){fit=it} }
     item { Toggle("Touch feedback", "Haptic feedback for touch controls",haptics){haptics=it} }
     item { Toggle("Virtual mouse", "Enable touchpad controls",mouse){mouse=it} }
     item { InfoCard("ABOUT","WinDroid 11 • UI foundation\nISO picker • Mouse UI • Gen-PC keyboard • Settings\nQEMU engine and Windows boot are not implemented yet."){} }
    }
   }
   }
   Text("WIN DROID 11  •  ANDROID HOST",color=Color(0xFF6F8CA9),fontSize=9.sp,modifier=Modifier.align(Alignment.CenterHorizontally).padding(9.dp))
  }
  if (keyboardExpanded && keyboardEnabled) {
   Surface(modifier=Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal=8.dp,bottom=78.dp),color=Color(0xF0142941),shape=RoundedCornerShape(18.dp),shadowElevation=12.dp) {
    Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
     Row(verticalAlignment=Alignment.CenterVertically) {
      Text("GEN-PC KEYBOARD",color=blue,fontSize=10.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
      Text("HIDE  ×",color=muted,fontSize=11.sp,modifier=Modifier.clickable{keyboardExpanded=false}.padding(6.dp))
     }
     BasicTextField(value=typed,onValueChange={typed=it},textStyle=TextStyle(color=Color.White,fontSize=13.sp),modifier=Modifier.fillMaxWidth().background(bg,RoundedCornerShape(8.dp)).padding(9.dp),decorationBox={inner->if(typed.isEmpty())Text("Input preview…",color=muted);inner()})
     GenKeyboard { key -> typed=when(key){"BACK"->typed.dropLast(1);"SPACE"->typed+" ";"ENTER"->typed+"\n";"CLEAR"->"";else->typed+key} }
     Text("Preview only • Windows input bridge comes later",color=muted,fontSize=9.sp)
    }
   }
  }
  SmallFloatingActionButton(onClick={if(keyboardEnabled) keyboardExpanded=!keyboardExpanded},modifier=Modifier.align(Alignment.BottomEnd).padding(14.dp),containerColor=if(keyboardExpanded)blue else Color(0xFF315273),contentColor=Color.White) {
   Text(if(keyboardExpanded) "×" else "⌨",fontSize=20.sp,fontWeight=FontWeight.Bold)
  }
 }
}

@Composable private fun InfoCard(title:String,subtitle:String,content:@Composable ColumnScope.()->Unit) {
 Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(18.dp)){Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){Text(title,color=blue,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp);Text(subtitle,color=Color.White,fontSize=13.sp);content()}}
}
@Composable private fun Toggle(title:String,sub:String,value:Boolean,onChange:(Boolean)->Unit){InfoCard(title,sub){Switch(value,onChange)}}
@Composable private fun TouchPad(){
 var p by remember{mutableStateOf(androidx.compose.ui.geometry.Offset(.5f,.5f))}
 BoxWithConstraints(Modifier.fillMaxWidth().height(170.dp).background(bg,RoundedCornerShape(14.dp)).pointerInput(Unit){detectDragGestures{change,drag->change.consume();p=androidx.compose.ui.geometry.Offset((p.x+drag.x/size.width.toFloat()).coerceIn(.05f,.95f),(p.y+drag.y/size.height.toFloat()).coerceIn(.08f,.92f))}}){
  Text("DRAG TO MOVE",color=muted,fontSize=10.sp,modifier=Modifier.align(Alignment.TopCenter).padding(10.dp))
  Box(Modifier.offset(x=maxWidth*p.x-8.dp,y=maxHeight*p.y-8.dp).size(16.dp).background(blue,RoundedCornerShape(50)))
  Text("TRACKPAD AREA",color=Color(0xFF58728E),fontSize=9.sp,modifier=Modifier.align(Alignment.BottomCenter).padding(8.dp))
 }
}
@Composable private fun GenKeyboard(onKey:(String)->Unit){
 val rows=listOf(listOf("ESC","1","2","3","4","5","6","7","8","9","0","BACK"),listOf("TAB","Q","W","E","R","T","Y","U","I","O","P"),listOf("CAPS","A","S","D","F","G","H","J","K","L","ENTER"),listOf("SHIFT","Z","X","C","V","B","N","M",",",".","SHIFT"),listOf("CTRL","FN","ALT","SPACE","ALT","←","↓","↑","→"))
 Column(verticalArrangement=Arrangement.spacedBy(4.dp)){rows.forEach{r->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){r.forEach{k->Surface(color=if(k in listOf("SPACE","ENTER","SHIFT","CTRL","ALT","TAB","BACK"))Color(0xFF315273) else Color(0xFF203A55),shape=RoundedCornerShape(5.dp),modifier=Modifier.weight(if(k=="SPACE")2.7f else if(k.length>1)1.25f else 1f).height(35.dp).clickable{onKey(when(k){"BACK"->"BACK";"SPACE"->"SPACE";"ENTER"->"ENTER";else->if(k.length==1)k.lowercase() else ""})}){Box(contentAlignment=Alignment.Center){Text(k,color=Color.White,fontSize=if(k.length>3)8.sp else 10.sp,fontWeight=FontWeight.SemiBold)}}}}};OutlinedButton(onClick={onKey("CLEAR")},modifier=Modifier.fillMaxWidth()){Text("CLEAR INPUT PREVIEW")} }
}
