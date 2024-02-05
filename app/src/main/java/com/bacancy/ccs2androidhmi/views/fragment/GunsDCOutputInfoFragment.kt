package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentDcMeterBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.setValue
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GunsDCOutputInfoFragment : BaseFragment() {

    private var selectedGunNumber: Int = 1
    private val appViewModel: AppViewModel by viewModels()
    private lateinit var binding: FragmentDcMeterBinding

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDcMeterBinding.inflate(layoutInflater)
        selectedGunNumber = arguments?.getInt(SELECTED_GUN)!!
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        observeGunsDCOutputInfo()
        return binding.root
    }

    private fun observeGunsDCOutputInfo() {
        lifecycleScope.launch {
            appViewModel.getUpdatedGunsDCMeterInfo(selectedGunNumber).collect {
                it?.let { it1 -> updateDCOutputUI(it1) }
            }
        }
    }

    private fun updateDCOutputUI(tbGunsChargingInfo: TbGunsDcMeterInfo) {
        binding.apply {
            tbGunsChargingInfo.apply {
                incVoltage.tvValue.setValue(voltage)
                incCurrent.tvValue.setValue(current)
                incPower.tvValue.setValue(power)
                incImportEnergy.tvValue.setValue(importEnergy)
                incExportEnergy.tvValue.setValue(exportEnergy)
                incMaxVoltage.tvValue.setValue(maxVoltage)
                incMinVoltage.tvValue.setValue(minVoltage)
                incMaxCurrent.tvValue.setValue(maxCurrent)
                incMinCurrent.tvValue.setValue(minCurrent)
            }
        }
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            if (selectedGunNumber == 1) {
                incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
            } else {
                incHeader.tvHeader.text = getString(R.string.lbl_gun_2)
            }
            incHeader.tvSubHeader.text = getString(R.string.lbl_dc_output_information)
            incHeader.tvSubHeader.visible()
        }
    }

    override fun onResume() {
        super.onResume()
        if (selectedGunNumber == 1) {
            prefHelper.setScreenVisible(CommonUtils.GUN_1_DC_METER_FRAG, true)
        } else {
            prefHelper.setScreenVisible(CommonUtils.GUN_2_DC_METER_FRAG, true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (selectedGunNumber == 1) {
            prefHelper.setScreenVisible(CommonUtils.GUN_1_DC_METER_FRAG, false)
        } else {
            prefHelper.setScreenVisible(CommonUtils.GUN_2_DC_METER_FRAG, false)
        }
    }

    override fun setupViews() {
        binding.apply {
            incVoltage.tvLabel.text = getString(R.string.lbl_voltage)
            incVoltage.tvValueUnit.text = getString(R.string.lbl_v)

            incMaxVoltage.tvLabel.text = getString(R.string.lbl_max_voltage)
            incMaxVoltage.tvValueUnit.text = getString(R.string.lbl_v)

            incMinVoltage.tvLabel.text = getString(R.string.lbl_min_voltage)
            incMinVoltage.tvValueUnit.text = getString(R.string.lbl_v)

            incCurrent.tvLabel.text = getString(R.string.lbl_current)
            incCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incMaxCurrent.tvLabel.text = getString(R.string.lbl_max_current)
            incMaxCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incMinCurrent.tvLabel.text = getString(R.string.lbl_min_current)
            incMinCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incPower.tvLabel.text = getString(R.string.lbl_power)
            incPower.tvValueUnit.text = getString(R.string.lbl_kw)

            incImportEnergy.tvLabel.text = getString(R.string.lbl_import_energy)
            incImportEnergy.tvValueUnit.text = getString(R.string.lbl_kwh)

            incExportEnergy.tvLabel.text = getString(R.string.lbl_export_energy)
            incExportEnergy.tvValueUnit.text = getString(R.string.lbl_kwh)
        }
    }

    override fun handleClicks() {}


}