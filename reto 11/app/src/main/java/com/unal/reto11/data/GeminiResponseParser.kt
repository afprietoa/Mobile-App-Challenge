package com.unal.reto11.data

import com.unal.reto11.model.ChatError
import com.unal.reto11.model.ChatException
import com.unal.reto11.model.ChatReply
import org.json.JSONException
import org.json.JSONObject

object GeminiResponseParser {

    /** Lanza [ChatException] si la respuesta no contiene texto utilizable. */
    fun parse(body: String): ChatReply {
        val root = try {
            JSONObject(body)
        } catch (e: JSONException) {
            throw ChatException(ChatError.BadJson)
        }

        root.optJSONObject("error")?.let { throw ChatException(errorFor(it.optInt("code"))) }

        val blockReason = root.optJSONObject("promptFeedback")?.optString("blockReason", "")
            ?.takeIf { it.isNotEmpty() }
        val candidates = root.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            if (blockReason != null) throw ChatException(ChatError.Blocked(blockReason))
            throw ChatException(ChatError.BadJson)
        }
        val candidate = candidates.optJSONObject(0) ?: throw ChatException(ChatError.BadJson)
        val finish = candidate.optString("finishReason", "").takeIf { it.isNotEmpty() }

        val parts = candidate.optJSONObject("content")?.optJSONArray("parts")
        val text = buildString {
            if (parts != null) for (i in 0 until parts.length()) {
                append(parts.optJSONObject(i)?.optString("text", "").orEmpty())
            }
        }
        if (text.isBlank()) {
            if (finish == "SAFETY" || finish == "PROHIBITED_CONTENT" || blockReason != null) {
                throw ChatException(ChatError.Blocked(blockReason ?: finish))
            }
            throw ChatException(ChatError.BadJson)
        }
        return ChatReply(text.trim(), finish)
    }

    fun errorFor(httpCode: Int): ChatError = when (httpCode) {
        400, 401, 403 -> ChatError.Unauthorized
        429 -> ChatError.RateLimited
        else -> ChatError.Server
    }
}
