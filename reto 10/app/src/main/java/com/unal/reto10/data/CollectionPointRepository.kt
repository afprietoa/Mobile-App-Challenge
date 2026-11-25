package com.unal.reto10.data

import com.unal.reto10.model.CollectionPoint
import com.unal.reto10.model.ErrorKind
import com.unal.reto10.model.SodaFilter

/** Error de servicio con su categoría, para mostrar un mensaje distinto a cada tipo. */
class ServiceException(val kind: ErrorKind, message: String? = null, cause: Throwable? = null) :
    Exception(message ?: kind.name, cause)

/** Contrato entre la UI y la red. Métodos bloqueantes: llamar fuera del hilo principal. */
interface CollectionPointRepository {
    fun search(filter: SodaFilter): Result<List<CollectionPoint>>
    fun count(filter: SodaFilter): Result<Int>
    fun municipalities(): Result<List<String>>
}

/** Punto de inyección simple; los tests instrumentados reemplazan [repository]. */
object ServiceLocator {
    @Volatile var repository: CollectionPointRepository? = null
    var isOnline: () -> Boolean = { true }

    fun provide(): CollectionPointRepository =
        repository ?: SodaCollectionPointRepository(isOnline = { isOnline() }).also { repository = it }
}
