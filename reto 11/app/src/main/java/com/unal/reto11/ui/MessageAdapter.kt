package com.unal.reto11.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unal.reto11.R
import com.unal.reto11.model.Message
import com.unal.reto11.model.Role

class MessageAdapter(
    private val onLongClick: (Message) -> Unit
) : ListAdapter<Message, MessageAdapter.Holder>(Diff) {

    override fun getItemViewType(position: Int) = if (getItem(position).role == Role.USER) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val layout = if (viewType == 0) R.layout.item_message_user else R.layout.item_message_model
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val m = getItem(position)
        holder.text.text = if (m.role == Role.MODEL) MarkdownFormatter.format(m.text) else m.text
        holder.text.setOnLongClickListener { onLongClick(m); true }
        holder.image?.let { iv ->
            val bmp = m.imagePath?.let { decode(it) }
            iv.visibility = if (bmp != null) android.view.View.VISIBLE else android.view.View.GONE
            iv.setImageBitmap(bmp)
            iv.setOnLongClickListener { onLongClick(m); true }
        }
    }

    /** Decodifica reduciendo el tamaño para no gastar memoria de más. */
    private fun decode(path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > MAX_PX) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    class Holder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.messageText)
        val image: ImageView? = view.findViewById(R.id.messageImage)
    }

    private companion object {
        const val MAX_PX = 1024
    }

    private object Diff : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
        override fun areContentsTheSame(a: Message, b: Message) = a == b
    }
}
