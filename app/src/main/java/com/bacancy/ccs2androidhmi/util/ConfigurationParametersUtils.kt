package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getRangedArray
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.hexStringToDecimal
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex

object ConfigurationParametersUtils {

    private val ACCESS_PARAM_KEY = 3..4
    private val RECTIFIER_SELECTION = 5..6
    private val NUMBER_OF_RECTIFIER_PER_GROUP = 7..8
    private val MAX_DC_OUTPUT_POWER_CAPACITY_OF_CHARGER = 9..10
    private val RECTIFIER_MAX_POWER = 11..12
    private val RECTIFIER_MAX_VOLTAGE = 13..14
    private val RECTIFIER_MAX_CURRENT = 15..16
    private val AC_METER_SELECTION = 17..18
    private val AC_METER_DATA_CONFIGURATION = 19..20
    private val VOLTAGE_V1N_REGISTER_ADDRESS = 21..22
    private val VOLTAGE_V2N_REGISTER_ADDRESS = 23..24
    private val VOLTAGE_V3N_REGISTER_ADDRESS = 25..26
    private val AVG_VOLTAGE_LN_REGISTER_ADDRESS = 27..28
    private val CURRENT_L1_REGISTER_ADDRESS = 29..30
    private val CURRENT_L2_REGISTER_ADDRESS = 31..32
    private val CURRENT_L3_REGISTER_ADDRESS = 33..34
    private val AVG_CURRENT_REGISTER_ADDRESS = 35..36
    private val ACTIVE_POWER_REGISTER_ADDRESS = 37..38
    private val TOTAL_ENERGY_REGISTER_ADDRESS = 39..40
    private val FREQUENCY_REGISTER_ADDRESS = 41..42
    private val AVG_PF_REGISTER_ADDRESS = 43..44
    private val DC_METER_SELECTION = 45..46
    private val DC_METER_DATA_CONFIGURATION = 47..48
    private val VOLTAGE_REGISTER_ADDRESS = 49..50
    private val CURRENT_REGISTER_ADDRESS = 51..52
    private val POWER_REGISTER_ADDRESS = 53..54
    private val IMPORT_ENERGY_REGISTER_ADDRESS = 55..56
    private val EXPORT_ENERGY_REGISTER_ADDRESS = 57..58
    private val MAX_VOLTAGE_REGISTER_ADDRESS = 59..60
    private val MIN_VOLTAGE_REGISTER_ADDRESS = 61..62
    private val MAX_CURRENT_REGISTER_ADDRESS = 63..64
    private val MIN_CURRENT_REGISTER_ADDRESS = 65..66
    private val FAULT_DETECTION_ENABLE_DISABLE = 67..68
    private val DC_GUN_TEMPERATURE_THRESHOLD_VALUE = 69..70
    private val PHASE_LOW_DETECTION_VOLTAGE = 71..72
    private val PHASE_HIGH_DETECTION_VOLTAGE = 73..74
    private val CHARGER_TYPE = 75..76
    private val AC_TYPE_2_FUNCTIONALITY = 77..78
    private val AUTHENTICATION_TYPE = 79..80
    private val AC_TYPE_2_CAPACITY = 81..82
    private val OFFLINE_MODE_FUNCTIONALITY = 83..84
    private val CHARGE_CONTROL_MODE = 85..86
    private val CHARGER_OPERATIVE_INOPERATIVE_CONTROL = 87..88
    private val TOTAL_REACTIVE_ENERGY_REGISTER_ADDRESS = 89..90
    private val GUN1_MAX_CURRENT_LIMIT = 91..92
    private val GUN2_MAX_CURRENT_LIMIT = 93..94

    fun getConfigAccessKey(response: ByteArray): String {
        return response.getRangedArray(ACCESS_PARAM_KEY).toHex()
    }

