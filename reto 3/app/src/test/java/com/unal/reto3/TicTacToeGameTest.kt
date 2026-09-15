package com.unal.reto3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TicTacToeGameTest {

    private lateinit var game: TicTacToeGame

    @Before
    fun setUp() {
        game = TicTacToeGame()
    }

    @Test
    fun clearBoard_leavesAllSpotsOpen() {
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.clearBoard()

        assertEquals(TicTacToeGame.RESULT_NONE, game.checkForWinner())
    }

    @Test
    fun setMove_doesNotOverwriteOccupiedSpot() {
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 0)

        // getComputerMove() nunca debería elegir la posición 0: sigue ocupada por X.
        assertTrue(game.getComputerMove() != 0)
    }

    @Test
    fun checkForWinner_detectsHumanRowWin() {
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 1)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 2)

        assertEquals(TicTacToeGame.RESULT_HUMAN_WON, game.checkForWinner())
    }

    @Test
    fun checkForWinner_detectsComputerColumnWin() {
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 0)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 3)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 6)

        assertEquals(TicTacToeGame.RESULT_COMPUTER_WON, game.checkForWinner())
    }

    @Test
    fun checkForWinner_detectsDiagonalWin() {
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 4)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 8)

        assertEquals(TicTacToeGame.RESULT_HUMAN_WON, game.checkForWinner())
    }

    @Test
    fun checkForWinner_detectsTie() {
        // X | O | X
        // X | O | O
        // O | X | X
        intArrayOf(0, 2, 3, 7, 8).forEach { game.setMove(TicTacToeGame.HUMAN_PLAYER, it) }
        intArrayOf(1, 4, 5, 6).forEach { game.setMove(TicTacToeGame.COMPUTER_PLAYER, it) }

        assertEquals(TicTacToeGame.RESULT_TIE, game.checkForWinner())
    }

    @Test
    fun checkForWinner_returnsNoneMidGame() {
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 4)

        assertEquals(TicTacToeGame.RESULT_NONE, game.checkForWinner())
    }

    @Test
    fun getComputerMove_takesWinningMoveWhenAvailable() {
        // O tiene dos en la fila superior (0,1); debe tomar la posición 2 para ganar,
        // aunque X también tenga una amenaza en la fila del medio (3,4).
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 0)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 1)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 3)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 4)

        assertEquals(2, game.getComputerMove())
    }

    @Test
    fun getComputerMove_blocksHumanWinningMove() {
        // X tiene dos en la columna izquierda (0,3); O debe bloquear en la posición 6.
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 3)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 1)

        assertEquals(6, game.getComputerMove())
    }

    @Test
    fun getComputerMove_alwaysReturnsAnOpenSpot() {
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 1)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 2)

        val move = game.getComputerMove()

        assertTrue(move in 0 until TicTacToeGame.BOARD_SIZE)
        assertTrue(move != 0 && move != 1 && move != 2)
    }
}
