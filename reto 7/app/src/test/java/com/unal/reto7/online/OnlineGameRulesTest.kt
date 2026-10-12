package com.unal.reto7.online

import com.unal.reto7.TicTacToeGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OnlineGameRulesTest {

    private val host = "uid-host"
    private val guest = "uid-guest"

    private fun waiting() = OnlineGame(id = "g1", hostId = host, hostName = "Ana")
    private fun playing() = OnlineGameRules.join(waiting(), guest, "Luis")!!

    // Aplica jugadas (jugador, casilla) en orden; falla si alguna es ilegal.
    private fun play(start: OnlineGame, vararg moves: Pair<String, Int>): OnlineGame =
        moves.fold(start) { game, (who, cell) ->
            checkNotNull(OnlineGameRules.applyMove(game, who, cell)) { "jugada ilegal: $who -> $cell" }
        }

    // X: 0,1,2  /  O: 3,4  -> gana el anfitrión
    private fun hostWins() =
        play(playing(), host to 0, guest to 3, host to 1, guest to 4, host to 2)

    @Test
    fun join_assignsGuestAndStartsGame() {
        val game = playing()

        assertEquals(OnlineGameRules.STATUS_PLAYING, game.status)
        assertEquals(guest, game.guestId)
        assertEquals("Luis", game.guestName)
        assertEquals("X", game.turn)
    }

    @Test
    fun join_rejectsHostJoiningOwnGame() {
        assertNull(OnlineGameRules.join(waiting(), host, "Ana"))
    }

    @Test
    fun join_rejectsThirdPlayer() {
        assertNull(OnlineGameRules.join(playing(), "uid-3", "Eva"))
    }

    @Test
    fun move_isRejectedBeforeSomeoneJoins() {
        assertNull(OnlineGameRules.applyMove(waiting(), host, 0))
    }

    @Test
    fun move_isRejectedOutOfTurn() {
        assertNull(OnlineGameRules.applyMove(playing(), guest, 0))
    }

    @Test
    fun move_isRejectedFromNonParticipant() {
        assertNull(OnlineGameRules.applyMove(playing(), "intruso", 0))
    }

    @Test
    fun move_isRejectedOnOccupiedCell() {
        val afterHost = OnlineGameRules.applyMove(playing(), host, 4)!!

        assertNull(OnlineGameRules.applyMove(afterHost, guest, 4))
    }

    @Test
    fun move_isRejectedOutOfRange() {
        assertNull(OnlineGameRules.applyMove(playing(), host, 9))
        assertNull(OnlineGameRules.applyMove(playing(), host, -1))
    }

    @Test
    fun move_placesMarkAndPassesTurn() {
        val game = OnlineGameRules.applyMove(playing(), host, 0)!!

        assertEquals('X', game.board[0])
        assertEquals("O", game.turn)
        assertEquals(OnlineGameRules.STATUS_PLAYING, game.status)
    }

    @Test
    fun move_detectsHostWin() {
        val game = hostWins()

        assertEquals(OnlineGameRules.STATUS_FINISHED, game.status)
        assertEquals(TicTacToeGame.RESULT_HUMAN_WON, game.result)
    }

    @Test
    fun move_detectsGuestWin() {
        // X: 0,1,8  /  O: 3,4,5
        val game = play(
            playing(),
            host to 0, guest to 3, host to 1, guest to 4, host to 8, guest to 5
        )

        assertEquals(OnlineGameRules.STATUS_FINISHED, game.status)
        assertEquals(TicTacToeGame.RESULT_COMPUTER_WON, game.result)
    }

    @Test
    fun move_detectsTie() {
        // X O X / X O O / O X X
        val game = play(
            playing(),
            host to 0, guest to 1, host to 2, guest to 4, host to 3,
            guest to 5, host to 7, guest to 6, host to 8
        )

        assertEquals(OnlineGameRules.STATUS_FINISHED, game.status)
        assertEquals(TicTacToeGame.RESULT_TIE, game.result)
    }

    @Test
    fun move_isRejectedAfterGameFinished() {
        assertNull(OnlineGameRules.applyMove(hostWins(), guest, 8))
    }

    @Test
    fun rematch_resetsBoardAndAlternatesStarter() {
        val next = OnlineGameRules.rematch(hostWins(), guest)

        assertNotNull(next)
        assertEquals(OnlineGameRules.EMPTY_BOARD, next!!.board)
        assertEquals("O", next.turn)
        assertEquals("O", next.firstMark)
        assertEquals(OnlineGameRules.STATUS_PLAYING, next.status)
        assertEquals(TicTacToeGame.RESULT_NONE, next.result)
    }

    @Test
    fun rematch_isRejectedWhileGameInProgress() {
        assertNull(OnlineGameRules.rematch(playing(), host))
    }

    @Test
    fun abandon_onlyWhilePlayingAndByParticipant() {
        assertEquals(
            OnlineGameRules.STATUS_ABANDONED,
            OnlineGameRules.abandon(playing(), guest)!!.status
        )
        assertNull(OnlineGameRules.abandon(playing(), "intruso"))
        assertNull(OnlineGameRules.abandon(waiting(), host))
    }
}
