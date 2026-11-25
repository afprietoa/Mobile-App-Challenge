package com.unal.reto10.data

import com.unal.reto10.model.CollectionPoint
import com.unal.reto10.model.ErrorKind
import com.unal.reto10.model.SodaFilter

/**
 * Repositorio en memoria para tests (unitarios e instrumentados): filtra y pagina
 * una lista fija y permite programar un error.
 */
class FakeCollectionPointRepository(
    var points: List<CollectionPoint> = emptyList(),
    var error: ErrorKind? = null,
    var municipalityList: List<String>? = null
) : CollectionPointRepository {
    var searchCalls = 0
        private set

    private fun matching(f: SodaFilter) = points.filter { p ->
        (f.normalizedMunicipality == null || p.municipality == f.normalizedMunicipality) &&
            (f.normalizedText.isEmpty() || "${p.businessName} ${p.address} ${p.wasteTypes}"
                .contains(f.normalizedText, ignoreCase = true))
    }

    private fun <T> guarded(block: () -> T): Result<T> =
        error?.let { Result.failure(ServiceException(it)) } ?: Result.success(block())

    override fun search(filter: SodaFilter): Result<List<CollectionPoint>> {
        searchCalls++
        return guarded { matching(filter).drop(filter.offset).take(filter.pageSize) }
    }

    override fun count(filter: SodaFilter): Result<Int> = guarded { matching(filter).size }

    override fun municipalities(): Result<List<String>> =
        guarded { municipalityList ?: points.map { it.municipality }.distinct().sorted() }

    companion object {
        fun sample(n: Int, municipality: String = "Ocaña") = (1..n).map {
            CollectionPoint(
                businessName = "Punto %02d".format(it), address = "Calle $it",
                municipality = municipality, department = "Norte de Santander",
                program = "Pilas con el Ambiente", wasteTypes = "Pilas y baterías", daneCode = "54498"
            )
        }
    }
}
