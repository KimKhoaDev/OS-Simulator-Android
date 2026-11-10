package com.kimkhoa.ossim

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class CApp(val name: String, val emoji: String, val a: Color, val b: Color)

val CAPPS = listOf(
    CApp("Camera", "📷", Color(0xFF6C63FF), Color(0xFF3F37C9)), CApp("Gallery", "🖼️", Color(0xFFFF7EB3), Color(0xFFFF758C)),
    CApp("Clock", "⏰", Color(0xFF2D3436), Color(0xFF000000)), CApp("Settings", "⚙️", Color(0xFF74B9FF), Color(0xFF0984E3)),
    CApp("Music", "🎧", Color(0xFFFF9F43), Color(0xFFEE5A24)), CApp("Calculator", "➗", Color(0xFF55EFC4), Color(0xFF00B894)),
    CApp("Weather", "🌤️", Color(0xFF81ECEC), Color(0xFF00CEC9)), CApp("Notes", "🗒️", Color(0xFFFFEAA7), Color(0xFFFDCB6E)),
)
val CDOCK = listOf(
    CApp("Phone", "📞", Color(0xFF55EFC4), Color(0xFF00B894)), CApp("Messages", "💬", Color(0xFF74B9FF), Color(0xFF0984E3)),
    CApp("Browser", "🌐", Color(0xFFFFEAA7), Color(0xFFFF9F43)), CApp("Store", "🛍️", Color(0xFFA29BFE), Color(0xFF6C5CE7)),
)

@Composable
fun ColorSim(onExit: () -> Unit) {
    var booted by remember { mutableStateOf(false) }
    if (!booted) Boot(Os.COLOR) { booted = true } else MiuixTheme { ColorRoot(onExit) }
}

@Composable
fun CIcon(a: CApp, size: Int = 58, label: Boolean = true, onClick: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (down) 0.86f else 1f, spring(0.45f, 700f), label = "s")
    Column(
        Modifier.graphicsLayer { scaleX = s; scaleY = s }.pointerInput(Unit) { detectTapGestures(onPress = { down = true; tryAwaitRelease(); down = false }, onTap = { onClick() }) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(size.dp).clip(RoundedCornerShape((size * 0.36f).dp)).background(Brush.linearGradient(listOf(a.a, a.b))), contentAlignment = Alignment.Center) { T(a.emoji, (size * 0.48f).sp) }
        if (label) { Spacer(Modifier.height(4.dp)); T(a.name, 11.sp) }
    }
}

