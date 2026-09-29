package com.unal.reto5

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unal.reto5.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val game = TicTacToeGame()

    private var gameOver = false
    private var humanGoesFirst = true
    private var humanWins = 0
    private var computerWins = 0
    private var ties = 0

    private var humanPlayer: MediaPlayer? = null
    private var computerPlayer: MediaPlayer? = null
    private var soundEnabled = true

    // Convierte el punto tocado en una casilla (0-8). Solo reacciona al primer contacto del dedo
    // (ACTION_DOWN) y devuelve false para no recibir los eventos MOVE/UP, como pide el tutorial.
    private val boardTouchListener = View.OnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            val board = binding.board
            if (board.boardCellWidth > 0 && board.boardCellHeight > 0) {
                val col = (event.x / board.boardCellWidth).toInt().coerceIn(0, 2)
                val row = (event.y / board.boardCellHeight).toInt().coerceIn(0, 2)
                onBoardTouched(row * 3 + col)
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.board.game = game
        binding.board.setOnTouchListener(boardTouchListener)

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.action_sound)?.isChecked = soundEnabled
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_new_game -> {
                        startNewGame()
                        true
                    }
                    R.id.action_difficulty -> {
                        showDifficultyDialog()
                        true
                    }
                    R.id.action_sound -> {
                        soundEnabled = !soundEnabled
                        menuItem.isChecked = soundEnabled
                        true
                    }
                    R.id.action_about -> {
                        showAboutDialog()
                        true
                    }
                    R.id.action_quit -> {
                        showQuitDialog()
                        true
                    }
                    else -> false
                }
            }
        })

        startNewGame()
    }

    // Los MediaPlayer consumen recursos compartidos del sistema: se crean al volver a primer
    // plano y se liberan al salir de él. create() puede devolver null, por eso son nulables.
    override fun onResume() {
        super.onResume()
        humanPlayer = MediaPlayer.create(applicationContext, R.raw.human_move)
        computerPlayer = MediaPlayer.create(applicationContext, R.raw.computer_move)
    }

    override fun onPause() {
        super.onPause()
        humanPlayer?.release()
        computerPlayer?.release()
        humanPlayer = null
        computerPlayer = null
    }

    // Muestra un diálogo de selección única con los niveles de dificultad;
    // la opción marcada de entrada es el nivel actual del juego.
    private fun showDifficultyDialog() {
        val levels = arrayOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_harder),
            getString(R.string.difficulty_expert)
        )
        val selected = game.difficultyLevel.ordinal

        AlertDialog.Builder(this)
            .setTitle(R.string.difficulty_choose)
            .setSingleChoiceItems(levels, selected) { dialog, which ->
                dialog.dismiss()
                game.difficultyLevel = TicTacToeGame.DifficultyLevel.entries[which]
                Toast.makeText(this, levels[which], Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // Pide confirmación antes de cerrar la Activity; "No" simplemente cierra el diálogo.
    private fun showQuitDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.quit_question)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { _, _ -> finish() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    // Infla dialog_about.xml y lo muestra dentro de un AlertDialog con un único botón OK.
    private fun showAboutDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_about, null)
        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    // Prepara un tablero nuevo. Alterna quién empieza respecto a la partida anterior
    // y actualiza el marcador visible.
    private fun startNewGame() {
        game.clearBoard()
        gameOver = false
        binding.board.invalidate() // Redibuja el tablero vacío

        updateScoreboard()

        if (humanGoesFirst) {
            binding.information.text = getString(R.string.first_human)
        } else {
            binding.information.text = getString(R.string.turn_computer)
            setMove(TicTacToeGame.COMPUTER_PLAYER, game.getComputerMove())
            binding.information.text = getString(R.string.turn_human)
        }
        humanGoesFirst = !humanGoesFirst
    }

    // Maneja el toque del humano sobre una casilla y, si el juego continúa, responde el computador.
    private fun onBoardTouched(location: Int) {
        if (gameOver || !setMove(TicTacToeGame.HUMAN_PLAYER, location)) return

        var winner = game.checkForWinner()

        if (winner == TicTacToeGame.RESULT_NONE) {
            binding.information.text = getString(R.string.turn_computer)
            setMove(TicTacToeGame.COMPUTER_PLAYER, game.getComputerMove())
            winner = game.checkForWinner()
        }

        if (winner == TicTacToeGame.RESULT_NONE) {
            binding.information.text = getString(R.string.turn_human)
        } else {
            endGame(winner)
        }
    }

    // Aplica la jugada al modelo y, si fue legal, redibuja el tablero y reproduce su sonido.
    private fun setMove(player: Char, location: Int): Boolean {
        if (!game.setMove(player, location)) return false
        binding.board.invalidate()
        playSound(if (player == TicTacToeGame.HUMAN_PLAYER) humanPlayer else computerPlayer)
        return true
    }

    // Reinicia el clip (seekTo(0)) para que vuelva a sonar aunque el anterior no haya terminado.
    private fun playSound(player: MediaPlayer?) {
        if (!soundEnabled) return
        player?.apply {
            seekTo(0)
            start()
        }
    }

    // Marca la partida como terminada, actualiza los contadores y el marcador.
    private fun endGame(result: Int) {
        gameOver = true

        binding.information.text = when (result) {
            TicTacToeGame.RESULT_TIE -> {
                ties++
                getString(R.string.result_tie)
            }
            TicTacToeGame.RESULT_HUMAN_WON -> {
                humanWins++
                getString(R.string.result_human_wins)
            }
            else -> {
                computerWins++
                getString(R.string.result_computer_wins)
            }
        }
        updateScoreboard()
    }

    private fun updateScoreboard() {
        binding.scoreHuman.text = getString(R.string.score_human_format, humanWins)
        binding.scoreTies.text = getString(R.string.score_ties_format, ties)
        binding.scoreComputer.text = getString(R.string.score_computer_format, computerWins)
    }
}
