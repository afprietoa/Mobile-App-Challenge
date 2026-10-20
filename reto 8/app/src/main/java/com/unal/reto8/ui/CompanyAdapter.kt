package com.unal.reto8.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.unal.reto8.R
import com.unal.reto8.databinding.ItemCompanyBinding
import com.unal.reto8.model.Company

class CompanyAdapter(
    private val onClick: (Company) -> Unit,
    private val onDelete: (Company) -> Unit
) : RecyclerView.Adapter<CompanyAdapter.Holder>() {

    private var items: List<Company> = emptyList()

    fun submit(companies: List<Company>) {
        items = companies
        notifyDataSetChanged() // lista corta; no justifica DiffUtil
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemCompanyBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class Holder(private val b: ItemCompanyBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(c: Company) {
            val ctx = b.root.context
            b.companyName.text = c.name
            b.companyClasses.text = c.classifications
                .joinToString(ctx.getString(R.string.class_separator)) { ctx.getString(it.label) }
            b.companyContact.text = listOf(c.phone, c.email).filter { it.isNotBlank() }.joinToString(" · ")
            b.companyContact.isVisible = b.companyContact.text.isNotEmpty()
            b.root.setOnClickListener { onClick(c) }
            b.buttonDelete.setOnClickListener { onDelete(c) }
        }
    }
}
