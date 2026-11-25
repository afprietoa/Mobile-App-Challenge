package com.unal.reto11.data

import com.unal.reto11.model.ChatError
import com.unal.reto11.model.ChatException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

/** Genera una imagen (bytes JPEG/PNG) a partir de un prompt. Bloqueante. */
interface ImageGenerator {
    fun generate(prompt: String): Result<ByteArray>
    fun cancel() {}
}

/** Servicio gratuito y sin clave (Pollinations). Solo HTTPS. */
class PollinationsImageGenerator : ImageGenerator {

    @Volatile
    private var connection: HttpURLConnection? = null

    override fun generate(prompt: String): Result<ByteArray> {
        var conn: HttpURLConnection? = null
        return try {
            val q = URLEncoder.encode(prompt.take(MAX_PROMPT), "UTF-8").replace("+", "%20")
            conn = URL("$BASE_URL/$q?width=$SIZE&height=$SIZE&nologo=true").openConnection() as HttpURLConnection
            connection = conn
            conn.connectTimeout = 10_000
            conn.readTimeout = 90_000
            val code = conn.responseCode
            if (code >= 400) return Result.failure(ChatException(GeminiResponseParser.errorFor(code)))
            val type = conn.contentType.orEmpty()
            if (!type.startsWith("image/")) return Result.failure(ChatException(ChatError.BadJson))
            Result.success(conn.inputStream.use { it.readBytes() })
        } catch (e: UnknownHostException) {
            Result.failure(ChatException(ChatError.NoNetwork))
        } catch (e: ConnectException) {
            Result.failure(ChatException(ChatError.NoNetwork))
        } catch (e: SocketTimeoutException) {
            Result.failure(ChatException(ChatError.Timeout))
        } catch (e: Exception) {
            Result.failure(ChatException(ChatError.Server))
        } finally {
            conn?.disconnect()
            connection = null
        }
    }

    override fun cancel() {
        connection?.disconnect()
    }

    private companion object {
        const val BASE_URL = "https://image.pollinations.ai/prompt"
        const val SIZE = 768
        const val MAX_PROMPT = 400
    }
}
