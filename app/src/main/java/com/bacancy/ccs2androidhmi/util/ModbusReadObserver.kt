package com.bacancy.ccs2androidhmi.util

import com.bacancy.ccs2androidhmi.util.ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS
import com.bacancy.ccs2androidhmi.util.ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class ModbusReadObserver {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    fun startObserving(
        mOutputStream: OutputStream?,
        mInputStream: InputStream?,
        responseSize: Int,
        requestFrame: ByteArray,
        onSuccess: (ByteArray) -> Unit,
        onFailure: (ByteArray) -> Unit
    ) {
        job = scope.launch {
            while (isActive) {
                try {
                    withContext(Dispatchers.IO) {
                        mOutputStream?.write(requestFrame)
                        val responseFrame = ByteArray(responseSize)
                        mInputStream?.read(responseFrame)

                        if (isValidResponse(responseFrame)) {
                            onSuccess(responseFrame)
                        } else {
                            onFailure(responseFrame)
                        }
                    }

                    delay(TimeUnit.SECONDS.toMillis(1))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isValidResponse(responseFrame: ByteArray): Boolean {
        val hexResponse = responseFrame.toHex()
        return hexResponse.startsWith(HOLDING_REGISTERS_CORRECT_RESPONSE_BITS) ||
                hexResponse.startsWith(INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
    }

    fun stopObserving() {
        job?.cancel()
    }
}