package com.unal.reto10.data

import com.unal.reto10.model.CollectionPoint
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class BadJsonException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Convierte las respuestas JSON de SODA usando `org.json` del SDK de Android. */
object CollectionPointParser {

    fun parseList(json: String): List<CollectionPoint> {
        val array = asArray(json)
        val result = ArrayList<CollectionPoint>(array.length())
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
            val name = o.optString(Dataset.COL_NAME, "").trim()
            val address = o.optString(Dataset.COL_ADDRESS, "").trim()
            if (name.isEmpty() && address.isEmpty()) continue
            result += CollectionPoint(
                businessName = name.ifEmpty { address },
                address = address,
                municipality = o.optString(Dataset.COL_MUNICIPALITY, "").trim(),
                department = o.optString(Dataset.COL_DEPARTMENT, "").trim(),
                program = o.optString(Dataset.COL_PROGRAM, "").trim(),
                wasteTypes = o.optString(Dataset.COL_WASTE, "").trim(),
                daneCode = o.optString(Dataset.COL_DANE, "").trim().ifEmpty { null }
            )
        }
        return result
    }

    /** Respuesta de `$select=count(*)`: `[{"count":"120"}]`. */
    fun parseCount(json: String): Int {
        val array = asArray(json)
        val first = array.optJSONObject(0) ?: return 0
        return first.optString("count", "0").toIntOrNull()
            ?: throw BadJsonException("count no numérico")
    }

    /** Respuesta de `distinct municipio`. */
    fun parseMunicipalities(json: String): List<String> {
        val array = asArray(json)
        return (0 until array.length())
            .mapNotNull { array.optJSONObject(it)?.optString(Dataset.COL_MUNICIPALITY, "")?.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun asArray(json: String): JSONArray {
        val trimmed = json.trim()
        if (!trimmed.startsWith("[")) {
            // Socrata devuelve {"error":true,...} en fallos
            val msg = try {
                JSONObject(trimmed).optString("message", "respuesta no es un arreglo")
            } catch (e: JSONException) {
                "respuesta no es JSON"
            }
            throw BadJsonException(msg)
        }
        return try {
            JSONArray(trimmed)
        } catch (e: JSONException) {
            throw BadJsonException("JSON inválido", e)
        }
    }
}
