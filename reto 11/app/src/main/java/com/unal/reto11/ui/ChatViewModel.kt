package com.unal.reto11.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unal.reto11.data.ChatRepository
import com.unal.reto11.data.ChatStore
import com.unal.reto11.model.ChatError
import com.unal.reto11.model.ChatException
import com.unal.reto11.model.ChatState
import com.unal.reto11.model.Message
import com.unal.reto11.model.Role
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ChatViewModel(
    private val repository: ChatRepository,
    private val store: ChatStore,
    private val executor: Executor = Executors.newSingleThreadExecutor(),
    /** Entrega resultados al hilo principal; los tests inyectan una versión síncrona. */
    private val deliver: (Runnable) -> Unit = { Handler(Looper.getMainLooper()).post(it) }
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>(store.loadMessages())
    val messages: LiveData<List<Message>> = _messages

    private val _state = MutableLiveData<ChatState>(ChatState.Idle)
    val state: LiveData<ChatState> = _state

    private var nextId = (store.loadMessages().maxOfOrNull { it.id } ?: 0L) + 1
    private var requestCounter = 0

    var systemPrompt: String
        get() = store.systemPrompt
        set(v) { store.systemPrompt = v }

    var temperature: Double
        get() = store.temperature
        set(v) { store.temperature = v.coerceIn(0.0, 1.0) }

    private fun current(): List<Message> = _messages.value.orEmpty()

    /** Envía [text]; ignora vacíos y no permite solapar solicitudes. */
    fun send(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || _state.value == ChatState.Sending) return
        // Un error previo visible no debe quedar en la conversación al reintentar.
        val base = current().filter { !it.isError }
        val updated = base + Message(nextId++, Role.USER, clean)
        publish(updated)
        request(updated)
    }

    /** Reintenta la última pregunta tras un error. */
    fun retryLast() {
        if (_state.value == ChatState.Sending) return
        val base = current().filter { !it.isError }
        if (base.lastOrNull()?.role != Role.USER) return
        publish(base)
        request(base)
    }

    fun cancel() {
        if (_state.value != ChatState.Sending) return
        requestCounter++
        repository.cancel()
        _state.value = ChatState.Idle
    }

    fun newConversation() {
        requestCounter++
        repository.cancel()
        publish(emptyList())
        _state.value = ChatState.Idle
    }

    private fun request(history: List<Message>) {
        _state.value = ChatState.Sending
        val ticket = ++requestCounter
        val system = systemPrompt
        val temp = temperature
        executor.execute {
            val result = repository.send(history, system, temp)
            deliver(Runnable { onResult(ticket, result) })
        }
    }

    private fun onResult(ticket: Int, result: Result<com.unal.reto11.model.ChatReply>) {
        if (ticket != requestCounter) return // respuesta obsoleta
        result.onSuccess {
            val id = nextId++
            val path = it.image?.let { bytes -> store.saveImage(id, bytes) }
            val text = if (it.image != null && path == null) "No se pudo guardar la imagen." else it.text
            publish(current() + Message(id, Role.MODEL, text, imagePath = path))
            _state.value = ChatState.Idle
        }.onFailure {
            val error = (it as? ChatException)?.error ?: ChatError.Server
            _state.value = ChatState.Failed(error)
        }
    }

    private fun publish(list: List<Message>) {
        _messages.value = list
        store.saveMessages(list)
    }

    override fun onCleared() {
        repository.cancel()
        (executor as? java.util.concurrent.ExecutorService)?.shutdownNow()
    }
}
