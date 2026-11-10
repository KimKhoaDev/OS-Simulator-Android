package com.kimkhoa.ossim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent { App() }
    }
}

@Composable
fun App() {
    var os by remember { mutableStateOf<Os?>(null) }
    BackHandler(enabled = os != null) { os = null }
    AnimatedContent(
        os, Modifier.fillMaxSize().background(Color.Black),
        transitionSpec = { (fadeIn(tween(350)) + scaleIn(initialScale = 0.92f)) togetherWith (fadeOut(tween(250)) + scaleOut(targetScale = 1.05f)) },
        label = "root",
    ) { cur ->
        when (cur) {
            null -> Launcher { os = it }
            Os.IOS -> IosSim { os = null }
            Os.WATCH -> WatchSim { os = null }
            Os.COLOR -> ColorSim { os = null }
            Os.TIZEN -> TizenSim { os = null }
        }
    }
}

@Composable
fun Launcher(onPick: (Os) -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B1020), Color(0xFF1B1030))))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.Center) {
            T("OS Simulator", 34.sp, weight = FontWeight.Bold)
            T("Pick a system to boot", 16.sp, Color(0xAAFFFFFF))
            Spacer(Modifier.height(24.dp))
            Os.entries.chunked(2).forEachIndexed { r, pair ->
                Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    pair.forEachIndexed { c, os ->
                        var shown by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay((r * 2 + c) * 120L); shown = true }
                        val a by animateFloatAsState(if (shown) 1f else 0f, tween(500), label = "a")
                        var down by remember { mutableStateOf(false) }
                        val s by animateFloatAsState(if (down) 0.94f else 1f, spring(0.5f, 600f), label = "s")
                        Column(
                            Modifier.weight(1f).height(170.dp).alpha(a)
                                .graphicsLayer { scaleX = s * (0.9f + 0.1f * a); scaleY = s * (0.9f + 0.1f * a); translationY = (1f - a) * 60f }
                                .clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(os.c1, os.c2)))
                                .pointerInput(Unit) { detectTapGestures(onPress = { down = true; tryAwaitRelease(); down = false }, onTap = { onPick(os) }) }
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            T(os.emoji, 44.sp)
                            Column { T(os.label, 22.sp, weight = FontWeight.Bold); T(os.sub, 12.sp, Color(0xCCFFFFFF)) }
                        }
                    }
                }
            }
        }
    }
}
