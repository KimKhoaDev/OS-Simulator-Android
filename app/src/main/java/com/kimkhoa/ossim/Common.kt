package com.kimkhoa.ossim

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class Os(val label: String, val sub: String, val emoji: String, val c1: Color, val c2: Color) {
    IOS("iOS", "Liquid Glass · Dynamic Island", "📱", Color(0xFF5AC8FA), Color(0xFF5E5CE6)),
    WATCH("watchOS", "Honeycomb · Rings · Crown", "⌚", Color(0xFF34C759), Color(0xFF00B4D8)),
    COLOR("ColorOS 17", "Aquamorphic · Miuix UI", "🎨", Color(0xFFFF9F43), Color(0xFFEE5A9B)),
    TIZEN("Tizen TV", "Smart Hub · Remote · OSD", "📺", Color(0xFF3B82F6), Color(0xFF0EA5E9)),
}

@Composable
fun T(
    text: String, size: TextUnit = 16.sp, color: Color = Color.White, weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier, align: TextAlign = TextAlign.Unspecified, maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(text, modifier, TextStyle(color = color, fontSize = size, fontWeight = weight, textAlign = align), maxLines = maxLines)
}

@Composable
fun rememberNow(step: Long = 1000L): State<Long> = produceState(System.currentTimeMillis()) {
    while (true) { value = System.currentTimeMillis(); delay(step) }
}

fun fmt(ms: Long, p: String): String = SimpleDateFormat(p, Locale.getDefault()).format(Date(ms))

@Composable
fun Boot(os: Os, onDone: () -> Unit) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, tween(2800, easing = LinearEasing)); onDone() }
    val t = rememberInfiniteTransition(label = "boot")
    val glow by t.animateFloat(0.7f, 1.15f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "g")
    val spin by t.animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "s")
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(110.dp).graphicsLayer { scaleX = glow; scaleY = glow; rotationZ = spin * 0.1f }
                    .background(Brush.radialGradient(listOf(os.c1, os.c2, Color.Transparent)), CircleShape),
            )
            Spacer(Modifier.height(28.dp))
            T(os.label, 30.sp, weight = FontWeight.Bold, modifier = Modifier.alpha((p.value * 2f).coerceIn(0f, 1f)))
            Spacer(Modifier.height(22.dp))
            Box(Modifier.width(150.dp).height(4.dp).clip(CircleShape).background(Color(0x33FFFFFF))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(p.value).background(Color.White))
            }
        }
    }
}

@Composable
fun Wallpaper(modifier: Modifier, colors: List<Color>) {
    val t = rememberInfiniteTransition(label = "wp")
    val a by t.animateFloat(0f, 1f, infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse), label = "a")
    Box(modifier.background(Brush.linearGradient(colors, start = Offset(0f, 0f), end = Offset(1200f * (0.5f + a), 2400f)))) {
        Canvas(Modifier.fillMaxSize()) {
            colors.forEachIndexed { i, c ->
                val ang = a * 6.28f + i * 2.1f
                drawCircle(
                    c.copy(alpha = 0.55f), size.minDimension * 0.55f,
                    Offset(size.width * (0.5f + 0.35f * cos(ang)), size.height * (0.5f + 0.35f * sin(ang * 1.3f))),
                )
            }
        }
    }
}

@Composable
fun GlassCapsule(
    backdrop: Backdrop, modifier: Modifier = Modifier, tint: Color = Color.White.copy(alpha = 0.16f),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier.drawBackdrop(
            backdrop = backdrop, shape = { Capsule() },
            effects = { vibrancy(); blur(4f.dp.toPx()); lens(10f.dp.toPx(), 20f.dp.toPx()) },
            onDrawSurface = { drawRect(tint) },
        ),
        contentAlignment = Alignment.Center, content = content,
    )
}

@Composable
fun GlassPanel(
    backdrop: Backdrop, modifier: Modifier = Modifier, corner: Dp = 28.dp, tint: Color = Color.White.copy(alpha = 0.18f),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier.drawBackdrop(
            backdrop = backdrop, shape = { shape },
            effects = { vibrancy(); blur(14f.dp.toPx()) },
            onDrawSurface = { drawRect(tint) },
        ),
        content = content,
    )
}

@Composable
fun SimpleSlider(value: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier, fill: Color = Color.White, track: Color = Color(0x44FFFFFF)) {
    Box(
        modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(track)
            .pointerInput(Unit) { detectHorizontalDragGestures { c, _ -> onChange((c.position.x / size.width).coerceIn(0f, 1f)) } },
    ) { Box(Modifier.fillMaxHeight().fillMaxWidth(value.coerceIn(0.02f, 1f)).background(fill)) }
}

