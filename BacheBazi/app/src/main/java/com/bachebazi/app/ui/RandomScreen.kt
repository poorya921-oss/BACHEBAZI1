package com.bachebazi.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import com.bachebazi.app.data.Entry
import com.bachebazi.app.data.RollState
import com.bachebazi.app.logic.fa
import com.bachebazi.app.logic.isReducedMotion
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RandomScreen(vm: AppViewModel, onBack: () -> Unit) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val draft by vm.draft.collectAsStateWithLifecycle()
    val error by vm.draftError.collectAsStateWithLifecycle()
    val roll by vm.roll.collectAsStateWithLifecycle()
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp)
    ) {
        // Top bar: back arrow physically on the LEFT, title centered.
        Box(Modifier.fillMaxWidth().height(56.dp)) {
            Text(
                Fa.randomTitle, color = Pal.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Fa.back, tint = Pal.Text)
                }
            }
        }
        Text(Fa.instruction, color = Pal.Subdued, fontSize = 13.sp, lineHeight = 20.sp)
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = draft,
            onValueChange = vm::onDraftChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            placeholder = { Text(Fa.inputHint, color = Pal.Subdued) },
            isError = error != null,
            supportingText = { error?.let { Text(it, color = Pal.Error) } },
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Pal.Text, unfocusedTextColor = Pal.Text,
                focusedBorderColor = Pal.Cyan, unfocusedBorderColor = Pal.Line,
                errorBorderColor = Pal.Error, cursorColor = Pal.Cyan,
                focusedContainerColor = Pal.Surface.copy(alpha = 0.7f),
                unfocusedContainerColor = Pal.Surface.copy(alpha = 0.55f),
                errorContainerColor = Pal.Surface.copy(alpha = 0.7f),
            ),
        )

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                Fa.counter(entries.size), color = Pal.Text, fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Pal.Surface2.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { confirmClear = true }, enabled = entries.isNotEmpty()) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(Fa.clearAll, fontSize = 13.sp)
            }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            if (entries.isEmpty()) {
                Text(
                    Fa.emptyList, color = Pal.Subdued, fontSize = 14.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    itemsIndexed(entries, key = { _, it -> it.id }) { i, e ->
                        EntryRow(
                            entry = e, index = i + 1, onRemove = { vm.removeEntry(e.id) },
                            modifier = Modifier.animateItemPlacement(tween(200)),
                        )
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { vm.register() },
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pal.Blue, contentColor = Color.White),
            ) { Text(Fa.register, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Button(
                onClick = { focus.clearFocus(); vm.roll() },
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pal.Cyan, contentColor = Pal.Navy),
            ) {
                DiceIcon(face = 5, iconSize = 22.dp, body = Pal.Navy, pip = Pal.Cyan)
                Spacer(Modifier.size(8.dp))
                Text(Fa.pick, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    ResultDialog(state = roll, entries = entries, onReroll = vm::roll, onClose = vm::closeResult)

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = Pal.Surface,
            titleContentColor = Pal.Text,
            textContentColor = Pal.Subdued,
            title = { Text(Fa.confirmClearTitle) },
            text = { Text(Fa.confirmClearBody) },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; vm.clearEntries() }) {
                    Text(Fa.confirm, color = Pal.Error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(Fa.cancel) } },
        )
    }
}

@Composable
private fun EntryRow(entry: Entry, index: Int, onRemove: () -> Unit, modifier: Modifier) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(tween(220)) + expandVertically(tween(220)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Pal.Surface2.copy(alpha = 0.85f))
                .border(1.dp, Pal.Line, RoundedCornerShape(14.dp))
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(26.dp).clip(CircleShape).background(Pal.Blue.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) { Text(index.fa(), color = Color.White, fontSize = 11.sp) }
            Spacer(Modifier.size(10.dp))
            Text(
                entry.text, color = Pal.Text, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = Fa.remove, tint = Pal.Subdued)
            }
        }
    }
}

@Composable
private fun ResultDialog(state: RollState, entries: List<Entry>, onReroll: () -> Unit, onClose: () -> Unit) {
    if (state is RollState.Idle) return
    val revealed = state is RollState.Revealed
    Dialog(
        onDismissRequest = { if (revealed) onClose() },
        properties = DialogProperties(
            dismissOnBackPress = revealed, dismissOnClickOutside = revealed, usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Pal.Surface2, Pal.Surface)))
                .border(1.dp, Pal.Cyan.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is RollState.Rolling -> RollingContent(entries)
                is RollState.Revealed -> RevealedContent(state.winner, onReroll, onClose)
                RollState.Idle -> {}
            }
        }
    }
}

/** Purely decorative: cycles faces/names. The real winner was chosen before this starts. */
@Composable
private fun RollingContent(entries: List<Entry>) {
    val ctx = LocalContext.current
    val noMotion = remember { isReducedMotion(ctx) }
    var face by remember { mutableIntStateOf(1) }
    var shown by remember { mutableStateOf(entries.firstOrNull()?.text ?: "") }
    val rotation by rememberInfiniteTransition(label = "roll").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Restart), label = "rot",
    )
    LaunchedEffect(Unit) {
        val cosmetic = Random.Default
        while (true) {
            face = cosmetic.nextInt(1, 7)
            if (entries.isNotEmpty()) shown = entries[cosmetic.nextInt(entries.size)].text
            delay(90)
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DiceIcon(face = face, iconSize = 84.dp, modifier = Modifier.rotate(if (noMotion) 0f else rotation),
            pip = Pal.Blue, description = Fa.diceDesc)
        Spacer(Modifier.height(18.dp))
        Text(Fa.rolling, color = Pal.Subdued, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            shown, color = Pal.Text.copy(alpha = 0.7f), fontSize = 20.sp, maxLines = 1,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RevealedContent(winner: Entry, onReroll: () -> Unit, onClose: () -> Unit) {
    val s = remember { Animatable(0.6f) }
    val a = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        a.animateTo(1f, tween(220))
    }
    LaunchedEffect(Unit) { s.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(s.value).graphicsAlpha(a.value),
    ) {
        Text(Fa.winner, color = Pal.Cyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Text(
            winner.text, color = Pal.Text, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center, maxLines = 5, overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onReroll,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Pal.Cyan, contentColor = Pal.Navy),
        ) { Text(Fa.reroll, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onClose) { Text(Fa.close, color = Pal.Subdued) }
    }
}

private fun Modifier.graphicsAlpha(alpha: Float): Modifier =
    this.then(Modifier.graphicsLayer { this.alpha = alpha })
