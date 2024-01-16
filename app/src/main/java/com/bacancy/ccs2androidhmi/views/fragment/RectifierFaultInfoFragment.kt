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
import com.bacancy.ccs2androidhmi.databinding.FragmentRectifierFaultsBinding
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RectifierFaultInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentRectifierFaultsBinding
    private val appViewModel: AppViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRectifierFaultsBinding.inflate(layoutInflater)
        observeRectifierFaults()
        return binding.root
    }

    private fun observeRectifierFaults() {
        appViewModel.latestMiscInfo.observe(requireActivity()) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updateRectifierFaultUI(latestMiscInfo)
            }
        }
    }

    private fun updateRectifierFaultUI(latestMiscInfo: TbMiscInfo) {
        binding.apply {
            tvRectifier1Value.text = latestMiscInfo.rectifier1Fault
            tvRectifier2Value.text = latestMiscInfo.rectifier2Fault
            tvRectifier3Value.text = latestMiscInfo.rectifier3Fault
            tvRectifier4Value.text = latestMiscInfo.rectifier4Fault
        }
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_rectifier_fault_information)
    }

    override fun setupViews() {}

    override fun handleClicks() {}
}