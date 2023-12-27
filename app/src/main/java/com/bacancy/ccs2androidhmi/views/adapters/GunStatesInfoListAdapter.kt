package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.RowItemGunStatesInfoBinding
import com.bacancy.ccs2androidhmi.models.GunStatesInfo

class GunStatesInfoListAdapter(var onItemClick: (GunStatesInfo) -> Unit) :
    ListAdapter<GunStatesInfo, GunStatesInfoListAdapter.SampleViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        val binding = RowItemGunStatesInfoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SampleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SampleViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, position)
    }

    inner class SampleViewHolder(private val binding: RowItemGunStatesInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {

                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val sampleModel = getItem(position)
                        bind(sampleModel, position)
                        onItemClick(sampleModel)
                    }
                }
            }
        }

        fun bind(sampleModel: GunStatesInfo, position: Int) {
            binding.apply {

                when(sampleModel.gunStateMode){
                    "White" -> {
                        tvGunStateLabel.setTextColor(itemView.resources.getColor(R.color.white))
                    }
                    "Blue" -> {
                        tvGunStateLabel.setTextColor(itemView.resources.getColor(R.color.sky_blue))
                    }
                    "Red" -> {
                        tvGunStateLabel.setTextColor(itemView.resources.getColor(R.color.red))
                    }
                    "Yellow" -> {
                        tvGunStateLabel.setTextColor(itemView.resources.getColor(R.color.yellow))
                    }
                    "Green" -> {
                        tvGunStateLabel.setTextColor(itemView.resources.getColor(R.color.green))
                    }
                }

                tvGunStateLabel.text = sampleModel.gunStateLabel + " - "
                tvGunStateValue.text = sampleModel.gunStateValue

            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GunStatesInfo>() {
        override fun areItemsTheSame(oldItem: GunStatesInfo, newItem: GunStatesInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: GunStatesInfo,
            newItem: GunStatesInfo
        ): Boolean {
            return oldItem == newItem
        }

    }
}