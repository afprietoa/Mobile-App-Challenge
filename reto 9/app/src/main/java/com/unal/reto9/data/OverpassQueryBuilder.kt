package com.unal.reto9.data

import com.unal.reto9.model.PoiCategory
import java.util.Locale

object OverpassQueryBuilder {
    const val MAX_RESULTS = 100

    fun build(lat: Double, lon: Double, radiusMeters: Int, categories: Set<PoiCategory>): String {
        require(categories.isNotEmpty()) { "Se necesita al menos una categoría" }
        val clauses = categories.joinToString("\n") {
            // Locale.US: el punto decimal no debe volverse coma en dispositivos en español.
            String.format(
                Locale.US, "  nwr[\"%s\"=\"%s\"](around:%d,%.6f,%.6f);",
                it.osmKey, it.osmValue, radiusMeters, lat, lon
            )
        }
        return "[out:json][timeout:25];\n(\n$clauses\n);\nout center tags $MAX_RESULTS;"
    }
}
