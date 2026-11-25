package com.unal.reto10.model

/** Parámetros que el usuario envía al servicio. `municipality == null` significa "todos". */
data class SodaFilter(
    val municipality: String? = null,
    val text: String = "",
    val pageSize: Int = 20,
    val offset: Int = 0
) {
    val normalizedText: String get() = text.trim()
    val normalizedMunicipality: String? get() = municipality?.trim()?.takeIf { it.isNotEmpty() }
}
