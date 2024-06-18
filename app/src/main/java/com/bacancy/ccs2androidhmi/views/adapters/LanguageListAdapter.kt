package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.databinding.ItemRadioButtonBinding
import com.bacancy.ccs2androidhmi.models.Language

class LanguageListAdapter(private val data: List<Language>,
                          private val onItemClickListener: (Language) -> Unit) :
    RecyclerView.Adapter<LanguageListAdapter.ViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRadioButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.radioButton.text = item.name
        holder.binding.tvLanguageDefault.text = "(${item.defaultName})"
        holder.binding.radioButton.isChecked = item.isSelected

        holder.binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (selectedPosition >= 0) {
                    data[selectedPosition].isSelected = false
                }
                selectedPosition = position
                item.isSelected = true
                notifyItemChanged(selectedPosition)
                if (selectedPosition != position) {
                    notifyItemChanged(position)
                }
                onItemClickListener(item)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(val binding: ItemRadioButtonBinding) : RecyclerView.ViewHolder(binding.root)

}