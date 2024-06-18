package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.databinding.RowItemSettingsBinding

class AppSettingsAdapter(private val data: List<Pair<String,String>>,
                         private val onItemClickListener: (Pair<String,String>) -> Unit) :
    RecyclerView.Adapter<AppSettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowItemSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.tvSettingsTitle.text = item.second
        holder.binding.tvSettingsTitle.setOnClickListener {
            onItemClickListener(item)
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(val binding: RowItemSettingsBinding) : RecyclerView.ViewHolder(binding.root)

}