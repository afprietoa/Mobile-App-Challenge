package com.unal.reto4

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unal.reto4.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var boardButtons: Array<Button>
    private val game = TicTacToeGame()

    private var gameOver = false
    private var humanGoesFirst = true
    private var humanWins = 0
    private var computerWins = 0
    private var ties = 0

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

        boardButtons = arrayOf(
            binding.button0, binding.button1, binding.button2,
            binding.button3, binding.button4, binding.button5,
            binding.button6, binding.button7, binding.button8
        )

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId != R.id.action_new_game) return false
                startNewGame()
                return true
            }
        })

        startNewGame()
    }

    // Prepara un tablero nuevo. Alterna quién empieza respecto a la partida anterior
    // y actualiza el marcador visible.
    private fun startNewGame() {
        game.clearBoard()
        gameOver = false

        boardButtons.forEachIndexed { index, button ->
            button.text = ""
            button.isEnabled = true
            button.setOnClickListener { onBoardButtonClicked(index) }
        }

        updateScoreboard()

        if (humanGoesFirst) {
            binding.information.text = getString(R.string.first_human)
        } else {
            binding.information.text = getString(R.string.turn_computer)
            val move = game.getComputerMove()
            setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            binding.information.text = getString(R.string.turn_human)
        }
        humanGoesFirst = !humanGoesFirst
    }

    // Maneja el toque del humano sobre una casilla y, si el juego continúa, responde el computador.
    private fun onBoardButtonClicked(location: Int) {
        if (gameOver || !boardButtons[location].isEnabled) return

        setMove(TicTacToeGame.HUMAN_PLAYER, location)
        var winner = game.checkForWinner()

        if (winner == TicTacToeGame.RESULT_NONE) {
            binding.information.text = getString(R.string.turn_computer)
            val move = game.getComputerMove()
            setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            winner = game.checkForWinner()
        }

        if (winner == TicTacToeGame.RESULT_NONE) {
            binding.information.text = getString(R.string.turn_human)
        } else {
            endGame(winner)
        }
    }

    // Actualiza el modelo y el botón correspondiente: texto, color y estado deshabilitado.
    private fun setMove(player: Char, location: Int) {
        game.setMove(player, location)
        boardButtons[location].apply {
            isEnabled = false
            text = player.toString()
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (player == TicTacToeGame.HUMAN_PLAYER) R.color.x_color else R.color.o_color
                )
            )
        }
    }

    // Marca la partida como terminada, actualiza los contadores y el marcador.
    private fun endGame(result: Int) {
        gameOver = true
        boardButtons.forEach { it.isEnabled = false }

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
