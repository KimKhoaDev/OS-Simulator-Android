package com.kimkhoa.ossim

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class TItem(val title: String, val emoji: String, val color: Color)

val TAPPS = listOf(
    TItem("Live TV", "📡", Color(0xFF2563EB)), TItem("StreamBox", "🎬", Color(0xFFDC2626)), TItem("Music", "🎶", Color(0xFF16A34A)),
    TItem("Gallery", "🖼️", Color(0xFF9333EA)), TItem("Browser", "🌐", Color(0xFF0891B2)), TItem("Games", "🎮", Color(0xFFEA580C)), TItem("Settings", "⚙️", Color(0xFF64748B)),
)
val TREC = listOf(
    TItem("Neon Horizon", "🌆", Color(0xFF7C3AED)), TItem("Ocean Planet", "🌊", Color(0xFF0284C7)), TItem("Mountain Run", "🏔️", Color(0xFF059669)),
    TItem("Galaxy Quest", "🚀", Color(0xFFDB2777)), TItem("Desert Wind", "🏜️", Color(0xFFD97706)),
)
val TSRC = listOf(TItem("HDMI 1", "🔌", Color(0xFF475569)), TItem("HDMI 2", "🔌", Color(0xFF475569)), TItem("USB", "💾", Color(0xFF475569)), TItem("Cast", "📲", Color(0xFF475569)))
val TROWS = listOf(TAPPS, TREC, TSRC)
val TMENU = listOf("Picture", "Sound", "Network", "General", "Support")
val TCHANNELS = listOf("Sci‑Fi HD", "News 24", "Nature One", "Sports Live", "Cinema Gold", "Kids Zone")

class TvState {
    var power by mutableStateOf(true)
    var screen by mutableStateOf("home")
    var row by mutableIntStateOf(0)
    val cols = mutableStateListOf(0, 0, 0)
    var volume by mutableIntStateOf(20)
    var channel by mutableIntStateOf(0)
    var menu by mutableIntStateOf(0)
    var playing by mutableStateOf(true)
    var osd by mutableStateOf("")
    var osdTick by mutableIntStateOf(0)

    fun showOsd(s: String) { osd = s; osdTick++ }
    fun key(k: String) {
        if (k == "POWER") { power = !power; return }
        if (!power) return
        when (k) {
            "VOL+" -> { volume = (volume + 5).coerceAtMost(100); showOsd("Volume") }
            "VOL-" -> { volume = (volume - 5).coerceAtLeast(0); showOsd("Volume") }
            "CH+" -> { channel = (channel + 1) % TCHANNELS.size; showOsd("Channel") }
            "CH-" -> { channel = (channel + TCHANNELS.size - 1) % TCHANNELS.size; showOsd("Channel") }
            "HOME" -> screen = "home"
            "BACK" -> screen = "home"
        }
        when (screen) {
            "home" -> when (k) {
                "L" -> cols[row] = (cols[row] - 1).coerceAtLeast(0)
                "R" -> cols[row] = (cols[row] + 1).coerceAtMost(TROWS[row].size - 1)
                "U" -> row = (row - 1).coerceAtLeast(0)
                "D" -> row = (row + 1).coerceAtMost(2)
                "OK" -> {
                    val it = TROWS[row][cols[row]]
                    screen = when (row) { 0 -> when (it.title) { "Live TV" -> "live"; "Settings" -> "settings"; else -> "app:${it.title}" }; 1 -> "player:${it.title}"; else -> "source:${it.title}" }
                    playing = true
                }
            }
            "settings" -> when (k) { "U" -> menu = (menu - 1).coerceAtLeast(0); "D" -> menu = (menu + 1).coerceAtMost(TMENU.size - 1) }
            "live" -> when (k) { "U" -> key("CH+"); "D" -> key("CH-"); "OK" -> showOsd("Channel") }
            else -> if (k == "OK") playing = !playing
        }
    }
}

