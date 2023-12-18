package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte

object LastChargingSummaryUtils {

     fun getSessionEndReason(buffer: ByteArray): String {
        val endReasonNumber = ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            buffer[37].getIntValueFromByte(),
            buffer[38].getIntValueFromByte()
        )
        return "Session End Reason = ${
            StateAndModesUtils.SessionEndReasons.fromStateValue(
                endReasonNumber
            ).description
        }"
    }

     fun getEnergyConsumption(buffer: ByteArray): String {
        val energyConsumption = ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(33, 33 + 4))
        return "Energy Consumption = ${energyConsumption.formatFloatToString()} kWh"
    }

     fun getStartSoc(buffer: ByteArray): String {
        val startSocLSB = buffer[29].getIntValueFromByte()
        val startSocMSB = buffer[30].getIntValueFromByte()
        return "Start Soc(%) = ${
            ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
                startSocLSB,
                startSocMSB
            )
        } %"
    }

     fun getEndSoc(buffer: ByteArray): String {
        val endSocLSB = buffer[31].getIntValueFromByte()
        val endSocMSB = buffer[32].getIntValueFromByte()
        return "End Soc(%) = ${
            ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
                endSocLSB,
                endSocMSB
            )
        } %"
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
        return "$totalChargingTime minutes"
    }

     fun getEVMacAddress(response: ByteArray): String {
        val macAddressArray = response.copyOfRange(3, 3 + 8)
        return CommonUtils.getSimpleMacAddress(macAddressArray, "-")
    }
    
}