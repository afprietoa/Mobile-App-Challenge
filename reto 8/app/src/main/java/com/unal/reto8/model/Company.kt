package com.unal.reto8.model

data class Company(
    val id: Long = 0,                 // 0 = aún no guardada
    val name: String,
    val website: String = "",
    val phone: String = "",
    val email: String = "",
    val products: String = "",
    val classification: Int = 0       // máscara de Classification
) {
    val classifications: Set<Classification> get() = Classification.fromMask(classification)
}
