package com.bacancy.ccs2androidhmi.util

import android.content.Context
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.models.GunStatesInfo

object StateAndModesUtils {
    enum class GunChargingState(val stateValue: Int, val description: String) {
        UNPLUGGED(0, "Unplugged"),
        PLUGGED_WAITING(1, "Plugged In & Waiting for Authentication"),
        PLUGGED_AUTHENTICATING(2, "Plugged In & Waiting for Authentication"),
        AUTHENTICATION_SUCCESS(3, "Authentication Success"),
        PREPARING_FOR_CHARGING(4, "Preparing For Charging"),
        CHARGING(5, "Charging"),
        COMPLETE(6, "Complete"),
        COMMUNICATION_ERROR(7, "Communication Error"),
        AUTHENTICATION_TIMEOUT(8, "Authentication Timeout"),
        PLC_FAULT(9, "PLC Fault"),
        RECTIFIER_FAULT(10, "Rectifier Fault"),
        EMERGENCY_STOP(11, "Emergency Stop"),
        AUTHENTICATION_DENIED(12, "Authentication Denied"),
        PRECHARGE_FAIL(13, "Precharge Fail"),
        ISOLATION_FAIL(14, "Isolation Fail"),
        TEMPERATURE_FAULT(15, "Temperature Fault"),
        SPD_FAULT(16, "SPD Fault"),
        SMOKE_FAULT(17, "Smoke Fault"),
        TAMPER_FAULT(18, "Tamper Fault"),
        MAINS_FAIL(19, "Mains Fail"),
        UNAVAILABLE(20, "Unavailable"),
        RESERVED(21, "Reserved"),
        LOADING(-1, "Fault");

        companion object {
            fun fromStateValue(value: Int): GunChargingState {
                return values().firstOrNull { it.stateValue == value } ?: LOADING
            }
        }
    }

    enum class SessionEndReasons(val stateValue: Int, val description: String) {
        REMOTE_STOP(1, "Remote Stop"),
        LOCAL_STOP(2, "Local Stop"),
        EV_REQUEST_STOP(3, "EV Request Stop"),
        EMERGENCY_STOP(4, "Emergency Stop"),
        FAULT_STOP(5, "Fault Stop"),
        UNKNOWN_STOP(-1, "Unknown Stop");

        companion object {
            // Function to get StopType from a numerical value
            fun fromStateValue(value: Int): SessionEndReasons {
                return values().firstOrNull { it.stateValue == value } ?: UNKNOWN_STOP
            }
        }
    }

    fun checkServerConnectedWith(array: CharArray): String {
        val firstOneIndex = array.indexOf('1')
        return when {
            firstOneIndex == 0 -> "Ethernet"
            firstOneIndex == 1 -> "GSM"
            firstOneIndex == 2 -> "Wifi"
            array.contains('1') -> "Unknown"
            else -> "Not Found"
        }
    }

    fun checkIfEthernetIsConnected(array: CharArray): String {
        return when {
            array.isNotEmpty() && array[0] == '1' -> "Connected"
            array.isEmpty() -> "Not Connected"
            else -> "Not Connected"
        }
    }

    fun checkGSMNetworkStrength(array: CharArray): String {
        return when (val firstOneIndex = array.indexOf('1')) {
            in 0..3 -> (firstOneIndex + 1).toString()
            else -> "0"
        }
    }

    fun checkWifiNetworkStrength(array: CharArray): String {
        return when (val firstOneIndex = array.indexOf('1')) {
            in 0..3 -> (firstOneIndex + 1).toString()
            else -> "0"
        }
    }

