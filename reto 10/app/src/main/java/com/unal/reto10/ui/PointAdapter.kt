package com.unal.reto10.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unal.reto10.databinding.ItemPointBinding
import com.unal.reto10.model.CollectionPoint

class PointAdapter(
    private val onClick: (CollectionPoint) -> Unit
) : ListAdapter<CollectionPoint, PointAdapter.Holder>(Diff) {

    class Holder(val binding: ItemPointBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemPointBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val p = getItem(position)
        with(holder.binding) {
            name.text = p.businessName
            address.text = listOf(p.address, p.municipality).filter { it.isNotEmpty() }.joinToString(" · ")
            program.text = p.program
            program.visibility = if (p.program.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            waste.text = p.wasteTypes
            waste.visibility = if (p.wasteTypes.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            root.setOnClickListener { onClick(p) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<CollectionPoint>() {
        override fun areItemsTheSame(a: CollectionPoint, b: CollectionPoint) =
            a.businessName == b.businessName && a.address == b.address && a.municipality == b.municipality
        override fun areContentsTheSame(a: CollectionPoint, b: CollectionPoint) = a == b
    }
}
