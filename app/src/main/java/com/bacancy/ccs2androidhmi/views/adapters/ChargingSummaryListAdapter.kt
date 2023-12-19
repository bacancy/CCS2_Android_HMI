package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.databinding.RowItemChargingSummaryBinding
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary

class ChargingSummaryListAdapter(var onItemClick: (ChargingSummary) -> Unit): ListAdapter<ChargingSummary, ChargingSummaryListAdapter.SampleViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        val binding = RowItemChargingSummaryBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return SampleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SampleViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class SampleViewHolder(private val binding: RowItemChargingSummaryBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION){
                        val sampleModel = getItem(position)
                        bind(sampleModel)
                        onItemClick(sampleModel)
                    }
                }
            }
        }

        fun bind(sampleModel: ChargingSummary){
            binding.apply {

                tvEvMacAddress.text = "EV Mac Address = ${sampleModel.evMacAddress}"
                tvChargingStartTime.text = "Charging Start Time = ${sampleModel.chargingStartTime}"
                tvChargingEndTime.text = "Charging End Time = ${sampleModel.chargingEndTime}"
                tvSessionEndReason.text = sampleModel.sessionEndReason
                tvChargingDuration.text = "Charging Duration = ${sampleModel.totalChargingTime} mins"
                tvStartSoC.text = "Start SoC = ${sampleModel.startSoc}%"
                tvEndSoC.text = "End SoC = ${sampleModel.endSoc}%"

            }
        }
    }




    class DiffCallback : DiffUtil.ItemCallback<ChargingSummary>(){
        override fun areItemsTheSame(oldItem: ChargingSummary, newItem: ChargingSummary): Boolean {
            return oldItem.summaryId == newItem.summaryId
        }

        override fun areContentsTheSame(oldItem: ChargingSummary, newItem: ChargingSummary): Boolean {
            return oldItem == newItem
        }

    }
}