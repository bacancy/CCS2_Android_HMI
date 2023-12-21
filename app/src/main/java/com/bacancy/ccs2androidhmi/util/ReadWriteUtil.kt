package com.bacancy.ccs2androidhmi.util

import android.util.Log
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
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

    suspend fun startReading(
        mOutputStream: OutputStream?, mInputStream: InputStream?, responseSize: Int,
        requestFrame: ByteArray, onAuthDataReceived: (ByteArray) -> Unit
    ) {
        // Simulate Modbus read operation
        coroutineScope {
            withContext(Dispatchers.IO) {
                try {
                    mOutputStream?.write(requestFrame)

                    val responseFrame = ByteArray(responseSize)
                    val size: Int? = mInputStream?.read(responseFrame)

                    if (size != null) {
                        if (size > 0) {
                            if (isValidResponse(responseFrame)) {
                                onAuthDataReceived(responseFrame)
                            } else {
                                Log.e("TAG", "readHoldingRegisters: Error = ${responseFrame.toHex()}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isValidResponse(responseFrame: ByteArray): Boolean {
        val hexResponse = responseFrame.toHex()
        return hexResponse.startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS) ||
                hexResponse.startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
    }
}