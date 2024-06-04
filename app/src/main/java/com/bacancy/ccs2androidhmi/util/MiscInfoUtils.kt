package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.AMBIENT_TEMPERATURE_BITS
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.BLUETOOTH_MAC_ADDRESS
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.COMMUNICATION_ERROR_CODES
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.DEVICE_PHYSICAL_CONNECTION_STATUS
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.EMERGENCY_BUTTON_STATUS
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.ETHERNET_STATUS_BITS
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.GSM_STATUS_BITS
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.NETWORK_STATUS_DATA
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.PLC1_FAULT
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.PLC2_FAULT
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.RECTIFIER_1_CODE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.RECTIFIER_2_CODE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.RECTIFIER_3_CODE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.RECTIFIER_4_CODE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.RFID_TAG_STATE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.SERVER_STATUS_BITS
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.UNIT_PRICE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.VENDOR_ERROR_CODE_INFORMATION
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.ByteRanges.WIFI_STATUS_BITS
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.FirmwareIndices.LED_MODULE_START_INDEX
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.FirmwareIndices.MCU_START_INDEX
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.FirmwareIndices.OCPP_START_INDEX
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.FirmwareIndices.PLC1_MODULE_START_INDEX
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.FirmwareIndices.PLC2_MODULE_START_INDEX
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.FirmwareIndices.RFID_START_INDEX
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getRangedArray
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex

object MiscInfoUtils {

    object ByteRanges {
        // Response byte ranges for various methods
        val EMERGENCY_BUTTON_STATUS = 49..50
        val UNIT_PRICE = 69..72
        val RECTIFIER_1_CODE = 29..32
        val RECTIFIER_2_CODE = 33..36
        val RECTIFIER_3_CODE = 37..40
        val RECTIFIER_4_CODE = 41..44
        val PLC1_FAULT = 13..20
        val PLC2_FAULT = 21..28
        val COMMUNICATION_ERROR_CODES = 45..48
        val DEVICE_PHYSICAL_CONNECTION_STATUS = 59..60
        val VENDOR_ERROR_CODE_INFORMATION = 91..98
        val RFID_TAG_STATE = 73..74
        val NETWORK_STATUS_DATA = 3..4
        val AMBIENT_TEMPERATURE_BITS = 5..8
        val WIFI_STATUS_BITS = 0..2
        val GSM_STATUS_BITS = 3..6
        val ETHERNET_STATUS_BITS = 7..7
        val SERVER_STATUS_BITS = 8..10
        val BLUETOOTH_MAC_ADDRESS = 135..140
    }

    object FirmwareIndices {
        const val MCU_START_INDEX = 9
        const val OCPP_START_INDEX = 65
        const val RFID_START_INDEX = 75
        const val LED_MODULE_START_INDEX = 79
        const val PLC1_MODULE_START_INDEX = 83
        const val PLC2_MODULE_START_INDEX = 87
    }

    const val NO_STATE = "No State"
    private const val RFID_TAG_DETECTED = "RFID Tag Detected"
    private const val RFID_TAG_VALID = "RFID Tag Valid"
    private const val RFID_TAG_INVALID = "RFID Tag Invalid"
    const val TOKEN_ID_NONE = "Token ID None"
    private const val TOKEN_ID_SUBMITTED = "Token ID Submitted"
    private const val TOKEN_ID_VALID = "Token ID Valid"
    private const val TOKEN_ID_INVALID = "Token ID Invalid"
    private const val DUAL_SOCKET_ENABLED = "Dual Socket Enabled"
    private const val DUAL_SOCKET_DISABLED = "Dual Socket Disabled"

    fun getAmbientTemperature(response: ByteArray): Float {
        return ModbusTypeConverter.byteArrayToFloat(response.getRangedArray(AMBIENT_TEMPERATURE_BITS))
    }

    fun getBluetoothMacAddress(response: ByteArray): String {
        val macAddressArray = response.getRangedArray(BLUETOOTH_MAC_ADDRESS)
        return CommonUtils.getSwappedMacAddress(macAddressArray, ":")
    }

