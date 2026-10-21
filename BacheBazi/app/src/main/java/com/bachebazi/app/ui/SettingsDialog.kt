package com.bachebazi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bachebazi.app.data.BgMode

@Composable
fun SettingsDialog(vm: AppViewModel, onPickImage: () -> Unit, onDismiss: () -> Unit) {
    val prefs by vm.bgPrefs.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    var overlay by remember { mutableFloatStateOf(prefs.overlay) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .padding(20.dp)
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Pal.Surface2, Pal.Surface)))
                .border(1.dp, Pal.Line, RoundedCornerShape(28.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(Fa.settings, color = Pal.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Section(Fa.bgSection)
            Button(
                onClick = onPickImage, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pal.Blue, contentColor = Color.White),
            ) { Text(Fa.chooseImage) }
            OutlinedButton(
                onClick = vm::resetBackground, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text(Fa.resetBg, color = Pal.Text) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(BgMode.Crop to Fa.modeCrop, BgMode.Fit to Fa.modeFit).forEach { (m, label) ->
                    FilterChip(
                        selected = prefs.mode == m, onClick = { vm.setBgMode(m) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Pal.Text,
                            selectedContainerColor = Pal.Blue, selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
            Text(Fa.overlayLabel, color = Pal.Subdued, fontSize = 13.sp)
            Slider(
                value = overlay,
                onValueChange = { overlay = it; vm.previewOverlay(it) },
                onValueChangeFinished = vm::commitOverlay,
                valueRange = 0f..0.9f,
                colors = SliderDefaults.colors(thumbColor = Pal.Cyan, activeTrackColor = Pal.Cyan),
            )

            HorizontalDivider(color = Pal.Line)
            Section(Fa.historySection)
            if (history.isEmpty()) {
                Text(Fa.historyEmpty, color = Pal.Subdued, fontSize = 13.sp)
            } else {
                history.take(8).forEach {
                    Text("• ${it.text}", color = Pal.Text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                TextButton(onClick = vm::clearHistory) { Text(Fa.clearHistory, color = Pal.Error) }
            }

            HorizontalDivider(color = Pal.Line)
            Section(Fa.aboutSection)
            Text(Fa.aboutBody, color = Pal.Subdued, fontSize = 13.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(Fa.close, color = Pal.Cyan) }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(title, color = Pal.Cyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
}
