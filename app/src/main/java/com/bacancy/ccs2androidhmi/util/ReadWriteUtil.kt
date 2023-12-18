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
        val requestFrame: ByteArray =
            ModBusUtils.createWriteSingleRegisterRequest(1, startAddress, regValue)

        withContext(Dispatchers.IO) {
            mOutputStream?.write(requestFrame)
        }

        val responseFrame = ByteArray(16)
        val size: Int = withContext(Dispatchers.IO) {
            mInputStream?.read(responseFrame) ?:  0
        }

        if (size > 0) {
            val requestFrame1: ByteArray =
                ModBusUtils.createReadHoldingRegistersRequest(1, startAddress, 1)

            withContext(Dispatchers.IO) {
                mOutputStream?.write(requestFrame1)
            }

            val responseFrame1 = ByteArray(64)
            val size1: Int = withContext(Dispatchers.IO) {
                mInputStream?.read(responseFrame1) ?: 0
            }

            if (size1 > 0) {
                onAuthDataReceived(responseFrame1)
            }
        }
    }
}