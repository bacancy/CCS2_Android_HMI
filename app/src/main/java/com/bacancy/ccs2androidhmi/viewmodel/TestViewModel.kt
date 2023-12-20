package com.bacancy.ccs2androidhmi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class TestViewModel: ViewModel() {

    private val inputRegistersLiveData = MutableLiveData<ByteArray>()
    private val holdingRegistersLiveData = MutableLiveData<ByteArray>()

    fun getInputRegistersLiveData(): LiveData<ByteArray> = inputRegistersLiveData
    fun getHoldingRegistersLiveData(): LiveData<ByteArray> = holdingRegistersLiveData

    private val scope = viewModelScope

    fun startObservingInputRegisters(
        mOutputStream: OutputStream?,
        mInputStream: InputStream?,
        responseSize: Int,
        requestFrame: ByteArray
    ) {
        scope.async {
            while (isActive) {
                try {
                    withContext(Dispatchers.IO) {
                        mOutputStream?.write(requestFrame)
                        val responseFrame = ByteArray(responseSize)
                        mInputStream?.read(responseFrame)

                        if (isValidResponse(responseFrame)) {
                            inputRegistersLiveData.postValue(responseFrame)
                        } else {
                            //onFailure
                        }
                    }

                    delay(TimeUnit.SECONDS.toMillis(1))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun startObservingHoldingRegisters(
        mOutputStream: OutputStream?,
        mInputStream: InputStream?,
        responseSize: Int,
        requestFrame: ByteArray
    ) {
        scope.launch {
            while (isActive) {
                try {
                    withContext(Dispatchers.IO) {
                        mOutputStream?.write(requestFrame)
                        val responseFrame = ByteArray(responseSize)
                        mInputStream?.read(responseFrame)

                        if (isValidResponse(responseFrame)) {
                            holdingRegistersLiveData.postValue(responseFrame)
                        } else {
                            //OnFailure
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
        return hexResponse.startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS) ||
                hexResponse.startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
    }

    fun cancelObservers(){
        scope.cancel()
    }

}