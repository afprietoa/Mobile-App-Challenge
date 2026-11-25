package com.unal.reto11.data

import com.unal.reto11.model.Message
import com.unal.reto11.model.Role

object ChatHistoryTrimmer {
    /** Conserva los últimos [maxMessages] mensajes válidos y garantiza que empiece con rol USER. */
    fun trim(history: List<Message>, maxMessages: Int = GeminiConfig.MAX_MESSAGES): List<Message> {
        val valid = history.filter { !it.isError }
        val tail = if (valid.size > maxMessages) valid.takeLast(maxMessages) else valid
        return tail.dropWhile { it.role != Role.USER }
    }
}
