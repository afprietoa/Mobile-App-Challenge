package com.unal.reto11.data

import android.content.Context
import com.unal.reto11.model.Message
import com.unal.reto11.model.Role
import org.json.JSONArray
import org.json.JSONObject

/** Persistencia mínima del historial y los ajustes. */
interface ChatStore {
    fun loadMessages(): List<Message>
    fun saveMessages(messages: List<Message>)
    var systemPrompt: String
    var temperature: Double

    /** Guarda los bytes de una imagen y devuelve su ruta, o null si no se pudo. */
    fun saveImage(id: Long, bytes: ByteArray): String? = null
}

class PrefsChatStore(context: Context) : ChatStore {
    private val prefs = context.applicationContext.getSharedPreferences("chat", Context.MODE_PRIVATE)

    override fun loadMessages(): List<Message> = ChatJson.decode(prefs.getString(KEY_MESSAGES, null))

    override fun saveMessages(messages: List<Message>) {
        prefs.edit().putString(KEY_MESSAGES, ChatJson.encode(messages)).apply()
    }

    private val imagesDir = java.io.File(context.applicationContext.filesDir, "images")

    override fun saveImage(id: Long, bytes: ByteArray): String? = try {
        imagesDir.mkdirs()
        java.io.File(imagesDir, "img_$id.jpg").also { it.writeBytes(bytes) }.absolutePath
    } catch (e: java.io.IOException) {
        null
    }

    override var systemPrompt: String
        get() = prefs.getString(KEY_SYSTEM, null) ?: GeminiConfig.DEFAULT_SYSTEM_PROMPT
        set(v) { prefs.edit().putString(KEY_SYSTEM, v).apply() }

    override var temperature: Double
        get() = prefs.getFloat(KEY_TEMP, GeminiConfig.DEFAULT_TEMPERATURE.toFloat()).toDouble()
        set(v) { prefs.edit().putFloat(KEY_TEMP, v.toFloat()).apply() }

    private companion object {
        const val KEY_MESSAGES = "messages"
        const val KEY_SYSTEM = "system_prompt"
        const val KEY_TEMP = "temperature"
    }
}

object ChatJson {
    fun encode(messages: List<Message>): String {
        val arr = JSONArray()
        messages.forEach {
            arr.put(
                JSONObject().put("id", it.id).put("role", it.role.name)
                    .put("text", it.text).put("error", it.isError)
                    .put("image", it.imagePath ?: JSONObject.NULL)
            )
        }
        return arr.toString()
    }

    fun decode(json: String?): List<Message> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                Message(o.getLong("id"), Role.valueOf(o.getString("role")), o.getString("text"), o.optBoolean("error"),
                    if (o.isNull("image")) null else o.optString("image"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
