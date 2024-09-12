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

object RectifierFaultsUtils {

    object ByteRanges {
        val RECTIFIER_5_FAULT = 3..6
        val RECTIFIER_6_FAULT = 7..10
        val RECTIFIER_7_FAULT = 11..14
        val RECTIFIER_8_FAULT = 15..18
        val RECTIFIER_9_FAULT = 19..22
        val RECTIFIER_10_FAULT = 23..26
        val RECTIFIER_11_FAULT = 27..30
        val RECTIFIER_12_FAULT = 31..34
        val RECTIFIER_13_FAULT = 35..38
        val RECTIFIER_14_FAULT = 39..42
        val RECTIFIER_15_FAULT = 43..46
        val RECTIFIER_16_FAULT = 47..50
    }

    fun getRectifier5Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_5_FAULT).toHex()
    }

    fun getRectifier6Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_6_FAULT).toHex()
    }

    fun getRectifier7Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_7_FAULT).toHex()
    }

    fun getRectifier8Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_8_FAULT).toHex()
    }

    fun getRectifier9Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_9_FAULT).toHex()
    }

    fun getRectifier10Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_10_FAULT).toHex()
    }

    fun getRectifier11Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_11_FAULT).toHex()
    }

    fun getRectifier12Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_12_FAULT).toHex()
    }

    fun getRectifier13Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_13_FAULT).toHex()
    }

    fun getRectifier14Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_14_FAULT).toHex()
    }

    fun getRectifier15Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_15_FAULT).toHex()
    }

    fun getRectifier16Fault(response: ByteArray): String {
        return response.getRangedArray(RECTIFIER_16_FAULT).toHex()
    }

}