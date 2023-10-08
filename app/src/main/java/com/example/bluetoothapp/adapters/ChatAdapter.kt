package com.example.bluetoothapp.adapters

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothapp.R
import com.example.bluetoothapp.classes.Chat

class ChatAdapter(private val list: List<Chat>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val time: TextView = itemView.findViewById(R.id.chat_time)
        val boxChat: LinearLayout = itemView.findViewById(R.id.chat_box_ctn)
        val bubbleChat: Button = itemView.findViewById(R.id.box_chat_bubble)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_chat_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = list[position]

        if (chat.status == 0) {
            holder.boxChat.gravity = Gravity.END
            holder.bubbleChat.setBackgroundColor(Color.parseColor("#19C37D"))
            holder.bubbleChat.setTextColor(Color.WHITE)
        } else {
            holder.boxChat.gravity = Gravity.START
            holder.bubbleChat.setBackgroundColor(Color.WHITE)
            holder.bubbleChat.setTextColor(Color.BLACK)
        }

        holder.time.text = chat.time
        holder.bubbleChat.text = chat.content
    }
}
