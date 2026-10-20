package com.unal.reto8.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import com.unal.reto8.R
import com.unal.reto8.data.CompanyDbHelper
import com.unal.reto8.data.CompanyRepository
import com.unal.reto8.data.SqliteCompanyRepository
import com.unal.reto8.databinding.ActivityCompanyFormBinding
import com.unal.reto8.model.Classification
import com.unal.reto8.model.Company
import com.unal.reto8.model.CompanyValidator
import com.unal.reto8.model.Field
import com.unal.reto8.model.FieldError
import java.util.concurrent.Executors

/** Crea una empresa nueva o edita una existente (si llega [EXTRA_COMPANY_ID] distinto de 0). */
class CompanyFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyFormBinding
    private lateinit var repository: CompanyRepository
    private val io = Executors.newSingleThreadExecutor()
    private var companyId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompanyFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            insets
        }

        repository = SqliteCompanyRepository(CompanyDbHelper(applicationContext))
        companyId = intent.getLongExtra(EXTRA_COMPANY_ID, 0L)
        setTitle(if (companyId == 0L) R.string.form_title_new else R.string.form_title_edit)

        binding.buttonSave.setOnClickListener { save() }

        // Al rotar, las vistas con id restauran lo escrito: solo se carga de la BD la primera vez.
        if (companyId != 0L && savedInstanceState == null) loadCompany()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        io.shutdown()
    }

    private fun loadCompany() {
        val id = companyId
        io.execute {
            val company = repository.getById(id)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                if (company == null) {
                    Toast.makeText(this, R.string.load_failed, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    fill(company)
                }
            }
        }
    }

    private fun fill(c: Company) = with(binding) {
        inputName.setText(c.name)
        inputWebsite.setText(c.website)
        inputPhone.setText(c.phone)
        inputEmail.setText(c.email)
        inputProducts.setText(c.products)
        val selected = c.classifications
        checkConsulting.isChecked = Classification.CONSULTING in selected
        checkCustom.isChecked = Classification.CUSTOM_DEVELOPMENT in selected
        checkFactory.isChecked = Classification.SOFTWARE_FACTORY in selected
    }

    private fun readForm(): Company = with(binding) {
        Company(
            id = companyId,
            name = inputName.text?.toString().orEmpty().trim(),
            website = CompanyValidator.normalizeUrl(inputWebsite.text?.toString().orEmpty()),
            phone = inputPhone.text?.toString().orEmpty().trim(),
            email = inputEmail.text?.toString().orEmpty().trim(),
            products = inputProducts.text?.toString().orEmpty().trim(),
            classification = Classification.toMask(buildList {
                if (checkConsulting.isChecked) add(Classification.CONSULTING)
                if (checkCustom.isChecked) add(Classification.CUSTOM_DEVELOPMENT)
                if (checkFactory.isChecked) add(Classification.SOFTWARE_FACTORY)
            })
        )
    }

    private fun save() {
        val company = readForm()
        val errors = CompanyValidator.validate(company)
        showErrors(errors)
        if (errors.isNotEmpty()) return

        binding.buttonSave.isEnabled = false
        io.execute {
            val ok = if (company.id == 0L) repository.insert(company) != -1L else repository.update(company)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                if (ok) {
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.buttonSave.isEnabled = true
                    Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showErrors(errors: Map<Field, FieldError>) {
        setError(binding.tilName, errors[Field.NAME])
        setError(binding.tilWebsite, errors[Field.WEBSITE])
        setError(binding.tilPhone, errors[Field.PHONE])
        setError(binding.tilEmail, errors[Field.EMAIL])
        binding.classError.isVisible = Field.CLASSIFICATION in errors
    }

    private fun setError(layout: TextInputLayout, error: FieldError?) {
        layout.error = when (error) {
            FieldError.REQUIRED -> getString(R.string.error_required)
            FieldError.INVALID -> getString(R.string.error_invalid)
            null -> null
        }
    }

    companion object {
        const val EXTRA_COMPANY_ID = "company_id"
    }
}
