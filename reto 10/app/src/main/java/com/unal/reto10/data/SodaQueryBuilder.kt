package com.unal.reto10.data

import com.unal.reto10.model.SodaFilter
import java.net.URLEncoder

/** Construye URLs de la API SODA (Socrata). Lógica pura, sin Android. */
object SodaQueryBuilder {

    fun list(filter: SodaFilter, baseUrl: String = Dataset.BASE_URL): String = build(
        baseUrl,
        listOfNotNull(
            whereClause(filter)?.let { "\$where" to it },
            filter.normalizedText.takeIf { it.isNotEmpty() }?.let { "\$q" to it },
            "\$order" to Dataset.COL_NAME,
            "\$limit" to filter.pageSize.coerceIn(1, 100).toString(),
            "\$offset" to filter.offset.coerceAtLeast(0).toString()
        )
    )

    fun count(filter: SodaFilter, baseUrl: String = Dataset.BASE_URL): String = build(
        baseUrl,
        listOfNotNull(
            "\$select" to "count(*)",
            whereClause(filter)?.let { "\$where" to it },
            filter.normalizedText.takeIf { it.isNotEmpty() }?.let { "\$q" to it }
        )
    )

    fun municipalities(baseUrl: String = Dataset.BASE_URL): String = build(
        baseUrl,
        listOf(
            "\$select" to "distinct ${Dataset.COL_MUNICIPALITY}",
            "\$order" to Dataset.COL_MUNICIPALITY,
            "\$limit" to "200"
        )
    )

    private fun whereClause(filter: SodaFilter): String? =
        filter.normalizedMunicipality?.let { "${Dataset.COL_MUNICIPALITY}='${escape(it)}'" }

    /** En SoQL un apóstrofe dentro de una cadena se duplica. */
    internal fun escape(value: String): String = value.replace("'", "''")

    private fun build(baseUrl: String, params: List<Pair<String, String>>): String =
        baseUrl + "?" + params.joinToString("&") { (k, v) -> "${enc(k)}=${enc(v)}" }

    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8").replace("+", "%20")
}
