package com.bacancy.ccs2androidhmi.util

import android.util.Log
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream

object ReadWriteUtil {

    private const val DELAY_BETWEEN_READ_AND_WRITE = 500L

    suspend fun writeToSingleHoldingRegisterNew(
        mOutputStream: OutputStream?,
        mInputStream: InputStream?,
        startAddress: Int,
        regValue: Int,
        onAuthDataReceived: (ByteArray?) -> Unit, onReadStopped: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val bufferedInputStream = BufferedInputStream(mInputStream)
            val bufferedOutputStream = BufferedOutputStream(mOutputStream)

            val writeRequestFrame: ByteArray =
                ModBusUtils.createWriteSingleRegisterRequest(startAddress, regValue)
            withTimeout(1000) {
                try {
                    withContext(Dispatchers.IO) {
                        bufferedOutputStream.write(writeRequestFrame)
                        bufferedOutputStream.flush()
                        Log.w(
                            "TAG",
                            "writeToSingleHoldingRegisterNew: BufferedInputStream available bytes - ${bufferedInputStream.available()}"
                        )
                        delay(DELAY_BETWEEN_READ_AND_WRITE)
                        val responseFrame = ByteArray(7)
                        bufferedInputStream.mark(0)
                        bufferedInputStream.read(responseFrame)
                        onAuthDataReceived(null)
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.e(
                        "RWU",
                        "writeToSingleHoldingRegisterNew: TimeOutException = ${e.printStackTrace()}",

                        )
                    withContext(Dispatchers.IO) {
                        delay(DELAY_BETWEEN_READ_AND_WRITE)
                        val responseFrame = ByteArray(7)
                        bufferedInputStream.mark(0)
                        bufferedInputStream.read(responseFrame)
                        onAuthDataReceived(null)
                    }
                }
            }
        }
    }

    suspend fun writeRequestAndReadResponse(
        mOutputStream: OutputStream?, mInputStream: InputStream?, responseSize: Int,
        requestFrame: ByteArray, onDataReceived: (ByteArray) -> Unit, onReadStopped: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                Log.e("TAG", "writeRequestAndReadResponse: BOS = $mOutputStream")
                Log.e("TAG", "writeRequestAndReadResponse: BIS = $mInputStream")
                if(mInputStream != null && mOutputStream!= null){
                    val bufferedInputStream = BufferedInputStream(mInputStream)
                    val bufferedOutputStream = BufferedOutputStream(mOutputStream)
                    withTimeoutOrNull(1000) {
                        try {
                            withContext(Dispatchers.IO) {
                                if(bufferedOutputStream!=null) {
                                    Log.w(
                                        "MONTAG",
                                        "writeRequestAndReadResponse 1: Calling write from Normal State"
                                    )
                                    bufferedOutputStream.write(requestFrame)
                                    Log.w(
                                        "MONTAG",
                                        "writeRequestAndReadResponse 2: Flush in outputstream"
                                    )
                                    bufferedOutputStream.flush()
                                }
                            }
                        } catch (e: TimeoutCancellationException) {
                            e.printStackTrace()
                            withContext(Dispatchers.IO) {
                                Log.w(
                                    "MONTAG",
                                    "writeRequestAndReadResponse 11: Calling write from Timeout State"
                                )
                                bufferedOutputStream.write(requestFrame)
                                bufferedOutputStream.flush()
                            }
                        }
                    }

                    Log.w(
                        "MONTAG",
                        "writeRequestAndReadResponse 4: Delaying for 500ms"
                    )
                    delay(DELAY_BETWEEN_READ_AND_WRITE) //waiting for 500ms between write and read

                    Log.w(
                        "MONTAG",
                        "writeRequestAndReadResponse 3: Init bytearray using responseSize"
                    )
                    val responseFrame = ByteArray(responseSize)

                    Log.w(
                        "MONTAG",
                        "writeRequestAndReadResponse 5: mark input stream with 0"
                    )
                    bufferedInputStream.mark(0)

                    Log.w(
                        "MONTAG",
                        "writeRequestAndReadResponse 6: Read responseFrame from inputstream - ${bufferedInputStream.available()}"
                    )

                    var size = 0

                    if (bufferedInputStream.available() > 0) {
                        size = bufferedInputStream.read(responseFrame)
                    } else {
                        Log.e("TAG", "writeRequestAndReadResponse 12: READ STOPPED")
                        onReadStopped()
                        return@withContext
                    }

                    Log.w(
                        "MONTAG",
                        "writeRequestAndReadResponse 7: Checking the size and valid response"
                    )
                    if (size > 0 && isValidResponse(responseFrame) && isValidCRCInResponse(responseFrame)) {
                        Log.w(
                            "MONTAG",
                            "writeRequestAndReadResponse 8: Sending the callback with correct response frame"
                        )
                        onDataReceived(responseFrame)
                    } else {
                        //writeRequestAndReadResponse(mOutputStream,mInputStream, responseSize, requestFrame, onDataReceived, onReadStopped)
                        //onReadStopped()
                        Log.e(
                            "RWU",
                            "writeRequestAndReadResponse 9: Error Frame (${responseFrame[2]}) - ${responseFrame.toHex()}"
                        )
                        onDataReceived(responseFrame)
                    }
                }
            } catch (e: Exception) {
                Log.e("TAG", "writeRequestAndReadResponse 10: In Catch - ${e.printStackTrace()}")
                e.printStackTrace()
            }
        }
    }

    private fun isValidResponse(responseFrame: ByteArray): Boolean {
        val hexResponse = responseFrame.toHex()
        return hexResponse.startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS) ||
                hexResponse.startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
    }

    private fun isValidCRCInResponse(responseBytes: ByteArray): Boolean {
        if (responseBytes.size < 2) {
            // Insufficient data for CRC
            Log.d("RWU", "validateResponseCRC: Insufficient data for CRC")
            return false
        }

        /*val receivedCRC = (responseBytes[responseBytes.size - 2].toInt() and 0xFF) or
                ((responseBytes[responseBytes.size - 1].toInt() and 0xFF) shl 8)*/

        val responseData = responseBytes.copyOfRange(0, responseBytes.size - 2)
        val calculatedCRC = calculateCRC(responseData)
        val recdCRC = responseBytes.copyOfRange(responseBytes.size - 2, responseBytes.size)
        Log.d("RWU", "validateResponseCRC: Recd CRC = ${recdCRC.toHex()}")
        Log.d(
            "RWU",
            "validateResponseCRC: Calc CRC = ${calculatedCRC.toHex()}"
        )
        return recdCRC.toHex() == calculatedCRC.toHex()
        //return receivedCRC == getActualIntValueFromHighAndLowBytes(calculatedCRC[0].toInt(),calculatedCRC[1].toInt())
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

}