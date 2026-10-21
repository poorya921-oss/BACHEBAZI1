package com.bachebazi.app.logic

import android.content.Context
import android.provider.Settings
import java.text.NumberFormat
import java.util.Locale

/** True when the user disabled system animations (animator duration scale = 0). */
fun isReducedMotion(context: Context): Boolean = try {
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
} catch (e: Exception) { false }

/** Number formatted with Persian digits. */
fun Int.fa(): String = NumberFormat.getInstance(Locale("fa")).format(this)
