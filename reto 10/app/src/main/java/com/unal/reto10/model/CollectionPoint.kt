package com.unal.reto10.model

import java.io.Serializable

/** Un punto de recolección de residuos posconsumo (fila del dataset `c2fr-ezse`). */
data class CollectionPoint(
    val businessName: String,   // raz_n_social
    val address: String,        // direccion
    val municipality: String,   // municipio
    val department: String,     // departamento
    val program: String,        // programa_de_recolecci_n
    val wasteTypes: String,     // tipo_de_residuos
    val daneCode: String?       // codigo_dane
) : Serializable
