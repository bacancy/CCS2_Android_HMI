package com.bacancy.ccs2androidhmi.util

import android.util.Log
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex

object ModBusUtils {

    const val INPUT_REGISTERS_CORRECT_RESPONSE_BITS = "0104"
    const val HOLDING_REGISTERS_CORRECT_RESPONSE_BITS = "0103"

    //Input Register
    const val READ_INPUT_REGISTERS_FUNCTION_CODE: Byte = 0x04

    //Holding Register
    const val WRITE_MULTIPLE_REGISTERS_FUNCTION_CODE: Byte = 0x10
    const val READ_HOLDING_REGISTERS_FUNCTION_CODE: Byte = 0x03
    const val WRITE_SINGLE_REGISTER_FUNCTION_CODE: Byte = 0x06

    const val METERING_INFO_INPUT_REGISTERS_FRAME = "010400000018F000"


    /**
     * This method is used to create request frame for reading input registers
     * Request frame example:
     * Slave Address 1
     * Function 04
     * Starting Address Hi 00
     * Starting Address Lo 08
     * No. of Points Hi 00
     * No. of Points Lo 01
     * Error Check (LRC or CRC) ––
     * */
    fun createReadInputRegistersRequest(
        startAddress: Int,
        quantity: Int,
        slaveAddress: Int = 1
    ): ByteArray {
        val byteArrayBeforeCRC = byteArrayOf(
            slaveAddress.toByte(),
            READ_INPUT_REGISTERS_FUNCTION_CODE,
            (startAddress shr 8).toByte(),
            startAddress.toByte(),
            (quantity shr 8).toByte(),
            quantity.toByte()
        )
        val newCRC = calculateCRC(byteArrayBeforeCRC)
        val finalByteArray = byteArrayOf(
            slaveAddress.toByte(),
            READ_INPUT_REGISTERS_FUNCTION_CODE,
            (startAddress shr 8).toByte(),
            startAddress.toByte(),
            (quantity shr 8).toByte(),
            quantity.toByte(),
            newCRC[0],
            newCRC[1]
        )
        return finalByteArray
    }

    /**
     * This method is used to create request frame for writing to multiple registers
     * Request frame example:
     * Slave Address 1
     * Function 10
     * Starting Address Hi 00
     * Starting Address Lo 01
     * No. of Registers Hi 00
     * No. of Registers Lo 02
     * Byte Count 04
     * Data Hi 00
     * Data Lo 0A
     * Data Hi 01
     * Data Lo 02
     * Error Check (LRC or CRC) ––
     * */
    fun createWriteMultipleRegistersRequest(
        startAddress: Int,
        data: IntArray,
        slaveAddress: Int = 1
    ): ByteArray {
        val quantity = data.size
        val byteCount = quantity * 2 // Each register is 2 bytes
        val frame = ByteArray(9 + byteCount)
        frame[0] = slaveAddress.toByte()
        frame[1] = WRITE_MULTIPLE_REGISTERS_FUNCTION_CODE
        frame[2] = (startAddress shr 8).toByte()
        frame[3] = startAddress.toByte()
        frame[4] = (quantity shr 8).toByte()
        frame[5] = quantity.toByte()
        frame[6] = byteCount.toByte()
        for (i in data.indices) {
            val value = data[i]
            val valueIndex = 7 + 2 * i
            frame[valueIndex] = (value.toInt() shr 8).toByte() // High byte of register value
            frame[valueIndex + 1] = value.toByte() // Low byte of register value
        }

        val newCRC = calculateCRC(frame.dropLast(2).toByteArray())

        frame[frame.size - 2] = newCRC[0]
        frame[frame.size - 1] = newCRC[1]
        Log.d("TAG", "createWriteMultipleRegistersRequest: FINAL HEX = ${frame.toHex()}")
        return frame
    }

