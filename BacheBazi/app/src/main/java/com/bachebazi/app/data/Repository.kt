package com.bachebazi.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "bachebazi")

/** All data is stored locally with Jetpack DataStore. No network, no accounts. */
class Repository(private val context: Context) {
    private object K {
        val entries = stringPreferencesKey("entries")
        val history = stringPreferencesKey("history")
        val mode = stringPreferencesKey("bg_mode")
        val overlay = floatPreferencesKey("bg_overlay")
        val version = longPreferencesKey("bg_version")
    }

    private suspend fun prefs(): Preferences = context.dataStore.data.first()

    suspend fun loadEntries(): List<Entry> = try {
        val s = prefs()[K.entries]
        if (s.isNullOrEmpty()) emptyList() else {
            val a = JSONArray(s)
            List(a.length()) { i -> a.getJSONObject(i).let { Entry(it.getLong("i"), it.getString("t")) } }
        }
    } catch (e: Exception) { emptyList() }

    suspend fun saveEntries(list: List<Entry>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("i", it.id).put("t", it.text)) }
        context.dataStore.edit { it[K.entries] = arr.toString() }
    }

    suspend fun loadHistory(): List<HistoryItem> = try {
        val s = prefs()[K.history]
        if (s.isNullOrEmpty()) emptyList() else {
            val a = JSONArray(s)
            List(a.length()) { i -> a.getJSONObject(i).let { HistoryItem(it.getString("t"), it.getLong("m")) } }
        }
    } catch (e: Exception) { emptyList() }

    suspend fun saveHistory(list: List<HistoryItem>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("t", it.text).put("m", it.time)) }
        context.dataStore.edit { it[K.history] = arr.toString() }
    }

    suspend fun loadBg(): BgPrefs {
        val p = prefs()
        return BgPrefs(
            mode = runCatching { BgMode.valueOf(p[K.mode] ?: "Crop") }.getOrDefault(BgMode.Crop),
            overlay = (p[K.overlay] ?: 0.45f).coerceIn(0f, 0.9f),
            version = p[K.version] ?: 0L,
        )
    }

    suspend fun saveBg(b: BgPrefs) {
        context.dataStore.edit {
            it[K.mode] = b.mode.name
            it[K.overlay] = b.overlay
            it[K.version] = b.version
        }
    }
}
