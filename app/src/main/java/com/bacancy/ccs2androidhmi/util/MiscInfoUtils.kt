package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex

object MiscInfoUtils {

    fun getMCUFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[9].getIntValueFromByte()
        val reg3LSB = response[10].getIntValueFromByte()
        val reg4MSB = response[11].getIntValueFromByte()
        val reg4LSB = response[12].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getOCPPFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[65].getIntValueFromByte()
        val reg3LSB = response[66].getIntValueFromByte()
        val reg4MSB = response[67].getIntValueFromByte()
        val reg4LSB = response[68].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getRFIDFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[75].getIntValueFromByte()
        val reg3LSB = response[76].getIntValueFromByte()
        val reg4MSB = response[77].getIntValueFromByte()
        val reg4LSB = response[78].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getLEDModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[79].getIntValueFromByte()
        val reg3LSB = response[80].getIntValueFromByte()
        val reg4MSB = response[81].getIntValueFromByte()
        val reg4LSB = response[82].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getPLC1ModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[83].getIntValueFromByte()
        val reg3LSB = response[84].getIntValueFromByte()
        val reg4MSB = response[85].getIntValueFromByte()
        val reg4LSB = response[86].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    fun getPLC2ModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[87].getIntValueFromByte()
        val reg3LSB = response[88].getIntValueFromByte()
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

}