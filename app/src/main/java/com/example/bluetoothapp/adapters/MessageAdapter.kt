package com.example.bluetoothapp.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothapp.R
import com.example.bluetoothapp.classes.Message

class MessageAdapter(private val list: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private var onItemClickListener: View.OnClickListener? = null

    fun setOnItemClickListener(listener: View.OnClickListener) {
        this.onItemClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.message_item_avatar)
        val name: TextView = itemView.findViewById(R.id.message_item_name)
        val content: TextView = itemView.findViewById(R.id.message_item_content)
        val time: TextView = itemView.findViewById(R.id.message_item_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_message_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]
        holder.name.text = currentItem.name
        holder.content.text = currentItem.content
        holder.time.text = currentItem.time
        if(currentItem.image != "DEFAULT") //Different Default
        {
            val imageBytes = android.util.Base64.decode(currentItem.image, android.util.Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.image.setImageBitmap(decodedImage)
        } else
        {
            holder.image.setImageResource(R.drawable.ic_account)
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.onClick(holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
