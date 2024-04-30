package com.bacancy.ccs2androidhmi.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bacancy.ccs2androidhmi.databinding.RowItemAppNotificationsBinding
import com.bacancy.ccs2androidhmi.db.entity.TbNotifications
import com.bacancy.ccs2androidhmi.models.ErrorCodes

class AppNotificationsListAdapter(var onItemClick: (ErrorCodes) -> Unit) :
    ListAdapter<TbNotifications, AppNotificationsListAdapter.SampleViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        val binding = RowItemAppNotificationsBinding.inflate(
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

    inner class SampleViewHolder(private val binding: RowItemAppNotificationsBinding) :
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

        fun bind(sampleModel: TbNotifications, position: Int) {
            binding.apply {
                tvNotificationMessage.text = sampleModel.notificationMessage
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TbNotifications>() {
        override fun areItemsTheSame(oldItem: TbNotifications, newItem: TbNotifications): Boolean {
            return oldItem.notificationId == newItem.notificationId
        }

        override fun areContentsTheSame(
            oldItem: TbNotifications,
            newItem: TbNotifications
        ): Boolean {
            return oldItem == newItem
        }

    }
}