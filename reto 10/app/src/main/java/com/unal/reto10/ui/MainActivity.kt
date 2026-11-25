package com.unal.reto10.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unal.reto10.R
import com.unal.reto10.data.ServiceLocator
import com.unal.reto10.databinding.ActivityMainBinding
import com.unal.reto10.model.ErrorKind
import com.unal.reto10.model.LoadState

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter = PointAdapter { DetailActivity.start(this, it) }

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory { initializer { MainViewModel(ServiceLocator.provide()) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ServiceLocator.isOnline = ::isOnline

        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !rv.canScrollVertically(1)) viewModel.loadMore()
            }
        })

        binding.searchButton.setOnClickListener { submit() }
        binding.textInput.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) { submit(); true } else false
        }
        binding.clearButton.setOnClickListener {
            binding.textInput.setText("")
            binding.municipalityInput.setText(getString(R.string.filter_all), false)
            hideKeyboard()
            viewModel.clearFilters()
        }
        binding.retryButton.setOnClickListener { viewModel.retry() }

        viewModel.municipalities.observe(this) { list ->
            val options = listOf(getString(R.string.filter_all)) + list
            binding.municipalityInput.setAdapter(
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
            )
        }
        viewModel.state.observe(this, ::render)

        viewModel.loadMunicipalities()
        if (savedInstanceState == null && viewModel.state.value == LoadState.Idle) submit()
    }

    private fun submit() {
        hideKeyboard()
        val selected = binding.municipalityInput.text.toString()
        val municipality = selected.takeIf { it.isNotBlank() && it != getString(R.string.filter_all) }
        viewModel.search(municipality, binding.textInput.text?.toString().orEmpty())
    }

    private fun render(state: LoadState) {
        binding.progress.visibility = View.GONE
        binding.progressMore.visibility = View.GONE
        binding.messageBox.visibility = View.GONE
        binding.retryButton.visibility = View.GONE
        binding.counter.visibility = View.GONE
        when (state) {
            LoadState.Idle -> adapter.submitList(emptyList())
            LoadState.Loading -> {
                adapter.submitList(emptyList())
                binding.progress.visibility = View.VISIBLE
            }
            is LoadState.Success -> {
                adapter.submitList(state.items)
                binding.counter.visibility = View.VISIBLE
                binding.counter.text = getString(R.string.results_count, state.items.size, state.total)
                binding.progressMore.visibility = if (state.loadingMore) View.VISIBLE else View.GONE
            }
            LoadState.Empty -> {
                adapter.submitList(emptyList())
                showMessage(getString(R.string.empty_results), retry = false)
            }
            is LoadState.Error -> {
                adapter.submitList(emptyList())
                showMessage(getString(errorText(state.kind)), retry = true)
            }
        }
    }

    private fun showMessage(text: String, retry: Boolean) {
        binding.message.text = text
        binding.messageBox.visibility = View.VISIBLE
        binding.retryButton.visibility = if (retry) View.VISIBLE else View.GONE
    }

    private fun errorText(kind: ErrorKind) = when (kind) {
        ErrorKind.NO_NETWORK -> R.string.error_no_network
        ErrorKind.TIMEOUT -> R.string.error_timeout
        ErrorKind.SERVER -> R.string.error_server
        ErrorKind.BAD_JSON -> R.string.error_bad_json
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        currentFocus?.clearFocus()
    }

    private fun isOnline(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
