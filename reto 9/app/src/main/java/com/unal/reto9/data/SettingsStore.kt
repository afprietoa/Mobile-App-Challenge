package com.unal.reto9.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.unal.reto9.model.PoiCategory
import com.unal.reto9.model.RadiusPolicy

class SettingsStore(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val radiusKm: Int
        get() = RadiusPolicy.clampKm(
            runCatching { prefs.getInt(KEY_RADIUS, RadiusPolicy.DEFAULT_KM) }.getOrDefault(RadiusPolicy.DEFAULT_KM)
        )

    val categories: Set<PoiCategory>
        get() = (prefs.getStringSet(KEY_CATEGORIES, null) ?: DEFAULTS.map { it.name }.toSet())
            .mapNotNull { runCatching { PoiCategory.valueOf(it) }.getOrNull() }
            .toSet()

    companion object {
        const val KEY_RADIUS = "radius_km"
        const val KEY_CATEGORIES = "poi_categories"
        private val DEFAULTS = setOf(PoiCategory.HOSPITAL, PoiCategory.ATTRACTION)
    }
}
