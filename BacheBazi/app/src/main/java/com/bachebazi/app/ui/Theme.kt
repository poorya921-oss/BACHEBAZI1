package com.bachebazi.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Pal {
    val Navy = Color(0xFF050B1F)
    val NavyMid = Color(0xFF0A1735)
    val Surface = Color(0xFF0E2149)
    val Surface2 = Color(0xFF14306A)
    val Blue = Color(0xFF2F6BFF)
    val Cyan = Color(0xFF22D3EE)
    val Text = Color(0xFFEAF2FF)
    val Subdued = Color(0xFFA9BCDD)
    val Line = Color(0x4D6FA0FF)
    val Error = Color(0xFFFF8A96)
}

@Composable
fun BacheBaziTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Pal.Blue, onPrimary = Color.White,
            secondary = Pal.Cyan, onSecondary = Pal.Navy,
            background = Pal.Navy, onBackground = Pal.Text,
            surface = Pal.Surface, onSurface = Pal.Text,
            error = Pal.Error,
        ),
        content = content,
    )
}

/** Dice face drawn natively (no image assets). */
@Composable
fun DiceIcon(
    face: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    body: Color = Color.White,
    pip: Color = Pal.Blue,
    description: String? = null,
) {
    val m = if (description != null) modifier.semantics { contentDescription = description } else modifier
    Canvas(m.size(iconSize)) {
        val s = this.size.minDimension
        drawRoundRect(body, cornerRadius = CornerRadius(s * 0.22f))
        pipsFor(face).forEach { drawCircle(pip, radius = s * 0.08f, center = Offset(it.x * s, it.y * s)) }
    }
}

private fun pipsFor(face: Int): List<Offset> {
    val a = 0.28f; val c = 0.5f; val b = 0.72f
    return when (face) {
        1 -> listOf(Offset(c, c))
        2 -> listOf(Offset(a, a), Offset(b, b))
        3 -> listOf(Offset(a, a), Offset(c, c), Offset(b, b))
        4 -> listOf(Offset(a, a), Offset(b, a), Offset(a, b), Offset(b, b))
        5 -> listOf(Offset(a, a), Offset(b, a), Offset(c, c), Offset(a, b), Offset(b, b))
        else -> listOf(Offset(a, a), Offset(a, c), Offset(a, b), Offset(b, a), Offset(b, c), Offset(b, b))
    }
}
