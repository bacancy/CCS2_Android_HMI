package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import kotlin.math.roundToInt

object GunsChargingInfoUtils {

    const val SELECTED_GUN = "SELECTED_GUN"

    const val UNPLUGGED ="Unplugged"
    const val PLUGGED_IN = "Plugged In & Waiting for Authentication"
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

    fun getInitialSoc(response: ByteArray): Int {
        val initialSocLSB = response[7].getIntValueFromByte()
        val initialSocMSB = response[8].getIntValueFromByte()
        return ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            initialSocLSB,
            initialSocMSB
        )
    }

    fun getChargingSoc(response: ByteArray): Int {
        val initialSocLSB = response[9].getIntValueFromByte()
        val initialSocMSB = response[10].getIntValueFromByte()
        return ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            initialSocLSB,
            initialSocMSB
        )
    }

    fun getDemandVoltage(response: ByteArray): Int {
        val initialSocLSB = response[19].getIntValueFromByte()
        val initialSocMSB = response[20].getIntValueFromByte()
        return ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            initialSocLSB,
            initialSocMSB
        )
    }

    fun getDemandCurrent(response: ByteArray): Int {
        val initialSocLSB = response[21].getIntValueFromByte()
        val initialSocMSB = response[22].getIntValueFromByte()
        return ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            initialSocLSB,
            initialSocMSB
        )
    }

    fun getChargingVoltage(response: ByteArray): Int {
        val initialSocLSB = response[15].getIntValueFromByte()
        val initialSocMSB = response[16].getIntValueFromByte()
        return ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            initialSocLSB,
            initialSocMSB
        )
    }

    fun getChargingCurrent(response: ByteArray): Int {
        val initialSocLSB = response[17].getIntValueFromByte()
        val initialSocMSB = response[18].getIntValueFromByte()
        return ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
            initialSocLSB,
            initialSocMSB
        )
    }

    fun getChargingDuration(response: ByteArray): String {
        val durationInMinutesLSB = response[11].getIntValueFromByte()
        val durationInMinutesMSB = response[12].getIntValueFromByte()
        val durationInHoursLSB = response[13].getIntValueFromByte()
        val durationInHoursMSB = response[14].getIntValueFromByte()
        val hour =
            ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
                durationInHoursLSB,
                durationInHoursMSB
            )
        val minutes =
            ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
                durationInMinutesLSB,
                durationInMinutesMSB
            )
        // Use String.format to add leading zeros
        val formattedHour = String.format("%02d", hour)
        val formattedMinutes = String.format("%02d", minutes)

        return "$formattedHour:$formattedMinutes"
    }

    fun getChargingEnergyConsumption(response: ByteArray): Float {
        val floatValue = ModbusTypeConverter.byteArrayToFloat(response.copyOfRange(23, 27))
        return (floatValue * 1000F).roundToInt() / 1000F //up to 3 points after decimal
    }

    fun getGunChargingState(response: ByteArray): StateAndModesUtils.GunChargingState {
        val chargingStateLSB = response[5].getIntValueFromByte()
        val chargingStateMSB = response[6].getIntValueFromByte()
        return StateAndModesUtils.GunChargingState.fromStateValue(
            ModbusTypeConverter.getActualIntValueFromHighAndLowBytes(
                chargingStateLSB,
                chargingStateMSB
            )
        )
    }

    fun getTotalCost(response: ByteArray): Float {
        return ModbusTypeConverter.byteArrayToFloat(response.copyOfRange(35, 39))
    }

}