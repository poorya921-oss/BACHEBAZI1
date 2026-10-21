package com.bachebazi.app.data

data class Entry(val id: Long, val text: String)

data class HistoryItem(val text: String, val time: Long)

enum class BgMode { Crop, Fit }

/** version == 0L means "default background"; otherwise a user image exists in app-private storage. */
data class BgPrefs(
    val mode: BgMode = BgMode.Crop,
    val overlay: Float = 0.45f,
    val version: Long = 0L,
)

sealed interface RollState {
    data object Idle : RollState
    data class Rolling(val winner: Entry) : RollState
    data class Revealed(val winner: Entry) : RollState
}
