package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte

object LastChargingSummaryUtils {

     fun getSessionEndReason(buffer: ByteArray): String {
        val endReasonNumber = ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            buffer[37].getIntValueFromByte(),
            buffer[38].getIntValueFromByte()
        )
        return StateAndModesUtils.SessionEndReasons.fromStateValue(
            endReasonNumber
        ).description
    }

    fun getCustomSessionEndReason(buffer: ByteArray): String {
        val endReasonNumber = ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            buffer[39].getIntValueFromByte(),
            buffer[40].getIntValueFromByte()
        )
        return StateAndModesUtils.SessionEndReasons.fromStateValue(
            endReasonNumber
        ).description
    }

     fun getEnergyConsumption(buffer: ByteArray): String {
        val energyConsumption = ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(33, 33 + 4))
        return energyConsumption.formatFloatToString()
    }

     fun getStartSoc(buffer: ByteArray): String {
        val startSocLSB = buffer[29].getIntValueFromByte()
        val startSocMSB = buffer[30].getIntValueFromByte()
        return "${
            ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
                startSocLSB,
                startSocMSB
            )
        }"
    }

     fun getEndSoc(buffer: ByteArray): String {
        val endSocLSB = buffer[31].getIntValueFromByte()
        val endSocMSB = buffer[32].getIntValueFromByte()
        return "${
            ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
                endSocLSB,
                endSocMSB
            )
        }"
    }

     fun getChargingEndTime(responseFrameArray: ByteArray): String {
        val chargingEndTimeArray = responseFrameArray.copyOfRange(19, 19 + 8)
        val mappedArray = chargingEndTimeArray.map { it.getIntValueFromByte() }
        val hexArray = ModbusTypeConverter.decimalArrayToHexArray(mappedArray)
        return CommonUtils.getDateAndTimeFromHexArray(hexArray)
    }

     fun getChargingStartTime(responseFrameArray: ByteArray): String {
        val chargingStartTimeArray = responseFrameArray.copyOfRange(11, 11 + 8)
        val mappedArray = chargingStartTimeArray.map { it.getIntValueFromByte() }
        val hexArray = ModbusTypeConverter.decimalArrayToHexArray(mappedArray)
        return CommonUtils.getDateAndTimeFromHexArray(hexArray)
    }

     fun getTotalChargingTime(response: ByteArray): String {
        val totalChargingTime = ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            response[27].getIntValueFromByte(),
            response[28].getIntValueFromByte()
        )
        return "$totalChargingTime"
    }

     fun getEVMacAddress(response: ByteArray): String {
        val macAddressArray = response.copyOfRange(3, 3 + 8)
        return CommonUtils.getSimpleMacAddress(macAddressArray, "-")
    }

    fun getTotalCost(response: ByteArray): String {
        val totalCost = ModbusTypeConverter.byteArrayToFloat(response.copyOfRange(41, 45))
        return "$totalCost"
    }
    
}