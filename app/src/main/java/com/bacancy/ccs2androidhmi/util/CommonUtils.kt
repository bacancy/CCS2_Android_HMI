package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.hexStringToDecimal
import java.util.Locale

object CommonUtils {

    const val CLOCK_DATE_AND_TIME_FORMAT = "dd/MM/yyyy EEE HH:mm:ss"

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
}