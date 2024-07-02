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
                Pair(incVoltageV1N, "Voltage V1N"),
                Pair(incVoltageV2N, "Voltage V2N"),
                Pair(incVoltageV3N, "Voltage V3N"),
                Pair(incAvgVoltageLN, "Average Voltage LN"),
                Pair(incFrequency, "Frequency"),
                Pair(incAvgPF, "Average PF"),
                Pair(incCurrentL1, "Current L1"),
                Pair(incCurrentL2, "Current L2"),
                Pair(incCurrentL3, "Current L3"),
                Pair(incAvgCurrent, "Average Current"),
                Pair(incActivePower, "Active Power"),
                Pair(incTotalEnergy, "Total Energy"),
                Pair(incTotalReactiveEnergy, "Total Reactive Energy")
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
                Pair(incVoltageParameter, "Voltage Parameter"),
                Pair(incCurrentParameter, "Current Parameter"),
                Pair(incPowerParameter, "Power Parameter"),
                Pair(incImportEnergyParameter, "Import Energy Parameter"),
                Pair(incExportEnergyParameter, "Export Energy Parameter"),
                Pair(incMaxVoltageParameter, "Max Voltage Parameter"),
                Pair(incMinVoltageParameter, "Min Voltage Parameter"),
                Pair(incMaxCurrent, "Max Current"),
                Pair(incMinCurrent, "Min Current")
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
            val acMeterList = mutableListOf<Int>()
            acMeterList.apply {
                add(selectedACMeter)
                add(decimal)
            }
            Log.d("CDMConfigurationFragment", "Ac Meter List: $acMeterList")
            prefHelper.setBoolean(CDM_AC_METER_UPDATED, true)
            prefHelper.setStringValue(AC_METER_DATA, acMeterList.toJsonString())
        }
    }

    private fun checkDCMeterDataAndSubmit() {
        binding.apply {
            val selectedDCMeter = spnDCMeterSelection.selectedItemPosition
            val isDCMeterMandatory = if (switchDCMeterMandatory.isChecked) 1 else 0
            val binaryString = currentConfigParameters.dcMeterDataConfiguration.substring(
                0,
                4
            ) + isDCMeterMandatory.toString()
            val decimal = Integer.parseInt(binaryString, 2)
            val dcMeterList = mutableListOf<Int>()
            dcMeterList.apply {
                add(decimal)
                add(selectedDCMeter)
            }
            Log.d("CDMConfigurationFragment", "checkDCMeterDataAndSubmit: $dcMeterList")
            prefHelper.setBoolean(CDM_DC_METER_UPDATED, true)
            prefHelper.setStringValue(DC_METER_DATA, dcMeterList.toJsonString())
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

            if (edtDcGunTemperatureThresholdValue.text.isEmpty() || edtDcGunTemperatureThresholdValue.text.toString()
                    .toInt() !in 0..850
            ) {
                isAllValid = false
                edtDcGunTemperatureThresholdValue.error = "Please enter value between 0-850"
            } else {
                gunTemperatureThresholdValue =
                    edtDcGunTemperatureThresholdValue.text.toString().toInt()
            }

            if (edtPhaseLowDetectionVoltage.text.isEmpty() || edtPhaseLowDetectionVoltage.text.toString()
                    .toInt() !in 0..400
            ) {
                isAllValid = false
                edtPhaseLowDetectionVoltage.error = "Please enter value between 0-400"
            } else {
                phaseLowDetectionVoltage = edtPhaseLowDetectionVoltage.text.toString().toInt()
            }

            if (edtPhaseHighDetectionVoltage.text.isEmpty() || edtPhaseHighDetectionVoltage.text.toString()
                    .toInt() !in 0..600
            ) {
                isAllValid = false
                edtPhaseHighDetectionVoltage.error = "Please enter value between 0-600"
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
                edtRectifierPerGroup.error = "Please enter value between 1-8"
            } else {
                rectifierPerGroup = edtRectifierPerGroup.text.toString().toInt()
            }

            if (edtRectifierMaxPower.text.isEmpty() || edtRectifierMaxPower.text.toString()
                    .toInt() !in 1..50
            ) {
                isAllValid = false
                edtRectifierMaxPower.error = "Please enter value between 1-50"
            } else {
                rectifierMaxPower = edtRectifierMaxPower.text.toString().toInt()
            }

            if (edtRectifierMaxVoltage.text.isEmpty() || edtRectifierMaxVoltage.text.toString()
                    .toInt() !in 200..1000
            ) {
                isAllValid = false
                edtRectifierMaxVoltage.error = "Please enter value between 200-1000"
            } else {
                rectifierMaxVoltage = edtRectifierMaxVoltage.text.toString().toInt()
            }

            if (edtRectifierMaxCurrent.text.isEmpty() || edtRectifierMaxCurrent.text.toString()
                    .toInt() !in 1..100
            ) {
                isAllValid = false
                edtRectifierMaxCurrent.error = "Please enter value between 1-100"
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