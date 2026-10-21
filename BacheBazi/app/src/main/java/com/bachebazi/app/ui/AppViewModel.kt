package com.bachebazi.app.ui

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bachebazi.app.data.BgMode
import com.bachebazi.app.data.BgPrefs
import com.bachebazi.app.data.Entry
import com.bachebazi.app.data.HistoryItem
import com.bachebazi.app.data.Repository
import com.bachebazi.app.data.RollState
import com.bachebazi.app.logic.Randomizer
import com.bachebazi.app.logic.isReducedMotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    /** Draft text lives here so leaving the screen never loses unsaved input. */
    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    private val _draftError = MutableStateFlow<String?>(null)
    val draftError: StateFlow<String?> = _draftError.asStateFlow()

    private val _roll = MutableStateFlow<RollState>(RollState.Idle)
    val roll: StateFlow<RollState> = _roll.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _bgPrefs = MutableStateFlow(BgPrefs())
    val bgPrefs: StateFlow<BgPrefs> = _bgPrefs.asStateFlow()

    private val _background = MutableStateFlow<ImageBitmap?>(null)
    val background: StateFlow<ImageBitmap?> = _background.asStateFlow()

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            _entries.value = repo.loadEntries()
            _history.value = repo.loadHistory()
            val p = repo.loadBg()
            _bgPrefs.value = p
            if (p.version != 0L) loadBackground()
        }
    }

    fun say(text: String) { messageChannel.trySend(text) }

    // ---------------- Entries ----------------

    fun onDraftChange(text: String) {
        _draft.value = text
        if (_draftError.value != null) _draftError.value = null
    }

    fun register() {
        val lines = _draft.value.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            _draftError.value = Fa.emptyError
            return
        }
        val current = _entries.value
        val room = MAX_ENTRIES - current.size
        if (room <= 0) { say(Fa.tooMany); return }
        var id = current.maxOfOrNull { it.id } ?: 0L
        var truncated = false
        val added = lines.take(room).map { raw ->
            val t = if (raw.length > MAX_LEN) { truncated = true; raw.take(MAX_LEN) } else raw
            Entry(++id, t)
        }
        _entries.value = current + added
        _draft.value = ""
        _draftError.value = null
        persistEntries()
        say(
            when {
                lines.size > room -> Fa.tooMany
                truncated -> Fa.truncated
                else -> Fa.registered(added.size)
            }
        )
    }

    fun removeEntry(id: Long) {
        _entries.value = _entries.value.filterNot { it.id == id }
        persistEntries()
    }

    fun clearEntries() {
        _entries.value = emptyList()
        persistEntries()
        say(Fa.listCleared)
    }

    private fun persistEntries() {
        val snap = _entries.value
        viewModelScope.launch { repo.saveEntries(snap) }
    }

    // ---------------- Random selection ----------------

    /**
     * The winner is chosen immediately and uniformly by [Randomizer]; the roll animation only
     * runs afterwards and never influences the outcome. Ignored while a roll is in progress.
     */
    fun roll() {
        if (_roll.value is RollState.Rolling) return
        val list = _entries.value
        val winner = Randomizer.pickOne(list)
        if (winner == null) { say(Fa.noEntries); return }
        _roll.value = RollState.Rolling(winner)
        viewModelScope.launch {
            delay(if (isReducedMotion(app)) 200L else ROLL_MS)
            _roll.value = RollState.Revealed(winner)
            val h = (listOf(HistoryItem(winner.text, System.currentTimeMillis())) + _history.value).take(MAX_HISTORY)
            _history.value = h
            repo.saveHistory(h)
        }
    }

    fun closeResult() {
        if (_roll.value is RollState.Revealed) _roll.value = RollState.Idle
    }

    fun clearHistory() {
        _history.value = emptyList()
        viewModelScope.launch { repo.saveHistory(emptyList()) }
        say(Fa.historyCleared)
    }

    // ---------------- Background ----------------

    fun onImagePicked(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { copyImage(uri) }
            if (ok) {
                updateBg(_bgPrefs.value.copy(version = System.currentTimeMillis()))
                loadBackground()
                say(Fa.bgSet)
            } else say(Fa.bgError)
        }
    }

    fun resetBackground() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { File(app.filesDir, BG_FILE).delete() }
            _background.value = null
            updateBg(_bgPrefs.value.copy(version = 0L))
            say(Fa.bgReset)
        }
    }

    fun setBgMode(mode: BgMode) = updateBg(_bgPrefs.value.copy(mode = mode))

    /** Live update while dragging the slider (not persisted). */
    fun previewOverlay(v: Float) { _bgPrefs.value = _bgPrefs.value.copy(overlay = v) }

    fun commitOverlay() = updateBg(_bgPrefs.value)

    private fun updateBg(p: BgPrefs) {
        _bgPrefs.value = p
        viewModelScope.launch { repo.saveBg(p) }
    }

    private suspend fun loadBackground() {
        _background.value = withContext(Dispatchers.IO) { decodeBackground() }
    }

    private fun copyImage(uri: Uri): Boolean {
        return try {
            val tmp = File(app.filesDir, "background.tmp")
            val input = app.contentResolver.openInputStream(uri) ?: return false
            input.use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(tmp.path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) { tmp.delete(); return false }
            val dest = File(app.filesDir, BG_FILE)
            if (dest.exists()) dest.delete()
            tmp.renameTo(dest)
        } catch (e: Exception) { false }
    }

    private fun decodeBackground(): ImageBitmap? {
        return try {
            val f = File(app.filesDir, BG_FILE)
            if (!f.exists()) return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(f.path, bounds)
            if (bounds.outWidth <= 0) return null
            var sample = 1
            while (bounds.outWidth / sample > 2048 || bounds.outHeight / sample > 2048) sample *= 2
            var bmp = BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inSampleSize = sample })
                ?: return null
            val deg = try {
                when (ExifInterface(f.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } catch (e: Exception) { 0f }
            if (deg != 0f) {
                val m = Matrix().apply { postRotate(deg) }
                bmp = android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
            }
            bmp.asImageBitmap()
        } catch (e: Throwable) { null }
    }

    companion object {
        const val MAX_ENTRIES = 1000
        const val MAX_LEN = 200
        const val MAX_HISTORY = 30
        const val ROLL_MS = 1300L
        const val BG_FILE = "background.img"
    }
}