@Composable
fun ColorRoot(onExit: () -> Unit) {
    var locked by remember { mutableStateOf(true) }
    var app by remember { mutableStateOf<CApp?>(null) }
    var shown by remember { mutableStateOf(CAPPS[0]) }
    var folder by remember { mutableStateOf(false) }
    var shade by remember { mutableStateOf(false) }
    var recents by remember { mutableStateOf(false) }
    val recentList = remember { mutableStateListOf(CAPPS[0], CAPPS[3], CAPPS[1], CAPPS[4]) }
    val now by rememberNow()
    fun back() {
        when { shade -> shade = false; recents -> recents = false; folder -> folder = false; app != null -> app = null; !locked -> locked = true; else -> onExit() }
    }
    BackHandler { back() }
    fun open(a: CApp) { shown = a; app = a; recents = false; if (a !in recentList) recentList.add(0, a) }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Wallpaper(Modifier.fillMaxSize(), listOf(Color(0xFFFFB199), Color(0xFFFF6FB5), Color(0xFF7F7FFF), Color(0xFF4FD1C5)))
        Column(Modifier.fillMaxSize().padding(top = 56.dp, bottom = 52.dp, start = 18.dp, end = 18.dp)) {
            Card(Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
                Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { T(fmt(now, "HH:mm"), 44.sp, Color(0xFF1B1B1F), FontWeight.Bold); T(fmt(now, "EEE, MMM d"), 13.sp, Color(0xFF666666)) }
                    Column(horizontalAlignment = Alignment.End) { T("🌤️ 26°", 26.sp, Color(0xFF1B1B1F)); T("Partly cloudy", 12.sp, Color(0xFF666666)) }
                }
            }
            Spacer(Modifier.height(24.dp))
            val cells = listOf<CApp?>(null) + CAPPS
            cells.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { a ->
                        if (a == null) {
                            var down by remember { mutableStateOf(false) }
                            Box(
                                Modifier.size(58.dp).clip(RoundedCornerShape(21.dp)).background(Color(0x55FFFFFF)).padding(6.dp)
                                    .pointerInput(Unit) { detectTapGestures(onTap = { folder = true }) },
                            ) {
                                Column { for (r in 0 until 2) Row { for (c in 0 until 2) Box(Modifier.padding(2.dp).size(20.dp).clip(RoundedCornerShape(7.dp)).background(CAPPS[r * 2 + c + 4].a)) } }
                            }
                        } else CIcon(a) { open(a) }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.size(58.dp)) }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Color(0x44FFFFFF)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                CDOCK.forEach { a -> CIcon(a, 56, false) { open(a) } }
            }
        }
        Box(Modifier.fillMaxWidth().height(40.dp).pointerInput(Unit) { detectVerticalDragGestures { _, d -> if (d > 6f) shade = true } })
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { T(fmt(now, "HH:mm"), 13.sp); T("5G 📶 🔋 84%", 12.sp) }
        // navigation bar
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(46.dp).background(Color(0x22000000)), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            T("◁", 22.sp, modifier = Modifier.clickable { back() }.padding(20.dp, 6.dp))
            T("○", 22.sp, modifier = Modifier.clickable { app = null; folder = false; shade = false; recents = false }.padding(20.dp, 6.dp))
            T("▢", 22.sp, modifier = Modifier.clickable { recents = !recents }.padding(20.dp, 6.dp))
        }
        // folder
        AnimatedVisibility(folder, Modifier.fillMaxSize(), enter = fadeIn() + scaleIn(spring(0.7f, 300f), 0.3f), exit = fadeOut() + scaleOut(targetScale = 0.3f)) {
            Box(Modifier.fillMaxSize().background(Color(0x66000000)).clickable(interactionSource = null, indication = null) { folder = false }, contentAlignment = Alignment.Center) {
                Column(Modifier.fillMaxWidth(0.84f).clip(RoundedCornerShape(36.dp)).background(Color(0xCCFFFFFF)).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    T("Tools", 18.sp, Color(0xFF1B1B1F), FontWeight.SemiBold); Spacer(Modifier.height(16.dp))
                    CAPPS.takeLast(4).chunked(2).forEach { r -> Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceEvenly) { r.forEach { a -> CIcon(a, 62) { folder = false; open(a) } } } }
                }
            }
        }
        // app
        AnimatedVisibility(app != null, Modifier.fillMaxSize(), enter = fadeIn(tween(200)) + scaleIn(spring(0.85f, 320f), 0.75f) + slideInVertically { it / 8 }, exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.8f)) {
            Box(Modifier.fillMaxSize().padding(bottom = 46.dp)) { ColorApp(shown) }
        }
        // recents
        AnimatedVisibility(recents, Modifier.fillMaxSize(), enter = fadeIn() + scaleIn(initialScale = 1.2f), exit = fadeOut() + scaleOut()) {
            Box(Modifier.fillMaxSize().background(Color(0xCC101018)).clickable(interactionSource = null, indication = null) { recents = false }) {
                Row(Modifier.align(Alignment.Center).horizontalScroll(rememberScrollState()).padding(horizontal = 40.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    recentList.toList().forEach { a -> key(a.name) { RecentCard(a, { open(a) }) { recentList.remove(a) } } }
                }
                Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp).clip(CircleShape).background(Color(0x33FFFFFF)).clickable { recentList.clear() }.padding(22.dp, 10.dp)) { T("✕  Clear all", 14.sp) }
            }
        }
        // shade
        AnimatedVisibility(shade, Modifier.fillMaxSize(), enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color(0x66000000)).clickable(interactionSource = null, indication = null) { shade = false }) {
                var br by remember { mutableFloatStateOf(0.6f) }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp)).background(Color(0xF01C1C22)).padding(18.dp).padding(top = 30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    T(fmt(now, "HH:mm"), 40.sp, weight = FontWeight.Bold)
                    listOf(listOf("📶" to "Wi‑Fi", "🔵" to "Bluetooth", "🔦" to "Torch", "✈️" to "Airplane"), listOf("🌙" to "Dark", "🔕" to "DND", "📍" to "Location", "🔄" to "Rotate")).forEach { r ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            r.forEach { (e, n) ->
                                var on by remember { mutableStateOf(n == "Wi‑Fi" || n == "Bluetooth") }
                                val bg by animateColorAsState(if (on) Color(0xFF3D7BFF) else Color(0xFF34343C), label = "t")
                                Column(Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(bg).clickable { on = !on }.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) { T(e, 22.sp); T(n, 11.sp) }
                            }
                        }
                    }
                    Slider(br, { br = it })
                }
            }
        }
        if (locked) LockScreen(listOf(Color(0xFFFFB199), Color(0xFFFF6FB5), Color(0xFF7F7FFF), Color(0xFF4FD1C5)), false) { locked = false }
    }
}

