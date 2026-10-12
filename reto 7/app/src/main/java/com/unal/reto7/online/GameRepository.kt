package com.unal.reto7.online

/** Permite cancelar un listener; se llama en onStop para no filtrar la Activity. */
fun interface Subscription {
    fun cancel()
}

/**
 * Acceso a los juegos online. La UI solo conoce esta interfaz: ninguna Activity importa
 * clases de Firebase, lo que permite sustituir la implementación (p. ej. en pruebas).
 */
interface GameRepository {

    /** Inicia sesión anónima si hace falta y entrega el uid, o null si falló (sin red, etc.). */
    fun signIn(onResult: (uid: String?) -> Unit)

    /** Lista en tiempo real los juegos `waiting` (el lobby filtra el propio y los viejos). */
    fun observeWaitingGames(onChange: (List<OnlineGame>) -> Unit, onError: () -> Unit): Subscription

    /** Crea un juego `waiting` y entrega su id (o null si falló). */
    fun createGame(hostId: String, hostName: String, onResult: (gameId: String?) -> Unit)

    /** Unirse atómicamente; `true` solo si esta llamada ocupó el segundo puesto. */
    fun joinGame(gameId: String, playerId: String, playerName: String, onResult: (Boolean) -> Unit)

    /** Observa un juego; entrega `null` si el nodo fue borrado. */
    fun observeGame(gameId: String, onChange: (OnlineGame?) -> Unit): Subscription

    /** Aplica la jugada en una transacción; `false` si fue ilegal o perdió la carrera con el rival. */
    fun makeMove(gameId: String, playerId: String, cell: Int, onResult: (Boolean) -> Unit = {})

    /** Reinicia un juego `finished` con el tablero vacío; empieza quien no empezó el anterior. */
    fun requestRematch(gameId: String, playerId: String, onResult: (Boolean) -> Unit = {})

    /** Marca presencia y programa `onDisconnect` para la bandera del jugador. */
    fun setPresence(gameId: String, isHost: Boolean)

    /** Sale del juego: borra si estaba en espera/terminado, o lo marca `abandoned` si estaba en curso. */
    fun leaveGame(gameId: String, playerId: String)
}
