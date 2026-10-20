package com.unal.reto8.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.unal.reto8.R
import com.unal.reto8.data.CompanyDbHelper
import com.unal.reto8.data.CompanyFilter
import com.unal.reto8.data.CompanyRepository
import com.unal.reto8.data.SqliteCompanyRepository
import com.unal.reto8.databinding.ActivityCompanyListBinding
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
    }

    override fun onStart() {
        super.onStart()
        reload() // recarga al volver del formulario
    }

    override fun onDestroy() {
        super.onDestroy()
        io.shutdown()
    }

    private fun reload() {
        val f = filter
        io.execute {
            val result = repository.search(f)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
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
