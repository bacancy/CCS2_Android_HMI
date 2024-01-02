package com.bacancy.ccs2androidhmi.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android_serialport_api.SerialPort
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.HMIApp
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

@AndroidEntryPoint
abstract class SerialPortBaseActivityNew : FragmentActivity() {

    protected var mApplication: HMIApp? = null
    protected var mSerialPort: SerialPort? = null
    protected var mOutputStream: OutputStream? = null
    var mInputStream: InputStream? = null
    private val mCommonDelay = 1000L

    val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)
        mApplication = application as HMIApp
        try {
            mSerialPort = mApplication!!.getSerialPort()
            mOutputStream = mSerialPort!!.outputStream
            mInputStream = mSerialPort!!.inputStream
        } catch (e: Exception) {
            Log.d("TAG", "onCreate: Exception = ${e.toString()}")
        }
    }

    private fun makeFullScreen() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onDestroy() {
        mApplication!!.closeSerialPort()
        mSerialPort = null
        super.onDestroy()
    }

    fun startReading() {
        lifecycleScope.launch {
            readMiscInfo()
        }
    }

    private suspend fun readMiscInfo() {
        Log.d(
            TAG,
            "readMiscInfo: Request Sent - ${ModbusRequestFrames.getMiscInfoRequestFrame().toHex()}"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.MISC_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getMiscInfoRequestFrame()
        ) {
            if (it.toHex().startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.d(TAG, "readMiscInfo: Response = ${it.toHex()}")

                lifecycleScope.launch {
                    Log.d("FRITAG", "MISC DATA RESPONSE: ${it.toHex()}")
                    val networkStatusBits =
                        ModbusTypeConverter.byteArrayToBinaryString(it.copyOfRange(3, 5))
                            .reversed()
                            .substring(0, 11)
                    val arrayOfNetworkStatusBits = networkStatusBits.toCharArray()
                    val wifiNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(0, 3)
                    val gsmNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(3, 7)
                    val ethernetConnectedBits = arrayOfNetworkStatusBits.copyOfRange(7, 8)
                    val serverConnectedWithBits = arrayOfNetworkStatusBits.copyOfRange(8, 11)

                    //Insert into DB
                    appViewModel.insertMiscInfo(
                        TbMiscInfo(
                            1,
                            serverConnectedWith = StateAndModesUtils.checkServerConnectedWith(
                                serverConnectedWithBits
                            ), ethernetStatus = StateAndModesUtils.checkIfEthernetIsConnected(
                                ethernetConnectedBits
                            ), gsmLevel = StateAndModesUtils.checkGSMNetworkStrength(
                                gsmNetworkStrengthBits
                            ).toInt(), wifiLevel = StateAndModesUtils.checkWifiNetworkStrength(
                                wifiNetworkStrengthBits
                            ).toInt()
                        )
                    )

                }
            } else {
                Log.e(TAG, "readMiscInfo: Error Response - ${it.toHex()}")
            }
            lifecycleScope.launch {
                delay(mCommonDelay)
                readAcMeterInfo()
            }
        }
    }

    private suspend fun readAcMeterInfo() {
        Log.d(
            TAG,
            "readAcMeterInfo: Request Sent - ${
                ModbusRequestFrames.getACMeterInfoRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getACMeterInfoRequestFrame()
        ) {
            if (it.toHex().startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.d(TAG, "readAcMeterInfo: Response = ${it.toHex()}")
                val newResponse = ModBusUtils.parseInputRegistersResponse(it)
                val tbAcMeterInfo = TbAcMeterInfo(
                    1,
                    voltageV1N = newResponse[0],
                    voltageV2N = newResponse[1],
                    voltageV3N = newResponse[2],
                    averageVoltageLN = newResponse[3],
                    currentL1 = newResponse[4],
                    currentL2 = newResponse[5],
                    currentL3 = newResponse[6],
                    averageCurrent = newResponse[7],
                    frequency = newResponse[10],
                    activePower = newResponse[11],
                    totalPower = newResponse[9]
                )
                appViewModel.insertAcMeterInfo(tbAcMeterInfo)
                Log.i(TAG, "readAcMeterInfo: INSERT DONE")
            } else {
                Log.e(TAG, "readAcMeterInfo: Error Response - ${it.toHex()}")
            }
            lifecycleScope.launch {
                delay(mCommonDelay)
                readGun1Info()
            }
        }
    }

    private suspend fun readGun1Info() {
        Log.d(
            TAG,
            "readGun1Info: Request Sent - ${ModbusRequestFrames.getGun1InfoRequestFrame().toHex()}"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.GUN_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getGun1InfoRequestFrame()
        ) {
            if (it.toHex().startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.d(TAG, "readGun1Info: Response = ${it.toHex()}")
            } else {
                Log.e(TAG, "readGun1Info: Error Response - ${it.toHex()}")
            }
            lifecycleScope.launch {
                delay(mCommonDelay)
                readGun1LastChargingSummaryInfo()
            }
        }
    }

    private suspend fun readGun1LastChargingSummaryInfo() {
        Log.d(
            TAG,
            "readGun1LastChargingSummaryInfo: Request Sent - ${
                ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE,
            ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame()
        ) {
            if (it.toHex().startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.d(TAG, "readGun1LastChargingSummaryInfo: Response = ${it.toHex()}")
            } else {
                Log.e(TAG, "readGun1LastChargingSummaryInfo: Error Response - ${it.toHex()}")
            }
            lifecycleScope.launch {
                delay(mCommonDelay)
                startReading()
            }
        }
    }

    companion object {
        private const val TAG = "SerialPortBaseActivityN"
    }

}