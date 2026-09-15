package com.unal.reto3

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unal.reto3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var boardButtons: Array<Button>
    private val game = TicTacToeGame()
    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    // Prepara un tablero nuevo: limpia el modelo, resetea los botones y engancha los clicks.
    private fun startNewGame() {
        game.clearBoard()
        gameOver = false

        boardButtons.forEachIndexed { index, button ->
            button.text = ""
            button.isEnabled = true
            button.setOnClickListener { onBoardButtonClicked(index) }
        }

        binding.information.text = "Tu turno."
    }

    // Maneja el toque del humano sobre una casilla y, si el juego continúa, responde el computador.
    private fun onBoardButtonClicked(location: Int) {
        if (gameOver || !boardButtons[location].isEnabled) return

        setMove(TicTacToeGame.HUMAN_PLAYER, location)
        var winner = game.checkForWinner()

        if (winner == TicTacToeGame.RESULT_NONE) {
            binding.information.text = "Turno de Android."
            val move = game.getComputerMove()
            setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            winner = game.checkForWinner()
        }

        if (winner == TicTacToeGame.RESULT_NONE) {
            binding.information.text = "Tu turno."
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
                if (player == TicTacToeGame.HUMAN_PLAYER) Color.rgb(0, 200, 0)
                else Color.rgb(200, 0, 0)
            )
        }
    }

    // Marca la partida como terminada, deshabilita todo el tablero y muestra el resultado.
    private fun endGame(result: Int) {
        gameOver = true
        boardButtons.forEach { it.isEnabled = false }

        binding.information.text = when (result) {
            TicTacToeGame.RESULT_TIE -> "Empate."
            TicTacToeGame.RESULT_HUMAN_WON -> "¡Ganaste!"
            else -> "Android ganó."
        }
    }
}
