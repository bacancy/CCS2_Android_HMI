package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.RowItemChargingHistoryBinding
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory

class ChargingHistoryListAdapter(var onItemClick: (TbChargingHistory) -> Unit) :
    ListAdapter<TbChargingHistory, ChargingHistoryListAdapter.SampleViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        val binding = RowItemChargingHistoryBinding.inflate(
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

    inner class SampleViewHolder(private val binding: RowItemChargingHistoryBinding) :
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

        fun bind(sampleModel: TbChargingHistory, position: Int) {
            binding.apply {
                tvEvMacAddress.text = sampleModel.evMacAddress
                tvChargingDuration.text = sampleModel.totalChargingTime + " min"
                tvChargingStartTime.text = sampleModel.chargingStartTime
                tvChargingEndTime.text = sampleModel.chargingEndTime
                tvStartSoC.text = sampleModel.startSoc + "%"
                tvEndSoC.text = sampleModel.endSoc + "%"
                tvEnergyConsumption.text = sampleModel.energyConsumption + " kwh"
                tvSessionEndReason.text = sampleModel.sessionEndReason
                tvTotalCost.text = itemView.context.getString(R.string.lbl_gun_total_cost_in_rs, if(sampleModel.totalCost.isEmpty()) 0.0F else sampleModel.totalCost.toFloat())
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TbChargingHistory>() {
        override fun areItemsTheSame(oldItem: TbChargingHistory, newItem: TbChargingHistory): Boolean {
            return oldItem.summaryId == newItem.summaryId
        }

        override fun areContentsTheSame(
            oldItem: TbChargingHistory,
            newItem: TbChargingHistory
        ): Boolean {
            return oldItem == newItem
        }

    }
}