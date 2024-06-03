package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.RowItemErrorCodesBinding
import com.bacancy.ccs2androidhmi.models.ErrorCodes

class ErrorCodesListAdapter(var onItemClick: (ErrorCodes) -> Unit) :
    ListAdapter<ErrorCodes, ErrorCodesListAdapter.SampleViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        val binding = RowItemErrorCodesBinding.inflate(
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

    inner class SampleViewHolder(private val binding: RowItemErrorCodesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {

               /* root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val sampleModel = getItem(position)
                        bind(sampleModel, position)
                        onItemClick(sampleModel)
                    }
                }*/
            }
        }

        fun bind(sampleModel: ErrorCodes, position: Int) {
            binding.apply {
                tvSerialNo.text = (sampleModel.id).toString()
                tvErrorDateTime.text = sampleModel.errorCodeDateTime
                tvErrorCodeName.text = sampleModel.errorCodeName

                if (sampleModel.errorCodeValue == 1) {
                    tvErrorCodeStatus.text = "Occurred"
                    tvErrorCodeStatus.setBackgroundResource(R.drawable.bg_red_rect_with_white_border)
                } else {
                    tvErrorCodeStatus.text = "Resolved"
                    tvErrorCodeStatus.setBackgroundResource(R.drawable.bg_green_rect_with_white_border)
                }
                tvErrorCodeSource.text = sampleModel.errorCodeSource
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ErrorCodes>() {
        override fun areItemsTheSame(oldItem: ErrorCodes, newItem: ErrorCodes): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ErrorCodes,
            newItem: ErrorCodes
        ): Boolean {
            return oldItem == newItem
        }

    }
}