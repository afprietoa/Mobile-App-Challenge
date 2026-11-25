package com.unal.reto11.data

import com.unal.reto11.model.ChatReply
import com.unal.reto11.model.Message

interface ChatRepository {
    /** Bloqueante: llamar desde un hilo de fondo. El fallo es un [com.unal.reto11.model.ChatException]. */
    fun send(history: List<Message>, systemPrompt: String, temperature: Double): Result<ChatReply>

    /** Aborta la solicitud en curso (si hay). */
    fun cancel()
}
