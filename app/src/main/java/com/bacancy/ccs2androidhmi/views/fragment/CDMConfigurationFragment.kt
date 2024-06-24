package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentCdmConfigurationBinding
import com.bacancy.ccs2androidhmi.db.entity.TbConfigurationParameters
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getACMetersList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getChargeControlModeList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getDCMetersList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getRectifiersList
import com.bacancy.ccs2androidhmi.util.TextViewUtils.setBold
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CDMConfigurationFragment : BaseFragment() {

    private lateinit var binding: FragmentCdmConfigurationBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCdmConfigurationBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        observeConfigurationParameters()
        handleTabSelections()
        return binding.root
    }

    private fun handleTabSelections() {
        binding.apply {

            tvCharger.setOnClickListener {
                updateSelectedTab(0)
            }

            tvRectifier.setOnClickListener {
                updateSelectedTab(1)
            }

            tvACMeter.setOnClickListener {
                updateSelectedTab(2)
            }

            tvDCMeter.setOnClickListener {
                updateSelectedTab(3)
            }

        }
    }

    private fun updateSelectedTab(tabId: Int) {
        binding.apply {
            tvCharger.updateSelectedTabColor(tabId == 0)
            tvRectifier.updateSelectedTabColor(tabId == 1)
            tvACMeter.updateSelectedTabColor(tabId == 2)
            tvDCMeter.updateSelectedTabColor(tabId == 3)
            binding.lnrCharger.visibility = if (tabId == 0) View.VISIBLE else View.GONE
            binding.lnrRectifier.visibility = if (tabId == 1) View.VISIBLE else View.GONE
            binding.lnrACMeter.visibility = if (tabId == 2) View.VISIBLE else View.GONE
            binding.lnrDCMeter.visibility = if (tabId == 3) View.VISIBLE else View.GONE
        }
    }

    private fun TextView.updateSelectedTabColor(isSelected: Boolean) {
        setTextColor(if (isSelected) resources.getColor(R.color.green, null) else resources.getColor(R.color.selected_radio_color, null))
        setBold(isSelected)
    }

    private fun observeConfigurationParameters() {
        appViewModel.getConfigurationParameters.observe(viewLifecycleOwner) { paramsList ->
            Log.d("CDMConfigurationFragment", "observeConfigurationParameters: $paramsList")
            paramsList?.let {
                it[0].apply {
                    setupChargeControlModeSpinner(chargeControlMode)
                    setupRectifierSelectionSpinner(selectedRectifier)
                    setupRectifierData(this)
                    setupACMeterSelectionSpinner(selectedACMeter)
                    setupDCMeterSelectionSpinner(selectedDCMeter)
                }
            }
        }
    }

    private fun setupRectifierData(tbConfigurationParameters: TbConfigurationParameters) {
        binding.apply {
            edtRectifierPerGroup.setText(tbConfigurationParameters.numberOfRectifierPerGroup.toString())
            edtRectifierMaxPower.setText(tbConfigurationParameters.rectifierMaxPower.toString())
            edtRectifierMaxVoltage.setText(tbConfigurationParameters.rectifierMaxVoltage.toString())
            edtRectifierMaxCurrent.setText(tbConfigurationParameters.rectifierMaxCurrent.toString())
        }
    }

    private fun setupDCMeterSelectionSpinner(selectedDcMeter: Int) {
        binding.apply {
            spnDCMeterSelection.adapter = getListAdapter(getDCMetersList())
            spnDCMeterSelection.setSelection(selectedDcMeter)
        }
    }

    private fun setupACMeterSelectionSpinner(selectedAcMeter: Int) {
        binding.apply {
            spnACMeterSelection.adapter = getListAdapter(getACMetersList())
            spnACMeterSelection.setSelection(selectedAcMeter)
        }
    }

    private fun setupChargeControlModeSpinner(chargeControlMode: Int) {
        binding.apply {
            spnChargeControlMode.adapter = getListAdapter(getChargeControlModeList())
            spnChargeControlMode.setSelection(chargeControlMode)
        }
    }

    private fun setupRectifierSelectionSpinner(selectedRectifier: Int) {
        binding.apply {
            spnRectifierSelection.adapter = getListAdapter(getRectifiersList())
            spnRectifierSelection.setSelection(selectedRectifier)
        }
    }

    private fun getListAdapter(list: Array<String>): ArrayAdapter<String> {
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = "CDM Configuration"
        }
    }

    override fun setupViews() {}

    override fun handleClicks() {}

}