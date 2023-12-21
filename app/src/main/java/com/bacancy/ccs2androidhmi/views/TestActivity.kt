package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityTestBinding
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil.startReading
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestActivity : SerialPortBaseActivity() {

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Testing Screen"

        //We have to wait for first request to provide its response and then send second request
        //Step 1: Request for misc data
        //Step 2: Read response of misc data
        //Step 3: Delay for 3 seconds
        //Step 4: Request for ac meter data
        //Step 5: Read response of ac meter data
        //Step 6: Now start Step 1 again with a delay of 3 seconds
        startReadingMiscAndAcMeterInformation()
    }

    private fun startReadingMiscAndAcMeterInformation() {
        lifecycleScope.launch {
            while (true) {
                withContext(Dispatchers.IO) {
                    //Step 1: Request for misc data
                    readMiscData()
                }
                //Step 6: Now start Step 1 again with a delay of 3 seconds
                delay(3000)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun readMiscData() {
        lifecycleScope.launch(Dispatchers.IO) {
            startReading(
                mOutputStream,
                mInputStream,
                ResponseSizes.MISC_INFORMATION_RESPONSE_SIZE,
                ModbusRequestFrames.getMiscInfoRequestFrame()
            ) {
                //Step 2: Read response of misc data
                if (it.toHex().startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)) {
                    Log.d("RUN_TAG", "MISC DATA RESPONSE: ${it.toHex()}")
                    val networkStatusBits =
                        ModbusTypeConverter.byteArrayToBinaryString(it.copyOfRange(3, 5)).reversed()
                            .substring(0, 11)
                    val arrayOfNetworkStatusBits = networkStatusBits.toCharArray()
                    val wifiNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(0, 3)
                    val gsmNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(3, 7)
                    val ethernetConnectedBits = arrayOfNetworkStatusBits.copyOfRange(7, 8)
                    val serverConnectedWithBits = arrayOfNetworkStatusBits.copyOfRange(8, 11)

                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.txtServerConnection.text =
                            "Server connection = ${
                                StateAndModesUtils.checkServerConnectedWith(
                                    serverConnectedWithBits
                                )
                            }"
                        updateServerStatus(
                            StateAndModesUtils.checkServerConnectedWith(
                                serverConnectedWithBits
                            )
                        )
                        binding.txtEthernetConnection.text =
                            "Ethernet = ${
                                StateAndModesUtils.checkIfEthernetIsConnected(
                                    ethernetConnectedBits
                                )
                            }"
                        updateEthernetStatus(
                            StateAndModesUtils.checkIfEthernetIsConnected(
                                ethernetConnectedBits
                            )
                        )
                        binding.txtGSMStrength.text =
                            "GSM Strength = ${
                                StateAndModesUtils.checkGSMNetworkStrength(
                                    gsmNetworkStrengthBits
                                )
                            }"
                        binding.txtWifiStrength.text =
                            "Wifi Strength = ${
                                StateAndModesUtils.checkWifiNetworkStrength(
                                    wifiNetworkStrengthBits
                                )
                            }"
                        adjustWifiLevel(
                            StateAndModesUtils.checkWifiNetworkStrength(
                                wifiNetworkStrengthBits
                            ).toInt()
                        )
                    }

                    Log.d(
                        "RUN_TAG", "Server connection = ${
                            StateAndModesUtils.checkServerConnectedWith(
                                serverConnectedWithBits
                            )
                        }"
                    )

                    Log.d(
                        "RUN_TAG", "Ethernet = ${
                            StateAndModesUtils.checkIfEthernetIsConnected(
                                ethernetConnectedBits
                            )
                        }"
                    )

                    Log.d(
                        "RUN_TAG", "GSM Strength = ${
                            StateAndModesUtils.checkGSMNetworkStrength(
                                gsmNetworkStrengthBits
                            )
                        }"
                    )

                    Log.d(
                        "RUN_TAG", "Wifi Strength = ${
                            StateAndModesUtils.checkWifiNetworkStrength(
                                wifiNetworkStrengthBits
                            )
                        }"
                    )

                    lifecycleScope.launch {
                        //Step 3: Delay for 3 seconds
                        delay(3000)
                        //Step 4: Request for ac meter data
                        readAcMeterData()
                    }
                }
            }
        }

    }

    private suspend fun readAcMeterData() {
        startReading(
            mOutputStream,
            mInputStream,
            ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getACMeterInfoRequestFrame()
        ) {
            //Step 5: Read response of ac meter data
            if (it.toHex().startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.d("RUN_TAG", "AC METER DATA RESPONSE: ${it.toHex()}")
                val newResponse = ModBusUtils.parseInputRegistersResponse(it)
                Log.d("RUN_TAG", "AC METER: ${newResponse.toList()}")
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.tvVoltageL1.text = newResponse[0].formatFloatToString()
                    binding.tvVoltageL2.text = newResponse[1].formatFloatToString()
                    binding.tvVoltageL3.text = newResponse[2].formatFloatToString()
                }
            }
        }

    }

    private fun adjustWifiLevel(wifiLevel: Int) {
        when (wifiLevel) {
            1 -> binding.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_1)
            2 -> binding.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_2)
            3 -> binding.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_3)
            else -> binding.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_0)
        }
    }

    private fun updateEthernetStatus(status: String) {
        when (status) {
            "Not Connected" -> binding.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_disconnected)
            "Connected" -> binding.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_connected)
            else -> binding.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_disconnected)
        }
    }

    private fun updateServerStatus(serverStatus: String) {
        when (serverStatus) {
            "Ethernet" -> binding.imgServerStatus.setImageResource(R.drawable.ic_server_with_ethernet)
            "GSM" -> binding.imgServerStatus.setImageResource(R.drawable.ic_server_with_gsm)
            "Wifi" -> binding.imgServerStatus.setImageResource(R.drawable.ic_server_with_wifi)
            else -> binding.imgServerStatus.setImageResource(R.drawable.ic_server_with_nothing)
        }
    }

    fun goToNewTestScreen(view: View) {
        startActivity(Intent(this, NewTestActivity::class.java))
    }

}