@Composable
fun TizenSim(onExit: () -> Unit) {
    val act = LocalContext.current as? Activity
    DisposableEffect(Unit) {
        act?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { act?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
    var booted by remember { mutableStateOf(false) }
    if (!booted) Boot(Os.TIZEN) { booted = true } else TvRoot(onExit)
}

@Composable
fun TvRoot(onExit: () -> Unit) {
    val tv = remember { TvState() }
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { fr.requestFocus() }
    BackHandler { if (tv.screen != "home") tv.screen = "home" else onExit() }
    Row(
        Modifier.fillMaxSize().background(Color(0xFF05070B)).focusRequester(fr).focusable().onPreviewKeyEvent {
            if (it.type == KeyEventType.KeyDown) {
                val k = when (it.key) {
                    Key.DirectionLeft -> "L"; Key.DirectionRight -> "R"; Key.DirectionUp -> "U"; Key.DirectionDown -> "D"
                    Key.Enter, Key.DirectionCenter, Key.NumPadEnter -> "OK"; Key.Escape -> "BACK"; else -> null
                }
                if (k != null) { tv.key(k); true } else false
            } else false
        },
    ) {
        Box(Modifier.weight(1f).fillMaxHeight().padding(8.dp), contentAlignment = Alignment.Center) {
            val off by animateFloatAsState(if (tv.power) 1f else 0.004f, tween(350), label = "off")
            Box(
                Modifier.fillMaxHeight().aspectRatio(16f / 9f, matchHeightConstraintsFirst = true).graphicsLayer { scaleY = off; scaleX = if (tv.power) 1f else 0.9f }
                    .clip(RoundedCornerShape(8.dp)).border(3.dp, Color(0xFF1B1F27), RoundedCornerShape(8.dp)).background(Color.Black),
            ) {
                if (tv.power) TvScreen(tv)
            }
        }
        Remote(tv, Modifier.width(150.dp).fillMaxHeight())
    }
}

@Composable
fun TvScreen(tv: TvState) {
    val now by rememberNow()
    var osdShow by remember { mutableStateOf(false) }
    LaunchedEffect(tv.osdTick) { if (tv.osdTick > 0) { osdShow = true; delay(1600); osdShow = false } }
    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            tv.screen, Modifier.fillMaxSize(),
            transitionSpec = { (fadeIn(tween(300)) + scaleIn(initialScale = 0.96f)) togetherWith (fadeOut(tween(200)) + scaleOut(targetScale = 1.04f)) },
            label = "tvs",
        ) { s ->
            when {
                s == "home" -> SmartHub(tv, now)
                s == "live" -> LiveTv(tv)
                s == "settings" -> TvSettings(tv)
                s.startsWith("player:") -> TvPlayer(s.removePrefix("player:"), tv)
                s.startsWith("source:") -> NoSignal(s.removePrefix("source:"))
                else -> TvApp(s.removePrefix("app:"))
            }
        }
        AnimatedVisibility(osdShow, Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp), enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
            Row(Modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xDD111827)).padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (tv.osd == "Volume") {
                    T(if (tv.volume == 0) "🔇" else "🔊", 18.sp)
                    Box(Modifier.width(140.dp).height(6.dp).clip(CircleShape).background(Color(0x44FFFFFF))) { Box(Modifier.fillMaxHeight().fillMaxWidth(tv.volume / 100f).background(Color(0xFF38BDF8))) }
                    T("${tv.volume}", 14.sp)
                } else T("CH ${101 + tv.channel}  ${TCHANNELS[tv.channel]}", 15.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SmartHub(tv: TvState, now: Long) {
    val focused = TROWS[tv.row][tv.cols[tv.row]]
    val t = rememberInfiniteTransition(label = "par")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), label = "p")
    Box(Modifier.fillMaxSize()) {
        Crossfade(focused.color, animationSpec = tween(600), label = "hero") { c ->
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(c, Color(0xFF05070B)), start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(1400f * (0.6f + p * 0.4f), 900f))))
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { T("TIZEN  Smart Hub", 12.sp, Color(0xCCFFFFFF), FontWeight.SemiBold); T(fmt(now, "HH:mm"), 12.sp) }
            Spacer(Modifier.weight(1f))
            AnimatedContent(focused.title, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) }, label = "title") { ttl ->
                Column { T(focused.emoji, 38.sp); T(ttl, 28.sp, weight = FontWeight.Bold); T(when (tv.row) { 0 -> "App"; 1 -> "Recommended for you"; else -> "Source" }, 12.sp, Color(0xBBFFFFFF)) }
            }
            Spacer(Modifier.height(10.dp))
            TROWS.forEachIndexed { r, items ->
                Row(Modifier.padding(bottom = 6.dp).height(if (r == 1) 62.dp else 44.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    items.forEachIndexed { c, it ->
                        val on = tv.row == r && tv.cols[r] == c
                        val sc by animateFloatAsState(if (on) 1.22f else 1f, spring(0.6f, 450f), label = "sc")
                        val w = if (r == 1) 96.dp else 54.dp
                        Box(
                            Modifier.width(w).fillMaxHeight().graphicsLayer { scaleX = sc; scaleY = sc }.clip(RoundedCornerShape(10.dp))
                                .background(if (on) it.color else Color(0x33FFFFFF)).then(if (on) Modifier.border(2.dp, Color.White, RoundedCornerShape(10.dp)) else Modifier)
                                .clickable { tv.row = r; tv.cols[r] = c; tv.key("OK") },
                            contentAlignment = Alignment.Center,
                        ) { Column(horizontalAlignment = Alignment.CenterHorizontally) { T(it.emoji, if (r == 1) 20.sp else 18.sp); if (r != 0) T(it.title, 9.sp, maxLines = 1) } }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveTv(tv: TvState) {
    val t = rememberInfiniteTransition(label = "live")
    val a by t.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse), label = "a")
    val cols = listOf(Color(0xFF1E3A8A), Color(0xFF7F1D1D), Color(0xFF14532D), Color(0xFF9A3412), Color(0xFF581C87), Color(0xFF155E75))
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(cols[tv.channel], Color.Black.copy(alpha = 0.4f + a * 0.3f))))) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { T("📡", 64.sp); T(TCHANNELS[tv.channel], 26.sp, weight = FontWeight.Bold); T("Use ▲ ▼ or CH to zap", 12.sp, Color(0xAAFFFFFF)) }
        Row(Modifier.padding(14.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x99000000)).padding(8.dp, 4.dp)) { T("CH ${101 + tv.channel}", 12.sp, weight = FontWeight.SemiBold) }
    }
}

