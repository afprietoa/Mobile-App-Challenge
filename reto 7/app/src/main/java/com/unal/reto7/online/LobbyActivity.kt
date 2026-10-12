package com.unal.reto7.online

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.unal.reto7.MainActivity
import com.unal.reto7.R
import com.unal.reto7.databinding.ActivityLobbyBinding

/**
 * Pantalla de arranque: lista en tiempo real los juegos disponibles, permite crear uno
 * o unirse a uno existente, y abre el modo contra Android del reto 6.
 */
class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding
    private val repository: GameRepository = FirebaseGameRepository()
    private lateinit var playerPrefs: PlayerPrefs
    private lateinit var adapter: GameListAdapter

    private var myId: String? = null
    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        playerPrefs = PlayerPrefs(this)
        adapter = GameListAdapter(::onGameClicked)
        binding.gamesList.layoutManager = LinearLayoutManager(this)
        binding.gamesList.adapter = adapter

        binding.buttonCreate.setOnClickListener { createGame() }
        binding.buttonVsAndroid.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.lobby_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId != R.id.action_change_name) return false
                askName()
                return true
            }
        })

        if (playerPrefs.name.isBlank()) askName()
    }

    // La sesión y el listener viven solo mientras el lobby es visible (se cancelan en onStop).
    override fun onStart() {
        super.onStart()
        binding.lobbyStatus.setText(R.string.lobby_connecting)
        repository.signIn { uid ->
            if (isDestroyed) return@signIn
            if (uid == null) {
                binding.lobbyStatus.setText(R.string.lobby_error)
                return@signIn
            }
            myId = uid
            binding.buttonCreate.isEnabled = true
            observeGames()
        }
    }

    override fun onStop() {
        super.onStop()
        subscription?.cancel()
        subscription = null
    }

    private fun observeGames() {
        subscription?.cancel()
        subscription = repository.observeWaitingGames(
            onChange = { all ->
                // Se ocultan los juegos propios y los que llevan mucho tiempo esperando.
                val cutoff = System.currentTimeMillis() - STALE_AFTER_MS
                val visible = all.filter { it.hostId != myId && it.createdAt >= cutoff }
                adapter.submit(visible)
                binding.lobbyStatus.text =
                    if (visible.isEmpty()) getString(R.string.lobby_empty)
                    else getString(R.string.lobby_available, visible.size)
            },
            onError = { binding.lobbyStatus.setText(R.string.lobby_error) }
        )
    }

    private fun createGame() {
        val uid = myId ?: return
        binding.buttonCreate.isEnabled = false
        repository.createGame(uid, displayName()) { gameId ->
            if (isDestroyed) return@createGame
            binding.buttonCreate.isEnabled = true
            if (gameId == null) {
                Toast.makeText(this, R.string.lobby_error, Toast.LENGTH_SHORT).show()
            } else {
                openGame(gameId)
            }
        }
    }

    // La unión es una transacción: si otro jugador llegó primero, no se entra.
    private fun onGameClicked(game: OnlineGame) {
        val uid = myId ?: return
        repository.joinGame(game.id, uid, displayName()) { joined ->
            if (isDestroyed) return@joinGame
            if (joined) {
                openGame(game.id)
            } else {
                Toast.makeText(this, R.string.lobby_join_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGame(gameId: String) {
        startActivity(
            Intent(this, OnlineGameActivity::class.java)
                .putExtra(OnlineGameActivity.EXTRA_GAME_ID, gameId)
                .putExtra(OnlineGameActivity.EXTRA_PLAYER_ID, myId)
        )
    }

    private fun displayName(): String =
        playerPrefs.name.ifBlank { getString(R.string.default_name_format, myId.orEmpty().takeLast(4)) }

    private fun askName() {
        val input = EditText(this).apply {
            setText(playerPrefs.name)
            setHint(R.string.name_hint)
            setSingleLine()
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.name_title)
            .setView(input)
            .setPositiveButton(R.string.ok) { _, _ -> playerPrefs.name = input.text.toString() }
            .show()
    }

    private companion object {
        const val STALE_AFTER_MS = 10 * 60 * 1000L // juegos en espera de hace más de 10 min se ocultan
    }
}