    fun Context.getGunStates(): MutableList<GunStatesInfo> {
        return mutableListOf(
            GunStatesInfo(1, getString(R.string.unplugged), getString(R.string.unplugged_description), "White"),
            GunStatesInfo(2, getString(R.string.plugged_in), getString(R.string.plugged_in_description), "Yellow"),
            GunStatesInfo(3, getString(R.string.authentication), getString(R.string.authentication_description), "White"),
            GunStatesInfo(4, getString(R.string.authentication_timeout), getString(R.string.authentication_timeout_description), "Red"),
            GunStatesInfo(5, getString(R.string.authentication_denied), getString(R.string.authentication_denied_description), "Red"),
            GunStatesInfo(6, getString(R.string.authentication_success), getString(R.string.authentication_success_description), "Green"),
            GunStatesInfo(7, getString(R.string.isolation_fail), getString(R.string.isolation_fail_description), "Red"),
            GunStatesInfo(8, getString(R.string.preparing_for_charging), getString(R.string.preparing_for_charging_description), "Blue"),
            GunStatesInfo(9, getString(R.string.precharge_fail), getString(R.string.precharge_fail_description), "Red"),
            GunStatesInfo(10, getString(R.string.charging), getString(R.string.charging_description), "Blue"),
            GunStatesInfo(11, getString(R.string.charging_complete), getString(R.string.charging_complete_description), "Green"),
            GunStatesInfo(12, getString(R.string.plc_fault), getString(R.string.plc_fault_description), "Red"),
            GunStatesInfo(13, getString(R.string.rectifier_fault), getString(R.string.rectifier_fault_description), "Red"),
            GunStatesInfo(14, getString(R.string.communication_error), getString(R.string.communication_error_description), "Red"),
            GunStatesInfo(15, getString(R.string.emergency_stop), getString(R.string.emergency_stop_description), "Red")
        )
    }

    enum class GunsErrorCode(val value: Int) {
        SYSTEMP_INIT_FAIL(0),
        GUN_TEMPERATURE_HIGH(1),
        HIGH_NEUTRAL_TO_EARTH_VOLTAGE(2),
        MAIN_SUPPLY_LOW(3),
        MAIN_SUPPLY_HIGH(4),
        RFID_COMM_FAIL(5),
        RELAY_BOARD_STOP_WORKING(6),
        AC_ENERGY_METER_COMM_FAIL(7),
        GUN_DC_METER_COMM_FAIL(8),
        ALL_DC_METER_COMM_FAIL(9),
        LED_BOARD_FAIL(10),
        HIGH_LEAKAGE_CURRENT(11),
        SYSTEM_TEMPERATURE_HIGH(12),
        SPD_FAULT(13),
        ALL_PLC_COMM_FAIL(14),
        PLC_COMM_FAIL(15),
        CONTROLLER_DASHBOARD_FAIL(16),
        SMOKE_DETECT(17),
        TAMPER_DETECT(18),
        RCBD_MCCB_FAIL(19),
        EMG_PRESSED(20),
        HMI_COMM_FAULT(21),
        MODEM_COMM_FAIL(22),
        INPUT_CONTACTOR_FAIL(23),
        ALL_RECTIFIER_COMM_FAIL(24),
        REC1_COMM_FAIL(25),
        REC2_COMM_FAIL(26),
        REC3_COMM_FAIL(27),
        REC4_COMM_FAIL(28),
        ISOLATION_FAIL(29),
        RECTIFIER_TEMP_HIGH(30),
        GUN_DC_OVER_VOLTAGE(31),
        GUN_UNDER_VOLTAGE(32),
        GUN_DC_OUTPUT_OVER_CURRENT(33),
        GUN_DC_CONTACTOR_FAIL(34),
        GUN_FUSE_DAMAGE(35),
        GUN_AUTH_TIMEOUT(36),
        GUN_CP_LINE_FAULT(37),
        GUN_SLAC_ERROR(38),
        GUN_V2G_ERROR(39),
        GUN_PRECHARGE_ERROR(40),
        EV_ERROR(41),
        AC_TYPE2_METER_COMM_FAIL(42),
        GUN_AC_TYPE2_OVER_VOLT(43),
        GUN_AC_TYPE2_UNDER_VOLT(44),
        GUN_AC_TYPE2_OVER_CURR(45),
        GUN_AC_TYPE2_UNDER_CURR(46),
        MAINS_FAIL(47),
        REC1_FAULT(48),
        REC2_FAULT(49)
    }

}