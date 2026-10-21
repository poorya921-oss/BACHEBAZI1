package com.bachebazi.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color

private val placeholderIcons: List<ImageVector> = listOf(
    Icons.Filled.Star, Icons.Filled.Favorite, Icons.Filled.Search, Icons.Filled.Person,
    Icons.Filled.Notifications, Icons.Filled.Email, Icons.Filled.Info, Icons.Filled.Home,
)

@Composable
fun MenuScreen(vm: AppViewModel, onOpenRandom: () -> Unit) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        vm.onImagePicked(uri)
    }
    val launchPicker = {
        try {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: Exception) {
            vm.say(Fa.pickerError)
        }
    }

    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        IconButton(
            onClick = { showSettings = true },
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
        ) {
            Icon(Icons.Filled.Settings, contentDescription = Fa.settings, tint = Pal.Text)
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 14 },
            modifier = Modifier.align(Alignment.Center),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(Fa.appName, color = Pal.Text, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text(Fa.menuSubtitle, color = Pal.Subdued, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Column(Modifier.widthIn(max = 460.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (row in 0 until 3) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (col in 0 until 3) {
                                val i = row * 3 + col
                                val m = Modifier.weight(1f).aspectRatio(1f)
                                if (i == 0) {
                                    MenuTile(
                                        title = Fa.randomTitle, status = Fa.active, enabled = true,
                                        modifier = m, onClick = onOpenRandom,
                                    ) { DiceIcon(face = 5, iconSize = 40.dp, description = Fa.diceDesc) }
                                } else {
                                    MenuTile(
                                        title = Fa.soon, status = Fa.comingSoon, enabled = false, modifier = m,
                                    ) {
                                        Icon(
                                            placeholderIcons[i - 1], contentDescription = null,
                                            tint = Pal.Subdued, modifier = Modifier.size(34.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(vm = vm, onPickImage = { launchPicker() }, onDismiss = { showSettings = false })
    }
}

@Composable
private fun MenuTile(
    title: String,
    status: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.93f else 1f, tween(120), label = "tile")
    val shape = RoundedCornerShape(22.dp)
    val fill = if (enabled) Brush.linearGradient(listOf(Pal.Surface2, Pal.Surface))
    else Brush.linearGradient(listOf(Pal.Surface.copy(alpha = 0.55f), Pal.Navy.copy(alpha = 0.45f)))

    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (enabled) 10.dp else 0.dp, shape, ambientColor = Pal.Blue, spotColor = Pal.Blue)
            .clip(shape)
            .background(fill)
            .border(BorderStroke(1.dp, if (enabled) Pal.Cyan.copy(alpha = 0.55f) else Pal.Line.copy(alpha = 0.3f)), shape)
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = src, indication = LocalIndication.current,
                    role = Role.Button, onClick = onClick,
                ) else Modifier
            )
            .semantics { contentDescription = "$title، $status"; if (!enabled) disabled() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            icon()
            Spacer(Modifier.height(8.dp))
            Text(
                title, color = if (enabled) Pal.Text else Pal.Subdued, fontSize = 12.sp, lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            Text(
                status, color = if (enabled) Pal.Cyan else Pal.Subdued.copy(alpha = 0.8f), fontSize = 10.sp,
                textAlign = TextAlign.Center, maxLines = 1,
            )
        }
    }
}
