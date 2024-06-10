package com.bacancy.ccs2androidhmi.util

import android.content.Context
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.models.GunStatesInfo
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_AUTHENTICATION_DENIED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_AUTHENTICATION_SUCCESS
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_AUTHENTICATION_TIMEOUT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_COMMUNICATION_ERROR
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_COMPLETE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_EMERGENCY_STOP
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_ISOLATION_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_MAINS_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_PLC_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_PRECHARGE_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_PREPARING_FOR_CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_RECTIFIER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_RESERVED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_UNAVAILABLE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_UNPLUGGED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLUGGED_IN

object StateAndModesUtils {
    enum class GunChargingState(val stateValue: Int, val descriptionToShow: Int,val descriptionToSave: String) {
        UNPLUGGED(0, R.string.unplugged,LBL_UNPLUGGED),
        PLUGGED_WAITING(1, R.string.lbl_plugged_in_waiting_for_authentication,PLUGGED_IN),
        PLUGGED_AUTHENTICATING(2, R.string.lbl_plugged_in_waiting_for_authentication,PLUGGED_IN),
        AUTHENTICATION_SUCCESS(3, R.string.authentication_success,LBL_AUTHENTICATION_SUCCESS),
        PREPARING_FOR_CHARGING(4, R.string.preparing_for_charging,LBL_PREPARING_FOR_CHARGING),
        CHARGING(5, R.string.charging,LBL_CHARGING),
        COMPLETE(6, R.string.lbl_complete,LBL_COMPLETE),
        COMMUNICATION_ERROR(7, R.string.communication_error,LBL_COMMUNICATION_ERROR),
        AUTHENTICATION_TIMEOUT(8, R.string.authentication_timeout,LBL_AUTHENTICATION_TIMEOUT),
        PLC_FAULT(9, R.string.plc_fault,LBL_PLC_FAULT),
        RECTIFIER_FAULT(10, R.string.rectifier_fault,LBL_RECTIFIER_FAULT),
        EMERGENCY_STOP(11, R.string.emergency_stop,LBL_EMERGENCY_STOP),
        AUTHENTICATION_DENIED(12, R.string.authentication_denied, LBL_AUTHENTICATION_DENIED),
        PRECHARGE_FAIL(13, R.string.precharge_fail,LBL_PRECHARGE_FAIL),
        ISOLATION_FAIL(14, R.string.isolation_fail,LBL_ISOLATION_FAIL),
        TEMPERATURE_FAULT(15, R.string.lbl_temperature_fault,LBL_TEMPERATURE_FAULT),
        SPD_FAULT(16, R.string.lbl_spd_fault,LBL_SPD_FAULT),
        SMOKE_FAULT(17, R.string.lbl_smoke_fault,LBL_SMOKE_FAULT),
        TAMPER_FAULT(18, R.string.lbl_tamper_fault,LBL_TAMPER_FAULT),
        MAINS_FAIL(19, R.string.lbl_mains_fail,LBL_MAINS_FAIL),
        UNAVAILABLE(20, R.string.lbl_unavailable,LBL_UNAVAILABLE),
        RESERVED(21, R.string.lbl_reserved,LBL_RESERVED),
        LOADING(-1, R.string.lbl_fault, "Fault");

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