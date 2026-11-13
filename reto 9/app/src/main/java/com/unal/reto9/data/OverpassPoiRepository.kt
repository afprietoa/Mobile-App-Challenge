package com.unal.reto9.data

import android.util.Log
import com.unal.reto9.model.PoiCategory
import com.unal.reto9.model.PointOfInterest
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

class OverpassPoiRepository(private val endpoints: List<String> = ENDPOINTS) : PoiRepository {

    constructor(endpoint: String) : this(listOf(endpoint))

    /**
     * Consulta todos los servidores a la vez y devuelve la primera respuesta válida: Overpass es
     * público y alguno suele estar saturado o inalcanzable desde la red del dispositivo.
     */
    override fun search(
        lat: Double, lon: Double, radiusMeters: Int, categories: Set<PoiCategory>
    ): List<PointOfInterest> {
        val pool = Executors.newFixedThreadPool(endpoints.size) { r -> Thread(r).apply { isDaemon = true } }
        try {
            val done = ExecutorCompletionService<List<PointOfInterest>>(pool)
            endpoints.forEach { endpoint ->
                done.submit { searchAt(endpoint, lat, lon, radiusMeters, categories) }
            }
            val errors = mutableListOf<PoiException>()
            repeat(endpoints.size) {
                try {
                    return done.take().get()
                } catch (e: ExecutionException) {
                    val error = e.cause as? PoiException ?: PoiException(PoiException.Kind.BAD_RESPONSE, e.cause)
                    Log.w(TAG, "Overpass falló: ${error.kind}", error)
                    errors += error
                }
            }
            throw errors.firstOrNull { it.kind != PoiException.Kind.NO_NETWORK }
                ?: errors.firstOrNull() ?: PoiException(PoiException.Kind.BAD_RESPONSE)
        } finally {
            pool.shutdownNow()
        }
    }

    private fun searchAt(
        endpoint: String, lat: Double, lon: Double, radiusMeters: Int, categories: Set<PoiCategory>
    ): List<PointOfInterest> {
        val query = OverpassQueryBuilder.build(lat, lon, radiusMeters, categories)
        val conn = try {
            (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }
        } catch (e: IOException) {
            throw PoiException(PoiException.Kind.BAD_RESPONSE, e)
        }
        try {
            conn.outputStream.use { it.write("data=${URLEncoder.encode(query, "UTF-8")}".toByteArray()) }
            return when (conn.responseCode) {
                200 -> OverpassParser.parse(conn.inputStream.bufferedReader().readText(), lat, lon)
                429, 504 -> throw PoiException(PoiException.Kind.SERVER_BUSY)
                else -> throw PoiException(PoiException.Kind.BAD_RESPONSE)
            }
        } catch (e: UnknownHostException) {
            throw PoiException(PoiException.Kind.NO_NETWORK, e)
        } catch (e: SocketTimeoutException) {
            throw PoiException(PoiException.Kind.TIMEOUT, e)
        } catch (e: IllegalArgumentException) {
            throw PoiException(PoiException.Kind.BAD_RESPONSE, e)
        } catch (e: IOException) {
            throw PoiException(PoiException.Kind.NO_NETWORK, e)
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        private const val TAG = "OverpassPoiRepository"
        const val ENDPOINT = "https://overpass-api.de/api/interpreter"
        val ENDPOINTS = listOf(
            ENDPOINT,
            "https://overpass.private.coffee/api/interpreter",
            "https://overpass.openstreetmap.fr/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter"
        )
        const val USER_AGENT = "com.unal.reto9 (curso Desarrollo Movil UNAL)"
    }
}
