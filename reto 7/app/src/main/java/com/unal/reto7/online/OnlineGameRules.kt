package com.unal.reto7.online

import com.unal.reto7.TicTacToeGame

/**
 * Reglas del juego online como funciones puras: reciben el estado actual y devuelven el
 * estado siguiente, o null si la acción no es legal. Se ejecutan dentro de una transacción
 * de Firebase, así dos dispositivos nunca pueden "ganarse" la misma jugada.
 *
 * En el modo online, RESULT_HUMAN_WON significa "ganó X (anfitrión)" y
 * RESULT_COMPUTER_WON "ganó O (invitado)"; se reutilizan las constantes de [TicTacToeGame].
 */
object OnlineGameRules {
    const val EMPTY_BOARD = "         " // 9 espacios
    const val HOST_MARK = "X"
    const val GUEST_MARK = "O"

    const val STATUS_WAITING = "waiting"
    const val STATUS_PLAYING = "playing"
    const val STATUS_FINISHED = "finished"
    const val STATUS_ABANDONED = "abandoned"

    /** "X" si [playerId] es el anfitrión, "O" si es el invitado, null si no participa. */
    fun markOf(game: OnlineGame, playerId: String): String? = when {
        playerId.isEmpty() -> null
        playerId == game.hostId -> HOST_MARK
        playerId == game.guestId -> GUEST_MARK
        else -> null
    }

    fun isMyTurn(game: OnlineGame, playerId: String): Boolean =
        game.status == STATUS_PLAYING && markOf(game, playerId) == game.turn

    /** Un jugador se une a un juego en espera. Null si está lleno, no es `waiting` o es el propio anfitrión. */
    fun join(game: OnlineGame, playerId: String, playerName: String): OnlineGame? {
        if (game.status != STATUS_WAITING || game.guestId.isNotEmpty()) return null
        if (playerId.isEmpty() || playerId == game.hostId) return null
        return game.copy(
            guestId = playerId,
            guestName = playerName,
            guestOnline = true,
            status = STATUS_PLAYING,
            turn = game.firstMark
        )
    }

    /** Aplica la jugada de [playerId] en [cell] (0-8) o devuelve null si es ilegal. */
    fun applyMove(game: OnlineGame, playerId: String, cell: Int): OnlineGame? {
        if (!isMyTurn(game, playerId)) return null
        if (cell !in 0 until TicTacToeGame.BOARD_SIZE || game.board.length != TicTacToeGame.BOARD_SIZE) return null
        if (game.board[cell] != TicTacToeGame.OPEN_SPOT) return null

        val mark = markOf(game, playerId) ?: return null
        val newBoard = StringBuilder(game.board).apply { setCharAt(cell, mark[0]) }.toString()

        val checker = TicTacToeGame().apply { setBoardState(newBoard.toCharArray()) }
        val result = checker.checkForWinner()

        return if (result == TicTacToeGame.RESULT_NONE) {
            game.copy(board = newBoard, turn = other(mark))
        } else {
            game.copy(board = newBoard, status = STATUS_FINISHED, result = result)
        }
    }

    /** Revancha: solo desde `finished`; tablero vacío y empieza el que NO empezó la anterior. */
    fun rematch(game: OnlineGame, playerId: String): OnlineGame? {
        if (game.status != STATUS_FINISHED || markOf(game, playerId) == null) return null
        val next = other(game.firstMark)
        return game.copy(
            board = EMPTY_BOARD,
            status = STATUS_PLAYING,
            result = TicTacToeGame.RESULT_NONE,
            firstMark = next,
            turn = next
        )
    }

    /** Marca la partida como abandonada si el jugador participa y está en curso. */
    fun abandon(game: OnlineGame, playerId: String): OnlineGame? {
        if (markOf(game, playerId) == null || game.status != STATUS_PLAYING) return null
        return game.copy(status = STATUS_ABANDONED)
    }

    private fun other(mark: String) = if (mark == HOST_MARK) GUEST_MARK else HOST_MARK
}
