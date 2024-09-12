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
import com.bacancy.ccs2androidhmi.databinding.FragmentRectifierFaultsNewBinding
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RectifierFaultInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentRectifierFaultsNewBinding
    private val appViewModel: AppViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRectifierFaultsNewBinding.inflate(layoutInflater)
        observeRectifierFaults()
        return binding.root
    }

    private fun observeRectifierFaults() {
        appViewModel.latestMiscInfo.observe(viewLifecycleOwner) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updateRectifierFaultUI(latestMiscInfo)
            }
        }
        appViewModel.allRectifierFaults.observe(viewLifecycleOwner) { allRectifierFaults ->
            Log.d("RectifierFaultInfoFragment", "observeRectifierFaults: ${Gson().toJson(allRectifierFaults)}")
        }
    }

    private fun updateRectifierFaultUI(latestMiscInfo: TbMiscInfo) {
        binding.apply {
            binding.incRectifier1.tvRectifierFault.text = latestMiscInfo.rectifier1Fault
            binding.incRectifier2.tvRectifierFault.text = latestMiscInfo.rectifier2Fault
            binding.incRectifier3.tvRectifierFault.text = latestMiscInfo.rectifier3Fault
            binding.incRectifier4.tvRectifierFault.text = latestMiscInfo.rectifier4Fault
        }
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_rectifier_fault_information)
    }

    override fun setupViews() {}

    override fun handleClicks() {}
}