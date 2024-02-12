package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex

object MiscInfoUtils {

    const val NO_STATE = "No State"
    const val RFID_TAG_DETECTED = "RFID Tag Detected"
    const val RFID_TAG_VALID = "RFID Tag Valid"
    const val RFID_TAG_INVALID = "RFID Tag Invalid"
    const val TOKEN_ID_NONE = "Token ID None"
    const val TOKEN_ID_SUBMITTED = "Token ID Submitted"
    const val TOKEN_ID_VALID = "Token ID Valid"
    const val TOKEN_ID_INVALID = "Token ID Invalid"

    fun getMCUFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[9].getIntValueFromByte()
        val reg4MSB = response[11].getIntValueFromByte()
        val reg4LSB = response[12].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getOCPPFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[65].getIntValueFromByte()
        val reg4MSB = response[67].getIntValueFromByte()
        val reg4LSB = response[68].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getUnitPrice(response: ByteArray): Float {
        return ModbusTypeConverter.byteArrayToFloat(response.copyOfRange(69, 73))
    }

    fun getEmergencyButtonStatus(response: ByteArray): Int {
        val status = response.copyOfRange(49, 51).toHex()
        return if (status.contains("1")) {
            1
        } else {
            0
        }
    }

    fun getRFIDFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[75].getIntValueFromByte()
        val reg4MSB = response[77].getIntValueFromByte()
        val reg4LSB = response[78].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getLEDModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[79].getIntValueFromByte()
        val reg4MSB = response[81].getIntValueFromByte()
        val reg4LSB = response[82].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getPLC1ModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[83].getIntValueFromByte()
        val reg4MSB = response[85].getIntValueFromByte()
        val reg4LSB = response[86].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getPLC2ModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[87].getIntValueFromByte()
        val reg4MSB = response[89].getIntValueFromByte()
        val reg4LSB = response[90].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getRectifier1Code(response: ByteArray): String {
        return response.copyOfRange(29, 33).toHex()
    }

    fun getRectifier2Code(response: ByteArray): String {
        return response.copyOfRange(33, 37).toHex()
    }

    fun getRectifier3Code(response: ByteArray): String {
        return response.copyOfRange(37, 41).toHex()
    }

    fun getRectifier4Code(response: ByteArray): String {
        return response.copyOfRange(41, 45).toHex()
    }

    fun getPLC1Fault(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.copyOfRange(13, 21))
            .reversed().substring(0, 1)
    }

    fun getPLC2Fault(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.copyOfRange(21, 29))
            .reversed().substring(0, 1)
    }

    fun getCommunicationErrorCodes(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.copyOfRange(45, 49))
            .reversed().substring(0, 4)
    }

    fun getDevicePhysicalConnectionStatus(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.copyOfRange(59, 61))
            .reversed().substring(0, 6)
    }

    fun getVendorErrorCodeInformation(response: ByteArray): String {
        return response.copyOfRange(91, 95).toHex()
    }

    fun getRFIDTagState(response: ByteArray): String {
        val statusMap = mapOf(
            "1" to RFID_TAG_DETECTED,
            "2" to RFID_TAG_VALID,
            "3" to RFID_TAG_INVALID,
            "5" to TOKEN_ID_SUBMITTED,
            "6" to TOKEN_ID_VALID,
            "7" to TOKEN_ID_INVALID
        )
        val status = response.copyOfRange(73, 75).toHex()
        return statusMap.getOrElse(status.last().toString()) { "No State" }
    }

}