package com.unal.reto11.model

enum class Role { USER, MODEL }

data class Message(
    val id: Long,
    val role: Role,
    val text: String,
    val isError: Boolean = false,
    /** Ruta de una imagen generada (archivo interno de la app), si la respuesta la incluye. */
    val imagePath: String? = null
)

data class ChatReply(
    val text: String,
    val finishReason: String? = null,
    val image: ByteArray? = null
)