@Composable
fun TvSettings(tv: TvState) {
    Row(Modifier.fillMaxSize().background(Color(0xFF0B1220))) {
        Column(Modifier.width(150.dp).fillMaxHeight().background(Color(0xFF111A2E)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            T("Settings", 20.sp, weight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
            TMENU.forEachIndexed { i, m ->
                val bg by animateColorAsState(if (tv.menu == i) Color(0xFF2563EB) else Color.Transparent, label = "m")
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(bg).clickable { tv.menu = i }.padding(10.dp)) { T(m, 14.sp) }
            }
        }
        Column(Modifier.weight(1f).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            T(TMENU[tv.menu], 22.sp, weight = FontWeight.Bold)
            listOf("Mode" to "Standard", "Brightness" to "${40 + tv.menu * 8}", "Contrast" to "${70 - tv.menu * 5}", "Eco mode" to "On").forEach { (k, v) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF16233D)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { T(k, 14.sp); T(v, 14.sp, Color(0xFF7DD3FC)) }
            }
        }
    }
}

@Composable
fun TvPlayer(title: String, tv: TvState) {
    var prog by remember { mutableFloatStateOf(0.05f) }
    LaunchedEffect(tv.playing) { while (tv.playing && prog < 1f) { delay(200); prog += 0.004f } }
    val t = rememberInfiniteTransition(label = "pl")
    val s by t.animateFloat(0.95f, 1.08f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "s")
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF312E81), Color.Black)))) {
        T(TREC.firstOrNull { it.title == title }?.emoji ?: "🎬", 90.sp, modifier = Modifier.align(Alignment.Center).graphicsLayer { scaleX = s; scaleY = s })
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(20.dp)) {
            T(title, 18.sp, weight = FontWeight.Bold); Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color(0x44FFFFFF))) { Box(Modifier.fillMaxHeight().fillMaxWidth(prog.coerceAtMost(1f)).background(Color(0xFF38BDF8))) }
            Spacer(Modifier.height(4.dp)); T(if (tv.playing) "▶ Playing  (OK to pause)" else "⏸ Paused", 11.sp, Color(0xCCFFFFFF))
        }
    }
}

@Composable
fun NoSignal(name: String) {
    val t = rememberInfiniteTransition(label = "ns")
    val a by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0A)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { T("🔌", 50.sp); T(name, 22.sp, weight = FontWeight.Bold); T("No signal", 14.sp, Color.White.copy(alpha = a)) }
    }
}

@Composable
fun TvApp(name: String) {
    val spin by rememberInfiniteTransition(label = "sp").animateFloat(0f, 360f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "r")
    val item = TAPPS.firstOrNull { it.title == name } ?: TAPPS[0]
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(item.color, Color.Black))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T(item.emoji, 70.sp); T(name, 26.sp, weight = FontWeight.Bold); Spacer(Modifier.height(10.dp))
            Box(Modifier.size(22.dp).graphicsLayer { rotationZ = spin }.border(3.dp, Color.White, CircleShape))
        }
    }
}

@Composable
fun Remote(tv: TvState, modifier: Modifier) {
    @Composable
    fun B(label: String, k: String, w: Int = 40, color: Color = Color(0xFF2B303B)) {
        var down by remember { mutableStateOf(false) }
        val s by animateFloatAsState(if (down) 0.88f else 1f, spring(0.5f, 700f), label = "b")
        Box(
            Modifier.graphicsLayer { scaleX = s; scaleY = s }.width(w.dp).height(32.dp).clip(RoundedCornerShape(16.dp)).background(color)
                .pointerInputTap({ down = it }) { tv.key(k) },
            contentAlignment = Alignment.Center,
        ) { T(label, 13.sp) }
    }
    Column(modifier.padding(6.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFF14171E)).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
        B("⏻", "POWER", 40, Color(0xFFB91C1C))
        B("▲", "U"); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { B("◀", "L", 34); B("OK", "OK", 40, Color(0xFF2563EB)); B("▶", "R", 34) }; B("▼", "D")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { B("⌂", "HOME", 50); B("↩", "BACK", 50) }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { B("V+", "VOL+", 50); B("CH+", "CH+", 50) }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { B("V−", "VOL-", 50); B("CH−", "CH-", 50) }
    }
}

fun Modifier.pointerInputTap(onPress: (Boolean) -> Unit, onTap: () -> Unit): Modifier =
    this.then(Modifier.pointerInput(Unit) {
        androidx.compose.foundation.gestures.detectTapGestures(onPress = { onPress(true); tryAwaitRelease(); onPress(false) }, onTap = { onTap() })
    })
