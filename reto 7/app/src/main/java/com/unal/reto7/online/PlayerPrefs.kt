package com.unal.reto7.online

import android.content.Context
import androidx.core.content.edit

/** Nombre que ven los demás en el lobby; se recuerda entre ejecuciones. */
class PlayerPrefs(context: Context) {
    // Mismo archivo que marcadores y dificultad de MainActivity (ttt_prefs).
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var name: String
        get() = prefs.getString(KEY_NAME, "").orEmpty()
        set(value) = prefs.edit { putString(KEY_NAME, value.trim().take(MAX_NAME)) }

    private companion object {
        const val PREFS_NAME = "ttt_prefs"
        const val KEY_NAME = "player_name"
        const val MAX_NAME = 16
    }
}
