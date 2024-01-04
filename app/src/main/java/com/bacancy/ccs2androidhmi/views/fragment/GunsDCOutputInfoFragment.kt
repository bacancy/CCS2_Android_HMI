package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentDcMeterBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GunsDCOutputInfoFragment : BaseFragment() {

    private var selectedGunNumber: Int = 1
    private val appViewModel: AppViewModel by viewModels()
    private lateinit var binding: FragmentDcMeterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDcMeterBinding.inflate(layoutInflater)
        selectedGunNumber = arguments?.getInt("SELECTED_GUN")!!
        setScreenHeaderViews()
        setupViews()
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(true)
        observeGunsDCOutputInfo()
        return binding.root
    }

    private fun observeGunsDCOutputInfo() {
        appViewModel.getUpdatedGunsDCMeterInfo(selectedGunNumber).observe(requireActivity()){
            it?.let {
                updateDCOutputUI(it)
            }
        }
    }

    private fun updateDCOutputUI(tbGunsChargingInfo: TbGunsDcMeterInfo) {
        binding.apply {
            tbGunsChargingInfo.apply {
                incVoltage.tvValue.text = voltage.toString()
                incCurrent.tvValue.text = current.toString()
                incPower.tvValue.text = power.toString()
                incImportEnergy.tvValue.text = importEnergy.toString()
                incExportEnergy.tvValue.text = exportEnergy.toString()
                incMaxVoltage.tvValue.text = maxVoltage.toString()
                incMinVoltage.tvValue.text = minVoltage.toString()
                incMaxCurrent.tvValue.text = maxCurrent.toString()
                incMinCurrent.tvValue.text = minCurrent.toString()
            }
        }
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
            incHeader.tvSubHeader.text = getString(R.string.lbl_dc_output_information)
            incHeader.tvSubHeader.visible()
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


}