    fun createWriteMultipleRegistersRequestForPinAuthNew(
        startAddress: Int,
        data: String,
        slaveAddress: Int = 1
    ): ByteArray {
        val quantity = 10
        val byteCount = 20 // Each register is 2 bytes
        val frame = ByteArray(9 + byteCount)
        frame[0] = slaveAddress.toByte()
        frame[1] = WRITE_MULTIPLE_REGISTERS_FUNCTION_CODE
        frame[2] = (startAddress shr 8).toByte()
        frame[3] = startAddress.toByte()
        frame[4] = (quantity shr 8).toByte()
        frame[5] = quantity.toByte()
        frame[6] = byteCount.toByte()

        //val newArray = hexStringToByteArray(data)
        val newArray = data.toByteArray(Charsets.UTF_8)
        val result = ByteArray(20)
        val elementsToCopy = minOf(newArray.size, 20)
        newArray.copyInto(result, endIndex = elementsToCopy)
        for (i in result.indices step 2) {
            val valueFirst = result[i]
            val valueSecond = result[i + 1]
            val j = if (i > 1) i - (i / 2) else 0
            val valueIndex = 7 + 2 * j
            frame[valueIndex] = valueFirst // High byte of register value
            frame[valueIndex + 1] = valueSecond // Low byte of register value
        }

        val newCRC = calculateCRC(frame.dropLast(2).toByteArray())

        frame[frame.size - 2] = newCRC[0]
        frame[frame.size - 1] = newCRC[1]
        Log.d("TAG", "createWriteMultipleRegistersRequestForPinAuthNew: FINAL HEX = ${frame.toHex()}")
        return frame
    }

    fun hexStringToByteArray(hexString: String): ByteArray {
        val result = ByteArray(hexString.length / 2)
        for (i in hexString.indices step 2) {
            val firstDigit = Character.digit(hexString[i], 16)
            val secondDigit = Character.digit(hexString[i + 1], 16)
            val byteValue = firstDigit shl 4 or secondDigit
            result[i / 2] = byteValue.toByte()
        }
        return result
    }

    /**
     * This method is used to create request frame for reading holding registers
     * Request frame example:
     * Slave Address 1
     * Function 03
     * Starting Address Hi 00
     * Starting Address Lo 6B
     * No. of Points Hi 00
     * No. of Points Lo 03
     * Error Check (LRC or CRC) ––
     * */
    fun createReadHoldingRegistersRequest(
        startAddress: Int,
        quantity: Int,
        slaveAddress: Int = 1
    ): ByteArray {
        val byteArrayBeforeCRC = byteArrayOf(
            slaveAddress.toByte(),
            READ_HOLDING_REGISTERS_FUNCTION_CODE,
            (startAddress shr 8).toByte(),
            startAddress.toByte(),
            (quantity shr 8).toByte(),
            quantity.toByte()
        )
        val newCRC = calculateCRC(byteArrayBeforeCRC)
        val finalByteArray = byteArrayOf(
            slaveAddress.toByte(),
            READ_HOLDING_REGISTERS_FUNCTION_CODE,
            (startAddress shr 8).toByte(),
            startAddress.toByte(),
            (quantity shr 8).toByte(),
            quantity.toByte(),
            newCRC[0],
            newCRC[1]
        )
        return finalByteArray
    }

    /**
     * This method is used to create request frame for writing to single register
     * Request frame sample:
     * Slave Address 1
     * Function 06
     * Register Address Hi 00
     * Register Address Lo 01
     * Preset Data Hi 00
     * Preset Data Lo 03
     * Error Check (LRC or CRC) ––
     * */
    fun createWriteSingleRegisterRequest(
        registerAddress: Int,
        registerValue: Int,
        slaveAddress: Int = 1
    ): ByteArray {

        val byteArrayBeforeCRC = byteArrayOf(
            slaveAddress.toByte(),
            WRITE_SINGLE_REGISTER_FUNCTION_CODE,
            (registerAddress shr 8).toByte(),
            registerAddress.toByte(),
            (registerValue shr 8).toByte(),
            registerValue.toByte()
        )
        val newCRC = calculateCRC(byteArrayBeforeCRC)

        val byteArrayWithCRC = byteArrayOf(
            slaveAddress.toByte(),
            WRITE_SINGLE_REGISTER_FUNCTION_CODE,
            (registerAddress shr 8).toByte(),
            registerAddress.toByte(),
            (registerValue shr 8).toByte(),
            registerValue.toByte(),
            newCRC[0],
            newCRC[1]
        )
        Log.d(
            "TAG",
            "createWriteSingleRegisterRequest: FINAL HEX = ${byteArrayWithCRC.toHex()}"
        )
        return byteArrayWithCRC
    }

