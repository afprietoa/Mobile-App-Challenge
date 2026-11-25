package com.unal.reto11.model

sealed interface ChatError {
    data object NoNetwork : ChatError
    data object Timeout : ChatError
    data object Unauthorized : ChatError
    data object RateLimited : ChatError
    data class Blocked(val reason: String?) : ChatError
    data object Server : ChatError
    data object BadJson : ChatError
    data object MissingKey : ChatError
}

class ChatException(val error: ChatError) : Exception(error.toString())

sealed interface ChatState {
    data object Idle : ChatState
    data object Sending : ChatState
    data class Failed(val error: ChatError) : ChatState
}