    private fun getFirmwareVersion(response: ByteArray, startIndex: Int): String {
        val reg3MSB = response[startIndex].getIntValueFromByte()
        val reg4MSB = response[startIndex + 2].getIntValueFromByte()
        val reg4LSB = response[startIndex + 3].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getMCUFirmwareVersion(response: ByteArray): String {
        return getFirmwareVersion(response, MCU_START_INDEX)
    }

    fun getOCPPFirmwareVersion(response: ByteArray): String {
        return getFirmwareVersion(response, OCPP_START_INDEX)
    }


    fun getRFIDFirmwareVersion(response: ByteArray): String {
        return getFirmwareVersion(response, RFID_START_INDEX)
    }

    fun getLEDModuleFirmwareVersion(response: ByteArray): String {
        return getFirmwareVersion(response, LED_MODULE_START_INDEX)
    }

    fun getPLC1ModuleFirmwareVersion(response: ByteArray): String {
        return getFirmwareVersion(response, PLC1_MODULE_START_INDEX)
    }

    fun getPLC2ModuleFirmwareVersion(response: ByteArray): String {
        return getFirmwareVersion(response, PLC2_MODULE_START_INDEX)
    }

    fun getEmergencyButtonStatus(response: ByteArray): Int {
        val status = response.getRangedArray(EMERGENCY_BUTTON_STATUS).toHex()
        return if (status.contains("1")) {
            1
        } else {
            0
        }
    }

    fun getUnitPrice(response: ByteArray): Float {
        return ModbusTypeConverter.byteArrayToFloat(response.getRangedArray(UNIT_PRICE))
    }

    fun getRectifier1Code(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_1_CODE).toHex()
    }

    fun getRectifier2Code(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_2_CODE).toHex()
    }

    fun getRectifier3Code(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_3_CODE).toHex()
    }

    fun getRectifier4Code(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_4_CODE).toHex()
    }

    fun getPLC1Fault(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.getRangedArray(PLC1_FAULT))
            .reversed().substring(0, 1)
    }

    fun getPLC2Fault(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.getRangedArray(PLC2_FAULT))
            .reversed().substring(0, 1)
    }

    fun getCommunicationErrorCodes(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.getRangedArray(
            COMMUNICATION_ERROR_CODES))
            .reversed().substring(0, 4)
    }

    fun getDevicePhysicalConnectionStatus(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.getRangedArray(
            DEVICE_PHYSICAL_CONNECTION_STATUS))
            .reversed().substring(0, 6)
    }

    fun getVendorErrorCodeInformation(response: ByteArray): String {
        return response.getRangedArray(VENDOR_ERROR_CODE_INFORMATION).toHex()
    }

    fun getRFIDTagState(response: ByteArray): String {
        val statusMap = mapOf(
            "1" to RFID_TAG_DETECTED,
            "2" to RFID_TAG_VALID,
            "3" to RFID_TAG_INVALID,
            "5" to TOKEN_ID_SUBMITTED,
            "6" to TOKEN_ID_VALID,
            "7" to TOKEN_ID_INVALID,
            "8" to DUAL_SOCKET_ENABLED,
            "9" to DUAL_SOCKET_DISABLED,
        )
        val status = response.getRangedArray(RFID_TAG_STATE).toHex()
        return statusMap.getOrElse(status.last().toString()) { "No State" }
    }

    private fun getNetworkStatusData(response: ByteArray): CharArray {
        return ModbusTypeConverter.byteArrayToBinaryString(response.getRangedArray(
            NETWORK_STATUS_DATA))
            .reversed()
            .substring(0, 11).toCharArray()
    }

    fun getWifiStatusBits(response: ByteArray): CharArray {
        return getNetworkStatusData(response).getRangedArray(WIFI_STATUS_BITS)
    }

    fun getGSMStatusBits(response: ByteArray): CharArray {
        return getNetworkStatusData(response).getRangedArray(GSM_STATUS_BITS)
    }

    fun getEthernetStatusBits(response: ByteArray): CharArray {
        return getNetworkStatusData(response).getRangedArray(ETHERNET_STATUS_BITS)
    }

    fun getServerStatusBits(response: ByteArray): CharArray {
        return getNetworkStatusData(response).getRangedArray(SERVER_STATUS_BITS)
    }

}