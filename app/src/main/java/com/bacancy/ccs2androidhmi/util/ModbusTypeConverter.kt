package com.bacancy.ccs2androidhmi.util

import java.nio.ByteBuffer
import java.nio.ByteOrder

object ModbusTypeConverter {

    fun Float.formatFloatToString(): String {
        return String.format("%.3f", this)
    }

    fun Byte.getIntValueFromByte(): Int {
        return this.toInt() and 0xFF
    }

    fun getActualIntValueFromHighAndLowBytes(highByte: Int, lowByte: Int): Int {
        return (highByte shl 8) or lowByte
    }

    fun byteArrayToFloat(bytes: ByteArray): Float {
        val byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        return if (byteBuffer.hasRemaining()) {
            byteBuffer.float
        } else {
            0.0F
        }
    }

    fun bytesToAsciiString(bytes: ByteArray): String {
        return String(bytes, Charsets.US_ASCII)
    }

    private fun decimalToHex(decimalValue: Int): String {
        return Integer.toHexString(decimalValue)
    }

    fun decimalArrayToHexArray(decimalList: List<Int>): MutableList<String> {
        val hexList = mutableListOf<String>()
        decimalList.forEach {
            if (it < 10) {
                hexList.add("0" + decimalToHex(it))
            } else {
                if (decimalToHex(it).length == 1) {
                    hexList.add("0" + decimalToHex(it))
                } else {
                    hexList.add(decimalToHex(it))
                }
            }
        }
        return hexList
    }

    fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    fun floatArrayToHexString(floatArray: FloatArray): String {
        val hexStringBuilder = StringBuilder()

        for (floatValue in floatArray) {
            val intBits = floatValue.toBits()
            val hexString = String.format("%08X", intBits)
            hexStringBuilder.append(hexString)
        }

        return hexStringBuilder.toString()
    }

    fun String.hexStringToDecimal(): Int {
        // Assuming the hexString is a valid hexadecimal representation
        if (this.isEmpty()) {
            // Handle the case of an empty string, e.g., return a default value or throw an exception
            return 0
        }

        return this.toInt(radix = 16)
    }

    fun byteArrayToBinaryString(byteArray: ByteArray): String {
        return byteArray.joinToString("") { byte ->
            String.format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
        }
    }
}