@Composable
fun RecentCard(a: CApp, onOpen: () -> Unit, onDismiss: () -> Unit) {
    val y = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier.width(190.dp).height(350.dp).graphicsLayer { translationY = y.value; alpha = (1f + y.value / 900f).coerceIn(0f, 1f) }
            .clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(a.a, a.b)))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { scope.launch { if (y.value < -220f) { y.animateTo(-1200f, tween(220)); onDismiss() } else y.animateTo(0f, spring(0.7f, 350f)) } },
                    onVerticalDrag = { c, d -> c.consume(); scope.launch { y.snapTo((y.value + d).coerceAtMost(0f)) } },
                )
            }.clickable { onOpen() },
        contentAlignment = Alignment.Center,
    ) { Column(horizontalAlignment = Alignment.CenterHorizontally) { T(a.emoji, 64.sp); T(a.name, 18.sp, weight = FontWeight.SemiBold) } }
}

@Composable
fun ColorApp(a: CApp) {
    val now by rememberNow(50)
    when (a.name) {
        "Settings" -> Column(Modifier.fillMaxSize().background(Color(0xFFF2F3F7)).verticalScroll(rememberScrollState()).padding(top = 70.dp, start = 16.dp, end = 16.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            T("Settings", 34.sp, Color(0xFF1B1B1F), FontWeight.Bold)
            listOf("Wi‑Fi" to true, "Bluetooth" to true, "Dark mode" to false, "Always-on display" to false, "Aquamorphic effects" to true).let { items ->
                Card(Modifier.fillMaxWidth()) {
                    items.forEach { (n, init) ->
                        var on by remember { mutableStateOf(init) }
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { T(n, 16.sp, Color(0xFF1B1B1F)); Switch(on, { on = it }) }
                    }
                }
            }
            var br by remember { mutableFloatStateOf(0.5f) }
            var fs by remember { mutableFloatStateOf(0.4f) }
            Card(Modifier.fillMaxWidth()) {
                T("Brightness", 15.sp, Color(0xFF1B1B1F)); Spacer(Modifier.height(8.dp)); Slider(br, { br = it })
                Spacer(Modifier.height(14.dp)); T("Font size", 15.sp, Color(0xFF1B1B1F)); Spacer(Modifier.height(8.dp)); Slider(fs, { fs = it })
            }
        }
        "Clock" -> Column(Modifier.fillMaxSize().background(Color.Black), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(100.dp)); AnalogClock(now, Modifier.size(290.dp), Color(0xFF1C1C22), Color.White, Color(0xFFFF6FB5)); Spacer(Modifier.height(20.dp)); T(fmt(now, "HH:mm:ss"), 38.sp, weight = FontWeight.Light)
        }
        "Calculator" -> CalculatorApp(Color(0xFF101014), Color(0xFF2A2A32), Color(0xFFFF6FB5), Color(0xFF45454F), false)
        "Camera" -> {
            var focus by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }
            var flash by remember { mutableStateOf(false) }
            val fa by animateFloatAsState(if (flash) 1f else 0f, tween(if (flash) 60 else 300), label = "fl", finishedListener = { flash = false })
            val fs by animateFloatAsState(if (focus != null) 1f else 1.6f, spring(0.6f, 400f), label = "fs")
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF232526), Color(0xFF414345)))).pointerInput(Unit) { detectTapGestures { focus = it } }) {
                focus?.let { f -> Box(Modifier.offset { IntOffset((f.x - 40.dp.toPx()).toInt(), (f.y - 40.dp.toPx()).toInt()) }.size(80.dp).graphicsLayer { scaleX = fs; scaleY = fs }.border(2.dp, Color(0xFFFFD60A), RoundedCornerShape(6.dp))) }
                Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).size(78.dp).clip(CircleShape).background(Color.White).clickable { flash = true }.padding(6.dp).clip(CircleShape).background(Color(0xFFEEEEEE)))
                Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = fa)))
            }
        }
        else -> {
            val t = rememberInfiniteTransition(label = "h")
            val s by t.animateFloat(0.9f, 1.12f, infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "s")
            Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(a.a, a.b))), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                T(a.emoji, 110.sp, modifier = Modifier.graphicsLayer { scaleX = s; scaleY = s }); T(a.name, 32.sp, weight = FontWeight.Bold)
            }
        }
    }
}
