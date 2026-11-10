package com.kimkhoa.ossim

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

data class IApp(val name: String, val emoji: String, val a: Color, val b: Color)

val IAPPS = listOf(
    IApp("Photos", "🌸", Color(0xFFFFD60A), Color(0xFFFF375F)), IApp("Camera", "📷", Color(0xFF8E8E93), Color(0xFF3A3A3C)),
    IApp("Clock", "🕒", Color(0xFF2C2C2E), Color(0xFF000000)), IApp("Maps", "🗺️", Color(0xFF30D158), Color(0xFF0A84FF)),
    IApp("Notes", "📝", Color(0xFFFFE066), Color(0xFFFFC300)), IApp("Settings", "⚙️", Color(0xFF8E8E93), Color(0xFF636366)),
    IApp("Weather", "⛅", Color(0xFF64D2FF), Color(0xFF0A84FF)), IApp("Calculator", "🧮", Color(0xFFFF9F0A), Color(0xFF3A3A3C)),
    IApp("Wallet", "💳", Color(0xFF1C1C1E), Color(0xFF48484A)), IApp("Health", "❤️", Color(0xFFFFFFFF), Color(0xFFFFD1DC)),
    IApp("Books", "📚", Color(0xFFFF9500), Color(0xFFFF6B00)), IApp("Files", "📁", Color(0xFF64B5F6), Color(0xFF1976D2)),
)
val IDOCK = listOf(
    IApp("Phone", "📞", Color(0xFF30D158), Color(0xFF248A3D)), IApp("Safari", "🧭", Color(0xFF64D2FF), Color(0xFF0A84FF)),
    IApp("Messages", "💬", Color(0xFF30D158), Color(0xFF34C759)), IApp("Music", "🎵", Color(0xFFFF375F), Color(0xFFFF2D55)),
)

@Composable
fun IosSim(onExit: () -> Unit) {
    var booted by remember { mutableStateOf(false) }
    if (!booted) Boot(Os.IOS) { booted = true } else IosRoot(onExit)
}

@Composable
fun IIcon(app: IApp, size: Dp, label: Boolean, jiggle: Boolean, seed: Int, onTap: (Offset) -> Unit, onLong: () -> Unit) {
    val t = rememberInfiniteTransition(label = "j")
    val rot by t.animateFloat(-2f, 2f, infiniteRepeatable(tween(100 + seed * 9), RepeatMode.Reverse), label = "r")
    var center by remember { mutableStateOf(Offset.Zero) }
    var pressed by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (pressed) 0.88f else 1f, spring(0.5f, 600f), label = "s")
    Column(
        Modifier.graphicsLayer { rotationZ = if (jiggle) rot else 0f; scaleX = s; scaleY = s }
            .pointerInput(jiggle) { detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false }, onTap = { onTap(center) }, onLongPress = { onLong() }) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(size).onGloballyPositioned { center = it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f) }
                .clip(RoundedCornerShape(size * 0.26f)).background(Brush.verticalGradient(listOf(app.a, app.b))),
            contentAlignment = Alignment.Center,
        ) { T(app.emoji, (size.value * 0.5f).sp) }
        if (label) { Spacer(Modifier.height(4.dp)); T(app.name, 11.sp) }
    }
}

@Composable
fun IosToggle(on: Boolean, onChange: (Boolean) -> Unit) {
    val x by animateDpAsState(if (on) 22.dp else 2.dp, spring(0.6f, 500f), label = "x")
    val bg by animateColorAsState(if (on) Color(0xFF34C759) else Color(0xFF39393D), label = "bg")
    Box(Modifier.width(52.dp).height(32.dp).clip(CircleShape).background(bg).clickable(interactionSource = null, indication = null) { onChange(!on) }) {
        Box(Modifier.offset(x, 2.dp).size(28.dp).clip(CircleShape).background(Color.White))
    }
}