@Composable
fun LockScreen(colors: List<Color>, bold: Boolean, onUnlock: () -> Unit) {
    val backdrop = rememberLayerBackdrop()
    val y = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val now by rememberNow()
    val hint by rememberInfiniteTransition(label = "h").animateFloat(0.3f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "h")
    Box(
        Modifier.fillMaxSize().graphicsLayer { translationY = y.value }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (y.value < -size.height * 0.22f) { y.animateTo(-size.height.toFloat(), tween(250)); onUnlock() }
                            else y.animateTo(0f, spring(0.7f, 300f))
                        }
                    },
                    onDragCancel = { scope.launch { y.animateTo(0f) } },
                    onVerticalDrag = { c, d -> c.consume(); scope.launch { y.snapTo((y.value + d).coerceAtMost(0f)) } },
                )
            },
    ) {
        Wallpaper(Modifier.fillMaxSize().layerBackdrop(backdrop), colors)
        Column(Modifier.fillMaxWidth().padding(top = 110.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            T(fmt(now, "EEEE, MMM d"), 18.sp, Color.White.copy(alpha = 0.85f))
            T(fmt(now, "HH:mm"), if (bold) 92.sp else 80.sp, weight = if (bold) FontWeight.Bold else FontWeight.Light)
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 34.dp).fillMaxWidth().padding(horizontal = 40.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            GlassCapsule(backdrop, Modifier.size(52.dp)) { T("🔦", 22.sp) }
            T("Swipe up to unlock", 14.sp, Color.White.copy(alpha = hint), modifier = Modifier.padding(bottom = 14.dp))
            GlassCapsule(backdrop, Modifier.size(52.dp)) { T("📷", 22.sp) }
        }
    }
}

@Composable
fun AnalogClock(ms: Long, modifier: Modifier, face: Color = Color(0xFF111111), fg: Color = Color.White, accent: Color = Color(0xFFFF9500)) {
    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2
        drawCircle(face, r, c)
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0 - 90).toFloat()
            val big = i % 5 == 0
            val l = if (big) r * 0.1f else r * 0.04f
            val d = Offset(cos(a), sin(a))
            drawLine(fg.copy(alpha = if (big) 1f else 0.4f), c + d * (r * 0.95f - l), c + d * (r * 0.95f), strokeWidth = if (big) 4f else 2f)
        }
        val cal = Calendar.getInstance().apply { timeInMillis = ms }
        val sec = cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000f
        val min = cal.get(Calendar.MINUTE) + sec / 60f
        val hr = (cal.get(Calendar.HOUR) % 12) + min / 60f
        fun hand(deg: Float, len: Float, w: Float, col: Color) {
            val a = Math.toRadians((deg - 90).toDouble()).toFloat()
            drawLine(col, c, c + Offset(cos(a), sin(a)) * (r * len), strokeWidth = w, cap = StrokeCap.Round)
        }
        hand(hr * 30f, 0.5f, 10f, fg)
        hand(min * 6f, 0.78f, 7f, fg)
        hand(sec * 6f, 0.88f, 3f, accent)
        drawCircle(accent, 7f, c)
    }
}

@Composable
fun CalculatorApp(bg: Color, num: Color, op: Color, fn: Color, round: Boolean) {
    var cur by remember { mutableStateOf("0") }
    var acc by remember { mutableStateOf<Double?>(null) }
    var pending by remember { mutableStateOf<String?>(null) }
    var fresh by remember { mutableStateOf(true) }
    fun f(r: Double): String = if (r.isNaN()) "Error" else if (r % 1.0 == 0.0 && kotlin.math.abs(r) < 1e15) r.toLong().toString() else r.toString()
    fun calc() {
        val a = acc
        val b = cur.toDoubleOrNull() ?: 0.0
        if (a != null && pending != null) {
            val r = when (pending) { "+" -> a + b; "−" -> a - b; "×" -> a * b; "÷" -> if (b == 0.0) Double.NaN else a / b; else -> b }
            cur = f(r); acc = r
        } else acc = b
    }
    fun press(k: String) {
        when (k) {
            "C" -> { cur = "0"; acc = null; pending = null; fresh = true }
            "±" -> cur = if (cur.startsWith("-")) cur.drop(1) else if (cur != "0") "-$cur" else cur
            "%" -> cur = f((cur.toDoubleOrNull() ?: 0.0) / 100)
            "+", "−", "×", "÷" -> { if (!fresh) calc() else if (acc == null) acc = cur.toDoubleOrNull(); pending = k; fresh = true }
            "=" -> if (pending != null) { calc(); pending = null; fresh = true }
            "." -> if (fresh) { cur = "0."; fresh = false } else if (!cur.contains(".")) cur += "."
            else -> if (fresh) { cur = k; fresh = false } else cur = if (cur == "0") k else cur + k
        }
    }
    val rows = listOf(listOf("C", "±", "%", "÷"), listOf("7", "8", "9", "×"), listOf("4", "5", "6", "−"), listOf("1", "2", "3", "+"), listOf("0", ".", "="))
    Column(Modifier.fillMaxSize().background(bg).padding(14.dp), verticalArrangement = Arrangement.Bottom) {
        T(cur, if (cur.length > 8) 48.sp else 72.sp, weight = FontWeight.Light, modifier = Modifier.align(Alignment.End).padding(8.dp), maxLines = 1)
        rows.forEach { r ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                r.forEach { k ->
                    val isOp = k in listOf("+", "−", "×", "÷", "=")
                    val c = if (isOp) op else if (k in listOf("C", "±", "%")) fn else num
                    var down by remember { mutableStateOf(false) }
                    val s by animateFloatAsState(if (down) 0.9f else 1f, spring(0.5f, 700f), label = "k")
                    Box(
                        Modifier.weight(if (k == "0") 2f else 1f).height(66.dp).graphicsLayer { scaleX = s; scaleY = s }
                            .clip(if (round) CircleShape else RoundedCornerShape(22.dp)).background(c)
                            .pointerInput(Unit) { detectTapGestures(onPress = { down = true; tryAwaitRelease(); down = false }, onTap = { press(k) }) },
                        contentAlignment = Alignment.Center,
                    ) { T(k, 28.sp) }
                }
            }
        }
    }
}
