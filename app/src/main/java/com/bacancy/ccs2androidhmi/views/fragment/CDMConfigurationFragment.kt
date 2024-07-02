package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentCdmConfigurationBinding
import com.bacancy.ccs2androidhmi.db.entity.TbConfigurationParameters
import com.bacancy.ccs2androidhmi.util.CommonUtils.AC_METER_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_AC_METER_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_CHARGER_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_DC_METER_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_FAULT_DETECTION_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_RECTIFIERS_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CHARGER_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.DC_METER_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.FAULT_DETECTION_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.RECTIFIERS_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.toJsonString
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getACMetersList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getChargeControlModeList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getDCMetersList
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils.getRectifiersList
import com.bacancy.ccs2androidhmi.util.InputFilterMinMax
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.TextViewUtils.setBold
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
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
        setupACMeterUserDefinedFields()
        setupDCMeterUserDefinedFields()
        return binding.root
    }

    private fun setupACMeterUserDefinedFields() {
        binding.apply {
            val hint = "(0-65535)"
            val min = 0
            val max = 65535
            val fields = listOf(
                Pair(incVoltageV1N, getString(R.string.lbl_voltage_v1n)),
                Pair(incVoltageV2N, getString(R.string.lbl_voltage_v2n)),
                Pair(incVoltageV3N, getString(R.string.lbl_voltage_v3n)),
                Pair(incAvgVoltageLN, getString(R.string.lbl_avg_voltage_ln)),
                Pair(incFrequency, getString(R.string.lbl_frequency)),
                Pair(incAvgPF, getString(R.string.lbl_average_pf)),
                Pair(incCurrentL1, getString(R.string.lbl_current_i1)),
                Pair(incCurrentL2, getString(R.string.lbl_current_i2)),
                Pair(incCurrentL3, getString(R.string.lbl_current_i3)),
                Pair(incAvgCurrent, getString(R.string.lbl_avg_current)),
                Pair(incActivePower, getString(R.string.lbl_active_power)),
                Pair(incTotalEnergy, getString(R.string.lbl_total_energy)),
                Pair(incTotalReactiveEnergy, getString(R.string.lbl_total_reactive_energy))
            )

            for ((view, label) in fields) {
                view.apply {
                    txtInputLabel.text = label
                    edtInputValue.hint = hint
                    edtInputValue.filters = arrayOf(InputFilterMinMax(min, max))
                }
            }
        }
    }

    private fun setupDCMeterUserDefinedFields() {
        binding.apply {
            val hint = "(0-65535)"
            val min = 0
            val max = 65535
            val fields = listOf(
                Pair(incVoltageParameter, getString(R.string.lbl_voltage_parameter)),
                Pair(incCurrentParameter, getString(R.string.lbl_current_parameter)),
                Pair(incPowerParameter, getString(R.string.lbl_power_parameter)),
                Pair(incImportEnergyParameter, getString(R.string.lbl_import_energy_parameter)),
                Pair(incExportEnergyParameter, getString(R.string.lbl_export_energy_parameter)),
                Pair(incMaxVoltageParameter, getString(R.string.lbl_max_voltage_parameter)),
                Pair(incMinVoltageParameter, getString(R.string.lbl_min_voltage_parameter)),
                Pair(incMaxCurrent, getString(R.string.lbl_max_current)),
                Pair(incMinCurrent, getString(R.string.lbl_min_current))
            )

            for ((view, label) in fields) {
                view.apply {
                    txtInputLabel.text = label
                    edtInputValue.hint = hint
                    edtInputValue.filters = arrayOf(InputFilterMinMax(min, max))
                }
            }
        }
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
                checkChargerDataAndSubmit()
            }

            btnSubmitRectifier.setOnClickListener {
                checkRectifiersDataAndSubmit()
            }

            btnSubmitACMeter.setOnClickListener {
                checkACMeterDataAndSubmit()
            }

            btnSubmitDCMeter.setOnClickListener {
                checkDCMeterDataAndSubmit()
            }

            btnSubmitFaultDetection.setOnClickListener {
                checkFaultDetectionDataAndSubmit()
            }
        }
    }

    private fun checkChargerDataAndSubmit() {
        binding.apply {
            val selectedChargeControlMode = spnChargeControlMode.selectedItemPosition
            val chargeControlModeList = mutableListOf<Int>()
            chargeControlModeList.add(selectedChargeControlMode)
            prefHelper.setBoolean(CDM_CHARGER_UPDATED, true)
            prefHelper.setStringValue(CHARGER_DATA, chargeControlModeList.toJsonString())
        }
    }

    private fun checkACMeterDataAndSubmit() {
        binding.apply {
            var isAllValid = true
            val selectedACMeter = spnACMeterSelection.selectedItemPosition
            val isACMeterMandatory = if (switchACMeterMandatory.isChecked) 1 else 0
            Log.d(
                "CDMConfigurationFragment",
                "Current AC Meter Config: ${currentConfigParameters.acMeterDataConfiguration}"
            )
            val binaryString = currentConfigParameters.acMeterDataConfiguration.substring(
                0,
                4
            ) + isACMeterMandatory.toString()
            Log.d("CDMConfigurationFragment", "Binary String: $binaryString")
            val decimal = Integer.parseInt(binaryString, 2)

            if (selectedACMeter == 0) {
                isAllValid = areAllACMeterUserDefinedFieldsValid()
            }

            if (isAllValid) {
                val acMeterList = mutableListOf<Int>()
                acMeterList.apply {
                    add(selectedACMeter)
                    add(decimal)
                    if(selectedACMeter == 0){
                        add(incVoltageV1N.edtInputValue.text.toString().toInt())
                        add(incVoltageV2N.edtInputValue.text.toString().toInt())
                        add(incVoltageV3N.edtInputValue.text.toString().toInt())
                        add(incAvgVoltageLN.edtInputValue.text.toString().toInt())
                        add(incFrequency.edtInputValue.text.toString().toInt())
                        add(incAvgPF.edtInputValue.text.toString().toInt())
                        add(incCurrentL1.edtInputValue.text.toString().toInt())
                        add(incCurrentL2.edtInputValue.text.toString().toInt())
                        add(incCurrentL3.edtInputValue.text.toString().toInt())
                        add(incAvgCurrent.edtInputValue.text.toString().toInt())
                        add(incActivePower.edtInputValue.text.toString().toInt())
                        add(incTotalEnergy.edtInputValue.text.toString().toInt())
                        //TO-DO - The address of total reactive energy is far from other parameters.
                        //add(incTotalReactiveEnergy.edtInputValue.text.toString().toInt())
                    }
                }
                Log.d("CDMConfigurationFragment", "Ac Meter List: $acMeterList")
                prefHelper.setBoolean(CDM_AC_METER_UPDATED, true)
                prefHelper.setStringValue(AC_METER_DATA, acMeterList.toJsonString())
            }
        }
    }

    private fun areAllACMeterUserDefinedFieldsValid(): Boolean {
        var isAllValid = true
        binding.apply {
            val fieldsToValidate = listOf(
                incVoltageV1N,
                incVoltageV2N,
                incVoltageV3N,
                incAvgVoltageLN,
                incFrequency,
                incAvgPF,
                incCurrentL1,
                incCurrentL2,
                incCurrentL3,
                incAvgCurrent,
                incActivePower,
                incTotalEnergy,
                incTotalReactiveEnergy
            )

            fieldsToValidate.forEach { field ->
                val editText = field.edtInputValue
                if (editText.text.isEmpty() || editText.text.toString().toInt() !in 0..65535) {
                    isAllValid = false
                    editText.error = getString(R.string.msg_please_enter_value_between_0_65535)
                }
            }
        }
        return isAllValid
    }

    private fun checkDCMeterDataAndSubmit() {
        binding.apply {
            var isAllValid = true
            val selectedDCMeter = spnDCMeterSelection.selectedItemPosition
            val isDCMeterMandatory = if (switchDCMeterMandatory.isChecked) 1 else 0
            val binaryString = currentConfigParameters.dcMeterDataConfiguration.substring(
                0,
                4
            ) + isDCMeterMandatory.toString()
            val decimal = Integer.parseInt(binaryString, 2)

            if (selectedDCMeter == 0) {
                isAllValid = areAllDCMeterUserDefinedFieldsValid()
            }

            if (isAllValid) {
                val dcMeterList = mutableListOf<Int>()
                dcMeterList.apply {
                    add(selectedDCMeter)
                    add(decimal)
                    if(selectedDCMeter == 0){
                        add(incVoltageParameter.edtInputValue.text.toString().toInt())
                        add(incCurrentParameter.edtInputValue.text.toString().toInt())
                        add(incPowerParameter.edtInputValue.text.toString().toInt())
                        add(incImportEnergyParameter.edtInputValue.text.toString().toInt())
                        add(incExportEnergyParameter.edtInputValue.text.toString().toInt())
                        add(incMaxVoltageParameter.edtInputValue.text.toString().toInt())
                        add(incMinVoltageParameter.edtInputValue.text.toString().toInt())
                        add(incMaxCurrent.edtInputValue.text.toString().toInt())
                        add(incMinCurrent.edtInputValue.text.toString().toInt())
                    }
                }
                Log.d("CDMConfigurationFragment", "checkDCMeterDataAndSubmit: $dcMeterList")
                prefHelper.setBoolean(CDM_DC_METER_UPDATED, true)
                prefHelper.setStringValue(DC_METER_DATA, dcMeterList.toJsonString())
            }
        }
    }

    private fun areAllDCMeterUserDefinedFieldsValid(): Boolean {
        var isAllValid = true
        binding.apply {
            val fieldsToValidate = listOf(
                incVoltageParameter,
                incCurrentParameter,
                incPowerParameter,
                incImportEnergyParameter,
                incExportEnergyParameter,
                incMaxVoltageParameter,
                incMinVoltageParameter,
                incMaxCurrent,
                incMinCurrent
            )

            fieldsToValidate.forEach { field ->
                val editText = field.edtInputValue
                if (editText.text.isEmpty() || editText.text.toString().toInt() !in 0..65535) {
                    isAllValid = false
                    editText.error = getString(R.string.msg_please_enter_value_between_0_65535)
                }
            }
        }
        return isAllValid
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

            if (edtDcGunTemperatureThresholdValue.text.isEmpty() || edtDcGunTemperatureThresholdValue.text.toString()
                    .toInt() !in 0..850
            ) {
                isAllValid = false
                edtDcGunTemperatureThresholdValue.error =
                    getString(R.string.msg_please_enter_value_between_0_850)
            } else {
                gunTemperatureThresholdValue =
                    edtDcGunTemperatureThresholdValue.text.toString().toInt()
            }

            if (edtPhaseLowDetectionVoltage.text.isEmpty() || edtPhaseLowDetectionVoltage.text.toString()
                    .toInt() !in 0..400
            ) {
                isAllValid = false
                edtPhaseLowDetectionVoltage.error =
                    getString(R.string.msg_please_enter_value_between_0_400)
            } else {
                phaseLowDetectionVoltage = edtPhaseLowDetectionVoltage.text.toString().toInt()
            }

            if (edtPhaseHighDetectionVoltage.text.isEmpty() || edtPhaseHighDetectionVoltage.text.toString()
                    .toInt() !in 0..600
            ) {
                isAllValid = false
                edtPhaseHighDetectionVoltage.error =
                    getString(R.string.msg_please_enter_value_between_0_600)
            } else {
                phaseHighDetectionVoltage = edtPhaseHighDetectionVoltage.text.toString().toInt()
            }

            if (isAllValid) {
                val faultDetectionList = mutableListOf<Int>()
                faultDetectionList.apply {
                    add(decimal)
                    add(gunTemperatureThresholdValue)
                    add(phaseLowDetectionVoltage)
                    add(phaseHighDetectionVoltage)
                }
                prefHelper.setBoolean(CDM_FAULT_DETECTION_UPDATED, true)
                prefHelper.setStringValue(FAULT_DETECTION_DATA, faultDetectionList.toJsonString())
            }
        }
    }

    private fun checkRectifiersDataAndSubmit() {
        binding.apply {

            var isAllValid = true
            val selectedRectifier = spnRectifierSelection.selectedItemPosition
            var rectifierPerGroup = 0
            var rectifierMaxPower = 0
            var rectifierMaxVoltage = 0
            var rectifierMaxCurrent = 0

            if (edtRectifierPerGroup.text.isEmpty() || edtRectifierPerGroup.text.toString()
                    .toInt() !in 1..8
            ) {
                isAllValid = false
                edtRectifierPerGroup.error = getString(R.string.msg_please_enter_value_between_1_8)
            } else {
                rectifierPerGroup = edtRectifierPerGroup.text.toString().toInt()
            }

            if (edtRectifierMaxPower.text.isEmpty() || edtRectifierMaxPower.text.toString()
                    .toInt() !in 1..50
            ) {
                isAllValid = false
                edtRectifierMaxPower.error = getString(R.string.msg_please_enter_value_between_1_50)
            } else {
                rectifierMaxPower = edtRectifierMaxPower.text.toString().toInt()
            }

            if (edtRectifierMaxVoltage.text.isEmpty() || edtRectifierMaxVoltage.text.toString()
                    .toInt() !in 200..1000
            ) {
                isAllValid = false
                edtRectifierMaxVoltage.error =
                    getString(R.string.msg_please_enter_value_between_200_1000)
            } else {
                rectifierMaxVoltage = edtRectifierMaxVoltage.text.toString().toInt()
            }

            if (edtRectifierMaxCurrent.text.isEmpty() || edtRectifierMaxCurrent.text.toString()
                    .toInt() !in 1..100
            ) {
                isAllValid = false
                edtRectifierMaxCurrent.error =
                    getString(R.string.msg_please_enter_value_between_1_100)
            } else {
                rectifierMaxCurrent = edtRectifierMaxCurrent.text.toString().toInt()
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
                Log.d("###CDMCONFIG", "checkRectifiersDataAndSubmit: $rectifiersList")
                prefHelper.setBoolean(CDM_RECTIFIERS_UPDATED, true)
                prefHelper.setStringValue(RECTIFIERS_DATA, rectifiersList.toJsonString())
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
            Log.d("###CDMCONFIG", "observeConfigurationParameters from DB: $paramsList")
            if (!isReadOnce && paramsList.isNotEmpty()) {
                isReadOnce = true
                paramsList[0].apply {
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

    private fun setupDCMeterData(tbConfigurationParameters: TbConfigurationParameters) {
        binding.apply {
            switchDCMeterMandatory.isChecked = tbConfigurationParameters.isDCMeterMandatory == 1
            if (tbConfigurationParameters.selectedDCMeter == 0) {
                val dcMeterUserDefinedFields = tbConfigurationParameters.dcMeterUserDefinedFields
                dcMeterUserDefinedFields?.apply {
                    incVoltageParameter.edtInputValue.setText(voltageParameter.toString())
                    incCurrentParameter.edtInputValue.setText(currentParameter.toString())
                    incPowerParameter.edtInputValue.setText(powerParameter.toString())
                    incImportEnergyParameter.edtInputValue.setText(importEnergyParameter.toString())
                    incExportEnergyParameter.edtInputValue.setText(exportEnergyParameter.toString())
                    incMaxVoltageParameter.edtInputValue.setText(maxVoltageParameter.toString())
                    incMinVoltageParameter.edtInputValue.setText(minVoltageParameter.toString())
                    incMaxCurrent.edtInputValue.setText(maxCurrent.toString())
                    incMinCurrent.edtInputValue.setText(minCurrent.toString())
                }
            }
        }
    }

    private fun setupACMeterData(tbConfigurationParameters: TbConfigurationParameters) {
        binding.apply {
            switchACMeterMandatory.isChecked = tbConfigurationParameters.isACMeterMandatory == 1
            if (tbConfigurationParameters.selectedACMeter == 0) {
                val acMeterUserDefinedFields = tbConfigurationParameters.acMeterUserDefinedFields
                acMeterUserDefinedFields?.apply {
                    incVoltageV1N.edtInputValue.setText(voltageV1N.toString())
                    incVoltageV2N.edtInputValue.setText(voltageV2N.toString())
                    incVoltageV3N.edtInputValue.setText(voltageV3N.toString())
                    incAvgVoltageLN.edtInputValue.setText(avgVoltageLN.toString())
                    incFrequency.edtInputValue.setText(frequency.toString())
                    incAvgPF.edtInputValue.setText(avgPF.toString())
                    incCurrentL1.edtInputValue.setText(currentL1.toString())
                    incCurrentL2.edtInputValue.setText(currentL2.toString())
                    incCurrentL3.edtInputValue.setText(currentL3.toString())
                    incAvgCurrent.edtInputValue.setText(avgCurrent.toString())
                    incActivePower.edtInputValue.setText(activePower.toString())
                    incTotalEnergy.edtInputValue.setText(totalEnergy.toString())
                    incTotalReactiveEnergy.edtInputValue.setText(totalReactiveEnergy.toString())
                }
            }
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
            spnDCMeterSelection.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    selectedPosition: Int,
                    p3: Long
                ) {
                    Log.d(
                        "CDMConfigurationFragment",
                        "Selected DC Meter: ${getDCMetersList()[selectedPosition]}"
                    )
                    if (selectedPosition == 0) {
                        binding.gridLayoutDCMeter.visible()
                    } else {
                        binding.gridLayoutDCMeter.gone()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Log.d("CDMConfigurationFragment", "Nothing Selected")
                }
            }
        }
    }

    private fun setupACMeterSelectionSpinner(selectedAcMeter: Int) {
        binding.apply {
            spnACMeterSelection.adapter = getListAdapter(getACMetersList())
            spnACMeterSelection.setSelection(selectedAcMeter)
            spnACMeterSelection.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    selectedPosition: Int,
                    p3: Long
                ) {
                    Log.d(
                        "CDMConfigurationFragment",
                        "Selected AC Meter: ${getACMetersList()[selectedPosition]}"
                    )
                    if (selectedPosition == 0) {
                        binding.gridLayoutACMeter.visible()
                    } else {
                        binding.gridLayoutACMeter.gone()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Log.d("CDMConfigurationFragment", "Nothing Selected")
                }
            }
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