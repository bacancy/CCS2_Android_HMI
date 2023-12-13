package com.bacancy.ccs2androidhmi.util

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
        onResponse: (ByteArray) -> Unit
    ) {
        job = scope.launch {
            while (isActive) {
                try {

                    withContext(Dispatchers.IO) {
                        mOutputStream?.write(requestFrame)
                    }

                    val responseFrame = ByteArray(responseSize)
                    withContext(Dispatchers.IO) {
                        mInputStream?.read(responseFrame)
                    }

                    onResponse(responseFrame)

                    delay(TimeUnit.SECONDS.toMillis(1))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopObserving() {
        job?.cancel()
    }
}