@Composable
fun IosRoot(onExit: () -> Unit) {
    val backdrop = rememberLayerBackdrop()
    var locked by remember { mutableStateOf(true) }
    var app by remember { mutableStateOf<IApp?>(null) }
    var shown by remember { mutableStateOf(IAPPS[0]) }
    var cc by remember { mutableStateOf(false) }
    var jiggle by remember { mutableStateOf(false) }
    var island by remember { mutableStateOf(false) }
    var root by remember { mutableStateOf(IntSize(1, 1)) }
    var origin by remember { mutableStateOf(TransformOrigin.Center) }
    val now by rememberNow()
    BackHandler {
        when {
            cc -> cc = false
            jiggle -> jiggle = false
            app != null -> app = null
            !locked -> locked = true
            else -> onExit()
        }
    }
    fun open(a: IApp, c: Offset) {
        origin = TransformOrigin(c.x / root.width.coerceAtLeast(1), c.y / root.height.coerceAtLeast(1))
        shown = a; app = a
    }
    Box(Modifier.fillMaxSize().background(Color.Black).onSizeChanged { root = it }) {
        Wallpaper(Modifier.fillMaxSize().layerBackdrop(backdrop), listOf(Color(0xFF5E5CE6), Color(0xFFFF375F), Color(0xFF0A84FF), Color(0xFFFF9F0A)))
        // home
        Column(Modifier.fillMaxSize().padding(top = 80.dp, bottom = 20.dp, start = 22.dp, end = 22.dp)) {
            IAPPS.chunked(4).forEachIndexed { r, row ->
                Row(Modifier.fillMaxWidth().padding(bottom = 22.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEachIndexed { c, a -> IIcon(a, 62.dp, true, jiggle, r * 4 + c, { p -> if (jiggle) jiggle = false else open(a, p) }, { jiggle = true }) }
                }
            }
            Spacer(Modifier.weight(1f))
            GlassPanel(backdrop, Modifier.fillMaxWidth().padding(bottom = 12.dp), 34.dp) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    IDOCK.forEachIndexed { i, a -> IIcon(a, 62.dp, false, jiggle, 20 + i, { p -> if (jiggle) jiggle = false else open(a, p) }, { jiggle = true }) }
                }
            }
        }
        // status bar + control center pull
        Box(Modifier.fillMaxWidth().height(44.dp).pointerInput(Unit) { detectVerticalDragGestures { _, d -> if (d > 6f) cc = true } })
        Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            T(fmt(now, "HH:mm"), 15.sp, weight = FontWeight.SemiBold); T("5G  🔋", 13.sp)
        }
        val w by animateDpAsState(if (island) 300.dp else 112.dp, spring(0.6f, 300f), label = "w")
        val h by animateDpAsState(if (island) 84.dp else 32.dp, spring(0.6f, 300f), label = "h")
        Box(Modifier.align(Alignment.TopCenter).padding(top = 10.dp).width(w).height(h).clip(RoundedCornerShape(h / 2)).background(Color.Black).pointerInput(Unit) { detectTapGestures { island = !island } }) {
            AnimatedVisibility(island, enter = fadeIn(tween(250, 150)), exit = fadeOut(tween(100))) {
                val t = rememberInfiniteTransition(label = "eq")
                Row(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { T("Now Playing", 12.sp, Color(0xFFAAAAAA)); T("Liquid Glass Mix", 16.sp, weight = FontWeight.SemiBold) }
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(30.dp)) {
                        for (i in 0 until 4) {
                            val bar by t.animateFloat(0.2f, 1f, infiniteRepeatable(tween(300 + i * 90), RepeatMode.Reverse), label = "b$i")
                            Box(Modifier.width(4.dp).fillMaxHeight(bar).clip(CircleShape).background(Color(0xFFFF375F)))
                        }
                    }
                }
            }
        }
        // app overlay
        AnimatedVisibility(
            app != null, Modifier.fillMaxSize(),
            enter = scaleIn(spring(0.8f, 280f), 0.1f, origin) + fadeIn(tween(150)),
            exit = scaleOut(tween(260), 0.1f, origin) + fadeOut(tween(220)),
        ) { IosAppScreen(shown) { app = null } }
        // control center
        AnimatedVisibility(cc, Modifier.fillMaxSize(), enter = slideInVertically { -it / 2 } + fadeIn(), exit = slideOutVertically { -it / 2 } + fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).clickable(interactionSource = null, indication = null) { cc = false }) {
                var br by remember { mutableFloatStateOf(0.6f) }
                GlassPanel(backdrop, Modifier.align(Alignment.TopCenter).padding(top = 70.dp, start = 20.dp, end = 20.dp).fillMaxWidth(), 36.dp) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        T("Control Center", 18.sp, weight = FontWeight.SemiBold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("✈️", "📶", "🔵", "🔦").forEach { e ->
                                var on by remember { mutableStateOf(e == "📶") }
                                GlassCapsule(backdrop, Modifier.size(64.dp).clickable(interactionSource = null, indication = null) { on = !on }, if (on) Color(0xFF0A84FF).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.16f)) { T(e, 26.sp) }
                            }
                        }
                        T("Brightness", 13.sp, Color(0xCCFFFFFF))
                        SimpleSlider(br, { br = it }, Modifier.fillMaxWidth())
                    }
                }
            }
        }
        if (locked) LockScreen(listOf(Color(0xFFFF9F0A), Color(0xFFFF375F), Color(0xFF5E5CE6), Color(0xFF0A84FF)), true) { locked = false }
    }
}