    fun getRectifierSelection(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_SELECTION).toHex()
    }

    fun getNumberOfRectifierPerGroup(response: ByteArray): String {
        return response.getRangedArray(NUMBER_OF_RECTIFIER_PER_GROUP).toHex()
    }

    fun getMaxDCOutputPowerCapacityOfCharger(response: ByteArray): String {
        return response.getRangedArray(MAX_DC_OUTPUT_POWER_CAPACITY_OF_CHARGER).toHex()
    }

    fun getRectifierMaxPower(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_MAX_POWER).toHex()
    }

    fun getRectifierMaxVoltage(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_MAX_VOLTAGE).toHex()
    }

    fun getRectifierMaxCurrent(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_MAX_CURRENT).toHex()
    }

    fun getACMeterSelection(response: ByteArray): String {
        return response.getRangedArray(AC_METER_SELECTION).toHex()
    }

    fun getACMeterDataConfiguration(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.getRangedArray(
            AC_METER_DATA_CONFIGURATION
        )).reversed().substring(0, 5)
    }

    fun getACMeterDataType(response: ByteArray): String {
        return when (getACMeterDataConfiguration(response)[0]) {
            '0' -> {
                "Integer"
            }
            '1' -> {
                "Float"
            }
            else -> ""
        }
    }

    fun getACMeterDataEndianness(response: ByteArray): String {
        return when (getACMeterDataConfiguration(response)[1]) {
            '0' -> {
                "Little Endian"
            }
            '1' -> {
                "Big Endian"
            }
            else -> ""
        }
    }

    fun getACMeterReadFunction(response: ByteArray): String {
        return when (getACMeterDataConfiguration(response)[2]) {
            '0' -> {
                "Input Register"
            }
            '1' -> {
                "Holding Register"
            }
            else -> ""
        }
    }

    fun getACMeterDataTypeInWattOrKW(response: ByteArray): String {
        return when (getACMeterDataConfiguration(response)[3]) {
            '0' -> {
                "Watt"
            }
            '1' -> {
                "KWatt"
            }
            else -> ""
        }
    }

    fun getACMeterMandatory(response: ByteArray): Int {
        /*return when (getACMeterDataConfiguration(response)[4]) {
            '0' -> {
                "No"
            }
            '1' -> {
                "Yes"
            }
            else -> ""
        }*/
        return getACMeterDataConfiguration(response)[4].digitToInt()
    }

    fun getVoltageV1NRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(VOLTAGE_V1N_REGISTER_ADDRESS).toHex()
    }

    fun getVoltageV2NRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(VOLTAGE_V2N_REGISTER_ADDRESS).toHex()
    }

    fun getVoltageV3NRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(VOLTAGE_V3N_REGISTER_ADDRESS).toHex()
    }

    fun getAvgVoltageLNRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(AVG_VOLTAGE_LN_REGISTER_ADDRESS).toHex()
    }

    fun getCurrentL1RegisterAddress(response: ByteArray): String {
        return response.getRangedArray(CURRENT_L1_REGISTER_ADDRESS).toHex()
    }

    fun getCurrentL2RegisterAddress(response: ByteArray): String {
        return response.getRangedArray(CURRENT_L2_REGISTER_ADDRESS).toHex()
    }

    fun getCurrentL3RegisterAddress(response: ByteArray): String {
        return response.getRangedArray(CURRENT_L3_REGISTER_ADDRESS).toHex()
    }

    fun getAvgCurrentRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(AVG_CURRENT_REGISTER_ADDRESS).toHex()
    }

    fun getActivePowerRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(ACTIVE_POWER_REGISTER_ADDRESS).toHex()
    }

    fun getTotalEnergyRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(TOTAL_ENERGY_REGISTER_ADDRESS).toHex()
    }

    fun getFrequencyRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(FREQUENCY_REGISTER_ADDRESS).toHex()
    }

    fun getAvgPFRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(AVG_PF_REGISTER_ADDRESS).toHex()
    }

    fun getDCMeterSelection(response: ByteArray): String {
        return response.getRangedArray(DC_METER_SELECTION).toHex()
    }

    fun getDCMeterDataConfiguration(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.getRangedArray(
            DC_METER_DATA_CONFIGURATION
        )).reversed().substring(0, 5)
    }

    fun getDCMeterDataType(response: ByteArray): String {
        return when (getDCMeterDataConfiguration(response)[0]) {
            '0' -> {
                "Integer"
            }
            '1' -> {
                "Float"
            }
            else -> ""
        }
    }

    fun getDCMeterDataEndianness(response: ByteArray): String {
        return when (getDCMeterDataConfiguration(response)[1]) {
            '0' -> {
                "Little Endian"
            }
            '1' -> {
                "Big Endian"
            }
            else -> ""
        }
    }

    fun getDCMeterReadFunction(response: ByteArray): String {
        return when (getDCMeterDataConfiguration(response)[2]) {
            '0' -> {
                "Input Register"
            }
            '1' -> {
                "Holding Register"
            }
            else -> ""
        }
    }

    fun getDCMeterDataTypeInWattOrKW(response: ByteArray): String {
        return when (getDCMeterDataConfiguration(response)[3]) {
            '0' -> {
                "Watt"
            }
            '1' -> {
                "KWatt"
            }
            else -> ""
        }
    }

    fun getDCMeterMandatory(response: ByteArray): Int {
        /*return when (getDCMeterDataConfiguration(response)[4]) {
            '0' -> {
                "No"
            }
            '1' -> {
                "Yes"
            }
            else -> ""
        }*/
        return getDCMeterDataConfiguration(response)[4].digitToInt()
    }

    fun getVoltageRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(VOLTAGE_REGISTER_ADDRESS).toHex()
    }

    fun getCurrentRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(CURRENT_REGISTER_ADDRESS).toHex()
    }

    fun getPowerRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(POWER_REGISTER_ADDRESS).toHex()
    }

    fun getImportEnergyRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(IMPORT_ENERGY_REGISTER_ADDRESS).toHex()
    }

    fun getExportEnergyRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(EXPORT_ENERGY_REGISTER_ADDRESS).toHex()
    }

    fun getMaxVoltageRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(MAX_VOLTAGE_REGISTER_ADDRESS).toHex()
    }

    fun getMinVoltageRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(MIN_VOLTAGE_REGISTER_ADDRESS).toHex()
    }

    fun getMaxCurrentRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(MAX_CURRENT_REGISTER_ADDRESS).toHex()
    }

    fun getMinCurrentRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(MIN_CURRENT_REGISTER_ADDRESS).toHex()
    }

    fun getFaultDetectionEnableDisable(response: ByteArray): String {
        return ModbusTypeConverter.byteArrayToBinaryString(response.getRangedArray(
            FAULT_DETECTION_ENABLE_DISABLE
        )).reversed().substring(0, 7)
    }

    fun getSPDFaultDetection(response: ByteArray): String {
        return when (getFaultDetectionEnableDisable(response)[1]) {
            '0' -> {
                "Disabled"
            }
            '1' -> {
                "Enabled"
            }
            else -> ""
        }
    }

    fun getSmokeFaultDetection(response: ByteArray): String {
        return when (getFaultDetectionEnableDisable(response)[2]) {
            '0' -> {
                "Disabled"
            }
            '1' -> {
                "Enabled"
            }
            else -> ""
        }
    }

    fun getTamperFaultDetection(response: ByteArray): String {
        return when (getFaultDetectionEnableDisable(response)[3]) {
            '0' -> {
                "Disabled"
            }
            '1' -> {
                "Enabled"
            }
            else -> ""
        }
    }

    fun getLEDModuleFaultDetection(response: ByteArray): String {
        return when (getFaultDetectionEnableDisable(response)[4]) {
            '0' -> {
                "Disabled"
            }
            '1' -> {
                "Enabled"
            }
            else -> ""
        }
    }

    fun getGunTemperatureFaultDetection(response: ByteArray): String {
        return when (getFaultDetectionEnableDisable(response)[5]) {
            '0' -> {
                "Disabled"
            }
            '1' -> {
                "Enabled"
            }
            else -> ""
        }
    }

    fun getIsolationFaultDetection(response: ByteArray): String {
        return when (getFaultDetectionEnableDisable(response)[6]) {
            '0' -> {
                "Disabled"
            }
            '1' -> {
                "Enabled"
            }
            else -> ""
        }
    }

    fun getDCGunTemperatureThresholdValue(response: ByteArray): String {
        return response.getRangedArray(DC_GUN_TEMPERATURE_THRESHOLD_VALUE).toHex()
    }

    fun getPhaseLowDetectionVoltage(response: ByteArray): String {
        return response.getRangedArray(PHASE_LOW_DETECTION_VOLTAGE).toHex()
    }

    fun getPhaseHighDetectionVoltage(response: ByteArray): String {
        return response.getRangedArray(PHASE_HIGH_DETECTION_VOLTAGE).toHex()
    }

    fun getChargerType(response: ByteArray): String {
        return response.getRangedArray(CHARGER_TYPE).toHex()
    }

    fun getACType2Functionality(response: ByteArray): String {
        return response.getRangedArray(AC_TYPE_2_FUNCTIONALITY).toHex()
    }

    fun getAuthenticationType(response: ByteArray): String {
        return response.getRangedArray(AUTHENTICATION_TYPE).toHex()
    }

    fun getACType2Capacity(response: ByteArray): String {
        return response.getRangedArray(AC_TYPE_2_CAPACITY).toHex()
    }

    fun getOfflineModeFunctionality(response: ByteArray): String {
        return response.getRangedArray(OFFLINE_MODE_FUNCTIONALITY).toHex()
    }

    fun getChargeControlMode(response: ByteArray): String {
        return when(response.getRangedArray(CHARGE_CONTROL_MODE).toHex().hexStringToDecimal()){
            0 -> "Standalone"
            1 -> "Dynamic"
            2 -> "Dual Socket"
            else -> ""
        }
    }

    fun getChargeControlModeValue(response: ByteArray): Int {
        return response.getRangedArray(CHARGE_CONTROL_MODE).toHex().hexStringToDecimal()
    }

    fun getChargerOperativeInoperativeControl(response: ByteArray): String {
        return response.getRangedArray(CHARGER_OPERATIVE_INOPERATIVE_CONTROL).toHex()
    }

    fun getTotalReactiveEnergyRegisterAddress(response: ByteArray): String {
        return response.getRangedArray(TOTAL_REACTIVE_ENERGY_REGISTER_ADDRESS).toHex()
    }

    fun getGUN1MaxCurrentLimit(response: ByteArray): String {
        return response.getRangedArray(GUN1_MAX_CURRENT_LIMIT).toHex()
    }

    fun getGUN2MaxCurrentLimit(response: ByteArray): String {
        return response.getRangedArray(GUN2_MAX_CURRENT_LIMIT).toHex()
    }

    fun getChargeControlModeList(): Array<String> {
        return arrayOf("Standalone Mode", "Dynamic Mode")
    }

    fun getRectifiersList(): Array<String> {
        return arrayOf(
            "Uugreen Rectifier",
            "Maxwell Rectifier",
            "Sicon Rectifier",
            "Tonhe Rectifier",
            "Huawei Rectifier"
        )
    }

    fun getACMetersList(): Array<String> {
        return arrayOf(
            "User Defined Custom AC Meter",
            "Selec EM4M",
            "Rishabh 3430",
            "Elmeasure M30",
            "Elmeasure LG2XX0D",
            "Havells SDM630",
            "Selec MFM384"
        )
    }

    fun getDCMetersList(): Array<String> {
        return arrayOf(
            "User Defined Custom DC Meter",
            "Rishabh EM6000",
            "Rishabh EM6001",
            "Selec EM2M",
            "Elecnova PD195Z",
            "Yada DCM3366D-J2",
            "Pilot DCMSPM90",
            "Elmeasure EDC2150D"
        )
    }
}