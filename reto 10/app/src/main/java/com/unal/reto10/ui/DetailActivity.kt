package com.unal.reto10.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unal.reto10.R
import com.unal.reto10.databinding.ActivityDetailBinding
import com.unal.reto10.model.CollectionPoint

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ViewCompat.setOnApplyWindowInsetsListener(binding.detailRoot) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val point = IntentCompat.getSerializableExtra(intent, EXTRA_POINT, CollectionPoint::class.java)
        if (point == null) { finish(); return }

        val na = getString(R.string.detail_not_available)
        fun v(s: String?) = s?.takeIf { it.isNotBlank() } ?: na
        binding.name.text = point.businessName
        binding.fields.text = listOf(
            R.string.detail_address to point.address,
            R.string.detail_municipality to point.municipality,
            R.string.detail_department to point.department,
            R.string.detail_program to point.program,
            R.string.detail_waste to point.wasteTypes,
            R.string.detail_dane to point.daneCode
        ).joinToString("\n\n") { (label, value) -> "${getString(label)}\n${v(value)}" }

        val query = listOf(point.address, point.municipality, point.department)
            .filter { it.isNotBlank() }.joinToString(", ")
        binding.directionsButton.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query))))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, R.string.no_maps_app, Toast.LENGTH_SHORT).show()
            }
        }
        binding.copyButton.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("address", query))
            Toast.makeText(this, R.string.address_copied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    companion object {
        private const val EXTRA_POINT = "point"
        fun start(context: Context, point: CollectionPoint) {
            context.startActivity(Intent(context, DetailActivity::class.java).putExtra(EXTRA_POINT, point))
        }
    }
}