@Composable
fun IosAppScreen(app: IApp, onClose: () -> Unit) {
    val now by rememberNow(50)
    Box(Modifier.fillMaxSize().background(if (app.name == "Calculator" || app.name == "Clock") Color.Black else Color(0xFF1C1C1E))) {
        when (app.name) {
            "Calculator" -> CalculatorApp(Color.Black, Color(0xFF333333), Color(0xFFFF9F0A), Color(0xFFA5A5A5), true)
            "Clock" -> Column(Modifier.fillMaxSize().padding(top = 90.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AnalogClock(now, Modifier.size(300.dp)); Spacer(Modifier.height(20.dp)); T(fmt(now, "HH:mm:ss"), 40.sp, weight = FontWeight.Light)
            }
            "Settings" -> {
                val names = listOf("Airplane Mode", "Wi‑Fi", "Bluetooth", "Dark Appearance", "Reduce Motion", "Haptics")
                Column(Modifier.fillMaxSize().padding(top = 80.dp, start = 16.dp, end = 16.dp)) {
                    T("Settings", 34.sp, weight = FontWeight.Bold); Spacer(Modifier.height(16.dp))
                    Column(Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF2C2C2E))) {
                        names.forEachIndexed { i, n ->
                            var on by remember { mutableStateOf(i in listOf(1, 2, 3, 5)) }
                            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                T(n, 17.sp); IosToggle(on) { on = it }
                            }
                        }
                    }
                }
            }
            else -> {
                val t = rememberInfiniteTransition(label = "hero")
                val b by t.animateFloat(0.9f, 1.15f, infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b")
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    T(app.emoji, 120.sp, modifier = Modifier.graphicsLayer { scaleX = b; scaleY = b })
                    Spacer(Modifier.height(16.dp)); T(app.name, 34.sp, weight = FontWeight.Bold); T("Simulated app", 15.sp, Color(0x99FFFFFF))
                }
            }
        }
        Box(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(150.dp).height(24.dp)
                .pointerInput(Unit) { detectVerticalDragGestures { _, d -> if (d < -10f) onClose() } }
                .clickable(interactionSource = null, indication = null) { onClose() },
            contentAlignment = Alignment.BottomCenter,
        ) { Box(Modifier.width(140.dp).height(5.dp).clip(CircleShape).background(Color.White)) }
    }
}
