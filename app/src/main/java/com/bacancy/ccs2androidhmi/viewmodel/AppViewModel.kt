package com.bacancy.ccs2androidhmi.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbConfigurationParameters
import com.bacancy.ccs2androidhmi.db.entity.TbErrorCodes
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.db.entity.TbNotifications
import com.bacancy.ccs2androidhmi.db.entity.TbRectifierFaults
import com.bacancy.ccs2androidhmi.db.model.ACMeterUserDefinedFields
import com.bacancy.ccs2androidhmi.db.model.DCMeterUserDefinedFields
import com.bacancy.ccs2androidhmi.models.ErrorCodes
import com.bacancy.ccs2androidhmi.repository.MainRepository
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_START_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_START_TIME
import com.bacancy.ccs2androidhmi.util.ConfigurationParametersUtils
import com.bacancy.ccs2androidhmi.util.DateTimeUtils
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.DATE_TIME_FORMAT
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.DATE_TIME_FORMAT_FOR_UI
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.calculateDifferenceInMinutes
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.convertDateFormatToDesiredFormat
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.hexStringToDecimal
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val mainRepository: MainRepository, private val prefHelper: PrefHelper) : ViewModel() {

    val latestAcMeterInfo: LiveData<TbAcMeterInfo> = mainRepository.getLatestAcMeterInfo()

    val latestMiscInfo: LiveData<TbMiscInfo> = mainRepository.getLatestMiscInfo()
    val allRectifierFaults: LiveData<TbRectifierFaults> = mainRepository.getRectifierFaults()

    val allErrorCodes: LiveData<List<TbErrorCodes>> = mainRepository.getAllErrorCodes()
    val allNotifications: LiveData<List<TbNotifications>> = mainRepository.getAllNotifications()

    private val _deviceMacAddress = MutableStateFlow("")
    val deviceMacAddress = _deviceMacAddress.asStateFlow()

    fun updateDeviceMacAddress(macAddress: String){
        _deviceMacAddress.value = macAddress
    }

    fun getUpdatedGunsChargingInfo(gunNumber: Int): LiveData<TbGunsChargingInfo> =
        mainRepository.getGunsChargingInfoByGunNumber(gunNumber)

    fun getUpdatedGunsDCMeterInfo(gunNumber: Int): Flow<TbGunsDcMeterInfo?> =
        mainRepository.getGunsDCMeterInfoByGunNumber(gunNumber)

    fun getGunsLastChargingSummary(gunNumber: Int): LiveData<TbGunsLastChargingSummary> =
        mainRepository.getGunsLastChargingSummary(gunNumber)

    fun insertChargingSummary(chargingSummary: TbChargingHistory) {
        viewModelScope.launch {
            mainRepository.insertChargingSummary(chargingSummary)
        }
    }

    fun getChargingHistoryByGunNumber(gunNumber: Int) = mainRepository.getGunsChargingHistory(gunNumber)

    fun deleteChargingHistoryByGunId(gunNumber: Int) {
        viewModelScope.launch {
            mainRepository.deleteChargingHistoryByGunId(gunNumber)
        }
    }

    fun insertAcMeterInfo(acMeterInfo: TbAcMeterInfo) {
        viewModelScope.launch {
            mainRepository.insertAcMeterInfo(acMeterInfo)
        }
    }

    private fun insertMiscInfo(tbMiscInfo: TbMiscInfo) {
        viewModelScope.launch {
            mainRepository.insertMiscInfo(tbMiscInfo)
        }
    }

    private fun insertGunsChargingInfo(tbGunsChargingInfo: TbGunsChargingInfo) {
        viewModelScope.launch {
            mainRepository.insertGunsChargingInfo(tbGunsChargingInfo)
        }
    }

    private fun insertGunsDCMeterInfo(tbGunsDcMeterInfo: TbGunsDcMeterInfo) {
        viewModelScope.launch {
            mainRepository.insertGunsDCMeterInfo(tbGunsDcMeterInfo)
        }
    }

    private fun insertGunsLastChargingSummary(tbGunsLastChargingSummary: TbGunsLastChargingSummary) {
        viewModelScope.launch {
            mainRepository.insertGunsLastChargingSummary(tbGunsLastChargingSummary)
        }
    }

    private fun insertErrorCode(tbErrorCodes: TbErrorCodes) {
        viewModelScope.launch {
            mainRepository.insertErrorCode(tbErrorCodes)
        }
    }

    private fun getErrorCodeFromDB(
        sourceId: Int,
        sourceErrorCodes: String
    ): List<TbErrorCodes> {
        return mainRepository.getErrorCodeFromDB(sourceId, sourceErrorCodes)
    }

    fun insertNotifications(tbNotifications: TbNotifications) {
        viewModelScope.launch {
            mainRepository.insertNotifications(tbNotifications)
        }
    }

    fun insertMiscInfoInDB(it: ByteArray) {
        Log.d("observeLatestMiscInfo", "insertMiscInfoInDB -> "+MiscInfoUtils.getRFIDTagState(it))
        insertMiscInfo(
            TbMiscInfo(
                1,
                serverConnectedWith = StateAndModesUtils.checkServerConnectedWith(
                    MiscInfoUtils.getServerStatusBits(it)
                ),
                ethernetStatus = StateAndModesUtils.checkIfEthernetIsConnected(
                    MiscInfoUtils.getEthernetStatusBits(it)
                ),
                gsmLevel = StateAndModesUtils.checkGSMNetworkStrength(
                    MiscInfoUtils.getGSMStatusBits(it)
                ).toInt(),
                wifiLevel = StateAndModesUtils.checkWifiNetworkStrength(
                    MiscInfoUtils.getWifiStatusBits(it)
                ).toInt(),
                mcuFirmwareVersion = MiscInfoUtils.getMCUFirmwareVersion(it),
                ocppFirmwareVersion = MiscInfoUtils.getOCPPFirmwareVersion(it),
                rfidFirmwareVersion = MiscInfoUtils.getRFIDFirmwareVersion(it),
                ledFirmwareVersion = MiscInfoUtils.getLEDModuleFirmwareVersion(it),
                plc1FirmwareVersion = MiscInfoUtils.getPLC1ModuleFirmwareVersion(it),
                plc2FirmwareVersion = MiscInfoUtils.getPLC2ModuleFirmwareVersion(it),
                plc1Fault = MiscInfoUtils.getPLC1Fault(it),
                plc2Fault = MiscInfoUtils.getPLC2Fault(it),
                rectifier1Fault = MiscInfoUtils.getRectifier1Code(it),
                rectifier2Fault = MiscInfoUtils.getRectifier2Code(it),
                rectifier3Fault = MiscInfoUtils.getRectifier3Code(it),
                rectifier4Fault = MiscInfoUtils.getRectifier4Code(it),
                communicationError = MiscInfoUtils.getCommunicationErrorCodes(it),
                devicePhysicalConnectionStatus = MiscInfoUtils.getDevicePhysicalConnectionStatus(
                    it
                ),
                unitPrice = MiscInfoUtils.getUnitPrice(it),
                emergencyButtonStatus = MiscInfoUtils.getEmergencyButtonStatus(it),
                rfidTagState = MiscInfoUtils.getRFIDTagState(it)
            )
        )

        processChargerErrorCodes(
            0,
            ModbusTypeConverter.hexToBinary(MiscInfoUtils.getVendorErrorCodeInformation(it))
        )
    }

    private fun processChargerErrorCodes(errorSource: Int, errorCodeString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val reversedString = errorCodeString.reversed()
            val errorCodesToAvoid = listOf(2, 6, 9, 11, 12, 14, 16, 19, 23, 24, 34, 35, 36)
            StateAndModesUtils.GunsErrorCode.values().forEachIndexed { index, gunsErrorCode ->
                if (index < reversedString.length) {
                    val errorCodeList = getErrorCodeFromDB(errorSource, gunsErrorCode.name)
                    if (gunsErrorCode.value in errorCodesToAvoid) {
                        //If we get error code from the list of error codes to avoid, then we will directly insert them in the DB
                        if (reversedString[index] == '1' && errorCodeList.isEmpty()) {
                            insertErrorCodesWithValues(errorSource, gunsErrorCode.name, 1)
                        }
                    } else {
                        //If we get error codes to not avoid then we will make the comparison and insert them in the DB
                        if (reversedString[index] == '1') {
                            if (errorCodeList.isEmpty()) {
                                insertErrorCodesWithValues(errorSource, gunsErrorCode.name, 1)
                            } else {
                                if (errorCodeList[errorCodeList.size - 1].sourceErrorValue == 0) {
                                    insertErrorCodesWithValues(errorSource, gunsErrorCode.name, 1)
                                }
                            }
                        } else if (reversedString[index] == '0' && errorCodeList.isNotEmpty() && errorCodeList[errorCodeList.size - 1].sourceErrorValue == 1) {
                            insertErrorCodesWithValues(errorSource, gunsErrorCode.name, 0)
                        }
                    }
                }
            }
        }
    }

    private fun insertErrorCodesWithValues(
        errorSource: Int,
        errorCodeName: String,
        errorCodeValue: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            insertErrorCode(
                TbErrorCodes(
                    sourceId = errorSource,
                    sourceErrorCodes = errorCodeName,
                    sourceErrorValue = errorCodeValue,
                    sourceErrorDateTime = DateTimeUtils.getCurrentDateTime()
                        .convertDateFormatToDesiredFormat(
                            currentFormat = DATE_TIME_FORMAT,
                            desiredFormat = DATE_TIME_FORMAT_FOR_UI
                        )
                )
            )
        }
    }

    fun insertGun1InfoInDB(it: ByteArray, context: Context) {
        insertGunsChargingInfo(
            TbGunsChargingInfo(
                gunId = 1,
                gunChargingState = "",
                gunChargingStateToSave = GunsChargingInfoUtils.getGunChargingState(it).descriptionToSave,
                gunChargingStateToShow = context.getString(GunsChargingInfoUtils.getGunChargingState(it).descriptionToShow),
                initialSoc = GunsChargingInfoUtils.getInitialSoc(it),
                chargingSoc = GunsChargingInfoUtils.getChargingSoc(it),
                demandVoltage = GunsChargingInfoUtils.getDemandVoltage(it),
                demandCurrent = GunsChargingInfoUtils.getDemandCurrent(it),
                chargingVoltage = GunsChargingInfoUtils.getChargingVoltage(it),
                chargingCurrent = GunsChargingInfoUtils.getChargingCurrent(it),
                duration = GunsChargingInfoUtils.getChargingDuration(it),
                energyConsumption = GunsChargingInfoUtils.getChargingEnergyConsumption(it),
                totalCost = GunsChargingInfoUtils.getTotalCost(it),
                gunTemperatureDCPositive = GunsChargingInfoUtils.getGunTemperatureDCPositive(it),
                gunTemperatureDCNegative = GunsChargingInfoUtils.getGunTemperatureDCNegative(it)
            )
        )

        processChargerErrorCodes(
            1,
            ModbusTypeConverter.hexToBinary(
                GunsChargingInfoUtils.getGunSpecificErrorCodeInformation(it)
            )
        )
    }

    fun insertGun1ChargingHistoryInDB(it: ByteArray) {
        val chargingSummary = TbChargingHistory(
            gunNumber = 1,
            evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
            chargingStartTime = prefHelper.getStringValue(GUN_1_CHARGING_START_TIME,""),
            chargingEndTime = prefHelper.getStringValue(GUN_1_CHARGING_END_TIME,""),
            totalChargingTime = calculateDifferenceInMinutes(prefHelper.getStringValue(GUN_1_CHARGING_START_TIME,""),
                prefHelper.getStringValue(GUN_1_CHARGING_END_TIME,"")),
            startSoc = LastChargingSummaryUtils.getStartSoc(it),
            endSoc = LastChargingSummaryUtils.getEndSoc(it),
            energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(it),
            sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(it),
            customSessionEndReason = "NA",
            totalCost = LastChargingSummaryUtils.getTotalCost(it)
        )
        insertChargingSummary(chargingSummary)
    }

    fun insertGun1LastChargingSummaryInDB(it: ByteArray) {
        insertGunsLastChargingSummary(
            TbGunsLastChargingSummary(
                gunId = 1,
                evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
                chargingDuration = calculateDifferenceInMinutes(prefHelper.getStringValue(GUN_1_CHARGING_START_TIME,""),
                    prefHelper.getStringValue(GUN_1_CHARGING_END_TIME,"")),
                chargingStartDateTime = prefHelper.getStringValue(GUN_1_CHARGING_START_TIME,""),
                chargingEndDateTime = prefHelper.getStringValue(GUN_1_CHARGING_END_TIME,""),
                startSoc = LastChargingSummaryUtils.getStartSoc(it),
                endSoc = LastChargingSummaryUtils.getEndSoc(it),
                energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(
                    it
                ),
                sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(
                    it
                ),
                totalCost = LastChargingSummaryUtils.getTotalCost(it)
            )
        )
    }

    fun insertGun1DCMeterInfoInDB(it: ByteArray) {
        val newResponse = ModBusUtils.parseInputRegistersResponse(it)
        if (newResponse.isNotEmpty()) {
            insertGunsDCMeterInfo(
                TbGunsDcMeterInfo(
                    gunId = 1,
                    voltage = newResponse[0],
                    current = newResponse[1],
                    power = newResponse[2],
                    importEnergy = newResponse[3],
                    exportEnergy = newResponse[4],
                    maxVoltage = newResponse[5],
                    minVoltage = newResponse[6],
                    maxCurrent = newResponse[7],
                    minCurrent = newResponse[8]
                )
            )
        }
    }

    fun insertGun2InfoInDB(it: ByteArray, context: Context) {
        insertGunsChargingInfo(
            TbGunsChargingInfo(
                gunId = 2,
                gunChargingState = "",
                gunChargingStateToSave = GunsChargingInfoUtils.getGunChargingState(it).descriptionToSave,
                gunChargingStateToShow = context.getString(GunsChargingInfoUtils.getGunChargingState(it).descriptionToShow),
                initialSoc = GunsChargingInfoUtils.getInitialSoc(it),
                chargingSoc = GunsChargingInfoUtils.getChargingSoc(it),
                demandVoltage = GunsChargingInfoUtils.getDemandVoltage(it),
                demandCurrent = GunsChargingInfoUtils.getDemandCurrent(it),
                chargingVoltage = GunsChargingInfoUtils.getChargingVoltage(it),
                chargingCurrent = GunsChargingInfoUtils.getChargingCurrent(it),
                duration = GunsChargingInfoUtils.getChargingDuration(it),
                energyConsumption = GunsChargingInfoUtils.getChargingEnergyConsumption(it),
                totalCost = GunsChargingInfoUtils.getTotalCost(it),
                gunTemperatureDCPositive = GunsChargingInfoUtils.getGunTemperatureDCPositive(it),
                gunTemperatureDCNegative = GunsChargingInfoUtils.getGunTemperatureDCNegative(it)
            )
        )

        processChargerErrorCodes(
            2,
            ModbusTypeConverter.hexToBinary(
                GunsChargingInfoUtils.getGunSpecificErrorCodeInformation(it)
            )
        )
    }

    fun insertGun2ChargingHistoryInDB(it: ByteArray) {
        val chargingSummary = TbChargingHistory(
            gunNumber = 2,
            evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
            chargingStartTime = prefHelper.getStringValue(GUN_2_CHARGING_START_TIME,""),
            chargingEndTime = prefHelper.getStringValue(GUN_2_CHARGING_END_TIME,""),
            totalChargingTime = calculateDifferenceInMinutes(prefHelper.getStringValue(GUN_2_CHARGING_START_TIME,""),
                prefHelper.getStringValue(GUN_2_CHARGING_END_TIME,"")),
            startSoc = LastChargingSummaryUtils.getStartSoc(it),
            endSoc = LastChargingSummaryUtils.getEndSoc(it),
            energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(it),
            sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(it),
            customSessionEndReason = "NA",
            totalCost = LastChargingSummaryUtils.getTotalCost(it)
        )
        insertChargingSummary(chargingSummary)
    }

    fun insertGun2LastChargingSummaryInDB(it: ByteArray) {
        insertGunsLastChargingSummary(
            TbGunsLastChargingSummary(
                gunId = 2,
                evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
                chargingDuration = calculateDifferenceInMinutes(prefHelper.getStringValue(GUN_2_CHARGING_START_TIME,""),
                    prefHelper.getStringValue(GUN_2_CHARGING_END_TIME,"")),
                chargingStartDateTime = prefHelper.getStringValue(GUN_2_CHARGING_START_TIME,""),
                chargingEndDateTime = prefHelper.getStringValue(GUN_2_CHARGING_END_TIME,""),
                startSoc = LastChargingSummaryUtils.getStartSoc(it),
                endSoc = LastChargingSummaryUtils.getEndSoc(it),
                energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(
                    it
                ),
                sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(
                    it
                ),
                totalCost = LastChargingSummaryUtils.getTotalCost(it)
            )
        )
    }

    fun insertGun2DCMeterInfoInDB(it: ByteArray) {
        val newResponse = ModBusUtils.parseInputRegistersResponse(it)
        if (newResponse.isNotEmpty()) {
            insertGunsDCMeterInfo(
                TbGunsDcMeterInfo(
                    gunId = 2,
                    voltage = newResponse[0],
                    current = newResponse[1],
                    power = newResponse[2],
                    importEnergy = newResponse[3],
                    exportEnergy = newResponse[4],
                    maxVoltage = newResponse[5],
                    minVoltage = newResponse[6],
                    maxCurrent = newResponse[7],
                    minCurrent = newResponse[8]
                )
            )
        }
    }

    fun getAbnormalErrorCodesList(
        errorCodeString: String,
        type: Int,
        errorDateTime: String
    ): MutableList<ErrorCodes> {

        // Reverse the string so that the LSB (Least Significant Bit) corresponds to the first index
        val reversedString = errorCodeString.reversed()
        val errorCodesList = mutableListOf<ErrorCodes>()

        val errorSource = when (type) {
            0 -> "Charger"
            1 -> "Gun 1"
            2 -> "Gun 2"
            else -> "Unknown"
        }

        StateAndModesUtils.GunsErrorCode.values().forEachIndexed { index, gunsErrorCode ->
            if (index < reversedString.length && reversedString[index] == '1') {
                errorCodesList.add(
                    ErrorCodes(
                        gunsErrorCode.value,
                        gunsErrorCode.name,
                        errorSource,
                        "", 1,
                        errorDateTime
                    )
                )
            }
        }

        return errorCodesList
    }

    fun insertConfigurationParametersInDB(it: ByteArray) {

        Log.d("CDM_TAG","Charge Control Mode = ${ConfigurationParametersUtils.getChargeControlMode(it)}")

        Log.d("CDM_TAG","Rectifier Selection = ${ConfigurationParametersUtils.getRectifierSelection(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","Number of Rectifier Per Group = ${ConfigurationParametersUtils.getNumberOfRectifierPerGroup(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","Rectifier Max Voltage = ${ConfigurationParametersUtils.getRectifierMaxVoltage(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","Rectifier Max Power = ${ConfigurationParametersUtils.getRectifierMaxPower(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","Rectifier Max Current = ${ConfigurationParametersUtils.getRectifierMaxCurrent(it).hexStringToDecimal()}")

        val tbConfigurationParameters = TbConfigurationParameters(
            id = 1,
            chargeControlMode = ConfigurationParametersUtils.getChargeControlModeValue(it),
            selectedRectifier = ConfigurationParametersUtils.getRectifierSelection(it).hexStringToDecimal(),
            numberOfRectifierPerGroup = ConfigurationParametersUtils.getNumberOfRectifierPerGroup(it).hexStringToDecimal(),
            maxDCOutputPowerCapacity = ConfigurationParametersUtils.getMaxDCOutputPowerCapacityOfCharger(it).hexStringToDecimal(),
            rectifierMaxPower = ConfigurationParametersUtils.getRectifierMaxPower(it).hexStringToDecimal(),
            rectifierMaxVoltage = ConfigurationParametersUtils.getRectifierMaxVoltage(it).hexStringToDecimal(),
            rectifierMaxCurrent = ConfigurationParametersUtils.getRectifierMaxCurrent(it).hexStringToDecimal(),
            selectedACMeter = ConfigurationParametersUtils.getACMeterSelection(it).hexStringToDecimal(),
            acMeterDataConfiguration = ConfigurationParametersUtils.getACMeterDataConfiguration(it),
            isACMeterMandatory = ConfigurationParametersUtils.getACMeterMandatory(it),
            selectedDCMeter = ConfigurationParametersUtils.getDCMeterSelection(it).hexStringToDecimal(),
            dcMeterDataConfiguration = ConfigurationParametersUtils.getDCMeterDataConfiguration(it),
            isDCMeterMandatory = ConfigurationParametersUtils.getDCMeterMandatory(it),
            spdFaultDetection = ConfigurationParametersUtils.getSPDFaultDetection(it),
            smokeFaultDetection = ConfigurationParametersUtils.getSmokeFaultDetection(it),
            tamperFaultDetection = ConfigurationParametersUtils.getTamperFaultDetection(it),
            ledModuleFaultDetection = ConfigurationParametersUtils.getLEDModuleFaultDetection(it),
            gunTempFaultDetection = ConfigurationParametersUtils.getGunTemperatureFaultDetection(it),
            isolationFaultDetection = ConfigurationParametersUtils.getIsolationFaultDetection(it),
            gunTemperatureThresholdValue = ConfigurationParametersUtils.getDCGunTemperatureThresholdValue(it).hexStringToDecimal(),
            phaseLowDetectionVoltage = ConfigurationParametersUtils.getPhaseLowDetectionVoltage(it).hexStringToDecimal(),
            phaseHighDetectionVoltage = ConfigurationParametersUtils.getPhaseHighDetectionVoltage(it).hexStringToDecimal(),
            acMeterUserDefinedFields = if(ConfigurationParametersUtils.getACMeterSelection(it).hexStringToDecimal() == 0) ACMeterUserDefinedFields(
                voltageV1N = ConfigurationParametersUtils.getVoltageV1NRegisterAddress(it).toInt(),
                voltageV2N = ConfigurationParametersUtils.getVoltageV2NRegisterAddress(it).toInt(),
                voltageV3N = ConfigurationParametersUtils.getVoltageV3NRegisterAddress(it).toInt(),
                avgVoltageLN = ConfigurationParametersUtils.getAvgVoltageLNRegisterAddress(it).toInt(),
                frequency = ConfigurationParametersUtils.getFrequencyRegisterAddress(it).toInt(),
                avgPF = ConfigurationParametersUtils.getAvgPFRegisterAddress(it).toInt(),
                currentL1 = ConfigurationParametersUtils.getCurrentL1RegisterAddress(it).toInt(),
                currentL2 = ConfigurationParametersUtils.getCurrentL2RegisterAddress(it).toInt(),
                currentL3 = ConfigurationParametersUtils.getCurrentL3RegisterAddress(it).toInt(),
                avgCurrent = ConfigurationParametersUtils.getAvgCurrentRegisterAddress(it).toInt(),
                activePower = ConfigurationParametersUtils.getActivePowerRegisterAddress(it).toInt(),
                totalEnergy = ConfigurationParametersUtils.getTotalEnergyRegisterAddress(it).toInt(),
                totalReactiveEnergy = ConfigurationParametersUtils.getTotalReactiveEnergyRegisterAddress(it).toInt()
            ) else null,
            dcMeterUserDefinedFields = if(ConfigurationParametersUtils.getDCMeterSelection(it).hexStringToDecimal() == 0) DCMeterUserDefinedFields(
                voltageParameter = ConfigurationParametersUtils.getVoltageRegisterAddress(it).toInt(),
                currentParameter = ConfigurationParametersUtils.getCurrentRegisterAddress(it).toInt(),
                powerParameter = ConfigurationParametersUtils.getPowerRegisterAddress(it).toInt(),
                importEnergyParameter = ConfigurationParametersUtils.getImportEnergyRegisterAddress(it).toInt(),
                exportEnergyParameter = ConfigurationParametersUtils.getExportEnergyRegisterAddress(it).toInt(),
                maxVoltageParameter = ConfigurationParametersUtils.getMaxVoltageRegisterAddress(it).toInt(),
                minVoltageParameter = ConfigurationParametersUtils.getMinVoltageRegisterAddress(it).toInt(),
                maxCurrent = ConfigurationParametersUtils.getMaxCurrentRegisterAddress(it).toInt(),
                minCurrent = ConfigurationParametersUtils.getMinCurrentRegisterAddress(it).toInt()
            ) else null
        )

        viewModelScope.launch {
            mainRepository.insertConfigurationParameters(tbConfigurationParameters)
        }

        Log.d("CDM_TAG","Config Access Params Key = ${ConfigurationParametersUtils.getConfigAccessKey(it)}")
        Log.d("CDM_TAG","Max DC Output Power Capacity of Charger = ${ConfigurationParametersUtils.getMaxDCOutputPowerCapacityOfCharger(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","AC Meter Selection = ${ConfigurationParametersUtils.getACMeterSelection(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","AC Meter Data Configuration = ${ConfigurationParametersUtils.getACMeterDataConfiguration(it)}")
        Log.d("CDM_TAG","ACMDC Data Type = ${ConfigurationParametersUtils.getACMeterDataType(it)}")
        Log.d("CDM_TAG","ACMDC Data Endianness = ${ConfigurationParametersUtils.getACMeterDataEndianness(it)}")
        Log.d("CDM_TAG","ACMDC Read Function = ${ConfigurationParametersUtils.getACMeterReadFunction(it)}")
        Log.d("CDM_TAG","ACMDC Data in Watt/KW = ${ConfigurationParametersUtils.getACMeterDataTypeInWattOrKW(it)}")
        Log.d("CDM_TAG","ACMDC Mandatory Yes/No = ${ConfigurationParametersUtils.getACMeterMandatory(it)}")

        Log.d("CDM_TAG","DC Meter Selection = ${ConfigurationParametersUtils.getDCMeterSelection(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","DC Meter Data Configuration = ${ConfigurationParametersUtils.getDCMeterDataConfiguration(it)}")
        Log.d("CDM_TAG","DCMDC Data Type = ${ConfigurationParametersUtils.getDCMeterDataType(it)}")
        Log.d("CDM_TAG","DCMDC Data Endianness = ${ConfigurationParametersUtils.getDCMeterDataEndianness(it)}")
        Log.d("CDM_TAG","DCMDC Read Function = ${ConfigurationParametersUtils.getDCMeterReadFunction(it)}")
        Log.d("CDM_TAG","DCMDC Data in Watt/KW = ${ConfigurationParametersUtils.getDCMeterDataTypeInWattOrKW(it)}")
        Log.d("CDM_TAG","DCMDC Mandatory Yes/No = ${ConfigurationParametersUtils.getDCMeterMandatory(it)}")

        Log.d("CDM_TAG","Fault Detection Enable/Disable = ${ConfigurationParametersUtils.getFaultDetectionEnableDisable(it)}")
        Log.d("CDM_TAG","SPD Fault Detection = ${ConfigurationParametersUtils.getSPDFaultDetection(it)}")
        Log.d("CDM_TAG","Smoke Fault Detection = ${ConfigurationParametersUtils.getSmokeFaultDetection(it)}")
        Log.d("CDM_TAG","Tamper Fault Detection = ${ConfigurationParametersUtils.getTamperFaultDetection(it)}")
        Log.d("CDM_TAG","LED Module Fault Detection = ${ConfigurationParametersUtils.getLEDModuleFaultDetection(it)}")
        Log.d("CDM_TAG","Gun Temp Fault Detection = ${ConfigurationParametersUtils.getGunTemperatureFaultDetection(it)}")
        Log.d("CDM_TAG","Isolation Fault Detection = ${ConfigurationParametersUtils.getIsolationFaultDetection(it)}")

        Log.d("CDM_TAG","Voltage V1N = ${ConfigurationParametersUtils.getVoltageV1NRegisterAddress(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","Voltage V2N = ${ConfigurationParametersUtils.getVoltageV2NRegisterAddress(it).hexStringToDecimal()}")
        Log.d("CDM_TAG","Voltage V3N = ${ConfigurationParametersUtils.getVoltageV3NRegisterAddress(it).hexStringToDecimal()}")


    }

    val getConfigurationParameters: LiveData<List<TbConfigurationParameters>> = mainRepository.getAllConfigurationParameters()

    fun insertRectifierFaultsInDB(it: ByteArray) {
        insertRectifierFaults(
            TbRectifierFaults(
                1,
                rectifier5Fault = RectifierFaultsUtils.getRectifier5Fault(it),
                rectifier6Fault = RectifierFaultsUtils.getRectifier6Fault(it),
                rectifier7Fault = RectifierFaultsUtils.getRectifier7Fault(it),
                rectifier8Fault = RectifierFaultsUtils.getRectifier8Fault(it),
                rectifier9Fault = RectifierFaultsUtils.getRectifier9Fault(it),
                rectifier10Fault = RectifierFaultsUtils.getRectifier10Fault(it),
                rectifier11Fault = RectifierFaultsUtils.getRectifier11Fault(it),
                rectifier12Fault = RectifierFaultsUtils.getRectifier12Fault(it),
                rectifier13Fault = RectifierFaultsUtils.getRectifier13Fault(it),
                rectifier14Fault = RectifierFaultsUtils.getRectifier14Fault(it),
                rectifier15Fault = RectifierFaultsUtils.getRectifier15Fault(it),
                rectifier16Fault = RectifierFaultsUtils.getRectifier16Fault(it)
            )
        )
    }

    private fun insertRectifierFaults(tbRectifierFaults: TbRectifierFaults) {
        viewModelScope.launch {
            mainRepository.insertRectifierFaults(tbRectifierFaults)
        }
    }
}