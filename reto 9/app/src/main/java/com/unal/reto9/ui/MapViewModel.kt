package com.unal.reto9.ui

import androidx.lifecycle.ViewModel
import com.unal.reto9.model.PoiCategory
import com.unal.reto9.model.PointOfInterest
import org.osmdroid.util.GeoPoint

/** Parámetros de la última consulta; sirve para saber si las preferencias cambiaron. */
data class PoiQuery(val lat: Double, val lon: Double, val radiusKm: Int, val categories: Set<PoiCategory>)

/** Conserva origen y resultados durante la rotación para no repetir GPS ni red. */
class MapViewModel : ViewModel() {
    var origin: GeoPoint? = null
    var address: String? = null
    var lastQuery: PoiQuery? = null
    var lastResult: List<PointOfInterest> = emptyList()
}
