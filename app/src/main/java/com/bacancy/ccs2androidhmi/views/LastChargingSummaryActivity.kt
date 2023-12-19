package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityLastChargingSummaryBinding
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getChargingEndTime
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getChargingStartTime
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getEVMacAddress
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getEndSoc
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getEnergyConsumption
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getSessionEndReason
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getStartSoc
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getTotalChargingTime
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE
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
                        lastChargingSummaryRequestFrame, { responseFrameArray ->
                            onDataReceived(responseFrameArray)
                        }, {
                            //OnFailure
                        })

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
        Log.d("TAG", "onDataReceived: ${responseFrameArray.toHex()}")
        lifecycleScope.launch(Dispatchers.Main) {
            binding.apply {
                txtEvMacAddress.text = "EV Mac Address = ${getEVMacAddress(responseFrameArray)}"
                txtChargingDuration.text =
                    "Charging Duration = ${getTotalChargingTime(responseFrameArray)} mins"
                txtChargingStartDateTime.text =
                    "Charging Start Date and Time = ${getChargingStartTime(responseFrameArray)}"
                txtChargingEndDateTime.text =
                    "Charging End Date and Time = ${getChargingEndTime(responseFrameArray)}"
                txtChargingStartSOC.text = "Start SoC = ${getStartSoc(responseFrameArray)}%"
                txtChargingEndSOC.text = "End Soc = ${getEndSoc(responseFrameArray)}%"
                txtEnergyConsumption.text = "Energy consumption = ${getEnergyConsumption(responseFrameArray)} kWh"
                txtSessionEndReason.text = "Session End Reason = ${getSessionEndReason(responseFrameArray)}"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        observer.stopObserving()
    }
}