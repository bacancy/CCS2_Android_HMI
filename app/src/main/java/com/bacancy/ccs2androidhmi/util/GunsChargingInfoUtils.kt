package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import kotlin.math.pow
import kotlin.math.roundToInt

object GunsChargingInfoUtils {

    const val SELECTED_GUN = "SELECTED_GUN"

    const val UNPLUGGED = "Unplugged"
    const val PLUGGED_IN = "Plugged In & Waiting for Authentication"
    const val AUTHENTICATION_SUCCESS = "Authentication Success"
    const val CHARGING = "Charging"
    const val PREPARING_FOR_CHARGING = "Preparing For Charging"
    const val COMPLETE = "Complete"
    const val EMERGENCY_STOP = "Emergency Stop"
    const val PLC_FAULT = "PLC Fault"
    const val RECTIFIER_FAULT = "Rectifier Fault"
    const val TEMPERATURE_FAULT = "Temperature Fault"
    const val SPD_FAULT = "SPD Fault"
    const val SMOKE_FAULT = "Smoke Fault"
    const val TAMPER_FAULT = "Tamper Fault"
    const val COMMUNICATION_ERROR = "Communication Error"
    const val AUTHENTICATION_TIMEOUT = "Authentication Timeout"
    const val AUTHENTICATION_DENIED = "Authentication Denied"
    const val PRECHARGE_FAIL = "Precharge Fail"
    const val ISOLATION_FAIL = "Isolation Fail"
    const val MAINS_FAIL = "Mains Fail"
    const val UNAVAILABLE = "Unavailable"
    const val RESERVED = "Reserved"

    fun getGunChargingState(response: ByteArray): StateAndModesUtils.GunChargingState {
        return StateAndModesUtils.GunChargingState.fromStateValue(
            getIntValueFromBytes(response, 5, 6)
        )
    }

    fun getInitialSoc(response: ByteArray): Int {
        return getIntValueFromBytes(response, 7, 8)
    }

    fun getChargingSoc(response: ByteArray): Int {
        return getIntValueFromBytes(response, 9, 10)
    }

    fun getChargingDuration(response: ByteArray): String {
        val minutes = getIntValueFromBytes(response, 11, 12)
        val hour = getIntValueFromBytes(response, 13, 14)
        // Use String.format to add leading zeros
        val formattedHour = String.format("%02d", hour)
        val formattedMinutes = String.format("%02d", minutes)
        return "$formattedHour:$formattedMinutes"
    }

    fun getChargingVoltage(response: ByteArray): Int {
        return getIntValueFromBytes(response, 15, 16)
    }

    fun getChargingCurrent(response: ByteArray): Int {
        return getIntValueFromBytes(response, 17, 18)
    }

    fun getDemandVoltage(response: ByteArray): Int {
        return getIntValueFromBytes(response, 19, 20)
    }

    fun getDemandCurrent(response: ByteArray): Int {
        return getIntValueFromBytes(response, 21, 22)
    }

    fun getChargingEnergyConsumption(response: ByteArray): Float {
        return getFloatValueFromBytes(response, 23, 27)
    }

    fun getGunTemperatureDCPositive(response: ByteArray): Float {
        return getFloatValueFromBytes(response, 27, 31, 2)
    }

    fun getGunTemperatureDCNegative(response: ByteArray): Float {
        return getFloatValueFromBytes(response, 31, 35, 2)
    }

    fun getTotalCost(response: ByteArray): Float {
        return getFloatValueFromBytes(response, 35, 39, 2)
    }

    fun getGunSpecificErrorCodeInformation(response: ByteArray): String {
        return response.copyOfRange(39, 47).toHex()
    }

    private fun getFloatValueFromBytes(response: ByteArray, fromIndex: Int, toIndex: Int, decimalPoints: Int = 3): Float {
        val floatValue = ModbusTypeConverter.byteArrayToFloat(response.copyOfRange(fromIndex, toIndex))
        val multiplier = 10.0.pow(decimalPoints.toDouble())
        return (floatValue * multiplier).roundToInt() / multiplier.toFloat()
    }

    private fun getIntValueFromBytes(response: ByteArray, lsbIndex: Int, msbIndex: Int): Int {
        val lsb = response[lsbIndex].getIntValueFromByte()
        val msb = response[msbIndex].getIntValueFromByte()
        return ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(lsb, msb)
    }
}