package com.bacancy.ccs2androidhmi.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.databinding.RowItemChargingHistoryBinding
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary
import com.bacancy.ccs2androidhmi.util.visible

class ChargingHistoryListAdapter(var onItemClick: (ChargingSummary) -> Unit) :
    ListAdapter<ChargingSummary, ChargingHistoryListAdapter.SampleViewHolder>(
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

        fun bind(sampleModel: ChargingSummary, position: Int) {
            binding.apply {
                if(position==0){
                    lnrTableHeader.visible()
                }
                tvEvMacAddress.text = sampleModel.evMacAddress
                tvChargingDuration.text = sampleModel.totalChargingTime + " min"
                tvChargingStartTime.text = sampleModel.chargingStartTime
                tvChargingEndTime.text = sampleModel.chargingEndTime
                tvStartSoC.text = sampleModel.startSoc + "%"
                tvEndSoC.text = sampleModel.endSoc + "%"
                tvEnergyConsumption.text = sampleModel.energyConsumption + " kwh"
                tvSessionEndReason.text = sampleModel.sessionEndReason
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChargingSummary>() {
        override fun areItemsTheSame(oldItem: ChargingSummary, newItem: ChargingSummary): Boolean {
            return oldItem.summaryId == newItem.summaryId
        }

        override fun areContentsTheSame(
            oldItem: ChargingSummary,
            newItem: ChargingSummary
        ): Boolean {
            return oldItem == newItem
        }

    }
}