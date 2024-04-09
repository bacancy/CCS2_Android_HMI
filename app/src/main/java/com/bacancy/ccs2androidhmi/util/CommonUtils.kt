package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.hexStringToDecimal
import java.util.Locale
import kotlin.random.Random

object CommonUtils {

    const val CLOCK_DATE_AND_TIME_FORMAT = "dd/MM/yyyy EEE HH:mm:ss"

    //Screen Constants
    const val AC_METER_FRAG = "AC_METER_FRAGMENT"
    const val GUN_1_DC_METER_FRAG = "GUN_1_DC_METER_FRAG"
    const val GUN_2_DC_METER_FRAG = "GUN_2_DC_METER_FRAG"
    const val GUN_1_LAST_CHARGING_SUMMARY_FRAG = "GUN_1_LAST_CHARGING_SUMMARY_FRAG"
    const val GUN_2_LAST_CHARGING_SUMMARY_FRAG = "GUN_2_LAST_CHARGING_SUMMARY_FRAG"
    const val INSIDE_LOCAL_START_STOP_SCREEN = "INSIDE_LOCAL_START_STOP_SCREEN"
    const val IS_GUN_1_CLICKED = "IS_GUN_1_CLICKED"
    const val IS_GUN_2_CLICKED = "IS_GUN_2_CLICKED"
    const val GUN_1_LOCAL_START = "GUN_1_LOCAL_START"
    const val GUN_2_LOCAL_START = "GUN_2_LOCAL_START"
    const val LOCAL_START_STOP_PIN = "123456"

    const val AUTH_PIN_VALUE="AUTH_PIN_VALUE"
    const val FILE_NAME_DATE_TIME_FORMAT = "yyyyMMdd_HHmmss"
    const val FILE_NAME_PREFIX = "ccs2_"
    const val FILE_NAME_EXTENSION = "csv"
    const val DEVICE_MAC_ADDRESS = "DEVICE_MAC_ADDRESS"
    const val CHARGER_RATINGS = "CHARGER_RATINGS"
    const val CHARGER_OUTPUTS = "CHARGER_OUTPUTS"
    const val IS_INITIAL_CHARGER_DETAILS_PUBLISHED = "IS_INITIAL_CHARGER_DETAILS_PUBLISHED"
    const val UNIT_PRICE = "UNIT_PRICE"

    private fun swapAdjacentElements(array: MutableList<Int>): MutableList<Int> {
        for (i in 0 until array.size - 1 step 2) {
            // Swap adjacent elements
            val temp = array[i]
            array[i] = array[i + 1]
            array[i + 1] = temp
        }
        return array
    }

    fun getSwappedMacAddress(macAddressArray: ByteArray, separator: String = ":"): String {
        val mappedArray = macAddressArray.map { it.getIntValueFromByte() }
        val convertedArray = swapAdjacentElements(mappedArray.toMutableList())
        return ModbusTypeConverter.decimalArrayToHexArray(convertedArray).joinToString(separator).uppercase(
            Locale.ROOT
        )
    }

    fun String.getCleanedMacAddress(): String {
        return this.replace(":","")
    }

    fun String.addColonsToMacAddress(): String {
        val formattedMacAddress = StringBuilder()
        for (i in this.indices) {
            formattedMacAddress.append(this[i])
            if (i % 2 == 1 && i < this.length - 1) {
                formattedMacAddress.append(':')
            }
        }
        return formattedMacAddress.toString()
    }

    fun getSimpleMacAddress(macAddressArray: ByteArray, separator: String = ":"): String {
        val mappedArray = macAddressArray.map { it.getIntValueFromByte() }
        return ModbusTypeConverter.decimalArrayToHexArray(mappedArray).joinToString(separator).uppercase(
            Locale.ROOT
        )
    }

    fun getDateAndTimeFromHexArray(hexArray: MutableList<String>): String {
        val year = hexArray[2] + hexArray[3]
        val formattedDate = "%02d/%02d/%04d".format(
            hexArray[0].hexStringToDecimal(),
            hexArray[1].hexStringToDecimal(),
            year.hexStringToDecimal()
        )
        val formattedTime = "%02d:%02d:%02d".format(
            hexArray[4].hexStringToDecimal(),
            hexArray[5].hexStringToDecimal(),
            hexArray[6].hexStringToDecimal()
        )

        return "$formattedDate $formattedTime"
    }

    fun generateRandomNumber(): Int {
        return Random.nextInt(1, 101)
    }
}