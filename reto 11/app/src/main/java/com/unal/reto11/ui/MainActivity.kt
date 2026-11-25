package com.unal.reto11.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.unal.reto11.R
import com.unal.reto11.data.GeminiChatRepository
import com.unal.reto11.data.GeminiConfig
import com.unal.reto11.data.PrefsChatStore
import com.unal.reto11.data.PromptTemplate
import com.unal.reto11.databinding.ActivityMainBinding
import com.unal.reto11.databinding.DialogSettingsBinding
import com.unal.reto11.model.ChatError
import com.unal.reto11.model.ChatState
import com.unal.reto11.model.Message

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MessageAdapter

    private val viewModel: ChatViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ChatViewModel(GeminiChatRepository(applicationContext), PrefsChatStore(applicationContext)) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupToolbar()
        setupList()
        setupInput()
        observe()
        binding.keyNotice.visibility = if (GeminiConfig.apiKey.isBlank()) View.VISIBLE else View.GONE
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.main_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_new_chat -> viewModel.newConversation()
                R.id.action_templates -> showTemplates()
                R.id.action_settings -> showSettings()
                else -> return@setOnMenuItemClickListener false
            }
            true
        }
    }

    private fun setupList() {
        adapter = MessageAdapter { showMessageActions(it) }
        binding.messageList.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.messageList.adapter = adapter
    }

    private fun setupInput() {
        binding.sendButton.setOnClickListener {
            if (viewModel.state.value == ChatState.Sending) viewModel.cancel() else submit()
        }
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { submit(); true } else false
        }
        binding.retryButton.setOnClickListener { viewModel.retryLast() }
        listOf(binding.chip1, binding.chip2, binding.chip3).forEach { chip ->
            chip.setOnClickListener { viewModel.send(chip.text.toString()) }
        }
    }

    private fun submit() {
        val text = binding.input.text?.toString().orEmpty()
        if (text.isBlank()) return
        viewModel.send(text)
        binding.input.text?.clear()
        hideKeyboard()
    }

    private fun observe() {
        viewModel.messages.observe(this) { list ->
            adapter.submitList(list) {
                if (list.isNotEmpty()) binding.messageList.scrollToPosition(list.size - 1)
            }
            binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.state.observe(this) { state ->
            val sending = state == ChatState.Sending
            binding.typing.visibility = if (sending) View.VISIBLE else View.GONE
            binding.sendButton.setText(if (sending) R.string.action_stop else R.string.action_send)
            if (state is ChatState.Failed) {
                binding.errorBar.visibility = View.VISIBLE
                binding.errorText.setText(errorMessage(state.error))
                binding.retryButton.visibility =
                    if (state.error == ChatError.MissingKey) View.GONE else View.VISIBLE
            } else {
                binding.errorBar.visibility = View.GONE
            }
        }
    }

    private fun errorMessage(e: ChatError): Int = when (e) {
        ChatError.NoNetwork -> R.string.error_no_network
        ChatError.Timeout -> R.string.error_timeout
        ChatError.Unauthorized -> R.string.error_unauthorized
        ChatError.RateLimited -> R.string.error_rate_limited
        is ChatError.Blocked -> R.string.error_blocked
        ChatError.Server -> R.string.error_server
        ChatError.BadJson -> R.string.error_bad_json
        ChatError.MissingKey -> R.string.error_missing_key
    }

    private fun showTemplates() {
        val templates = PromptTemplate.entries
        AlertDialog.Builder(this)
            .setTitle(R.string.template_dialog_title)
            .setItems(templates.map { it.label }.toTypedArray()) { _, which -> askTemplateInput(templates[which]) }
            .show()
    }

    private fun askTemplateInput(template: PromptTemplate) {
        val input = EditText(this).apply { hint = template.inputHint; setPadding(48, 32, 48, 32) }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.template_input_title, template.label))
            .setView(input)
            .setPositiveButton(R.string.action_ok) { _, _ ->
                template.apply(input.text.toString())?.let { viewModel.send(it) }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showSettings() {
        val b = DialogSettingsBinding.inflate(LayoutInflater.from(this))
        b.systemPromptInput.setText(viewModel.systemPrompt)
        b.temperatureInput.setText(viewModel.temperature.toString())
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_title)
            .setView(b.root)
            .setPositiveButton(R.string.settings_save) { _, _ ->
                viewModel.systemPrompt = b.systemPromptInput.text.toString()
                b.temperatureInput.text.toString().toDoubleOrNull()?.let { viewModel.temperature = it }
            }
            .setNeutralButton(R.string.settings_reset) { _, _ ->
                viewModel.systemPrompt = GeminiConfig.DEFAULT_SYSTEM_PROMPT
                viewModel.temperature = GeminiConfig.DEFAULT_TEMPERATURE
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showMessageActions(m: Message) {
        AlertDialog.Builder(this)
            .setItems(arrayOf(getString(R.string.action_copy), getString(R.string.action_share))) { _, which ->
                if (which == 0) {
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("respuesta", m.text))
                    Snackbar.make(binding.root, R.string.copied, Snackbar.LENGTH_SHORT).show()
                } else {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, m.text)
                    }
                    startActivity(Intent.createChooser(send, getString(R.string.share_title)))
                }
            }
            .show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.input.windowToken, 0)
    }
}
