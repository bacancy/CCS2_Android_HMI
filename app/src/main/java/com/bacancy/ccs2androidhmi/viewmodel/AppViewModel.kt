package com.bacancy.ccs2androidhmi.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbErrorCodes
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.db.entity.TbNotifications
import com.bacancy.ccs2androidhmi.models.ErrorCodes
import com.bacancy.ccs2androidhmi.repository.MainRepository
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_START_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_START_TIME
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
import com.bacancy.ccs2androidhmi.util.PrefHelper
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

    fun insertGun1InfoInDB(it: ByteArray) {
        insertGunsChargingInfo(
            TbGunsChargingInfo(
                gunId = 1,
                gunChargingState = GunsChargingInfoUtils.getGunChargingState(it).description,
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

    fun insertGun2InfoInDB(it: ByteArray) {
        insertGunsChargingInfo(
            TbGunsChargingInfo(
                gunId = 2,
                gunChargingState = GunsChargingInfoUtils.getGunChargingState(it).description,
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
}