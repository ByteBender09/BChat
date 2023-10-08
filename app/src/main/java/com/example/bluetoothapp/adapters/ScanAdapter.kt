package com.example.bluetoothapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothapp.R
import com.example.bluetoothapp.classes.Device

class ScanAdapter(private val list: List<Device>) : RecyclerView.Adapter<ScanAdapter.ViewHolder>() {
    private var onItemClickListener: View.OnClickListener? = null

    fun setOnItemClickListener(listener: View.OnClickListener) {
        this.onItemClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.scan_item_name)
        val address: TextView = itemView.findViewById(R.id.scan_item_address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_scan_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]
        holder.name.text = currentItem.name
        holder.address.text = currentItem.address

        holder.itemView.setOnClickListener {
            onItemClickListener?.onClick(holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
