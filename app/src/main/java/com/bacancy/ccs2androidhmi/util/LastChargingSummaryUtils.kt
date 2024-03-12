package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getRangedArray

object LastChargingSummaryUtils {

    object ByteRanges {
        // Response byte ranges for various methods
        val SESSION_END_REASON = 37..38
        val CUSTOM_SESSION_END_REASON = 39..40
        val ENERGY_CONSUMPTION = 33..36
        val START_SOC = 29..30
        val END_SOC = 31..32
        val CHARGING_END_TIME = 19..26
        val CHARGING_START_TIME = 11..18
        val TOTAL_CHARGING_TIME = 27..28
        val EV_MAC_ADDRESS = 3..10
        val TOTAL_COST = 41..44
    }

     fun getSessionEndReason(response: ByteArray): String {
        val endReasonNumber = ModbusTypeConverter.getIntValueFromBytes(
            response,
            ByteRanges.SESSION_END_REASON.first,
            ByteRanges.SESSION_END_REASON.last
        )
        return StateAndModesUtils.SessionEndReasons.fromStateValue(
            endReasonNumber
        ).description
    }

    fun getCustomSessionEndReason(response: ByteArray): String {
        val endReasonNumber = ModbusTypeConverter.getIntValueFromBytes(
            response,
            ByteRanges.CUSTOM_SESSION_END_REASON.first,
            ByteRanges.CUSTOM_SESSION_END_REASON.last
        )
        return StateAndModesUtils.SessionEndReasons.fromStateValue(
            endReasonNumber
        ).description
    }

     fun getEnergyConsumption(response: ByteArray): String {
        val energyConsumption = ModbusTypeConverter.byteArrayToFloat(response.getRangedArray(ByteRanges.ENERGY_CONSUMPTION))
        return energyConsumption.formatFloatToString()
    }

     fun getStartSoc(response: ByteArray): String {
        return "${
            ModbusTypeConverter.getIntValueFromBytes(
                response,
                ByteRanges.START_SOC.first,
                ByteRanges.START_SOC.last
            )
        }"
    }

     fun getEndSoc(response: ByteArray): String {
        return "${
            ModbusTypeConverter.getIntValueFromBytes(
                response,
                ByteRanges.END_SOC.first,
                ByteRanges.END_SOC.last
            )
        }"
    }

     fun getChargingEndTime(responseFrameArray: ByteArray): String {
        val chargingEndTimeArray = responseFrameArray.getRangedArray(ByteRanges.CHARGING_END_TIME)
        val mappedArray = chargingEndTimeArray.map { it.getIntValueFromByte() }
        val hexArray = ModbusTypeConverter.decimalArrayToHexArray(mappedArray)
        return CommonUtils.getDateAndTimeFromHexArray(hexArray)
    }

     fun getChargingStartTime(responseFrameArray: ByteArray): String {
        val chargingStartTimeArray = responseFrameArray.getRangedArray(ByteRanges.CHARGING_START_TIME)
        val mappedArray = chargingStartTimeArray.map { it.getIntValueFromByte() }
        val hexArray = ModbusTypeConverter.decimalArrayToHexArray(mappedArray)
        return CommonUtils.getDateAndTimeFromHexArray(hexArray)
    }

     fun getTotalChargingTime(response: ByteArray): String {
        val totalChargingTime = ModbusTypeConverter.getIntValueFromBytes(
            response,
            ByteRanges.TOTAL_CHARGING_TIME.first,
            ByteRanges.TOTAL_CHARGING_TIME.last
        )
        return "$totalChargingTime"
    }

     fun getEVMacAddress(response: ByteArray): String {
        val macAddressArray = response.getRangedArray(ByteRanges.EV_MAC_ADDRESS)
        return CommonUtils.getSimpleMacAddress(macAddressArray, "-")
    }

    fun getTotalCost(response: ByteArray): String {
        val totalCost = ModbusTypeConverter.byteArrayToFloat(response.getRangedArray(ByteRanges.TOTAL_COST))
        return "$totalCost"
    }
    
}