package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentAppNotificationsBinding
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.adapters.AppNotificationsListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppNotificationsFragment : BaseFragment() {

    private lateinit var appNotificationsListAdapter: AppNotificationsListAdapter
    private lateinit var binding: FragmentAppNotificationsBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppNotificationsBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        observeAllNotifications()
        return binding.root
    }

    private fun observeAllNotifications() {
        appViewModel.allNotifications.observe(requireActivity()) { notificationsList ->
            if (notificationsList.isNotEmpty()) {
                appNotificationsListAdapter.submitList(notificationsList)
                binding.rvAppNotifications.visible()
                binding.tvNoDataFound.gone()
            } else {
                binding.rvAppNotifications.gone()
                binding.tvNoDataFound.visible()
            }
        }
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = getString(R.string.lbl_notifications)
        }
    }

    override fun setupViews() {
        appNotificationsListAdapter = AppNotificationsListAdapter {}
        binding.apply {
            rvAppNotifications.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = appNotificationsListAdapter
            }
        }
    }

    override fun handleClicks() {}

}