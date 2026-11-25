package com.unal.reto11

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.unal.reto11.data.ChatRepository
import com.unal.reto11.data.ChatStore
import com.unal.reto11.model.ChatError
import com.unal.reto11.model.ChatException
import com.unal.reto11.model.ChatReply
import com.unal.reto11.model.ChatState
import com.unal.reto11.model.Message
import com.unal.reto11.model.Role
import com.unal.reto11.ui.ChatViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FakeChatRepository : ChatRepository {
    var next: Result<ChatReply> = Result.success(ChatReply("respuesta"))
    var calls = 0
    var lastHistory: List<Message> = emptyList()
    var cancelled = 0
    override fun send(history: List<Message>, systemPrompt: String, temperature: Double): Result<ChatReply> {
        calls++
        lastHistory = history
        return next
    }
    override fun cancel() { cancelled++ }
}

class FakeStore(var saved: List<Message> = emptyList()) : ChatStore {
    override fun loadMessages() = saved
    override fun saveMessages(messages: List<Message>) { saved = messages }
    override var systemPrompt = "sys"
    override var temperature = 0.5
}

class ChatViewModelTest {
    @get:Rule val rule = InstantTaskExecutorRule()

    private val repo = FakeChatRepository()
    private val store = FakeStore()

    private fun vm(executor: java.util.concurrent.Executor = java.util.concurrent.Executor { it.run() },
               deliver: (Runnable) -> Unit = { it.run() }) =
        ChatViewModel(repo, store, executor, deliver)

    @Test fun success_appends_user_and_model_messages() {
        val vm = vm()
        vm.send("hola")
        val list = vm.messages.value!!
        assertEquals(listOf(Role.USER, Role.MODEL), list.map { it.role })
        assertEquals("respuesta", list[1].text)
        assertEquals(ChatState.Idle, vm.state.value)
        assertEquals(list, store.saved)
    }

    @Test fun blank_input_is_ignored() {
        val vm = vm()
        vm.send("   ")
        assertEquals(0, repo.calls)
        assertTrue(vm.messages.value!!.isEmpty())
    }

    @Test fun error_keeps_question_and_retry_succeeds() {
        repo.next = Result.failure(ChatException(ChatError.NoNetwork))
        val vm = vm()
        vm.send("hola")
        assertEquals(ChatState.Failed(ChatError.NoNetwork), vm.state.value)
        assertEquals(1, vm.messages.value!!.size)
        repo.next = Result.success(ChatReply("ok"))
        vm.retryLast()
        assertEquals(2, vm.messages.value!!.size)
        assertEquals(1, vm.messages.value!!.count { it.role == Role.USER })
        assertEquals(ChatState.Idle, vm.state.value)
    }

    @Test fun no_double_send_while_sending() {
        val pending = mutableListOf<Runnable>()
        val vm = vm(executor = { pending.add(it) })
        vm.send("uno")
        vm.send("dos")
        assertEquals(1, vm.messages.value!!.size)
        assertEquals(ChatState.Sending, vm.state.value)
    }

    @Test fun cancel_discards_late_response() {
        val pending = mutableListOf<Runnable>()
        val vm = vm(executor = { pending.add(it) })
        vm.send("uno")
        vm.cancel()
        pending.forEach { it.run() }
        assertEquals(ChatState.Idle, vm.state.value)
        assertEquals(1, vm.messages.value!!.size)
        assertEquals(1, repo.cancelled)
    }

    @Test fun new_conversation_clears() {
        val vm = vm()
        vm.send("hola")
        vm.newConversation()
        assertTrue(vm.messages.value!!.isEmpty())
        assertTrue(store.saved.isEmpty())
    }

    @Test fun restores_history_from_store() {
        val old = listOf(Message(7, Role.USER, "a"), Message(8, Role.MODEL, "b"))
        val vm = ChatViewModel(repo, FakeStore(old), { it.run() }, { it.run() })
        assertEquals(old, vm.messages.value)
        vm.send("c")
        assertEquals(9L, vm.messages.value!![2].id)
        assertEquals(4, repo.lastHistory.size + 1) // historial enviado: a, b, c
    }
}
