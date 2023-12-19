package com.bacancy.ccs2androidhmi.util

import kotlinx.coroutines.Dispatchers
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
}