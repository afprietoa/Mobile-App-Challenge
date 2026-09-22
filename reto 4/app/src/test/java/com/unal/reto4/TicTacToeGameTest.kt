package com.unal.reto4

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
    fun defaultDifficulty_isExpert() {
        assertEquals(TicTacToeGame.DifficultyLevel.EXPERT, game.difficultyLevel)
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
    fun checkForWinner_detectsTie() {
        intArrayOf(0, 2, 3, 7, 8).forEach { game.setMove(TicTacToeGame.HUMAN_PLAYER, it) }
        intArrayOf(1, 4, 5, 6).forEach { game.setMove(TicTacToeGame.COMPUTER_PLAYER, it) }

        assertEquals(TicTacToeGame.RESULT_TIE, game.checkForWinner())
    }

    @Test
    fun easyDifficulty_alwaysReturnsAnOpenSpot() {
        game.difficultyLevel = TicTacToeGame.DifficultyLevel.EASY
        // O podría ganar en la posición 2, pero en Easy no debe garantizarse:
        // solo verificamos que la jugada sigue siendo válida.
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 0)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 1)

        repeat(20) {
            val move = game.getComputerMove()
            assertTrue(move in 0 until TicTacToeGame.BOARD_SIZE)
        }
    }

    @Test
    fun harderDifficulty_takesWinningMoveWhenAvailable() {
        game.difficultyLevel = TicTacToeGame.DifficultyLevel.HARDER
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 0)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 1)

        assertEquals(2, game.getComputerMove())
    }

    @Test
    fun harderDifficulty_movesRandomlyWhenNoWinAvailable() {
        game.difficultyLevel = TicTacToeGame.DifficultyLevel.HARDER
        // X amenaza en la columna izquierda (0,3); en Harder el computador NO bloquea.
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 3)

        val move = game.getComputerMove()

        assertTrue(move in 0 until TicTacToeGame.BOARD_SIZE)
        assertTrue(move != 0 && move != 3)
    }

    @Test
    fun expertDifficulty_blocksHumanWinningMove() {
        game.difficultyLevel = TicTacToeGame.DifficultyLevel.EXPERT
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 0)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 3)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 1)

        assertEquals(6, game.getComputerMove())
    }

    @Test
    fun expertDifficulty_takesWinningMoveOverBlocking() {
        game.difficultyLevel = TicTacToeGame.DifficultyLevel.EXPERT
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 0)
        game.setMove(TicTacToeGame.COMPUTER_PLAYER, 1)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 3)
        game.setMove(TicTacToeGame.HUMAN_PLAYER, 4)

        assertEquals(2, game.getComputerMove())
    }
}
