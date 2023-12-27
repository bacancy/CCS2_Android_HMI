package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityGun1InformationBinding
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getChargingEndTime
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getChargingStartTime
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getEVMacAddress
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getEndSoc
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getEnergyConsumption
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getSessionEndReason
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getStartSoc
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getTotalChargingTime
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils.getTotalCost
import com.bacancy.ccs2androidhmi.util.ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getActualIntValueFromHighAndLowBytes
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.util.ResponseSizes.GUN_INFORMATION_RESPONSE_SIZE
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@AndroidEntryPoint
class Gun1InformationActivity : SerialPortBaseActivity() {

    private var isChargingStarted: Boolean = false
    private var isGun1: Boolean? = null
    private val appViewModel: AppViewModel by viewModels()
    private lateinit var observer: ModbusReadObserver
    private lateinit var binding: ActivityGun1InformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGun1InformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isGun1 = intent.extras?.getBoolean("IS_GUN1", true)
        if (isGun1 == true) {
            supportActionBar?.title = "Gun 1 Information"
        } else {
            supportActionBar?.title = "Gun 2 Information"
        }
        startReadingGun1Information()
    }

    private fun startReadingGun1Information() {

        val gunRequestFrame: ByteArray = if (isGun1 == true) {
            ModbusRequestFrames.getGun1InfoRequestFrame()
        } else {
            ModbusRequestFrames.getGun2InfoRequestFrame()
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    observer = ModbusReadObserver()
                    observer.startObserving(
                        mOutputStream,
                        mInputStream, GUN_INFORMATION_RESPONSE_SIZE,
                        gunRequestFrame, { responseFrameArray ->
                            onDataReceived(responseFrameArray)
                        }, {
                            //OnFailure
                        })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onDataReceived(buffer: ByteArray) {
        Log.d("TAG", "onDataReceived: ${buffer.toHex()}")
        lifecycleScope.launch(Dispatchers.Main) {
            binding.apply {

                val connectorStatusLSB = buffer[3].getIntValueFromByte()
                val connectorStatusMSB = buffer[4].getIntValueFromByte()
                val connectorStatus =
                    getActualIntValueFromHighAndLowBytes(connectorStatusLSB, connectorStatusMSB)
                if (connectorStatus == 0) {
                    txtConnectorStatus.text = "Connector Status = OFF"
                } else {
                    txtConnectorStatus.text = "Connector Status = ON"
                }


                val initialSocLSB = buffer[7].getIntValueFromByte()
                val initialSocMSB = buffer[8].getIntValueFromByte()
                txtInitialSoc.text = "Initial Soc(%) = ${
                    getActualIntValueFromHighAndLowBytes(
                        initialSocLSB,
                        initialSocMSB
                    )
                } %"

                val currentSocLSB = buffer[9].getIntValueFromByte()
                val currentSocMSB = buffer[10].getIntValueFromByte()
                txtCurrentSoc.text = "Current Soc(%) = ${
                    getActualIntValueFromHighAndLowBytes(
                        currentSocLSB,
                        currentSocMSB
                    )
                } %"

                val durationInMinutesLSB = buffer[11].getIntValueFromByte()
                val durationInMinutesMSB = buffer[12].getIntValueFromByte()
                val durationInHoursLSB = buffer[13].getIntValueFromByte()
                val durationInHoursMSB = buffer[14].getIntValueFromByte()
                val hour =
                    getActualIntValueFromHighAndLowBytes(durationInHoursLSB, durationInHoursMSB)
                val minutes =
                    getActualIntValueFromHighAndLowBytes(
                        durationInMinutesLSB,
                        durationInMinutesMSB
                    )
                txtDurationInHoursMinutes.text = "Duration (hh:mm) = $hour:$minutes"

                val chargingVoltageLSB = buffer[15].getIntValueFromByte()
                val chargingVoltageMSB = buffer[16].getIntValueFromByte()
                txtChargingVoltage.text = "Charging Voltage = ${
                    getActualIntValueFromHighAndLowBytes(
                        chargingVoltageLSB,
                        chargingVoltageMSB
                    )
                } V"

                val chargingCurrentLSB = buffer[17].getIntValueFromByte()
                val chargingCurrentMSB = buffer[18].getIntValueFromByte()
                txtChargingCurrent.text = "Charging Current = ${
                    getActualIntValueFromHighAndLowBytes(
                        chargingCurrentLSB,
                        chargingCurrentMSB
                    )
                } A"

                val demandVoltageLSB = buffer[19].getIntValueFromByte()
                val demandVoltageMSB = buffer[20].getIntValueFromByte()
                txtDemandVoltage.text = "Demand Voltage = ${
                    getActualIntValueFromHighAndLowBytes(
                        demandVoltageLSB,
                        demandVoltageMSB
                    )
                } V"

                val demandCurrentLSB = buffer[21].getIntValueFromByte()
                val demandCurrentMSB = buffer[22].getIntValueFromByte()
                txtDemandCurrent.text = "Demand Current = ${
                    getActualIntValueFromHighAndLowBytes(
                        demandCurrentLSB,
                        demandCurrentMSB
                    )
                } A"

                val energyConsumption =
                    ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(23, 27))
                txtEnergyConsumption.text = "Energy Consumption = $energyConsumption kw"

                val gunTemperatureDCPositive =
                    ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(27, 31))
                val gunTemperatureDCNegative =
                    ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(31, 35))
                val totalCost = ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(35, 39))

                val chargingStateLSB = buffer[5].getIntValueFromByte()
                val chargingStateMSB = buffer[6].getIntValueFromByte()
                val gunChargingState = StateAndModesUtils.GunChargingState.fromStateValue(
                    getActualIntValueFromHighAndLowBytes(chargingStateLSB, chargingStateMSB)
                )
                txtChargingState.text = "Charging State = ${gunChargingState.description}"

                when (gunChargingState) {
                    StateAndModesUtils.GunChargingState.CHARGING -> isChargingStarted = true
                    StateAndModesUtils.GunChargingState.COMPLETE -> {
                        if (isChargingStarted) {
                            isChargingStarted = false
                            getLastChargingSummary()
                        }
                    }

                    StateAndModesUtils.GunChargingState.EMERGENCY_STOP -> {
                        if (isChargingStarted) {
                            isChargingStarted = false
                            getLastChargingSummary()
                        }
                    }

                    else -> isChargingStarted = false
                }

            }
        }
    }

    private fun getLastChargingSummary() {
        observer.stopObserving()
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
                        mInputStream, ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE,
                        lastChargingSummaryRequestFrame, { responseFrameArray ->
                            onLastChargingSummaryDataReceived(responseFrameArray)
                        }, {
                            //OnFailure
                        })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onLastChargingSummaryDataReceived(responseFrameArray: ByteArray) {
        Log.d("TAG", "onLastChargingSummaryDataReceived: ${responseFrameArray.toHex()}")
        lifecycleScope.launch(Dispatchers.Main) {

            if (responseFrameArray.toHex().startsWith(HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)) {
                observer.stopObserving()
                val chargingSummary = ChargingSummary(
                    gunNumber = if(isGun1 == true) 1 else 2,
                    evMacAddress = getEVMacAddress(responseFrameArray),
                    chargingStartTime = getChargingStartTime(responseFrameArray),
                    chargingEndTime = getChargingEndTime(responseFrameArray),
                    totalChargingTime = getTotalChargingTime(responseFrameArray),
                    startSoc = getStartSoc(responseFrameArray),
                    endSoc = getEndSoc(responseFrameArray),
                    energyConsumption = getEnergyConsumption(responseFrameArray),
                    sessionEndReason = getSessionEndReason(responseFrameArray),
                    customSessionEndReason = "NA",
                    totalCost = getTotalCost(responseFrameArray)
                )
                appViewModel.insertChargingSummary(chargingSummary)
                startReadingGun1Information()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        observer.stopObserving()
    }

    fun goToLastChargingSummary(view: View) {
        startActivity(
            Intent(this, LastChargingSummaryActivity::class.java).putExtra(
                "IS_GUN1",
                isGun1
            )
        )
    }
}