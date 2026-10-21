package com.bachebazi.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bachebazi.app.logic.isReducedMotion
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Title fades/scales in, pauses, then slides sideways off screen while the overlay fades out. Tap to skip. */
@Composable
fun IntroOverlay(onFinished: () -> Unit) {
    val ctx = LocalContext.current
    val reduced = remember { isReducedMotion(ctx) }
    val alpha = remember { Animatable(if (reduced) 1f else 0f) }
    val scale = remember { Animatable(if (reduced) 1f else 0.85f) }
    val exit = remember { Animatable(0f) }
    val bgAlpha = remember { Animatable(1f) }
    val widthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    LaunchedEffect(Unit) {
        if (reduced) {
            delay(600)
            bgAlpha.animateTo(0f, tween(150))
        } else {
            coroutineScope {
                launch { alpha.animateTo(1f, tween(600)) }
                launch { scale.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
            }
            delay(650)
            coroutineScope {
                launch { exit.animateTo(1f, tween(450, easing = FastOutLinearInEasing)) }
                launch { delay(150); bgAlpha.animateTo(0f, tween(300)) }
            }
        }
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onFinished() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = bgAlpha.value }
                .background(Brush.verticalGradient(listOf(Pal.Navy, Pal.NavyMid, Pal.Navy)))
        )
        Text(
            text = Fa.appName,
            style = TextStyle(
                brush = Brush.horizontalGradient(listOf(Pal.Cyan, Pal.Blue, Pal.Text)),
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            ),
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha.value * (1f - exit.value * 0.5f)
                scaleX = scale.value
                scaleY = scale.value
                translationX = -exit.value * (widthPx + 400f)
            },
        )
    }
}
