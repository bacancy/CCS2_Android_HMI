package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.databinding.RowItemAcMeterInfoBinding
import com.bacancy.ccs2androidhmi.models.GunsDCMeterModel

class GunsDCMeterListAdapter(var onItemClick: (GunsDCMeterModel) -> Unit): ListAdapter<GunsDCMeterModel, GunsDCMeterListAdapter.SampleViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        val binding = RowItemAcMeterInfoBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return SampleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SampleViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class SampleViewHolder(private val binding: RowItemAcMeterInfoBinding): RecyclerView.ViewHolder(binding.root) {
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

        fun bind(sampleModel: GunsDCMeterModel){
            binding.apply {
                incVoltageL1.tvLabel.text = "Voltage"
                incVoltageL1.tvValue.text = sampleModel.voltage.formatFloatToString()

                incVoltageL2.tvLabel.text = "Current"
                incVoltageL2.tvValue.text = sampleModel.current.formatFloatToString()

                incVoltageL3.tvLabel.text = "Power"
                incVoltageL3.tvValue.text = sampleModel.power.formatFloatToString()

                incVoltageAvg.tvLabel.text = "Import Energy"
                incVoltageAvg.tvValue.text = sampleModel.importEnergy.formatFloatToString()

                incCurrentL1.tvLabel.text = "Export Energy"
                incCurrentL1.tvValue.text = sampleModel.exportEnergy.formatFloatToString()

                incCurrentL2.tvLabel.text = "Max Voltage"
                incCurrentL2.tvValue.text = sampleModel.maxVoltage.formatFloatToString()

                incCurrentL3.tvLabel.text = "Min Voltage"
                incCurrentL3.tvValue.text = sampleModel.minVoltage.formatFloatToString()

                incCurrentAvg.tvLabel.text = "Max Current"
                incCurrentAvg.tvValue.text = sampleModel.maxCurrent.formatFloatToString()

                incTotalKW.tvLabel.text = "Min Current"
                incTotalKW.tvValue.text = sampleModel.minCurrent.formatFloatToString()

            }
        }
    }

    fun Float.formatFloatToString(): String{
        return String.format("%.2f", this)
    }


    class DiffCallback : DiffUtil.ItemCallback<GunsDCMeterModel>(){
        override fun areItemsTheSame(oldItem: GunsDCMeterModel, newItem: GunsDCMeterModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GunsDCMeterModel, newItem: GunsDCMeterModel): Boolean {
            return oldItem == newItem
        }

    }
}