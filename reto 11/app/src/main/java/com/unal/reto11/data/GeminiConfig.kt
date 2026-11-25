package com.unal.reto11.data

import com.unal.reto11.BuildConfig

object GeminiConfig {
    /** Verificar con GET /v1beta/models que siga vigente. */
    const val MODEL = "gemini-3.5-flash"
    const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    const val CONNECT_TIMEOUT_MS = 10_000
    const val READ_TIMEOUT_MS = 60_000
    const val MAX_MESSAGES = 20
    const val DEFAULT_TEMPERATURE = 0.7
    const val MAX_OUTPUT_TOKENS = 1024

    val apiKey: String get() = BuildConfig.GEMINI_API_KEY

    const val DEFAULT_SYSTEM_PROMPT =
        "Eres un tutor de desarrollo móvil (Android y Kotlin). Responde siempre en español, " +
            "de forma clara y concisa, con ejemplos de código en Kotlin cuando ayuden."
}
