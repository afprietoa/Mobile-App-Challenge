package com.unal.reto9.data

import com.unal.reto9.model.PoiCategory
import com.unal.reto9.model.PointOfInterest
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

class OverpassPoiRepository(private val endpoint: String = ENDPOINT) : PoiRepository {

    override fun search(
        lat: Double, lon: Double, radiusMeters: Int, categories: Set<PoiCategory>
    ): List<PointOfInterest> {
        val query = OverpassQueryBuilder.build(lat, lon, radiusMeters, categories)
        val conn = try {
            (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 30_000
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
        const val ENDPOINT = "https://overpass-api.de/api/interpreter"
        const val USER_AGENT = "com.unal.reto9 (curso Desarrollo Movil UNAL)"
    }
}
