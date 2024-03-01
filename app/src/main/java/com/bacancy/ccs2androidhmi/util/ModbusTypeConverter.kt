package com.bacancy.ccs2androidhmi.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt

object ModbusTypeConverter {

    fun Float.formatFloatToString(): String {
        return String.format("%.2f", this)
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

    fun binaryToDecimal(binary: String): Int {
        // Check if the input is a valid binary string
        if (!binary.matches(Regex("[01]+"))) {
            throw IllegalArgumentException("Invalid binary string: $binary")
        }

        // Convert binary to decimal
        return Integer.parseInt(binary, 2)
    }

    fun Float.changeFloatTo2Points(): Float {
        return (this * 100F).roundToInt() / 100F //up to 2 points after decimal
    }

    fun hexToBinary(hex: String): String {
        val hexChars = "0123456789ABCDEF"
        val binaryChars = "0000 0001 0010 0011 0100 0101 0110 0111 1000 1001 1010 1011 1100 1101 1110 1111".split(" ")
        val binaryMap = mutableMapOf<Char, String>()
        hexChars.forEachIndexed { index, c ->
            binaryMap[c] = binaryChars[index]
        }

        val binaryStringBuilder = StringBuilder()
        hex.forEach { c ->
            binaryStringBuilder.append(binaryMap[c])
        }

        return binaryStringBuilder.toString()
    }

    fun stringToIntArray(input: String): IntArray {
        val intArray = IntArray(input.length)
        for (i in input.indices) {
            intArray[i] = input[i].code
        }
        return intArray
    }
}