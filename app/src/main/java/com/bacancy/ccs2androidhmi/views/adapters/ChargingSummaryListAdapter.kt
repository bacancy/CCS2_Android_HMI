package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.databinding.RowItemChargingSummaryBinding
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory

class ChargingSummaryListAdapter(var onItemClick: (TbChargingHistory) -> Unit): ListAdapter<TbChargingHistory, ChargingSummaryListAdapter.SampleViewHolder>(
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

        fun bind(sampleModel: TbChargingHistory){
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




    class DiffCallback : DiffUtil.ItemCallback<TbChargingHistory>(){
        override fun areItemsTheSame(oldItem: TbChargingHistory, newItem: TbChargingHistory): Boolean {
            return oldItem.summaryId == newItem.summaryId
        }

        override fun areContentsTheSame(oldItem: TbChargingHistory, newItem: TbChargingHistory): Boolean {
            return oldItem == newItem
        }

    }
}