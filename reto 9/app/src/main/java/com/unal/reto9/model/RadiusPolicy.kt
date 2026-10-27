package com.unal.reto9.model

object RadiusPolicy {
    const val MIN_KM = 1
    const val MAX_KM = 20
    const val DEFAULT_KM = 3

    /** Una preferencia corrupta o fuera de rango nunca debe llegar a la consulta. */
    fun clampKm(km: Int): Int = km.coerceIn(MIN_KM, MAX_KM)
    fun toMeters(km: Int): Int = clampKm(km) * 1000
}
