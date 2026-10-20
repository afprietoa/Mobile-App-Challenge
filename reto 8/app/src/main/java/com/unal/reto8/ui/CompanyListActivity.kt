package com.unal.reto8.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.unal.reto8.R
import com.unal.reto8.data.CompanyDbHelper
import com.unal.reto8.data.CompanyFilter
import com.unal.reto8.data.CompanyRepository
import com.unal.reto8.data.SqliteCompanyRepository
import com.unal.reto8.databinding.ActivityCompanyListBinding
import com.unal.reto8.model.Classification
import com.unal.reto8.model.Company
import java.util.concurrent.Executors

class CompanyListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyListBinding
    private lateinit var repository: CompanyRepository
    private lateinit var adapter: CompanyAdapter
    private val io = Executors.newSingleThreadExecutor()
    private var filter = CompanyFilter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompanyListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        repository = SqliteCompanyRepository(CompanyDbHelper(applicationContext))
        adapter = CompanyAdapter(onClick = ::openForm, onDelete = ::confirmDelete)
        binding.companiesList.layoutManager = LinearLayoutManager(this)
        binding.companiesList.adapter = adapter
        binding.fabAdd.setOnClickListener { openForm(null) }

        binding.inputSearch.doAfterTextChanged { onFilterChanged() }
        listOf(binding.chipConsulting, binding.chipCustom, binding.chipFactory).forEach {
            it.setOnCheckedChangeListener { _, _ -> onFilterChanged() }
        }
    }

    override fun onStart() {
        super.onStart()
        // Tras rotar, el texto y los chips ya se restauraron pero `filter` de esta instancia está vacío.
        filter = currentFilter()
        reload() // recarga al volver del formulario
    }

    override fun onDestroy() {
        super.onDestroy()
        io.shutdown()
    }

    /** La UI es la única fuente de verdad del filtro. */
    private fun currentFilter() = CompanyFilter(
        nameQuery = binding.inputSearch.text?.toString().orEmpty(),
        classificationMask = Classification.toMask(buildList {
            if (binding.chipConsulting.isChecked) add(Classification.CONSULTING)
            if (binding.chipCustom.isChecked) add(Classification.CUSTOM_DEVELOPMENT)
            if (binding.chipFactory.isChecked) add(Classification.SOFTWARE_FACTORY)
        })
    )

    private fun onFilterChanged() {
        filter = currentFilter()
        reload()
    }

    private fun reload() {
        val f = filter
        io.execute {
            val result = repository.search(f)
            runOnUiThread {
                if (isDestroyed || f != filter) return@runOnUiThread // resultado obsoleto
                adapter.submit(result)
                binding.emptyView.isVisible = result.isEmpty()
                binding.emptyView.setText(
                    if (f == CompanyFilter()) R.string.list_empty else R.string.list_empty_filtered
                )
            }
        }
    }

    private fun confirmDelete(company: Company) {
        DeleteConfirmation.show(this, company) {
            io.execute {
                repository.delete(company.id) // si ya no existía, igual se refresca la lista
                runOnUiThread {
                    if (isDestroyed) return@runOnUiThread
                    Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
                    reload()
                }
            }
        }
    }

    private fun openForm(company: Company?) {
        startActivity(
            Intent(this, CompanyFormActivity::class.java)
                .putExtra(CompanyFormActivity.EXTRA_COMPANY_ID, company?.id ?: 0L)
        )
    }
}
