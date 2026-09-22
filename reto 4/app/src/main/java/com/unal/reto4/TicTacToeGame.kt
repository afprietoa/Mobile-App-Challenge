package com.unal.reto4

import kotlin.random.Random

/**
 * Lógica pura del juego: estado del tablero, reglas y una IA simple
 * (gana si puede, bloquea si el humano puede ganar, si no, mueve al azar).
 */
class TicTacToeGame {

    private val board = CharArray(BOARD_SIZE) { OPEN_SPOT }
    private val random = Random(System.currentTimeMillis())

    /** Vacía el tablero, dejando todas las posiciones en OPEN_SPOT. */
    fun clearBoard() {
        for (i in board.indices) board[i] = OPEN_SPOT
    }

    /** Coloca a [player] en [location] solo si esa posición está libre. */
    fun setMove(player: Char, location: Int) {
        if (board[location] == OPEN_SPOT) {
            board[location] = player
        }
    }

    /**
     * Devuelve la mejor jugada para el computador (0-8).
     * No mueve por sí sola: hay que llamar a [setMove] con el resultado.
     */
    fun getComputerMove(): Int {
        return getWinningMove(COMPUTER_PLAYER)
            ?: getWinningMove(HUMAN_PLAYER)
            ?: getRandomOpenMove()
    }

    /**
     * @return RESULT_NONE si el juego sigue, RESULT_TIE si es empate,
     * RESULT_HUMAN_WON o RESULT_COMPUTER_WON si hay ganador.
     */
    fun checkForWinner(): Int {
        for ((a, b, c) in WINNING_LINES) {
            if (board[a] != OPEN_SPOT && board[a] == board[b] && board[b] == board[c]) {
                return if (board[a] == HUMAN_PLAYER) RESULT_HUMAN_WON else RESULT_COMPUTER_WON
            }
        }
        return if (board.any { it == OPEN_SPOT }) RESULT_NONE else RESULT_TIE
    }

    /** Busca una línea donde [player] tenga 2 marcas y 1 hueco; devuelve ese hueco o null. */
    private fun getWinningMove(player: Char): Int? {
        for ((a, b, c) in WINNING_LINES) {
            val line = intArrayOf(a, b, c)
            val values = line.map { board[it] }
            if (values.count { it == player } == 2 && values.count { it == OPEN_SPOT } == 1) {
                return line[values.indexOf(OPEN_SPOT)]
            }
        }
        return null
    }

    private fun getRandomOpenMove(): Int {
        var move: Int
        do {
            move = random.nextInt(BOARD_SIZE)
        } while (board[move] != OPEN_SPOT)
        return move
    }

    companion object {
        const val BOARD_SIZE = 9
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '

        const val RESULT_NONE = 0
        const val RESULT_TIE = 1
        const val RESULT_HUMAN_WON = 2
        const val RESULT_COMPUTER_WON = 3

        // Filas, columnas y diagonales del tablero 3x3 (índices 0-8)
        private val WINNING_LINES = arrayOf(
            Triple(0, 1, 2), Triple(3, 4, 5), Triple(6, 7, 8), // filas
            Triple(0, 3, 6), Triple(1, 4, 7), Triple(2, 5, 8), // columnas
            Triple(0, 4, 8), Triple(2, 4, 6)                   // diagonales
        )
    }
}
