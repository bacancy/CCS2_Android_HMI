package com.bacancy.ccs2androidhmi.viewmodel

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
import com.bacancy.ccs2androidhmi.models.ErrorCodes
import com.bacancy.ccs2androidhmi.repository.MainRepository
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {

    val latestAcMeterInfo: LiveData<TbAcMeterInfo> = mainRepository.getLatestAcMeterInfo()

    val latestMiscInfo: LiveData<TbMiscInfo> = mainRepository.getLatestMiscInfo()

    val allErrorCodes: LiveData<List<TbErrorCodes>> = mainRepository.getAllErrorCodes()

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

        insertErrorCode(
            TbErrorCodes(
                0,
                ModbusTypeConverter.hexToBinary(MiscInfoUtils.getVendorErrorCodeInformation(it))
            )
        )
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

        insertErrorCode(
            TbErrorCodes(
                1,
                ModbusTypeConverter.hexToBinary(
                    GunsChargingInfoUtils.getGunSpecificErrorCodeInformation(
                        it
                    )
                )
            )
        )
    }

    fun insertGun1ChargingHistoryInDB(it: ByteArray) {
        val chargingSummary = TbChargingHistory(
            gunNumber = 1,
            evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
            chargingStartTime = LastChargingSummaryUtils.getChargingStartTime(it),
            chargingEndTime = LastChargingSummaryUtils.getChargingEndTime(it),
            totalChargingTime = LastChargingSummaryUtils.getTotalChargingTime(it),
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
                chargingDuration = LastChargingSummaryUtils.getTotalChargingTime(
                    it
                ),
                chargingStartDateTime = LastChargingSummaryUtils.getChargingStartTime(
                    it
                ),
                chargingEndDateTime = LastChargingSummaryUtils.getChargingEndTime(
                    it
                ),
                startSoc = LastChargingSummaryUtils.getStartSoc(it),
                endSoc = LastChargingSummaryUtils.getEndSoc(it),
                energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(
                    it
                ),
                sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(
                    it
                )
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

        insertErrorCode(
            TbErrorCodes(
                2,
                ModbusTypeConverter.hexToBinary(
                    GunsChargingInfoUtils.getGunSpecificErrorCodeInformation(
                        it
                    )
                )
            )
        )
    }

    fun insertGun2ChargingHistoryInDB(it: ByteArray) {
        val chargingSummary = TbChargingHistory(
            gunNumber = 2,
            evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
            chargingStartTime = LastChargingSummaryUtils.getChargingStartTime(it),
            chargingEndTime = LastChargingSummaryUtils.getChargingEndTime(it),
            totalChargingTime = LastChargingSummaryUtils.getTotalChargingTime(it),
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
                chargingDuration = LastChargingSummaryUtils.getTotalChargingTime(
                    it
                ),
                chargingStartDateTime = LastChargingSummaryUtils.getChargingStartTime(
                    it
                ),
                chargingEndDateTime = LastChargingSummaryUtils.getChargingEndTime(
                    it
                ),
                startSoc = LastChargingSummaryUtils.getStartSoc(it),
                endSoc = LastChargingSummaryUtils.getEndSoc(it),
                energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(
                    it
                ),
                sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(
                    it
                )
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
        type: Int
    ): MutableList<ErrorCodes> {

        // Reverse the string so that the LSB (Least Significant Bit) corresponds to the first index
        val reversedString = errorCodeString.reversed()

        val abnormalErrors = mutableListOf<StateAndModesUtils.GunsErrorCode>()
        val normalErrors = mutableListOf<StateAndModesUtils.GunsErrorCode>()

        for (index in StateAndModesUtils.GunsErrorCode.values().indices) {
            val char = if (index < reversedString.length) reversedString[index] else '0'
            val errorCode = StateAndModesUtils.GunsErrorCode.values()[index]
            if (char == '1') {
                abnormalErrors.add(errorCode)
            } else {
                normalErrors.add(errorCode)
            }
        }

        val newErrorCodesList = mutableListOf<ErrorCodes>()
        abnormalErrors.forEachIndexed { index, gunsErrorCode ->
            when (type) {
                0 -> {
                    newErrorCodesList.add(
                        ErrorCodes(
                            gunsErrorCode.value,
                            gunsErrorCode.name,
                            "Charger"
                        )
                    )
                }

                1 -> {
                    newErrorCodesList.add(
                        ErrorCodes(
                            gunsErrorCode.value,
                            gunsErrorCode.name,
                            "Gun 1"
                        )
                    )
                }

                2 -> {
                    newErrorCodesList.add(
                        ErrorCodes(
                            gunsErrorCode.value,
                            gunsErrorCode.name,
                            "Gun 2"
                        )
                    )
                }
            }
        }

        return newErrorCodesList
    }
}