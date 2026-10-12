package com.unal.reto7.online

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

/** Implementación de [GameRepository] sobre Firebase Realtime Database + Auth anónima. */
class FirebaseGameRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : GameRepository {

    private val games: DatabaseReference = database.getReference("games")

    override fun signIn(onResult: (String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            onResult(user.uid)
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { onResult(it.user?.uid) }
            .addOnFailureListener { onResult(null) }
    }

    override fun observeWaitingGames(
        onChange: (List<OnlineGame>) -> Unit,
        onError: () -> Unit
    ): Subscription {
        val query = games.orderByChild("status").equalTo(OnlineGameRules.STATUS_WAITING)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onChange(snapshot.children.mapNotNull { it.toGame() })
            }

            override fun onCancelled(error: DatabaseError) = onError()
        }
        query.addValueEventListener(listener)
        return Subscription { query.removeEventListener(listener) }
    }

    override fun createGame(hostId: String, hostName: String, onResult: (String?) -> Unit) {
        val ref = games.push()
        val data = mapOf(
            "hostId" to hostId,
            "hostName" to hostName,
            "guestId" to "",
            "guestName" to "",
            "board" to OnlineGameRules.EMPTY_BOARD,
            "turn" to OnlineGameRules.HOST_MARK,
            "firstMark" to OnlineGameRules.HOST_MARK,
            "status" to OnlineGameRules.STATUS_WAITING,
            "result" to 0,
            "createdAt" to ServerValue.TIMESTAMP,
            "hostOnline" to true,
            "guestOnline" to false
        )
        ref.setValue(data)
            .addOnSuccessListener {
                // Si el anfitrión se cae mientras espera, el juego desaparece de la lista.
                ref.onDisconnect().removeValue()
                onResult(ref.key)
            }
            .addOnFailureListener { onResult(null) }
    }

    override fun joinGame(
        gameId: String,
        playerId: String,
        playerName: String,
        onResult: (Boolean) -> Unit
    ) = transact(gameId, { OnlineGameRules.join(it, playerId, playerName) }, onResult)

    override fun observeGame(gameId: String, onChange: (OnlineGame?) -> Unit): Subscription {
        val ref = games.child(gameId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) = onChange(snapshot.toGame())
            override fun onCancelled(error: DatabaseError) = onChange(null)
        }
        ref.addValueEventListener(listener)
        return Subscription { ref.removeEventListener(listener) }
    }

    override fun makeMove(gameId: String, playerId: String, cell: Int, onResult: (Boolean) -> Unit) =
        transact(gameId, { OnlineGameRules.applyMove(it, playerId, cell) }, onResult)

    override fun requestRematch(gameId: String, playerId: String, onResult: (Boolean) -> Unit) =
        transact(gameId, { OnlineGameRules.rematch(it, playerId) }, onResult)

    override fun setPresence(gameId: String, isHost: Boolean) {
        val flag = games.child(gameId).child(if (isHost) "hostOnline" else "guestOnline")
        // Primero se registra lo que pasará al desconectarse y luego se marca en línea.
        flag.onDisconnect().setValue(false).addOnCompleteListener { flag.setValue(true) }
    }

    override fun leaveGame(gameId: String, playerId: String) {
        val ref = games.child(gameId)
        ref.onDisconnect().cancel()
        // Si estaba en curso se marca `abandoned` para avisar al rival; en cualquier otro caso
        // (waiting, finished, abandoned) la transacción se aborta y el nodo se borra.
        transact(gameId, { OnlineGameRules.abandon(it, playerId) }) { abandoned ->
            if (!abandoned) ref.removeValue()
        }
    }

    // Ejecuta [update] sobre el valor ACTUAL del servidor. Si devuelve null se aborta (acción ilegal).
    private fun transact(
        gameId: String,
        update: (OnlineGame) -> OnlineGame?,
        onDone: (Boolean) -> Unit
    ) {
        games.child(gameId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(data: MutableData): Transaction.Result {
                // La 1.ª pasada puede llegar con data == null (caché vacía): se devuelve tal cual
                // y Firebase reintenta con el valor real del servidor.
                val current = data.getValue(OnlineGame::class.java) ?: return Transaction.success(data)
                val next = update(current) ?: return Transaction.abort()
                data.value = next
                return Transaction.success(data)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                onDone(error == null && committed && snapshot?.exists() == true)
            }
        })
    }

    private fun DataSnapshot.toGame(): OnlineGame? =
        getValue(OnlineGame::class.java)?.copy(id = key.orEmpty())
}
