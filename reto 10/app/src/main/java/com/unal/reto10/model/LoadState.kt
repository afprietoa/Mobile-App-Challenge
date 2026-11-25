package com.unal.reto10.model

enum class ErrorKind { NO_NETWORK, TIMEOUT, SERVER, BAD_JSON }

sealed interface LoadState {
    data object Idle : LoadState
    data object Loading : LoadState
    data class Success(
        val items: List<CollectionPoint>,
        val total: Int,
        val loadingMore: Boolean = false
    ) : LoadState
    data object Empty : LoadState
    data class Error(val kind: ErrorKind) : LoadState
}
