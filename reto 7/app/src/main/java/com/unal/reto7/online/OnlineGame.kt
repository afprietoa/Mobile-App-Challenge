package com.unal.reto7.online

import com.google.firebase.database.Exclude

/**
 * Documento `games/{id}` de Realtime Database. Todos los campos tienen valor por defecto
 * (constructor sin argumentos) porque Firebase instancia la clase por reflexión.
 */
data class OnlineGame(
    @get:Exclude val id: String = "",      // es la clave del nodo, no un campo guardado
    val hostId: String = "",
    val hostName: String = "",
    val guestId: String = "",
    val guestName: String = "",
    val board: String = OnlineGameRules.EMPTY_BOARD,
    val turn: String = OnlineGameRules.HOST_MARK,
    val firstMark: String = OnlineGameRules.HOST_MARK,
    val status: String = OnlineGameRules.STATUS_WAITING,
    val result: Int = 0,
    val createdAt: Long = 0L,
    val hostOnline: Boolean = true,
    val guestOnline: Boolean = false
)
