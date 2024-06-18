package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getFloatValueFromBytes
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromBytes
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getRangedArray
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex

object GunsChargingInfoUtils {

    object ByteRanges {
        // Response byte ranges for various methods
        val GUN_CHARGING_STATE = 5..6
        val INITIAL_SOC = 7..8
        val CHARGING_SOC = 9..10
        val CHARGING_DURATION = 11..14
        val CHARGING_VOLTAGE = 15..16
        val CHARGING_CURRENT = 17..18
        val DEMAND_VOLTAGE = 19..20
        val DEMAND_CURRENT = 21..22
        val CHARGING_ENERGY_CONSUMPTION = 23..27
        val GUN_TEMPERATURE_DC_POSITIVE = 27..31
        val GUN_TEMPERATURE_DC_NEGATIVE = 31..35
        val TOTAL_COST = 35..39
        val GUN_SPECIFIC_ERROR_CODE_INFORMATION = 39..46
    }

    const val SELECTED_GUN = "SELECTED_GUN"
    const val LBL_UNPLUGGED = "Unplugged"
    const val PLUGGED_IN = "Plugged In & Waiting for Authentication"
    const val LBL_AUTHENTICATION_SUCCESS = "Authentication Success"
    const val LBL_CHARGING = "Charging"
    const val LBL_PREPARING_FOR_CHARGING = "Preparing For Charging"
    const val LBL_COMPLETE = "Complete"
    const val LBL_EMERGENCY_STOP = "Emergency Stop"
    const val LBL_PLC_FAULT = "PLC Fault"
    const val LBL_RECTIFIER_FAULT = "Rectifier Fault"
    const val LBL_TEMPERATURE_FAULT = "Temperature Fault"
    const val LBL_SPD_FAULT = "SPD Fault"
    const val LBL_SMOKE_FAULT = "Smoke Fault"
    const val LBL_TAMPER_FAULT = "Tamper Fault"
    const val LBL_COMMUNICATION_ERROR = "Communication Error"
    const val LBL_AUTHENTICATION_TIMEOUT = "Authentication Timeout"
    const val LBL_AUTHENTICATION_DENIED = "Authentication Denied"
    const val LBL_PRECHARGE_FAIL = "Precharge Fail"
    const val LBL_ISOLATION_FAIL = "Isolation Fail"
    const val LBL_MAINS_FAIL = "Mains Fail"
    const val LBL_UNAVAILABLE = "Unavailable"
    const val LBL_RESERVED = "Reserved"

    fun getGunChargingState(response: ByteArray): StateAndModesUtils.GunChargingState {
        return StateAndModesUtils.GunChargingState.fromStateValue(
            getIntValueFromBytes(response, ByteRanges.GUN_CHARGING_STATE.first, ByteRanges.GUN_CHARGING_STATE.last)
        )
    }

    fun getInitialSoc(response: ByteArray): Int {
        return getIntValueFromBytes(response, ByteRanges.INITIAL_SOC.first, ByteRanges.INITIAL_SOC.last)
    }

    fun getChargingSoc(response: ByteArray): Int {
        return getIntValueFromBytes(response, ByteRanges.CHARGING_SOC.first, ByteRanges.CHARGING_SOC.last)
    }

    fun getChargingDuration(response: ByteArray): String {
        val minutes = getIntValueFromBytes(response, ByteRanges.CHARGING_DURATION.first, ByteRanges.CHARGING_DURATION.first + 1)
        val hour = getIntValueFromBytes(response, ByteRanges.CHARGING_DURATION.first + 2, ByteRanges.CHARGING_DURATION.last)
        // Use String.format to add leading zeros
        val formattedHour = String.format("%02d", hour)
        val formattedMinutes = String.format("%02d", minutes)
        return "$formattedHour:$formattedMinutes"
    }

    fun getChargingVoltage(response: ByteArray): Int {
        return getIntValueFromBytes(response, ByteRanges.CHARGING_VOLTAGE.first, ByteRanges.CHARGING_VOLTAGE.last)
    }

    fun getChargingCurrent(response: ByteArray): Int {
        return getIntValueFromBytes(response, ByteRanges.CHARGING_CURRENT.first, ByteRanges.CHARGING_CURRENT.last)
    }

    fun getDemandVoltage(response: ByteArray): Int {
        return getIntValueFromBytes(response, ByteRanges.DEMAND_VOLTAGE.first, ByteRanges.DEMAND_VOLTAGE.last)
    }

    fun getDemandCurrent(response: ByteArray): Int {
        return getIntValueFromBytes(response, ByteRanges.DEMAND_CURRENT.first, ByteRanges.DEMAND_CURRENT.last)
    }

    fun getChargingEnergyConsumption(response: ByteArray): Float {
        return getFloatValueFromBytes(response, ByteRanges.CHARGING_ENERGY_CONSUMPTION.first, ByteRanges.CHARGING_ENERGY_CONSUMPTION.last)
    }

    fun getGunTemperatureDCPositive(response: ByteArray): Float {
        return getFloatValueFromBytes(response, ByteRanges.GUN_TEMPERATURE_DC_POSITIVE.first, ByteRanges.GUN_TEMPERATURE_DC_POSITIVE.last, 2)
    }

    fun getGunTemperatureDCNegative(response: ByteArray): Float {
        return getFloatValueFromBytes(response, ByteRanges.GUN_TEMPERATURE_DC_NEGATIVE.first, ByteRanges.GUN_TEMPERATURE_DC_NEGATIVE.last, 2)
    }

    fun getTotalCost(response: ByteArray): Float {
        return getFloatValueFromBytes(response, ByteRanges.TOTAL_COST.first, ByteRanges.TOTAL_COST.last, 2)
    }

    fun getGunSpecificErrorCodeInformation(response: ByteArray): String {
        return response.getRangedArray(ByteRanges.GUN_SPECIFIC_ERROR_CODE_INFORMATION).toHex()
    }

}