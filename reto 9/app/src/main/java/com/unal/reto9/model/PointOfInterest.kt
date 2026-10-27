package com.unal.reto9.model

data class PointOfInterest(
    val id: Long,
    val name: String,
    val category: PoiCategory,
    val lat: Double,
    val lon: Double,
    val distanceMeters: Double      // desde el origen de la búsqueda
)
