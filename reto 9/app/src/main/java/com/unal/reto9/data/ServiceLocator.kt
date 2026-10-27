package com.unal.reto9.data

import android.content.Context
import com.unal.reto9.location.LocationProvider
import com.unal.reto9.location.LocationSource

/** Puntos de sustitución para los tests instrumentados (repositorio y GPS falsos). */
object ServiceLocator {
    var poiRepository: PoiRepository = OverpassPoiRepository()
    var locationSourceFactory: (Context) -> LocationSource = { LocationProvider(it) }

    fun reset() {
        poiRepository = OverpassPoiRepository()
        locationSourceFactory = { LocationProvider(it) }
    }
}
