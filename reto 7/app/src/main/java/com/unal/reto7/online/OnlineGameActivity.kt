package com.unal.reto7.online

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    private var presenceRegistered = false
    private var leaving = false
    private var endDialogShown = false

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

        // La revancha es una transacción: si ambos la piden a la vez, solo se aplica una.
        binding.buttonRematch.setOnClickListener { repository.requestRematch(gameId, myId) }

        // Salir en mitad de una partida pide confirmación porque avisa y cierra el juego del rival.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (current?.status == OnlineGameRules.STATUS_PLAYING) {
                    AlertDialog.Builder(this@OnlineGameActivity)
                        .setTitle(R.string.online_leave_title)
                        .setMessage(R.string.online_leave_message)
                        .setPositiveButton(R.string.yes) { _, _ -> leave() }
                        .setNegativeButton(R.string.no, null)
                        .show()
                } else {
                    leave()
                }
            }
        })
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
        if (leaving) return
        if (game == null) {
            // El nodo fue borrado (p. ej. el anfitrión salió de un juego en espera o terminado).
            showEndAndExit(R.string.online_game_gone)
            return
        }
        if (game.status == OnlineGameRules.STATUS_ABANDONED) {
            showEndAndExit(R.string.online_opponent_left)
            return
        }
        current = game

        boardModel.setBoardState(game.board.toCharArray())
        binding.board.invalidate()

        val iAmHost = myId == game.hostId
        val opponentName = (if (iAmHost) game.guestName else game.hostName).ifBlank { "?" }

        binding.players.text = getString(
            R.string.online_players_format,
            game.hostName.ifBlank { "?" },
            game.guestName.ifBlank { "…" }
        )

        // Presencia: se registra una vez que la partida está en curso (ver setPresence).
        if (game.status == OnlineGameRules.STATUS_PLAYING && !presenceRegistered) {
            presenceRegistered = true
            repository.setPresence(gameId, iAmHost)
        }
        val opponentOffline = if (iAmHost) !game.guestOnline else !game.hostOnline

        binding.information.text = when (game.status) {
            OnlineGameRules.STATUS_WAITING -> getString(R.string.online_waiting_guest)
            OnlineGameRules.STATUS_PLAYING -> when {
                opponentOffline -> getString(R.string.online_opponent_offline, opponentName)
                OnlineGameRules.isMyTurn(game, myId) ->
                    getString(R.string.online_your_turn, OnlineGameRules.markOf(game, myId))
                else -> getString(R.string.online_opponent_turn, opponentName)
            }
            OnlineGameRules.STATUS_FINISHED -> resultText(game, iAmHost)
            else -> ""
        }

        binding.buttonRematch.visibility =
            if (game.status == OnlineGameRules.STATUS_FINISHED) View.VISIBLE else View.GONE
    }

    // RESULT_HUMAN_WON significa aquí "ganó X (anfitrión)" y RESULT_COMPUTER_WON "ganó O (invitado)".
    private fun resultText(game: OnlineGame, iAmHost: Boolean): String = when (game.result) {
        TicTacToeGame.RESULT_TIE -> getString(R.string.online_tie)
        TicTacToeGame.RESULT_HUMAN_WON ->
            getString(if (iAmHost) R.string.online_you_won else R.string.online_you_lost)
        else ->
            getString(if (iAmHost) R.string.online_you_lost else R.string.online_you_won)
    }

    // waiting/finished: borra el juego · playing: lo marca `abandoned` para avisar al rival.
    private fun leave() {
        leaving = true
        repository.leaveGame(gameId, myId)
        finish()
    }

    // Avisa que la partida terminó por causa externa y vuelve al lobby al aceptar.
    private fun showEndAndExit(message: Int) {
        if (isFinishing || endDialogShown) return
        endDialogShown = true
        AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ -> leave() } // limpia el nodo si quedó
            .show()
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
