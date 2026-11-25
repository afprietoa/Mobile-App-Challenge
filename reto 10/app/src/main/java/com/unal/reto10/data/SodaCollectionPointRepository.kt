package com.unal.reto10.data

import com.unal.reto10.model.CollectionPoint
import com.unal.reto10.model.ErrorKind
import com.unal.reto10.model.SodaFilter
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/** Implementación real: `HttpURLConnection` + `org.json`, sin librerías de red. */
class SodaCollectionPointRepository(
    private val isOnline: () -> Boolean = { true },
    private val baseUrl: String = Dataset.BASE_URL
) : CollectionPointRepository {

    override fun search(filter: SodaFilter): Result<List<CollectionPoint>> = call {
        CollectionPointParser.parseList(get(SodaQueryBuilder.list(filter, baseUrl)))
    }

    override fun count(filter: SodaFilter): Result<Int> = call {
        CollectionPointParser.parseCount(get(SodaQueryBuilder.count(filter, baseUrl)))
    }

    override fun municipalities(): Result<List<String>> = call {
        CollectionPointParser.parseMunicipalities(get(SodaQueryBuilder.municipalities(baseUrl)))
    }

    private fun <T> call(block: () -> T): Result<T> = try {
        if (!isOnline()) throw ServiceException(ErrorKind.NO_NETWORK)
        Result.success(block())
    } catch (e: ServiceException) {
        Result.failure(e)
    } catch (e: BadJsonException) {
        Result.failure(ServiceException(ErrorKind.BAD_JSON, e.message, e))
    } catch (e: SocketTimeoutException) {
        Result.failure(ServiceException(ErrorKind.TIMEOUT, cause = e))
    } catch (e: UnknownHostException) {
        Result.failure(ServiceException(ErrorKind.NO_NETWORK, cause = e))
    } catch (e: IOException) {
        // ConnectException, SSL, reset...: sin conexión utilizable
        Result.failure(ServiceException(ErrorKind.NO_NETWORK, cause = e))
    }

    private fun get(url: String): String {
        require(url.startsWith("https://")) { "Solo se permite https" }
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "Reto10/1.0")
            val code = conn.responseCode
            if (code >= 400) {
                val body = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw ServiceException(ErrorKind.SERVER, "HTTP $code ${body.take(200)}")
            }
            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000
    }
}
