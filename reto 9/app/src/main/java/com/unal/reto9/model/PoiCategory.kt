package com.unal.reto9.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.unal.reto9.R

/** Categorías de interés; [osmKey]/[osmValue] es el tag de OpenStreetMap que las identifica. */
enum class PoiCategory(
    val osmKey: String,
    val osmValue: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
) {
    HOSPITAL("amenity", "hospital", R.string.cat_hospital, R.drawable.ic_poi_hospital),
    PHARMACY("amenity", "pharmacy", R.string.cat_pharmacy, R.drawable.ic_poi_pharmacy),
    ATTRACTION("tourism", "attraction", R.string.cat_attraction, R.drawable.ic_poi_attraction),
    MUSEUM("tourism", "museum", R.string.cat_museum, R.drawable.ic_poi_museum),
    RESTAURANT("amenity", "restaurant", R.string.cat_restaurant, R.drawable.ic_poi_restaurant),
    PARK("leisure", "park", R.string.cat_park, R.drawable.ic_poi_park);

    companion object {
        fun fromTags(tags: Map<String, String>): PoiCategory? =
            entries.firstOrNull { tags[it.osmKey] == it.osmValue }
    }
}
