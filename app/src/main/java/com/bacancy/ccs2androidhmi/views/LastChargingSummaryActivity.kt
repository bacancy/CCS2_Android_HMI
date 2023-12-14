package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityLastChargingSummaryBinding
import com.bacancy.ccs2androidhmi.util.CommonUtils.getDateAndTimeFromHexArray
import com.bacancy.ccs2androidhmi.util.CommonUtils.getSimpleMacAddress
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getActualIntValueFromHighAndLowBytes
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.hexStringToDecimal
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LastChargingSummaryActivity : SerialPortBaseActivity() {

    private lateinit var binding: ActivityLastChargingSummaryBinding
    private var isGun1: Boolean? = null
    private lateinit var observer: ModbusReadObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLastChargingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Last Charging Summary"
        isGun1 = intent.extras?.getBoolean("IS_GUN1", true)
        startReadingLastChargingSummary()
    }

    private fun startReadingLastChargingSummary() {

        val lastChargingSummaryRequestFrame: ByteArray = if (isGun1 == true) {
            ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame()
        } else {
            ModbusRequestFrames.getGun2LastChargingSummaryRequestFrame()
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    observer = ModbusReadObserver()
                    observer.startObserving(
                        mOutputStream,
                        mInputStream, LAST_CHARGING_SUMMARY_RESPONSE_SIZE,
                        lastChargingSummaryRequestFrame
                    ) { responseFrameArray ->
                        onDataReceived(responseFrameArray)
                    }

                    delay(5000)
                    observer.stopObserving()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onDataReceived(responseFrameArray: ByteArray) {

        lifecycleScope.launch(Dispatchers.Main) {
            binding.apply {
                txtEvMacAddress.text = "EV Mac Address = ${getEVMacAddress(responseFrameArray)}"
                txtChargingDuration.text = "Charging Duration = ${getTotalChargingTime(responseFrameArray)}"
                txtChargingStartDateTime.text = "Charging Start Date and Time = ${getChargingStartTime(responseFrameArray)}"
                txtChargingEndDateTime.text = "Charging End Date and Time = ${getChargingEndTime(responseFrameArray)}"
                txtChargingStartSOC.text = getStartSoc(responseFrameArray)
                txtChargingEndSOC.text = getEndSoc(responseFrameArray)
                txtEnergyConsumption.text = getEnergyConsumption(responseFrameArray)
                txtSessionEndReason.text = getSessionEndReason(responseFrameArray)
            }
        }

        Log.d("TAG", "onDataReceived: ${responseFrameArray.toHex()}")
        //0103009600156429
        //01 03 2a 00 00 00 01 87 0f 66 30 0e 0c 07 e7 0c 2e 08 00 0e 0c 07 e7 0c 2f 09 00 00 01 00 32 00 32 3c 21 00 00 00 04 00 00 00 00 00 00 e4 57
        Log.d("TAG", "onDataReceived: EV MAC ADDRESS - ${getEVMacAddress(responseFrameArray)}")
        Log.d(
            "TAG",
            "onDataReceived: Total Charging Time - ${getTotalChargingTime(responseFrameArray)}"
        )
        Log.d(
            "TAG",
            "onDataReceived: Charging Start Time - ${getChargingStartTime(responseFrameArray)}"
        )
        Log.d(
            "TAG",
            "onDataReceived: Charging End Time - ${getChargingEndTime(responseFrameArray)}"
        )

        Log.d("TAG", "onDataReceived: Start SOC = ${getStartSoc(responseFrameArray)}")
        Log.d("TAG", "onDataReceived: End SOC = ${getEndSoc(responseFrameArray)}")
        Log.d("TAG", "onDataReceived: ${getEnergyConsumption(responseFrameArray)}")
        Log.d("TAG", "onDataReceived: ${getSessionEndReason(responseFrameArray)}")
    }

    private fun getSessionEndReason(buffer: ByteArray): String {
        val endReasonNumber = getActualIntValueFromHighAndLowBytes(
            buffer[37].getIntValueFromByte(),
            buffer[38].getIntValueFromByte()
        )
        return "Session End Reason = ${
            StateAndModesUtils.SessionEndReasons.fromStateValue(
                endReasonNumber
            ).description
        }"
    }

    private fun getEnergyConsumption(buffer: ByteArray): String {
        val energyConsumption = ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(33, 33 + 4))
        return "Energy Consumption = ${energyConsumption.formatFloatToString()} kWh"
    }

    private fun getStartSoc(buffer: ByteArray): String {
        val startSocLSB = buffer[29].getIntValueFromByte()
        val startSocMSB = buffer[30].getIntValueFromByte()
        return "Start Soc(%) = ${
            getActualIntValueFromHighAndLowBytes(
                startSocLSB,
                startSocMSB
            )
        } %"
    }

    private fun getEndSoc(buffer: ByteArray): String {
        val endSocLSB = buffer[31].getIntValueFromByte()
        val endSocMSB = buffer[32].getIntValueFromByte()
        return "End Soc(%) = ${
            getActualIntValueFromHighAndLowBytes(
                endSocLSB,
                endSocMSB
            )
        } %"
    }

    private fun getChargingEndTime(responseFrameArray: ByteArray): String {
        val chargingEndTimeArray = responseFrameArray.copyOfRange(19, 19 + 8)
        val mappedArray = chargingEndTimeArray.map { it.getIntValueFromByte() }
        val hexArray = ModbusTypeConverter.decimalArrayToHexArray(mappedArray)
        return getDateAndTimeFromHexArray(hexArray)
    }

    private fun getChargingStartTime(responseFrameArray: ByteArray): String {
        val chargingStartTimeArray = responseFrameArray.copyOfRange(11, 11 + 8)
        val mappedArray = chargingStartTimeArray.map { it.getIntValueFromByte() }
        val hexArray = ModbusTypeConverter.decimalArrayToHexArray(mappedArray)
        return getDateAndTimeFromHexArray(hexArray)
    }

    private fun getTotalChargingTime(response: ByteArray): String {
        val totalChargingTime = getActualIntValueFromHighAndLowBytes(
            response[27].getIntValueFromByte(),
            response[28].getIntValueFromByte()
        )
        return "$totalChargingTime minutes"
    }

    private fun getEVMacAddress(response: ByteArray): String {
        val macAddressArray = response.copyOfRange(3, 3 + 8)
        return getSimpleMacAddress(macAddressArray, "-")
    }

    override fun onPause() {
        super.onPause()
        observer.stopObserving()
    }
}