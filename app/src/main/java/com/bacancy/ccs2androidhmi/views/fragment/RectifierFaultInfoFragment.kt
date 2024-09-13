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
import com.bacancy.ccs2androidhmi.db.entity.TbRectifierFaults
import com.bacancy.ccs2androidhmi.db.entity.TbRectifierTemperature
import com.bacancy.ccs2androidhmi.util.CommonUtils.RECTIFIER_FAULTS_FRAGMENT
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RectifierFaultInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentRectifierFaultsNewBinding
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRectifierFaultsNewBinding.inflate(layoutInflater)
        setupRectifiersLabel()
        setupRectifiersSampleTemp()
        observeRectifierFaults()
        observeRectifierTemperature()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        prefHelper.setBoolean(RECTIFIER_FAULTS_FRAGMENT,true)
    }

    private fun setupRectifiersLabel() {
        binding.apply {
            val rectifiers = listOf(
                incRectifier1,
                incRectifier2,
                incRectifier3,
                incRectifier4,
                incRectifier5,
                incRectifier6,
                incRectifier7,
                incRectifier8,
                incRectifier9,
                incRectifier10,
                incRectifier11,
                incRectifier12,
                incRectifier13,
                incRectifier14,
                incRectifier15,
                incRectifier16
            )

            rectifiers.forEachIndexed { index, rectifier ->
                if(index == 8){
                    rectifier.tvRectifierLabel.text = "Rectifier   ${index + 1}"
                }else{
                    rectifier.tvRectifierLabel.text = "Rectifier ${index + 1}"
                }
            }
        }
    }

    private fun setupRectifiersSampleTemp() {
        binding.apply {
            val rectifiers = listOf(
                incRectifier1,
                incRectifier2,
                incRectifier3,
                incRectifier4,
                incRectifier5,
                incRectifier6,
                incRectifier7,
                incRectifier8,
                incRectifier9,
                incRectifier10,
                incRectifier11,
                incRectifier12,
                incRectifier13,
                incRectifier14,
                incRectifier15,
                incRectifier16
            )

            rectifiers.forEachIndexed { index, rectifier ->
                rectifier.tvRectifierTemperature.text = "0.0 °C"
            }
        }
    }

    private fun observeRectifierFaults() {
        appViewModel.latestMiscInfo.observe(viewLifecycleOwner) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updateRectifierFaultUI(latestMiscInfo)
            }
        }
        appViewModel.allRectifierFaults.observe(viewLifecycleOwner) { allRectifierFaults ->
            Log.d("RectifierFaultInfoFragment", "observeRectifierFaults: ${Gson().toJson(allRectifierFaults)}")
            allRectifierFaults?.let {
                updateRectifier5to16FaultUI(allRectifierFaults)
            }
        }
    }

    private fun observeRectifierTemperature() {
        appViewModel.allRectifierTemperature.observe(viewLifecycleOwner) { allRectifierTemperature ->
            Log.d("RectifierFaultInfoFragment", "observeRectifierTemperature: ${Gson().toJson(allRectifierTemperature)}")
            allRectifierTemperature?.let {
                updateRectifierTemperatureUI(allRectifierTemperature)
            }
        }
    }

    private fun updateRectifier5to16FaultUI(allRectifierFaults: TbRectifierFaults) {
        binding.apply {
            incRectifier5.tvRectifierFault.text = allRectifierFaults.rectifier5Fault
            incRectifier6.tvRectifierFault.text = allRectifierFaults.rectifier6Fault
            incRectifier7.tvRectifierFault.text = allRectifierFaults.rectifier7Fault
            incRectifier8.tvRectifierFault.text = allRectifierFaults.rectifier8Fault
            incRectifier9.tvRectifierFault.text = allRectifierFaults.rectifier9Fault
            incRectifier10.tvRectifierFault.text = allRectifierFaults.rectifier10Fault
            incRectifier11.tvRectifierFault.text = allRectifierFaults.rectifier11Fault
            incRectifier12.tvRectifierFault.text = allRectifierFaults.rectifier12Fault
            incRectifier13.tvRectifierFault.text = allRectifierFaults.rectifier13Fault
            incRectifier14.tvRectifierFault.text = allRectifierFaults.rectifier14Fault
            incRectifier15.tvRectifierFault.text = allRectifierFaults.rectifier15Fault
            incRectifier16.tvRectifierFault.text = allRectifierFaults.rectifier16Fault
        }
    }

    private fun updateRectifierTemperatureUI(tbRectifierTemperature: TbRectifierTemperature) {
        binding.apply {
            val rectifierTemps = listOf(
                tbRectifierTemperature.rectifier1Temp,
                tbRectifierTemperature.rectifier2Temp,
                tbRectifierTemperature.rectifier3Temp,
                tbRectifierTemperature.rectifier4Temp,
                tbRectifierTemperature.rectifier5Temp,
                tbRectifierTemperature.rectifier6Temp,
                tbRectifierTemperature.rectifier7Temp,
                tbRectifierTemperature.rectifier8Temp,
                tbRectifierTemperature.rectifier9Temp,
                tbRectifierTemperature.rectifier10Temp,
                tbRectifierTemperature.rectifier11Temp,
                tbRectifierTemperature.rectifier12Temp,
                tbRectifierTemperature.rectifier13Temp,
                tbRectifierTemperature.rectifier14Temp,
                tbRectifierTemperature.rectifier15Temp,
                tbRectifierTemperature.rectifier16Temp
            )

            val rectifiers = listOf(
                incRectifier1,
                incRectifier2,
                incRectifier3,
                incRectifier4,
                incRectifier5,
                incRectifier6,
                incRectifier7,
                incRectifier8,
                incRectifier9,
                incRectifier10,
                incRectifier11,
                incRectifier12,
                incRectifier13,
                incRectifier14,
                incRectifier15,
                incRectifier16
            )

            rectifiers.forEachIndexed { index, rectifier ->
                rectifier.tvRectifierTemperature.text = getString(
                    R.string.lbl_rectifier_temperature_with_celcius,
                    rectifierTemps[index]
                )
            }
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