    /**
     * This method is used to create request frame for writing to single register
     * Request frame sample:
     * Slave Address 1
     * Function 06
     * Register Address Hi 00
     * Register Address Lo 01
     * Preset Data Hi 00
     * Preset Data Lo 03
     * Error Check (LRC or CRC) ––
     * */
    fun createWriteStringToSingleRegisterRequest(
        registerAddress: Int,
        inputString: String,
        slaveAddress: Int = 1
    ): ByteArray {

        val binaryData = inputString.toByteArray(Charsets.UTF_8)

        // Assuming a single holding register stores 16 bits (2 bytes)
        val registerValue = if (binaryData.size >= 2) {
            val highByte = binaryData[1].toInt() and 0xFF
            val lowByte = binaryData[0].toInt() and 0xFF
            (highByte shl 8) or lowByte
        } else {
            // If the string is shorter than 2 bytes, pad with 0
            (binaryData.getOrNull(0)?.toInt()?.and(0xFF)) ?: 0
        }

        val byteArrayBeforeCRC = byteArrayOf(
            slaveAddress.toByte(),
            WRITE_SINGLE_REGISTER_FUNCTION_CODE,
            (registerAddress shr 8).toByte(),
            registerAddress.toByte(),
            (registerValue shr 8).toByte(),
            registerValue.toByte()
        )

        val newCRC = calculateCRC(byteArrayBeforeCRC)

        val byteArrayWithCRC = byteArrayOf(
            slaveAddress.toByte(),
            WRITE_SINGLE_REGISTER_FUNCTION_CODE,
            (registerAddress shr 8).toByte(),
            registerAddress.toByte(),
            (registerValue shr 8).toByte(),
            registerValue.toByte(),
            newCRC[0],
            newCRC[1]
        )
        Log.d(
            "TAG",
            "createWriteSingleRegisterRequest: FINAL HEX = ${byteArrayWithCRC.toHex()}"
        )
        return byteArrayWithCRC
    }

    /**
     * This method is used to calculate CRC for given request frame
     * */
    private fun calculateCRC(
        data: ByteArray,
        startByte: Int = 0
    ): ByteArray {
        val auchCRCHi = byteArrayOf(
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64,
            1,
            -64,
            -128,
            65,
            1,
            -64,
            -128,
            65,
            0,
            -63,
            -127,
            64
        )
        val auchCRCLo = byteArrayOf(
            0,
            -64,
            -63,
            1,
            -61,
            3,
            2,
            -62,
            -58,
            6,
            7,
            -57,
            5,
            -59,
            -60,
            4,
            -52,
            12,
            13,
            -51,
            15,
            -49,
            -50,
            14,
            10,
            -54,
            -53,
            11,
            -55,
            9,
            8,
            -56,
            -40,
            24,
            25,
            -39,
            27,
            -37,
            -38,
            26,
            30,
            -34,
            -33,
            31,
            -35,
            29,
            28,
            -36,
            20,
            -44,
            -43,
            21,
            -41,
            23,
            22,
            -42,
            -46,
            18,
            19,
            -45,
            17,
            -47,
            -48,
            16,
            -16,
            48,
            49,
            -15,
            51,
            -13,
            -14,
            50,
            54,
            -10,
            -9,
            55,
            -11,
            53,
            52,
            -12,
            60,
            -4,
            -3,
            61,
            -1,
            63,
            62,
            -2,
            -6,
            58,
            59,
            -5,
            57,
            -7,
            -8,
            56,
            40,
            -24,
            -23,
            41,
            -21,
            43,
            42,
            -22,
            -18,
            46,
            47,
            -17,
            45,
            -19,
            -20,
            44,
            -28,
            36,
            37,
            -27,
            39,
            -25,
            -26,
            38,
            34,
            -30,
            -29,
            35,
            -31,
            33,
            32,
            -32,
            -96,
            96,
            97,
            -95,
            99,
            -93,
            -94,
            98,
            102,
            -90,
            -89,
            103,
            -91,
            101,
            100,
            -92,
            108,
            -84,
            -83,
            109,
            -81,
            111,
            110,
            -82,
            -86,
            106,
            107,
            -85,
            105,
            -87,
            -88,
            104,
            120,
            -72,
            -71,
            121,
            -69,
            123,
            122,
            -70,
            -66,
            126,
            127,
            -65,
            125,
            -67,
            -68,
            124,
            -76,
            116,
            117,
            -75,
            119,
            -73,
            -74,
            118,
            114,
            -78,
            -77,
            115,
            -79,
            113,
            112,
            -80,
            80,
            -112,
            -111,
            81,
            -109,
            83,
            82,
            -110,
            -106,
            86,
            87,
            -105,
            85,
            -107,
            -108,
            84,
            -100,
            92,
            93,
            -99,
            95,
            -97,
            -98,
            94,
            90,
            -102,
            -101,
            91,
            -103,
            89,
            88,
            -104,
            -120,
            72,
            73,
            -119,
            75,
            -117,
            -118,
            74,
            78,
            -114,
            -113,
            79,
            -115,
            77,
            76,
            -116,
            68,
            -124,
            -123,
            69,
            -121,
            71,
            70,
            -122,
            -126,
            66,
            67,
            -125,
            65,
            -127,
            -128,
            64
        )
        var usDataLen = data.size.toShort()
        var uchCRCHi: Byte = -1
        var uchCRCLo: Byte = -1
        var i = 0
        while (usDataLen > 0) {
            --usDataLen
            var uIndex = uchCRCLo.toInt() xor data[i + startByte].toInt()
            if (uIndex < 0) {
                uIndex += 256
            }
            uchCRCLo = (uchCRCHi.toInt() xor auchCRCHi[uIndex].toInt()).toByte()
            uchCRCHi = auchCRCLo[uIndex]
            ++i
        }
        return byteArrayOf(uchCRCLo, uchCRCHi)
    }

