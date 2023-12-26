package com.bacancy.ccs2androidhmi.views

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivityNew
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AsyncTestActivity : SerialPortBaseActivityNew() {


    private var startTime: Long = 0L
    private var endTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_async_test)

        performReadOperation(true)
    }

    private fun performReadOperation(isAcResponseGot: Boolean = false) {
        Log.e("WELL_TAG", "performReadOperation: isAcResponseGot = $isAcResponseGot")
        lifecycleScope.launch {
            while (isAcResponseGot) {
                readMiscInfo()
                //readAcMeterInfo()
                delay(3000)
            }
        }
    }

    suspend fun startOne() {
        Log.d("TAG", "startOne: Started")
        delay(3000)
        Log.d("TAG", "startOne: Ended")
    }

    suspend fun startTwo() {
        Log.i("TAG", "startTwo: Started")
        delay(3000)
        Log.i("TAG", "startTwo: Ended")
    }

    suspend fun startThree() {
        Log.e("TAG", "startThree: Started")
        delay(3000)
        Log.e("TAG", "startThree: Ended")
    }

    private suspend fun readMiscInfo() {
        Log.i(
            "WELL_TAG",
            "readMiscInfo: Request HEX = ${ModbusRequestFrames.getMiscInfoRequestFrame().toHex()}"
        )
        startTime = System.currentTimeMillis()
        ReadWriteUtil.startReading(
            mOutputStream,
            mInputStream,
            ResponseSizes.MISC_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getMiscInfoRequestFrame()
        ) {
            if (it.toHex().startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.i("WELL_TAG", "readMiscInfo Response Hex: ${it.toHex()}")
                lifecycleScope.launch {
                    readAcMeterInfo()
                }
            }
        }
    }

    private suspend fun readAcMeterInfo() {
        Log.e(
            "WELL_TAG",
            "readAcMeterInfo: Request HEX = ${
                ModbusRequestFrames.getACMeterInfoRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.startReading(
            mOutputStream,
            mInputStream,
            ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getACMeterInfoRequestFrame()
        ) {
            if (it.toHex().startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.e("WELL_TAG", "readAcMeterInfo Response Hex: ${it.toHex()}")
                performReadOperation(true)
            }
        }
    }

    private suspend fun readLastChargingSummary() {
        Log.d(
            "WELL_TAG",
            "readLastChargingSummary: Request HEX = ${
                ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.startReading(
            mOutputStream,
            mInputStream,
            ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE,
            ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame()
        ) {
            Log.d("WELL_TAG", "readLastChargingSummary: ${it.toHex()}")
        }
    }

}