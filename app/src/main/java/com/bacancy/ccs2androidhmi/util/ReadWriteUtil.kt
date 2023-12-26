package com.bacancy.ccs2androidhmi.util

import android.util.Log
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object ReadWriteUtil {

    suspend fun writeToSingleHoldingRegisterNew(
        mOutputStream: OutputStream?,
        mInputStream: InputStream?,
        startAddress: Int,
        regValue: Int,
        onAuthDataReceived: (ByteArray) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val writeRequestFrame: ByteArray =
                ModBusUtils.createWriteSingleRegisterRequest(startAddress, regValue)
            mOutputStream?.write(writeRequestFrame)

            val writeResponseFrame = ByteArray(16)
            val size: Int = mInputStream?.read(writeResponseFrame) ?: 0

            if (size > 0) {
                val readRequestFrame: ByteArray =
                    ModBusUtils.createReadHoldingRegistersRequest(startAddress, 1)
                mOutputStream?.write(readRequestFrame)

                val readResponseFrame = ByteArray(64)
                val size1: Int = mInputStream?.read(readResponseFrame) ?: 0

                if (size1 > 0) {
                    onAuthDataReceived(readResponseFrame)
                }
            }
        }
    }

    fun readInputStream(inputStream: InputStream, responseSize: Int): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            val buffer = ByteArray(responseSize) // Adjust the buffer size as needed

            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.toByteArray()
        }
    }

    suspend fun startReading(
        mOutputStream: OutputStream?, mInputStream: InputStream?, responseSize: Int,
        requestFrame: ByteArray, onAuthDataReceived: (ByteArray) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                mOutputStream?.write(requestFrame)

                val responseFrame = ByteArray(responseSize)

                val size: Int? = mInputStream?.read(responseFrame)
                if(isValidResponse(responseFrame)){
                    //Log.w("LOST", "startReading: ${readInputStream(mInputStream!!, responseSize).toHex()}")
                    Log.w("LOST", "startReading: ${responseFrame.toHex()}")
                }

                if (size != null) {
                    if (size > 0) {
                        if (isValidResponse(responseFrame)) {
                            onAuthDataReceived(responseFrame)
                        } else {
                            //Log.e("WELL_TAG", "Reading Error = ${responseFrame.toHex()}")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isValidResponse(responseFrame: ByteArray): Boolean {
        val hexResponse = responseFrame.toHex()
        return hexResponse.startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS) ||
                hexResponse.startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
    }

    suspend fun startReadingSync(
        mOutputStream: OutputStream?, mInputStream: InputStream?, responseSize: Int,
        requestFrame: ByteArray
    ): ByteArray {
        val byteArrayResponse = ByteArray(responseSize)
        // Simulate Modbus read operation
        coroutineScope {
            withContext(Dispatchers.IO) {
                try {
                    mOutputStream?.write(requestFrame)

                    //val responseFrame = ByteArray(responseSize)
                    val size: Int? = mInputStream?.read(byteArrayResponse)

                    if (size != null) {
                        if (size > 0) {
                            if (isValidResponse(byteArrayResponse)) {
                                byteArrayResponse
                            } else {
                                Log.e(
                                    "TAG",
                                    "readHoldingRegisters: Error = ${byteArrayResponse.toHex()}"
                                )
                            }
                        } else {

                        }
                    } else {

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return byteArrayResponse
    }
}