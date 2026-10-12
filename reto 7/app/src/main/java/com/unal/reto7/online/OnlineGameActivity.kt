package com.unal.reto7.online

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unal.reto7.R
import com.unal.reto7.TicTacToeGame
import com.unal.reto7.databinding.ActivityOnlineGameBinding

/**
 * Partida online entre dos dispositivos. El estado vive en Firebase: la Activity solo
 * guarda el id del juego y el del jugador, y pinta lo que llega del servidor.
 */
class OnlineGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnlineGameBinding
    private val repository: GameRepository = FirebaseGameRepository()

    private lateinit var gameId: String
    private lateinit var myId: String

    // Solo sirve para que BoardView pinte el tablero remoto.
    private val boardModel = TicTacToeGame()
    private var current: OnlineGame? = null
    private var subscription: Subscription? = null

    // Igual que en MainActivity: convierte el punto tocado en una casilla (0-8) solo en ACTION_DOWN.
    private val boardTouchListener = View.OnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            val board = binding.board
            if (board.boardCellWidth > 0 && board.boardCellHeight > 0) {
                val col = (event.x / board.boardCellWidth).toInt().coerceIn(0, 2)
                val row = (event.y / board.boardCellHeight).toInt().coerceIn(0, 2)
                onCellTouched(row * 3 + col)
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnlineGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        gameId = checkNotNull(intent.getStringExtra(EXTRA_GAME_ID)) { "falta EXTRA_GAME_ID" }
        myId = checkNotNull(intent.getStringExtra(EXTRA_PLAYER_ID)) { "falta EXTRA_PLAYER_ID" }

        binding.board.game = boardModel
        binding.board.setOnTouchListener(boardTouchListener)
    }

    // El servidor es la fuente de verdad: al volver a primer plano (o tras rotar) se re-suscribe
    // y recibe el estado completo, por eso no hace falta onSaveInstanceState.
    override fun onStart() {
        super.onStart()
        subscription = repository.observeGame(gameId, ::render)
    }

    override fun onStop() {
        super.onStop()
        subscription?.cancel()
        subscription = null
    }

    private fun render(game: OnlineGame?) {
        if (game == null) return // nodo borrado: el manejo llega con la salida/desconexión
        current = game

        boardModel.setBoardState(game.board.toCharArray())
        binding.board.invalidate()

        binding.players.text = getString(
            R.string.online_players_format,
            game.hostName.ifBlank { "?" },
            game.guestName.ifBlank { "…" }
        )

        // Turnos y resultado se muestran cuando se sincronicen las jugadas.
        binding.information.text =
            if (game.status == OnlineGameRules.STATUS_WAITING) getString(R.string.online_waiting_guest)
            else ""
    }

    private fun onCellTouched(cell: Int) {
        val game = current ?: return
        // Tablero bloqueado mientras no haya dos jugadores en juego.
        if (game.status != OnlineGameRules.STATUS_PLAYING) return
        if (!OnlineGameRules.isMyTurn(game, myId)) {
            Toast.makeText(this, R.string.not_your_turn, Toast.LENGTH_SHORT).show()
            return
        }
        if (game.board[cell] != TicTacToeGame.OPEN_SPOT) return

        // Validación local rápida; la decisión final la toma la transacción en el servidor
        // (si el rival movió primero, se aborta y el tablero vuelve al estado real).
        repository.makeMove(gameId, myId, cell)
    }

    companion object {
        const val EXTRA_GAME_ID = "gameId"
        const val EXTRA_PLAYER_ID = "playerId"
    }
}
