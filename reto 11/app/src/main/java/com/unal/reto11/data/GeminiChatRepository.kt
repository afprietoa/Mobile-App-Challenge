package com.unal.reto11.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.unal.reto11.model.ChatError
import com.unal.reto11.model.ChatException
import com.unal.reto11.model.ChatReply
import com.unal.reto11.model.Message
import com.unal.reto11.model.Role
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

class GeminiChatRepository(
    context: Context,
    private val apiKey: String = GeminiConfig.apiKey,
    private val model: String = GeminiConfig.MODEL,
    private val images: ImageGenerator = PollinationsImageGenerator()
) : ChatRepository {

    private val appContext = context.applicationContext

    @Volatile
    private var connection: HttpURLConnection? = null

    override fun send(history: List<Message>, systemPrompt: String, temperature: Double): Result<ChatReply> {
        val imagePrompt = history.lastOrNull()?.takeIf { it.role == Role.USER }
            ?.let { ImageIntent.promptFor(it.text) }
        if (imagePrompt != null) return sendImage(imagePrompt)
        if (apiKey.isBlank()) return fail(ChatError.MissingKey)
        if (!hasNetwork()) return fail(ChatError.NoNetwork)
        val body = GeminiRequestBuilder
            .build(ChatHistoryTrimmer.trim(history), systemPrompt, temperature)
            .toString()
        var conn: HttpURLConnection? = null
        return try {
            conn = URL("${GeminiConfig.BASE_URL}/$model:generateContent").openConnection() as HttpURLConnection
            connection = conn
            conn.requestMethod = "POST"
            conn.connectTimeout = GeminiConfig.CONNECT_TIMEOUT_MS
            conn.readTimeout = GeminiConfig.READ_TIMEOUT_MS
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("x-goog-api-key", apiKey)
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            if (code >= 400) {
                fail(GeminiResponseParser.errorFor(code))
            } else {
                val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                Result.success(GeminiResponseParser.parse(text))
            }
        } catch (e: ChatException) {
            fail(e.error)
        } catch (e: UnknownHostException) {
            fail(ChatError.NoNetwork)
        } catch (e: ConnectException) {
            fail(ChatError.NoNetwork)
        } catch (e: SocketTimeoutException) {
            fail(ChatError.Timeout)
        } catch (e: Exception) {
            // Cancelación (disconnect) o fallo de E/S genérico.
            fail(ChatError.Server)
        } finally {
            conn?.disconnect()
            connection = null
        }
    }

    private fun sendImage(prompt: String): Result<ChatReply> {
        if (!hasNetwork()) return fail(ChatError.NoNetwork)
        return images.generate(prompt).map { ChatReply("Imagen generada: $prompt", image = it) }
    }

    override fun cancel() {
        connection?.disconnect()
        images.cancel()
    }

    private fun fail(error: ChatError): Result<ChatReply> = Result.failure(ChatException(error))

    private fun hasNetwork(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
