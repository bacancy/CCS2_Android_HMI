package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentPlcFaultsBinding
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PLCFaultsFragment : BaseFragment() {

    private lateinit var binding: FragmentPlcFaultsBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlcFaultsBinding.inflate(layoutInflater)
        observePLCFaults()
        return binding.root
    }

    private fun observePLCFaults() {
        appViewModel.latestMiscInfo.observe(viewLifecycleOwner) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updatePLCFaultUI(latestMiscInfo)
            }
        }
    }

    private fun updatePLCFaultUI(latestMiscInfo: TbMiscInfo) {
        val plc1Fault = latestMiscInfo.plc1Fault
        val plc2Fault = latestMiscInfo.plc2Fault

        when (plc1Fault.toInt()) {
            0 -> {
                binding.ivGun1Fault.setImageResource(R.drawable.ic_green_dot)
            }

            1 -> {
                binding.ivGun1Fault.setImageResource(R.drawable.ic_red_dot)
            }
        }

        when (plc2Fault.toInt()) {
            0 -> {
                binding.ivGun2Fault.setImageResource(R.drawable.ic_green_dot)
            }

            1 -> {
                binding.ivGun2Fault.setImageResource(R.drawable.ic_red_dot)
            }
        }
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_plc_faults)
    }

    override fun setupViews() {}

    override fun handleClicks() {}
}