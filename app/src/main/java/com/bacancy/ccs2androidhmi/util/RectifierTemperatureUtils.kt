package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getRangedArray
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_10_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_11_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_12_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_13_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_14_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_15_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_16_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_5_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_6_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_7_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_8_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierFaultsUtils.ByteRanges.RECTIFIER_9_FAULT
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_10_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_11_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_12_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_13_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_14_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_15_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_16_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_1_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_2_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_3_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_4_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_5_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_6_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_7_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_8_TEMP
import com.bacancy.ccs2androidhmi.util.RectifierTemperatureUtils.ByteRanges.RECTIFIER_9_TEMP

object RectifierTemperatureUtils {

    object ByteRanges {
        val RECTIFIER_1_TEMP = 3..4
        val RECTIFIER_2_TEMP = 5..6
        val RECTIFIER_3_TEMP = 7..8
        val RECTIFIER_4_TEMP = 9..10
        val RECTIFIER_5_TEMP = 11..12
        val RECTIFIER_6_TEMP = 13..14
        val RECTIFIER_7_TEMP = 15..16
        val RECTIFIER_8_TEMP = 17..18
        val RECTIFIER_9_TEMP = 19..20
        val RECTIFIER_10_TEMP = 21..22
        val RECTIFIER_11_TEMP = 23..24
        val RECTIFIER_12_TEMP = 25..26
        val RECTIFIER_13_TEMP = 27..28
        val RECTIFIER_14_TEMP = 29..30
        val RECTIFIER_15_TEMP = 31..32
        val RECTIFIER_16_TEMP = 33..34
    }

    fun getRectifier1Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_1_TEMP).toHex()
    }

    fun getRectifier2Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_2_TEMP).toHex()
    }

    fun getRectifier3Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_3_TEMP).toHex()
    }

    fun getRectifier4Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_4_TEMP).toHex()
    }

    fun getRectifier5Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_5_TEMP).toHex()
    }

    fun getRectifier6Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_6_TEMP).toHex()
    }

    fun getRectifier7Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_7_TEMP).toHex()
    }

    fun getRectifier8Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_8_TEMP).toHex()
    }

    fun getRectifier9Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_9_TEMP).toHex()
    }

    fun getRectifier10Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_10_TEMP).toHex()
    }

    fun getRectifier11Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_11_TEMP).toHex()
    }

    fun getRectifier12Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_12_TEMP).toHex()
    }

    fun getRectifier13Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_13_TEMP).toHex()
    }

    fun getRectifier14Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_14_TEMP).toHex()
    }

    fun getRectifier15Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_15_TEMP).toHex()
    }

    fun getRectifier16Temp(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_16_TEMP).toHex()
    }

}