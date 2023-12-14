package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityGun1InformationBinding
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getActualIntValueFromHighAndLowBytes
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ResponseSizes.GUN_INFORMATION_RESPONSE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class Gun1InformationActivity : SerialPortBaseActivity() {

    private var isGun1: Boolean? = null
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
            Log.d("TAG", "startReadingGun1Information: Gun1")
            ModbusRequestFrames.getGun1InfoRequestFrame()
        } else {
            Log.d("TAG", "startReadingGun1Information: Gun2")
            ModbusRequestFrames.getGun2InfoRequestFrame()
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    observer = ModbusReadObserver()
                    observer.startObserving(
                        mOutputStream,
                        mInputStream, GUN_INFORMATION_RESPONSE_SIZE,
                        gunRequestFrame
                    ) { responseFrameArray ->
                        onDataReceived(responseFrameArray)
                    }
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
                    Log.d("TAG", "onDataReceived: Connector Status = Connector OFF")
                    txtConnectorStatus.text = "Connector Status = OFF"
                } else {
                    Log.d("TAG", "onDataReceived: Connector Status = Connector ON")
                    txtConnectorStatus.text = "Connector Status = ON"
                }

                val chargingStateLSB = buffer[5].getIntValueFromByte()
                val chargingStateMSB = buffer[6].getIntValueFromByte()
                txtChargingState.text = "Charging State = ${
                    StateAndModesUtils.GunChargingState.fromStateValue(
                        getActualIntValueFromHighAndLowBytes(chargingStateLSB, chargingStateMSB)
                    ).description
                }"

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
                    getActualIntValueFromHighAndLowBytes(durationInMinutesLSB, durationInMinutesMSB)
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

                /*val energyConsumption = ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(23, 23+1))
                txtEnergyConsumption.text = "Energy Consumption = $energyConsumption kw"*/

                /*val gunTemperatureDCPositive =
                    ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(25, 26))
                val gunTemperatureDCNegative =
                    ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(27, 28))
                val totalCost = ModbusTypeConverter.byteArrayToFloat(buffer.copyOfRange(29, 30))*/
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