package com.unal.reto9.data

import com.unal.reto9.model.PoiCategory
import com.unal.reto9.model.PointOfInterest

interface PoiRepository {
    /** Lanza [PoiException]; nunca devuelve null. Debe llamarse fuera del hilo principal. */
    @Throws(PoiException::class)
    fun search(lat: Double, lon: Double, radiusMeters: Int, categories: Set<PoiCategory>): List<PointOfInterest>
}

class PoiException(val kind: Kind, cause: Throwable? = null) : Exception(kind.name, cause) {
    enum class Kind { NO_NETWORK, TIMEOUT, SERVER_BUSY, BAD_RESPONSE }
}