    /**
     * This method is used to convert the ModBus RTU response frame into readable string
     * Response frame example: 01 10 00 02 00 02 04 00 09 00 04
     * */
    fun convertModbusResponseFrameToString(response: ByteArray): String {
        val responseString = response.joinToString(" ") { it.toString() }
        Log.d("TAG", "convertModbusResponseFrameToString: $responseString")
        if (response.size < 3) {
            return "Invalid response length"
        }

        // Extract relevant information
        val slaveAddress = response[0].toInt() and 0xFF
        val functionCode = response[1].toInt() and 0xFF
        val byteCount = response[2].toInt() and 0xFF

        // Check if the response length is as expected
        if (response.size < 3 + byteCount) {
            return "Invalid response length"
        }

        // Extract register values
        val registerValues = mutableListOf<Int>()
        for (i in 3 until 3 + byteCount step 2) {
            val highByte = response[i].toInt() and 0xFF
            val lowByte = response[i + 1].toInt() and 0xFF
            val registerValue = (highByte shl 8) or lowByte
            registerValues.add(registerValue)
        }

        // Create a readable string
        val readableString = buildString {
            append("Slave Address: $slaveAddress\n")
            append("Function Code: $functionCode\n")
            append("Number of Registers: ${registerValues.size}\n")
            append("Register Values: ${registerValues.joinToString(", ")}")
        }

        return readableString
    }

    fun convertModbusResponseFrameToStringSingleElement(response: ByteArray): Int {
        val responseString = response.joinToString(" ") { it.toString() }
        Log.d("TAG", "convertModbusResponseFrameToString: $responseString")
        if (response.size < 3) {
            return 0
        }

        // Extract relevant information
        val slaveAddress = response[0].toInt() and 0xFF
        val functionCode = response[1].toInt() and 0xFF
        val byteCount = response[2].toInt() and 0xFF

        // Check if the response length is as expected
        if (response.size < 3 + byteCount) {
            return 0
        }

        // Extract register values
        val registerValues = mutableListOf<Int>()
        for (i in 3 until 3 + byteCount step 2) {
            val highByte = response[i].toInt() and 0xFF
            val lowByte = response[i + 1].toInt() and 0xFF
            val registerValue = (highByte shl 8) or lowByte
            registerValues.add(registerValue)
        }

        return registerValues[0]
    }

    fun parseInputRegistersResponse(response: ByteArray): FloatArray {
        if(response.toHex().startsWith(INPUT_REGISTERS_CORRECT_RESPONSE_BITS)){
            val floatValues = FloatArray(response.size / 4)

            for (i in 3..response.size step 4) {
                if (i < response.size - 4) {
                    val floatBytes = response.copyOfRange(i, i + 4)
                    floatValues[i / 4] = ModbusTypeConverter.byteArrayToFloat(floatBytes)
                }

            }

            return floatValues
        }
        return FloatArray(0)
    }
}