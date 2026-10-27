package com.unal.reto9.model

import androidx.annotation.StringRes
import com.unal.reto9.R

/** Categorías de interés; [osmKey]/[osmValue] es el tag de OpenStreetMap que las identifica. */
enum class PoiCategory(val osmKey: String, val osmValue: String, @StringRes val label: Int) {
    HOSPITAL("amenity", "hospital", R.string.cat_hospital),
    PHARMACY("amenity", "pharmacy", R.string.cat_pharmacy),
    ATTRACTION("tourism", "attraction", R.string.cat_attraction),
    MUSEUM("tourism", "museum", R.string.cat_museum),
    RESTAURANT("amenity", "restaurant", R.string.cat_restaurant),
    PARK("leisure", "park", R.string.cat_park);

    companion object {
        fun fromTags(tags: Map<String, String>): PoiCategory? =
            entries.firstOrNull { tags[it.osmKey] == it.osmValue }
    }
}
