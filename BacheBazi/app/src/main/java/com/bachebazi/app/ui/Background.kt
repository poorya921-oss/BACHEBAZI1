package com.bachebazi.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bachebazi.app.data.BgMode

/** App-wide background. Reads its own state so changes recompose only this layer. */
@Composable
fun BackgroundLayer(vm: AppViewModel) {
    val bmp by vm.background.collectAsStateWithLifecycle()
    val prefs by vm.bgPrefs.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pal.Navy, Pal.NavyMid, Pal.Navy)))
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        listOf(Pal.Blue.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.08f),
                        radius = size.width * 0.9f,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(Pal.Cyan.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.95f),
                        radius = size.width * 0.8f,
                    )
                )
            }
    ) {
        Crossfade(targetState = bmp, label = "bg") { image ->
            if (image != null) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        bitmap = image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = if (prefs.mode == BgMode.Crop) ContentScale.Crop else ContentScale.Fit,
                    )
                }
            }
        }
        if (bmp != null) {
            Box(Modifier.fillMaxSize().drawBehind { drawRect(Pal.Navy.copy(alpha = prefs.overlay)) })
        }
    }
}
