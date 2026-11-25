package com.unal.reto11.data

import com.unal.reto11.model.Message
import com.unal.reto11.model.Role
import org.json.JSONArray
import org.json.JSONObject

object GeminiRequestBuilder {

    fun build(
        history: List<Message>,
        systemPrompt: String,
        temperature: Double = GeminiConfig.DEFAULT_TEMPERATURE,
        maxOutputTokens: Int = GeminiConfig.MAX_OUTPUT_TOKENS
    ): JSONObject {
        val contents = JSONArray()
        history.filter { !it.isError }.forEach { m ->
            contents.put(
                JSONObject()
                    .put("role", if (m.role == Role.USER) "user" else "model")
                    .put("parts", JSONArray().put(JSONObject().put("text", m.text)))
            )
        }
        val root = JSONObject()
        if (systemPrompt.isNotBlank()) {
            root.put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            )
        }
        root.put("contents", contents)
        root.put(
            "generationConfig",
            JSONObject().put("temperature", temperature).put("maxOutputTokens", maxOutputTokens)
        )
        return root
    }
}
