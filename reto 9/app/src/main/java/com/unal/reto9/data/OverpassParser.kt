package com.unal.reto9.data

import com.unal.reto9.model.GeoMath
import com.unal.reto9.model.PoiCategory
import com.unal.reto9.model.PointOfInterest
import org.json.JSONException
import org.json.JSONObject

object OverpassParser {

    /** Lanza [IllegalArgumentException] si el JSON es inválido. Descarta elementos sin nombre. */
    fun parse(json: String, originLat: Double, originLon: Double): List<PointOfInterest> {
        try {
            val elements = JSONObject(json).getJSONArray("elements")
            val result = mutableListOf<PointOfInterest>()
            for (i in 0 until elements.length()) {
                val el = elements.getJSONObject(i)
                val tagsJson = el.optJSONObject("tags") ?: continue
                val tags = tagsJson.keys().asSequence().associateWith { tagsJson.getString(it) }
                val name = tags["name"]?.trim().orEmpty()
                if (name.isEmpty()) continue
                val category = PoiCategory.fromTags(tags) ?: continue
                val point = el.optJSONObject("center") ?: el
                if (!point.has("lat") || !point.has("lon")) continue
                val lat = point.getDouble("lat")
                val lon = point.getDouble("lon")
                result += PointOfInterest(
                    id = el.optLong("id"),
                    name = name,
                    category = category,
                    lat = lat,
                    lon = lon,
                    distanceMeters = GeoMath.distanceMeters(originLat, originLon, lat, lon)
                )
            }
            return result.sortedBy { it.distanceMeters }
        } catch (e: JSONException) {
            throw IllegalArgumentException("Respuesta de Overpass no válida", e)
        }
    }
}
