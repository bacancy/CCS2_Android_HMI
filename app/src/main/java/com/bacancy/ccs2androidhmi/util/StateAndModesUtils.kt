package com.bacancy.ccs2androidhmi.util

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
        LOADING(-1, "Loading...");

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

    fun getGunStates(): MutableList<GunStatesInfo> {
        return mutableListOf(
            GunStatesInfo(1, "Unplugged", "Gun Not Connected", "White"),
            GunStatesInfo(2, "Plugged In", "Gun Connected with EV", "Yellow"),
            GunStatesInfo(
                3,
                "Authentication",
                "Authentication using RFID/OCPP with in 55 Sec of start this state",
                "White"
            ),
            GunStatesInfo(
                4,
                "Authentication Timeout",
                "Authentication not done within the time interval",
                "Red"
            ),
            GunStatesInfo(
                5,
                "Authentication Denied",
                "Authentication rejected by the Server",
                "Red"
            ),
            GunStatesInfo(
                6,
                "Authentication Success",
                "Authentication Success Response from Server",
                "Green"
            ),
            GunStatesInfo(7, "Isolation Fail", "Isolation Test Fail", "Red"),
            GunStatesInfo(8, "Preparing For Charging", "Initializing for charging", "Blue"),
            GunStatesInfo(9, "Precharge Fail", "Precharge Test Fail", "Red"),
            GunStatesInfo(10, "Charging", "EV Charge in Progress", "Blue"),
            GunStatesInfo(11, "Charging Complete", "EV charge Completed", "Green"),
            GunStatesInfo(12, "PLC Fault", "Fault Occurred in PLC Module", "Red"),
            GunStatesInfo(13, "Rectifier Fault", "Fault Occurred in Rectifier Module", "Red"),
            GunStatesInfo(14, "Communication Error", "Communication Break with EV", "Red"),
            GunStatesInfo(15, "Emergency Stop", "Emergency Stop Triggered by user", "Red")
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
        OCPP_COMM_FAIL(17),
        SMOKE_DETECT(18),
        TAMPER_DETECT(19),
        RCBD_MCCB_FAIL(20),
        EMG_PRESSED(21),
        HMI_COMM_FAULT(22),
        MODEM_COMM_FAIL(23),
        INPUT_CONTACTOR_FAIL(24),
        ALL_RECTIFIER_COMM_FAIL(25),
        REC1_COMM_FAIL(26),
        REC2_COMM_FAIL(27),
        REC3_COMM_FAIL(28),
        REC4_COMM_FAIL(29),
        ISOLATION_FAIL(30),
        RECTIFIER_TEMP_HIGH(31),
        GUN_DC_OVER_VOLTAGE(32),
        GUN_UNDER_VOLTAGE(33),
        GUN_DC_OUTPUT_OVER_CURRENT(34),
        GUN_DC_CONTACTOR_FAIL(35),
        GUN_FUSE_DAMAGE(36),
        GUN_AUTH_TIMEOUT(37),
        GUN_CP_LINE_FAULT(38),
        GUN_SLAC_ERROR(39),
        GUN_V2G_ERROR(40),
        GUN_PRECHARGE_ERROR(41),
        EV_ERROR(42),
        AC_TYPE2_METER_COMM_FAIL(43),
        GUN_AC_TYPE2_OVER_VOLT(44),
        GUN_AC_TYPE2_UNDER_VOLT(45),
        GUN_AC_TYPE2_OVER_CURR(46),
        GUN_AC_TYPE2_UNDER_CURR(47),
        MAINS_FAIL(48),
        REC1_FAULT(49),
        REC2_FAULT(50)
    }

}