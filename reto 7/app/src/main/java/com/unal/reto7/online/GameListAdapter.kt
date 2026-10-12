package com.unal.reto7.online

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unal.reto7.R
import com.unal.reto7.databinding.ItemGameBinding

/** Muestra los juegos en espera; tocar uno equivale a unirse. */
class GameListAdapter(
    private val onClick: (OnlineGame) -> Unit
) : RecyclerView.Adapter<GameListAdapter.Holder>() {

    private var items: List<OnlineGame> = emptyList()

    fun submit(games: List<OnlineGame>) {
        items = games
        notifyDataSetChanged() // la lista es corta (solo juegos en espera); no justifica DiffUtil
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class Holder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(game: OnlineGame) {
            binding.gameHost.text =
                binding.root.context.getString(R.string.lobby_host_format, game.hostName.ifBlank { "?" })
            binding.root.setOnClickListener { onClick(game) }
        }
    }
}
