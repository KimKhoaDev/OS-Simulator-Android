package com.kimkhoa.ossim

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

data class WApp(val name: String, val emoji: String, val color: Color)

val WAPPS = listOf(
    WApp("Activity", "🔥", Color(0xFFFF2D55)), WApp("Heart", "❤️", Color(0xFFFF3B30)), WApp("Timer", "⏱️", Color(0xFFFF9500)),
    WApp("Workout", "🏃", Color(0xFFA3E635)), WApp("Messages", "💬", Color(0xFF34C759)), WApp("Music", "🎵", Color(0xFFFF375F)),
    WApp("Weather", "⛅", Color(0xFF0A84FF)), WApp("Maps", "🗺️", Color(0xFF30B0C7)), WApp("Settings", "⚙️", Color(0xFF8E8E93)),
    WApp("Mail", "✉️", Color(0xFF0A84FF)), WApp("Wallet", "💳", Color(0xFF636366)), WApp("Sleep", "🌙", Color(0xFF5E5CE6)),
    WApp("Breathe", "🌬️", Color(0xFF64D2FF)), WApp("Notes", "📝", Color(0xFFFFD60A)), WApp("Camera", "📷", Color(0xFF48484A)),
    WApp("Photos", "🌸", Color(0xFFFF6482)), WApp("Calc", "🧮", Color(0xFFFF9F0A)), WApp("Compass", "🧭", Color(0xFFFF453A)),
    WApp("Torch", "🔦", Color(0xFFFFD60A)),
)

@Composable
fun WatchSim(onExit: () -> Unit) {
    var booted by remember { mutableStateOf(false) }
    if (!booted) Boot(Os.WATCH) { booted = true } else WatchRoot(onExit)
}

@Composable
fun WatchRoot(onExit: () -> Unit) {
    var screen by remember { mutableStateOf("face") }
    var sel by remember { mutableStateOf(WAPPS[0]) }
    var face by remember { mutableIntStateOf(0) }
    val panX = remember { Animatable(0f) }
    val panY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    BackHandler { when (screen) { "app" -> screen = "grid"; "grid" -> screen = "face"; else -> onExit() } }
    Box(Modifier.fillMaxSize().background(Color(0xFF0B0B0F)), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxHeight(0.78f).aspectRatio(0.82f)) {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(56.dp)).background(Color(0xFF2A2A30)).padding(6.dp)) {
                Box(Modifier.fillMaxSize().clip(RoundedCornerShape(50.dp)).background(Color.Black)) {
                    AnimatedContent(
                        screen,
                        transitionSpec = { (fadeIn(tween(300)) + scaleIn(initialScale = 0.7f)) togetherWith (fadeOut(tween(200)) + scaleOut(targetScale = 1.3f)) },
                        label = "ws",
                    ) { s ->
                        when (s) {
                            "face" -> WatchFace(face) { dir -> face = (face + dir + 3) % 3 }
                            "grid" -> Honeycomb(panX.value, panY.value, { dx, dy -> scope.launch { panX.snapTo(panX.value + dx); panY.snapTo(panY.value + dy) } }, { a -> sel = a; screen = "app" })
                            else -> WatchApp(sel)
                        }
                    }
                }
            }
            // digital crown
            Box(
                Modifier.align(Alignment.CenterEnd).offset(x = 9.dp, y = (-30).dp).width(12.dp).height(52.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFFF9500))
                    .pointerInput(Unit) { detectVerticalDragGestures { _, d -> scope.launch { panY.snapTo(panY.value + d * 2.5f) } } }
                    .clickable { screen = if (screen == "face") "grid" else "face" },
            )
            Box(Modifier.align(Alignment.CenterEnd).offset(x = 6.dp, y = 34.dp).width(7.dp).height(34.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF3A3A40)).clickable { if (screen != "face") screen = "grid" })
        }
    }
}

