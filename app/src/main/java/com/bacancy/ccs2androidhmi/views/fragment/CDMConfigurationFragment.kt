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
import com.bacancy.ccs2androidhmi.util.CommonUtils.toJsonString
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getACMetersList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getChargeControlModeList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getDCMetersList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getRectifiersList
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.TextViewUtils.setBold
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CDMConfigurationFragment : BaseFragment() {

    private lateinit var currentConfigParameters: TbConfigurationParameters
    private var isReadOnce: Boolean = false
    private lateinit var binding: FragmentCdmConfigurationBinding
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCdmConfigurationBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        updateSelectedTab(0)
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

            tvFaultDetection.setOnClickListener {
                updateSelectedTab(4)
            }

            btnSubmitCharger.setOnClickListener {
                prefHelper.setBoolean("CDM_CHARGER_UPDATED", true)
                checkChargerDataAndSubmit()
            }

            btnSubmitRectifier.setOnClickListener {
                prefHelper.setBoolean("CDM_RECTIFIERS_UPDATED", true)
                checkRectifiersDataAndSubmit()
            }

            btnSubmitACMeter.setOnClickListener {
                prefHelper.setBoolean("CDM_AC_METER_UPDATED", true)
                checkACMeterDataAndSubmit()
            }

            btnSubmitDCMeter.setOnClickListener {
                prefHelper.setBoolean("CDM_DC_METER_UPDATED", true)
                checkDCMeterDataAndSubmit()
            }

            btnSubmitFaultDetection.setOnClickListener {
                prefHelper.setBoolean("CDM_FAULT_DETECTION_UPDATED", true)
                checkFaultDetectionDataAndSubmit()
            }
        }
    }

    private fun checkChargerDataAndSubmit() {
        binding.apply {
            val selectedChargeControlMode = spnChargeControlMode.selectedItemPosition
            val chargeControlModeList = mutableListOf<Int>()
            chargeControlModeList.add(selectedChargeControlMode)
            prefHelper.setStringValue("CHARGER_DATA", chargeControlModeList.toJsonString())
        }
    }

    private fun checkACMeterDataAndSubmit(){
        binding.apply {
            val selectedACMeter = spnACMeterSelection.selectedItemPosition
            val isACMeterMandatory = if (switchACMeterMandatory.isChecked) 1 else 0
            val binaryString = currentConfigParameters.acMeterDataConfiguration.substring(0, 4) + isACMeterMandatory.toString()
            val decimal = Integer.parseInt(binaryString, 2)
            val acMeterList = mutableListOf<Int>()
            acMeterList.apply {
                add(decimal)
                add(selectedACMeter)
            }
            Log.d("CDMConfigurationFragment", "checkACMeterDataAndSubmit: $acMeterList")
            prefHelper.setStringValue("AC_METER_DATA", acMeterList.toJsonString())
        }
    }

    private fun checkDCMeterDataAndSubmit(){
        binding.apply {
            val selectedDCMeter = spnDCMeterSelection.selectedItemPosition
            val isDCMeterMandatory = if (switchDCMeterMandatory.isChecked) 1 else 0
            val binaryString = currentConfigParameters.dcMeterDataConfiguration.substring(0, 4) + isDCMeterMandatory.toString()
            val decimal = Integer.parseInt(binaryString, 2)
            val dcMeterList = mutableListOf<Int>()
            dcMeterList.apply {
                add(decimal)
                add(selectedDCMeter)
            }
            Log.d("CDMConfigurationFragment", "checkDCMeterDataAndSubmit: $dcMeterList")
            prefHelper.setStringValue("DC_METER_DATA", dcMeterList.toJsonString())
        }
    }

    private fun checkFaultDetectionDataAndSubmit() {
        binding.apply {
            val spdFaultDetection = if (switchSPDFaultDetection.isChecked) 1 else 0
            val smokeFaultDetection = if (switchSmokeFaultDetection.isChecked) 1 else 0
            val tamperFaultDetection = if (switchTamperFaultDetection.isChecked) 1 else 0
            val ledModuleFaultDetection = if (switchLEDFaultDetection.isChecked) 1 else 0
            val gunTempFaultDetection = if (switchGunTemperatureFaultDetection.isChecked) 1 else 0
            val isolationFaultDetection = if (switchIsolationFaultDetection.isChecked) 1 else 0

            val binaryString =
                "$isolationFaultDetection$gunTempFaultDetection$ledModuleFaultDetection$tamperFaultDetection$smokeFaultDetection$spdFaultDetection"
            val decimal = Integer.parseInt(binaryString, 2)

            var gunTemperatureThresholdValue = 0
            var phaseLowDetectionVoltage = 0
            var phaseHighDetectionVoltage = 0
            var isAllValid = true

            if (edtDcGunTemperatureThresholdValue.text.toString().isNotEmpty()) {
                gunTemperatureThresholdValue =
                    edtDcGunTemperatureThresholdValue.text.toString().toInt()
            } else {
                isAllValid = false
                edtDcGunTemperatureThresholdValue.error = "Please enter value"
            }

            if (edtPhaseLowDetectionVoltage.text.toString().isNotEmpty()) {
                phaseLowDetectionVoltage = edtPhaseLowDetectionVoltage.text.toString().toInt()
            } else {
                isAllValid = false
                edtPhaseLowDetectionVoltage.error = "Please enter value"
            }

            if (edtPhaseHighDetectionVoltage.text.toString().isNotEmpty()) {
                phaseHighDetectionVoltage = edtPhaseHighDetectionVoltage.text.toString().toInt()
            } else {
                isAllValid = false
                edtPhaseHighDetectionVoltage.error = "Please enter value"
            }

            if (isAllValid) {
                val faultDetectionList = mutableListOf<Int>()
                faultDetectionList.apply {
                    add(decimal)
                    add(gunTemperatureThresholdValue)
                    add(phaseLowDetectionVoltage)
                    add(phaseHighDetectionVoltage)
                }
                prefHelper.setStringValue("FAULT_DETECTION_DATA", faultDetectionList.toJsonString())
            }
        }
    }

    private fun checkRectifiersDataAndSubmit() {
        binding.apply {

            var isAllValid = true
            var rectifierPerGroup = 0
            var rectifierMaxPower = 0
            var rectifierMaxVoltage = 0
            var rectifierMaxCurrent = 0
            val selectedRectifier = spnRectifierSelection.selectedItemPosition

            if (edtRectifierPerGroup.text.toString().isNotEmpty()) {
                rectifierPerGroup = edtRectifierPerGroup.text.toString().toInt()
            } else {
                isAllValid = false
                edtRectifierPerGroup.error = "Please enter value"
            }

            if (edtRectifierMaxPower.text.toString().isNotEmpty()) {
                rectifierMaxPower = edtRectifierMaxPower.text.toString().toInt()
            } else {
                isAllValid = false
                edtRectifierMaxPower.error = "Please enter value"
            }

            if (edtRectifierMaxVoltage.text.toString().isNotEmpty()) {
                rectifierMaxVoltage = edtRectifierMaxVoltage.text.toString().toInt()
            } else {
                isAllValid = false
                edtRectifierMaxVoltage.error = "Please enter value"
            }

            if (edtRectifierMaxCurrent.text.toString().isNotEmpty()) {
                rectifierMaxCurrent = edtRectifierMaxCurrent.text.toString().toInt()
            } else {
                isAllValid = false
                edtRectifierMaxCurrent.error = "Please enter value"

            }

            if (isAllValid) {
                val rectifiersList = mutableListOf<Int>()
                rectifiersList.apply {
                    add(selectedRectifier)
                    add(rectifierPerGroup)
                    add(currentConfigParameters.maxDCOutputPowerCapacity)
                    add(rectifierMaxPower)
                    add(rectifierMaxVoltage)
                    add(rectifierMaxCurrent)
                }
                prefHelper.setStringValue("RECTIFIERS_DATA", rectifiersList.toJsonString())
            }
        }
    }

    private fun updateSelectedTab(tabId: Int) {
        binding.apply {
            tvCharger.updateSelectedTabColor(tabId == 0)
            tvRectifier.updateSelectedTabColor(tabId == 1)
            tvACMeter.updateSelectedTabColor(tabId == 2)
            tvDCMeter.updateSelectedTabColor(tabId == 3)
            tvFaultDetection.updateSelectedTabColor(tabId == 4)
            binding.lnrCharger.visibility = if (tabId == 0) View.VISIBLE else View.GONE
            binding.lnrRectifier.visibility = if (tabId == 1) View.VISIBLE else View.GONE
            binding.lnrACMeter.visibility = if (tabId == 2) View.VISIBLE else View.GONE
            binding.lnrDCMeter.visibility = if (tabId == 3) View.VISIBLE else View.GONE
            binding.lnrFaultDetection.visibility = if (tabId == 4) View.VISIBLE else View.GONE
        }
    }

    private fun TextView.updateSelectedTabColor(isSelected: Boolean) {
        setTextColor(
            if (isSelected) resources.getColor(
                R.color.green,
                null
            ) else resources.getColor(R.color.selected_radio_color, null)
        )
        setBold(isSelected)
    }

    private fun observeConfigurationParameters() {
        appViewModel.getConfigurationParameters.observe(viewLifecycleOwner) { paramsList ->
            Log.d("CDMConfigurationFragment", "observeConfigurationParameters from DB: $paramsList")
            if (!isReadOnce) {
                isReadOnce = true
                paramsList?.let {
                    if (it.isNotEmpty()) {
                        it[0].apply {
                            currentConfigParameters = this
                            setupChargeControlModeSpinner(chargeControlMode)
                            setupRectifierSelectionSpinner(selectedRectifier)
                            setupACMeterSelectionSpinner(selectedACMeter)
                            setupDCMeterSelectionSpinner(selectedDCMeter)
                            setupRectifierData(this)
                            setupACMeterData(this)
                            setupDCMeterData(this)
                            setupFaultDetectionData(this)
                        }
                    }
                }
            }
        }
    }

    private fun setupDCMeterData(tbConfigurationParameters: TbConfigurationParameters) {
        binding.apply {
            switchDCMeterMandatory.isChecked = tbConfigurationParameters.isDCMeterMandatory == 1
        }
    }

    private fun setupACMeterData(tbConfigurationParameters: TbConfigurationParameters) {
        binding.apply {
            switchACMeterMandatory.isChecked = tbConfigurationParameters.isACMeterMandatory == 1
        }
    }

    private fun setupFaultDetectionData(tbConfigurationParameters: TbConfigurationParameters) {
        binding.apply {
            switchSPDFaultDetection.isChecked = tbConfigurationParameters.spdFaultDetection == 1
            switchSmokeFaultDetection.isChecked = tbConfigurationParameters.smokeFaultDetection == 1
            switchTamperFaultDetection.isChecked =
                tbConfigurationParameters.tamperFaultDetection == 1
            switchLEDFaultDetection.isChecked =
                tbConfigurationParameters.ledModuleFaultDetection == 1
            switchGunTemperatureFaultDetection.isChecked =
                tbConfigurationParameters.gunTempFaultDetection == 1
            switchIsolationFaultDetection.isChecked =
                tbConfigurationParameters.isolationFaultDetection == 1
            edtDcGunTemperatureThresholdValue.setText(tbConfigurationParameters.gunTemperatureThresholdValue.toString())
            edtPhaseLowDetectionVoltage.setText(tbConfigurationParameters.phaseLowDetectionVoltage.toString())
            edtPhaseHighDetectionVoltage.setText(tbConfigurationParameters.phaseHighDetectionVoltage.toString())
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