@Composable
fun Rings(modifier: Modifier, widths: Float = 22f) {
    val targets = listOf(0.82f, 0.64f, 0.91f)
    val anims = remember { List(3) { Animatable(0f) } }
    LaunchedEffect(Unit) { anims.forEachIndexed { i, a -> launch { delay(i * 180L); a.animateTo(targets[i], tween(1400, easing = FastOutSlowInEasing)) } } }
    val cols = listOf(Color(0xFFFF2D55), Color(0xFFA3E635), Color(0xFF32D3F0))
    Canvas(modifier) {
        for (i in 0 until 3) {
            val inset = i * (widths * 1.25f) + widths / 2
            val d = size.minDimension - inset * 2
            val tl = Offset(inset, inset)
            drawArc(cols[i].copy(alpha = 0.25f), 0f, 360f, false, tl, Size(d, d), style = Stroke(widths, cap = StrokeCap.Round))
            drawArc(cols[i], -90f, 360f * anims[i].value, false, tl, Size(d, d), style = Stroke(widths, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun WatchFace(face: Int, onSwipe: (Int) -> Unit) {
    val now by rememberNow(33)
    var acc by remember { mutableFloatStateOf(0f) }
    Box(
        Modifier.fillMaxSize().pointerInput(Unit) {
            detectHorizontalDragGestures(onDragEnd = { if (acc > 80f) onSwipe(-1) else if (acc < -80f) onSwipe(1); acc = 0f }) { _, d -> acc += d }
        },
    ) {
        Crossfade(face, label = "face") { f ->
            when (f) {
                0 -> Box(Modifier.fillMaxSize()) {
                    Rings(Modifier.align(Alignment.Center).fillMaxSize(0.92f), 14f)
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        T(fmt(now, "HH:mm"), 40.sp, weight = FontWeight.Bold); T(fmt(now, "EEE d"), 14.sp, Color(0xFFFF9500))
                    }
                }
                1 -> Box(Modifier.fillMaxSize().padding(10.dp)) { AnalogClock(now, Modifier.fillMaxSize(), Color.Black, Color.White, Color(0xFFFF9500)) }
                else -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    T(fmt(now, "HH"), 74.sp, Color(0xFF32D3F0), FontWeight.Black)
                    T(fmt(now, "mm"), 74.sp, Color(0xFFFF2D55), FontWeight.Black)
                    T(fmt(now, "ss"), 20.sp, Color.White)
                }
            }
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            for (i in 0 until 3) Box(Modifier.size(if (i == face) 7.dp else 5.dp).clip(CircleShape).background(if (i == face) Color.White else Color(0x66FFFFFF)))
        }
    }
}

@Composable
fun Honeycomb(panX: Float, panY: Float, onPan: (Float, Float) -> Unit, onOpen: (WApp) -> Unit) {
    val coords = listOf(
        0 to 0, 1 to 0, 0 to 1, -1 to 1, -1 to 0, 0 to -1, 1 to -1,
        2 to 0, 1 to 1, 0 to 2, -1 to 2, -2 to 2, -2 to 1, -2 to 0, -1 to -1, 0 to -2, 1 to -2, 2 to -2, 2 to -1,
    )
    BoxWithConstraints(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { c, d -> c.consume(); onPan(d.x, d.y) } }) {
        val d = 74f
        val dens = LocalDensity.current.density
        val maxR = constraints.maxWidth * 0.62f
        WAPPS.forEachIndexed { i, a ->
            val (q, r) = coords[i]
            val x = d * (q + r / 2f) * dens + panX
            val y = d * (r * 0.866f) * dens + panY
            val dist = sqrt(x * x + y * y)
            val sc = (1.15f - dist / maxR).coerceIn(0.3f, 1f)
            Box(
                Modifier.align(Alignment.Center).offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .graphicsLayer { scaleX = sc; scaleY = sc; alpha = (sc * 1.4f).coerceIn(0f, 1f) }
                    .size(64.dp).clip(CircleShape).background(a.color).pointerInput(Unit) { detectTapGestures { onOpen(a) } },
                contentAlignment = Alignment.Center,
            ) { T(a.emoji, 28.sp) }
        }
    }
}

@Composable
fun WatchApp(app: WApp) {
    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
        when (app.name) {
            "Activity" -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Rings(Modifier.size(150.dp), 18f); Spacer(Modifier.height(8.dp))
                T("Move 410/500 CAL", 12.sp, Color(0xFFFF2D55), FontWeight.SemiBold)
            }
            "Heart" -> {
                val t = rememberInfiniteTransition(label = "hb")
                val beat by t.animateFloat(1f, 1.3f, infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "b")
                var bpm by remember { mutableIntStateOf(72) }
                LaunchedEffect(Unit) { while (true) { delay(1500); bpm = Random.nextInt(62, 80) } }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    T("❤️", 56.sp, modifier = Modifier.graphicsLayer { scaleX = beat; scaleY = beat })
                    T("$bpm", 48.sp, weight = FontWeight.Bold); T("BPM", 14.sp, Color(0xFFFF3B30))
                }
            }
            "Timer" -> {
                var left by remember { mutableIntStateOf(60) }
                var run by remember { mutableStateOf(false) }
                LaunchedEffect(run) { while (run && left > 0) { delay(1000); left-- }; if (left == 0) run = false }
                val prog by animateFloatAsState(left / 60f, tween(900, easing = LinearEasing), label = "tp")
                Box(Modifier.clickable { if (left == 0) left = 60 else run = !run }, contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(150.dp)) {
                        drawArc(Color(0x33FF9500), 0f, 360f, false, style = Stroke(14f, cap = StrokeCap.Round))
                        drawArc(Color(0xFFFF9500), -90f, 360f * prog, false, style = Stroke(14f, cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { T("$left", 44.sp, weight = FontWeight.Bold); T(if (run) "tap to pause" else "tap to start", 10.sp, Color(0xFFFF9500)) }
                }
            }
            "Workout" -> {
                var run by remember { mutableStateOf(false) }
                var sec by remember { mutableIntStateOf(0) }
                LaunchedEffect(run) { while (run) { delay(1000); sec++ } }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    T(if (run) "🏃" else "🚶", 44.sp); T("%02d:%02d".format(sec / 60, sec % 60), 38.sp, Color(0xFFA3E635), FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.clip(CircleShape).background(if (run) Color(0xFFFF453A) else Color(0xFFA3E635)).clickable { run = !run }.padding(horizontal = 22.dp, vertical = 8.dp)) { T(if (run) "Stop" else "Start", 16.sp, Color.Black, FontWeight.Bold) }
                }
            }
            else -> {
                val t = rememberInfiniteTransition(label = "g")
                val s by t.animateFloat(0.92f, 1.1f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "s")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    T(app.emoji, 60.sp, modifier = Modifier.graphicsLayer { scaleX = s; scaleY = s }); T(app.name, 20.sp, app.color, FontWeight.Bold)
                }
            }
        }
    }
}
