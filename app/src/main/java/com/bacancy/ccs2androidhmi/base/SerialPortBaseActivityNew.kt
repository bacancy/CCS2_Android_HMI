package com.bacancy.ccs2androidhmi.base

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android_serialport_api.SerialPort
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.HMIApp
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants
import com.bacancy.ccs2androidhmi.mqtt.models.ChargerStatusConfirmationRequestBody
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.CommonUtils.AC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.AUTH_PIN_VALUE
import com.bacancy.ccs2androidhmi.util.CommonUtils.CHARGER_OUTPUTS
import com.bacancy.ccs2androidhmi.util.CommonUtils.CHARGER_RATINGS
import com.bacancy.ccs2androidhmi.util.CommonUtils.DEVICE_MAC_ADDRESS
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_DC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_LAST_CHARGING_SUMMARY_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_LOCAL_START
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_DC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_LAST_CHARGING_SUMMARY_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_LOCAL_START
import com.bacancy.ccs2androidhmi.util.CommonUtils.INSIDE_LOCAL_START_STOP_SCREEN
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_APP_RESTARTED
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_CHARGER_ACTIVE
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_DUAL_SOCKET_MODE_SELECTED
import com.bacancy.ccs2androidhmi.util.CommonUtils.UNIT_PRICE
import com.bacancy.ccs2androidhmi.util.CommonUtils.addColonsToMacAddress
import com.bacancy.ccs2androidhmi.util.CommonUtils.generateRandomNumber
import com.bacancy.ccs2androidhmi.util.CommonUtils.getCleanedMacAddress
import com.bacancy.ccs2androidhmi.util.CommonUtils.toJsonString
import com.bacancy.ccs2androidhmi.util.DateTimeUtils
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.convertToUtc
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomDialog
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_DENIED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_SUCCESS
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_TIMEOUT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMMUNICATION_ERROR
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMPLETE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.EMERGENCY_STOP
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.ISOLATION_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.MAINS_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLC_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLUGGED_IN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PRECHARGE_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.RECTIFIER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.RESERVED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.UNAVAILABLE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.UNPLUGGED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getGunChargingState
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.viewmodel.MQTTViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject


@AndroidEntryPoint
abstract class SerialPortBaseActivityNew : AppCompatActivity() {

    private lateinit var dialog: Dialog
    private var readStopCount: Int = 0
    private var isGun1PluggedIn: Boolean = false
    private var isGun2PluggedIn: Boolean = false
    protected var mApplication: HMIApp? = null
    private var mSerialPort: SerialPort? = null
    private var mOutputStream: OutputStream? = null
    private var mInputStream: InputStream? = null
    private val mCommonDelay = 300L

    val appViewModel: AppViewModel by viewModels()
    private val mqttViewModel: MQTTViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)
        dialog = showCustomDialog(getString(R.string.message_device_communication_error)) {
            resetReadStopCount()
        }
    }

    private fun setupSerialPort() {
        mApplication = application as HMIApp
        try {
            mApplication?.let {
                mSerialPort = it.getSerialPort()
            }
            mSerialPort?.let {
                mOutputStream = it.outputStream
                mInputStream = it.inputStream
            }
        } catch (e: Exception) {
            Log.d("TAG", "onCreate: Exception = ${e.toString()}")
        }
    }

    override fun onResume() {
        super.onResume()
        setupPortsAndStartReading()
    }

    fun setupPortsAndStartReading() {
        setupSerialPort()
        startReading()
    }

    private fun makeFullScreen() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onPause() {
        resetPorts()
        super.onPause()
    }

    fun resetPorts() {
        mApplication?.closeSerialPort()
        mSerialPort = null
        mOutputStream = null
        mInputStream = null
    }

    private fun startReading() {
        lifecycleScope.launch {
            delay(mCommonDelay)
            val isChargerActiveDeactiveMessageRecd =
                prefHelper.getBoolean(CommonUtils.CHARGER_ACTIVE_DEACTIVE_MESSAGE_RECD, false)
            if (isChargerActiveDeactiveMessageRecd) {
                prefHelper.setBoolean(CommonUtils.CHARGER_ACTIVE_DEACTIVE_MESSAGE_RECD, false)
                val isChargerActive = prefHelper.getBoolean(IS_CHARGER_ACTIVE, true)
                Log.i(
                    TAG,
                    "startReading: MAKING CHARGER - ${if (isChargerActive) "OPERATIVE" else "INOPERATIVE"}"
                )
                writeForChargerActiveDeactive()
            } else {
                //readChargerActiveDeactiveState()
                writeForDualSocketMode(if(prefHelper.getBoolean(IS_DUAL_SOCKET_MODE_SELECTED, false)) 1 else 0)
            }
        }
    }

    private fun writeForChargerActiveDeactive() {
        val isChargerActive = prefHelper.getBoolean(IS_CHARGER_ACTIVE, true)
        Log.i(TAG, "writeForChargerActiveDeactive Request Started - $isChargerActive")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                442,
                if (isChargerActive) 1 else 0, {
                    Log.d(TAG, "writeForChargerActiveDeactive: Response Got")
                    sendChargerStatusConfirmation(isChargerActive)
                    lifecycleScope.launch {
                        //readChargerActiveDeactiveState()
                        writeForDualSocketMode(if(prefHelper.getBoolean(IS_DUAL_SOCKET_MODE_SELECTED, false)) 1 else 0)
                    }
                }, {})
        }
    }

    private fun sendChargerStatusConfirmation(isChargerActive: Boolean) {
        mqttViewModel.setIsChargerActive(isChargerActive)
        val deviceMacAddress = prefHelper.getStringValue(DEVICE_MAC_ADDRESS, "")
        val statusMessage = if (isChargerActive) "activated" else "deactivated"
        val chargerStatusConfirmationRequestBody = ChargerStatusConfirmationRequestBody(
            deviceMacAddress = deviceMacAddress.addColonsToMacAddress(),
            message = "Charger $statusMessage successfully",
            statusDateTime = DateTimeUtils.getCurrentDateTime().convertToUtc().orEmpty()
        )
        val topic = ServerConstants.getTopicAtoB(deviceMacAddress)
        val jsonString = chargerStatusConfirmationRequestBody.toJsonString()
        mqttViewModel.publishMessageToTopic(topic, jsonString)
    }

    private suspend fun readChargerActiveDeactiveState() {
        Log.i(
            TAG,
            "readChargerActiveDeactiveState: Request Sent - ${
                ModbusRequestFrames.getChargerActiveDeactiveStateRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.SINGLE_REGISTER_RESPONSE_SIZE,
            ModbusRequestFrames.getChargerActiveDeactiveStateRequestFrame(),
            onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readChargerActiveDeactiveState: Response = ${it.toHex()}")
                    val state = ModbusTypeConverter.getIntValueFromBytes(
                        it,
                        3,
                        4
                    )
                    Log.d(
                        TAG, "readChargerActiveDeactiveState: Data = $state"
                    )
                    prefHelper.setBoolean(IS_CHARGER_ACTIVE, state == 1)
                    mqttViewModel.setIsChargerActive(state == 1)
                } else {
                    Log.e(TAG, "readChargerActiveDeactiveState: Error Response - ${it.toHex()}")
                }
                lifecycleScope.launch {
                    readMiscInfo()
                }
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readChargerActiveDeactiveState: OnReadStopped Called")
                lifecycleScope.launch {
                    readMiscInfo()
                }
            })
    }

    private suspend fun readMiscInfo() {
        Log.i(
            TAG,
            "readMiscInfo: Request Sent - ${
                ModbusRequestFrames.getMiscInfoRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.MISC_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getMiscInfoRequestFrame(),
            onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readMiscInfo: Response = ${it.toHex()}")
                    appViewModel.updateDeviceMacAddress(
                        MiscInfoUtils.getBluetoothMacAddress(
                            it
                        ).getCleanedMacAddress()
                    )
                    lifecycleScope.launch {
                        appViewModel.insertMiscInfoInDB(it)
                    }
                } else {
                    lifecycleScope.launch {
                        readChargerRatings()
                    }
                    Log.e(TAG, "readMiscInfo: Error Response - ${it.toHex()}")
                }
                lifecycleScope.launch {
                    readChargerRatings()
                }
            }, onReadStopped = {
                Log.e(TAG, "readMiscInfo: OnReadStopped Called")
                showReadStoppedUI()
                lifecycleScope.launch {
                    readChargerRatings()
                }
            })

    }

    private fun resetReadStopCount() {
        readStopCount = 0
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    private suspend fun readChargerRatings() {
        Log.i(
            TAG,
            "readChargerRatings: Request Sent - ${
                ModbusRequestFrames.getChargerRatingsRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.SINGLE_REGISTER_RESPONSE_SIZE,
            ModbusRequestFrames.getChargerRatingsRequestFrame(),
            onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readChargerRatings: Response = ${it.toHex()}")
                    val ratings = ModbusTypeConverter.getIntValueFromBytes(
                        it,
                        3,
                        4
                    )
                    Log.d(
                        TAG, "readChargerRatings: Data = $ratings"
                    )
                    prefHelper.setStringValue(CHARGER_RATINGS, ratings.toString())
                } else {
                    Log.e(TAG, "readChargerRatings: Error Response - ${it.toHex()}")
                }
                lifecycleScope.launch {
                    readChargerTotalOutputs()
                }
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readChargerRatings: OnReadStopped Called")
                lifecycleScope.launch {
                    readChargerTotalOutputs()
                }
            })
    }

    private suspend fun readChargerTotalOutputs() {
        Log.i(
            TAG,
            "readChargerTotalOutputs: Request Sent - ${
                ModbusRequestFrames.getChargerOutputsRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.SINGLE_REGISTER_RESPONSE_SIZE,
            ModbusRequestFrames.getChargerOutputsRequestFrame(),
            onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readChargerTotalOutputs: Response = ${it.toHex()}")
                    val outputs = ModbusTypeConverter.getIntValueFromBytes(
                        it,
                        3,
                        4
                    )
                    Log.d(
                        TAG, "readChargerTotalOutputs: Data = $outputs"
                    )
                    prefHelper.setStringValue(CHARGER_OUTPUTS, outputs.toString())
                    sendInitialChargerDetailsToServer()
                } else {
                    Log.e(TAG, "readChargerTotalOutputs: Error Response - ${it.toHex()}")
                }
                openAcMeterInfo()
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readChargerTotalOutputs: OnReadStopped Called")
                openAcMeterInfo()
            })
    }

    private fun sendInitialChargerDetailsToServer() {
        if (prefHelper.getBoolean(IS_APP_RESTARTED, false)) {
            val deviceMacAddress = prefHelper.getStringValue(DEVICE_MAC_ADDRESS, "")
            val chargerRatings = prefHelper.getStringValue(CHARGER_RATINGS, "")
            val chargerOutputs = prefHelper.getStringValue(CHARGER_OUTPUTS, "")
            val unitPrice = prefHelper.getStringValue(UNIT_PRICE, "")

            if (deviceMacAddress.isNotEmpty() && chargerRatings.isNotEmpty() && chargerOutputs.isNotEmpty()) {
                val initialChargerDetails = mqttViewModel.getInitialChargerDetails(
                    deviceMacAddress,
                    chargerRatings,
                    chargerOutputs,
                    unitPrice
                )
                if(isInternetConnected()){
                    mqttViewModel.publishMessageToTopic(
                        initialChargerDetails.first,
                        initialChargerDetails.second
                    )
                    prefHelper.setBoolean(IS_APP_RESTARTED, false)
                }
            }
        }
    }

    private fun showReadStoppedUI() {
        readStopCount++
        if (readStopCount == 5) {
            prefHelper.setStringValue(AUTH_PIN_VALUE, "")
            lifecycleScope.launch(Dispatchers.Main) {
                dialog.show()
            }
            resetReadStopCount()
        } else {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }

    private fun openAcMeterInfo() {
        lifecycleScope.launch {
            delay(mCommonDelay)
            if (prefHelper.getScreenVisible(
                    AC_METER_FRAG,
                    false
                )
            ) {
                readAcMeterInfo()
            } else {
                readGun1Info()
            }
        }
    }

    private suspend fun readAcMeterInfo() {
        Log.i(
            TAG,
            "readAcMeterInfo: Request Sent - ${
                ModbusRequestFrames.getACMeterInfoRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getACMeterInfoRequestFrame(), onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readAcMeterInfo: Response = ${it.toHex()}")
                    insertACMeterInfoInDB(it)
                } else {
                    Log.e(TAG, "readAcMeterInfo: Error Response - ${it.toHex()}")
                }
                lifecycleScope.launch {
                    delay(mCommonDelay)
                    readGun1Info()
                }
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readAcMeterInfo: OnReadStopped Called")
                lifecycleScope.launch {
                    delay(mCommonDelay)
                    readGun1Info()
                }
            })
    }

    private fun insertACMeterInfoInDB(it: ByteArray) {
        val newResponse = ModBusUtils.parseInputRegistersResponse(it)
        if (newResponse.isNotEmpty()) {
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
                activePower = newResponse[8],
                totalPower = newResponse[9]
            )
            appViewModel.insertAcMeterInfo(tbAcMeterInfo)
            Log.i("readAcMeterInfo", "readAcMeterInfo INSERT INTO DB")
        }
    }

    private suspend fun readGun1Info() {
        Log.i(
            TAG,
            "readGun1Info: Request Sent - ${ModbusRequestFrames.getGun1InfoRequestFrame().toHex()}"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.GUN_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getGun1InfoRequestFrame(), onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readGun1Info: Response = ${it.toHex()}")
                    appViewModel.insertGun1InfoInDB(it)
                    Log.d(
                        TAG,
                        "readGun1Info: Gun Current State: ${getGunChargingState(it).description}"
                    )
                    when (getGunChargingState(it).description) {
                        UNPLUGGED -> {
                            isGun1PluggedIn = false
                            openGun1LastChargingSummary()
                        }

                        PLUGGED_IN,
                        AUTHENTICATION_SUCCESS,
                        CHARGING -> {
                            isGun1PluggedIn = true
                            openGun1LastChargingSummary()
                        }

                        COMPLETE,
                        COMMUNICATION_ERROR,
                        AUTHENTICATION_TIMEOUT,
                        PLC_FAULT,
                        RECTIFIER_FAULT,
                        AUTHENTICATION_DENIED,
                        PRECHARGE_FAIL,
                        ISOLATION_FAIL,
                        TEMPERATURE_FAULT,
                        SPD_FAULT,
                        SMOKE_FAULT,
                        TAMPER_FAULT,
                        MAINS_FAIL,
                        UNAVAILABLE,
                        RESERVED,
                        EMERGENCY_STOP,
                        -> {
                            if (isGun1PluggedIn) {
                                isGun1PluggedIn = false
                                openGun1LastChargingSummary(true)
                            } else {
                                openGun1LastChargingSummary()
                            }
                        }

                        else -> {
                            isGun1PluggedIn = false
                            openGun1LastChargingSummary()
                        }
                    }

                } else {
                    Log.e(TAG, "readGun1Info: Error Response - ${it.toHex()}")
                    isGun1PluggedIn = false
                    openGun1LastChargingSummary()
                }

            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readGun1Info: OnReadStopped Called")
                isGun1PluggedIn = false
                openGun1LastChargingSummary()
            })
    }

    private fun openGun1LastChargingSummary(shouldSave: Boolean = false) {
        lifecycleScope.launch {
            delay(mCommonDelay)
            if (shouldSave || prefHelper.getScreenVisible(
                    GUN_1_LAST_CHARGING_SUMMARY_FRAG,
                    false
                )
            ) {
                readGun1LastChargingSummaryInfo(shouldSave)
            } else {
                openGun1DCMeterInfo()
            }
        }
    }

    private fun openGun2LastChargingSummary(shouldSave: Boolean = false) {
        lifecycleScope.launch {
            delay(mCommonDelay)
            if (shouldSave || prefHelper.getScreenVisible(
                    GUN_2_LAST_CHARGING_SUMMARY_FRAG,
                    false
                )
            ) {
                readGun2LastChargingSummaryInfo(shouldSave)
            } else {
                openGun2DCMeterInfo()
            }
        }
    }

    private suspend fun readGun1LastChargingSummaryInfo(shouldSaveLastChargingSummary: Boolean = false) {
        Log.i(
            "SAVER",
            "readGun1LastChargingSummaryInfo: Request Sent - ${
                ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE,
            ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame(), onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(
                        "SAVER",
                        "readGun1LastChargingSummaryInfo: Response = ${it.toHex()}"
                    )
                    if (shouldSaveLastChargingSummary) {
                        Log.w("SAVER", "INSERT LCS IN DB")
                        appViewModel.insertGun1LastChargingSummaryInDB(it)
                        appViewModel.insertGun1ChargingHistoryInDB(it)
                        if (mqttViewModel.isMqttConnected.value) {
                            mqttViewModel.sendPublishMessageRequest(
                                mqttViewModel.convertByteArrayToPublishRequest(
                                    prefHelper.getStringValue(
                                        DEVICE_MAC_ADDRESS, ""
                                    ),
                                    1,
                                    it
                                )
                            )
                        }
                    }
                } else {
                    Log.e(
                        TAG,
                        "readGun1LastChargingSummaryInfo: Error Response - ${it.toHex()}"
                    )
                }
                openGun1DCMeterInfo()
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readGun1LCS: OnReadStopped Called")
                openGun1DCMeterInfo()
            })
    }

    private fun openGun1DCMeterInfo() {
        lifecycleScope.launch {
            delay(mCommonDelay)
            if (prefHelper.getScreenVisible(
                    GUN_1_DC_METER_FRAG,
                    false
                )
            ) {
                readGun1DCMeterInfo()
            } else {
                readGun2Info()
            }
        }
    }

    private fun openGun2DCMeterInfo() {
        lifecycleScope.launch {
            delay(mCommonDelay)
            if (prefHelper.getScreenVisible(
                    GUN_2_DC_METER_FRAG,
                    false
                )
            ) {
                readGun2DCMeterInfo()
            } else {
                chooseLocalStartStopOrAuthenticateMethod()
            }
        }
    }

    private fun chooseLocalStartStopOrAuthenticateMethod() {
        Log.i(
            TAG, "chooseLocalStartStopOrAuthenticateMethod: INSIDE_LOCAL_START_STOP_SCREEN => ${
                prefHelper.getBoolean(
                    INSIDE_LOCAL_START_STOP_SCREEN,
                    false
                )
            }"
        )
        if (prefHelper.getBoolean(
                INSIDE_LOCAL_START_STOP_SCREEN,
                false
            )
        ) {
            writeForLocalStartStop(determineLocalStartStop())
        } else if (prefHelper.getSelectedGunNumber(SELECTED_GUN, 0) != 0) {
            val selectedGunNumber =
                prefHelper.getSelectedGunNumber(SELECTED_GUN, 0)
            authenticateGun(selectedGunNumber)
        } else if (prefHelper.getStringValue(AUTH_PIN_VALUE, "").isNotEmpty()) {
            writeForPinAuthorization(prefHelper.getStringValue(AUTH_PIN_VALUE, ""))
        } else {
            setupTestMode()
        }
    }

    private fun setupTestMode() {
        val isInTestMode = prefHelper.getBoolean("IS_IN_TEST_MODE", false)
        if (isInTestMode) {
            writeForTestModeOnOff(1)
        } else {
            writeForTestModeOnOff(0)
        }
    }

    private suspend fun readGun1DCMeterInfo() {
        Log.i(
            TAG,
            "readGun1DCMeterInfo: Request Sent - ${
                ModbusRequestFrames.getGunOneDCMeterInfoRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.GUN_DC_METER_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getGunOneDCMeterInfoRequestFrame(), onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readGun1DCMeterInfo: Response = ${it.toHex()}")
                    appViewModel.insertGun1DCMeterInfoInDB(it)

                } else {
                    Log.e(TAG, "readGun1DCMeterInfo: Error Response - ${it.toHex()}")
                }
                lifecycleScope.launch {
                    delay(mCommonDelay)
                    readGun2Info()
                }
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readGun1DCMeterInfo: OnReadStopped Called")
                lifecycleScope.launch {
                    delay(mCommonDelay)
                    readGun2Info()
                }
            })
    }

    private suspend fun readGun2Info() {
        Log.i(
            TAG,
            "readGun2Info: Request Sent - ${ModbusRequestFrames.getGun2InfoRequestFrame().toHex()}"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.GUN_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getGun2InfoRequestFrame(), onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readGun2Info: Response = ${it.toHex()}")

                    appViewModel.insertGun2InfoInDB(it)
                    Log.d(
                        TAG,
                        "readGun2Info: Gun Current State: ${getGunChargingState(it).description}"
                    )
                    when (getGunChargingState(it).description) {
                        UNPLUGGED -> {
                            isGun2PluggedIn = false
                            openGun2LastChargingSummary()
                        }

                        PLUGGED_IN,
                        AUTHENTICATION_SUCCESS,
                        CHARGING -> {
                            isGun2PluggedIn = true
                            openGun2LastChargingSummary()
                        }

                        COMPLETE,
                        COMMUNICATION_ERROR,
                        AUTHENTICATION_TIMEOUT,
                        PLC_FAULT,
                        RECTIFIER_FAULT,
                        AUTHENTICATION_DENIED,
                        PRECHARGE_FAIL,
                        ISOLATION_FAIL,
                        TEMPERATURE_FAULT,
                        SPD_FAULT,
                        SMOKE_FAULT,
                        TAMPER_FAULT,
                        MAINS_FAIL,
                        UNAVAILABLE,
                        RESERVED,
                        EMERGENCY_STOP,
                        -> {
                            if (isGun2PluggedIn) {
                                isGun2PluggedIn = false
                                openGun2LastChargingSummary(true)
                            } else {
                                openGun2LastChargingSummary()
                            }
                        }

                        else -> {
                            isGun2PluggedIn = false
                            openGun2LastChargingSummary()
                        }
                    }
                } else {
                    Log.e(TAG, "readGun2Info: Error Response - ${it.toHex()}")
                    isGun2PluggedIn = false
                    openGun2LastChargingSummary()
                }
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readGun2Info: OnReadStopped Called")
                isGun2PluggedIn = false
                openGun2LastChargingSummary()
            })
    }

    private suspend fun readGun2LastChargingSummaryInfo(shouldSaveLastChargingSummary: Boolean = false) {
        Log.i(
            TAG,
            "readGun2LastChargingSummaryInfo: Request Sent - ${
                ModbusRequestFrames.getGun2LastChargingSummaryRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE,
            ModbusRequestFrames.getGun2LastChargingSummaryRequestFrame(), onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readGun2LastChargingSummaryInfo: Response = ${it.toHex()}")
                    if (shouldSaveLastChargingSummary) {
                        appViewModel.insertGun2LastChargingSummaryInDB(it)
                        appViewModel.insertGun2ChargingHistoryInDB(it)
                        if (mqttViewModel.isMqttConnected.value) {
                            mqttViewModel.sendPublishMessageRequest(
                                mqttViewModel.convertByteArrayToPublishRequest(
                                    prefHelper.getStringValue(
                                        DEVICE_MAC_ADDRESS, ""
                                    ),
                                    2,
                                    it
                                )
                            )
                        }
                    }
                } else {
                    Log.e(
                        TAG,
                        "readGun2LastChargingSummaryInfo: Error Response - ${it.toHex()}"
                    )
                }
                openGun2DCMeterInfo()
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readGun2LCS: OnReadStopped Called")
                openGun2DCMeterInfo()
            })
    }

    private suspend fun readGun2DCMeterInfo() {
        Log.i(
            TAG,
            "readGun2DCMeterInfo: Request Sent - ${
                ModbusRequestFrames.getGunTwoDCMeterInfoRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.GUN_DC_METER_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getGunTwoDCMeterInfoRequestFrame(), onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    Log.d(TAG, "readGun2DCMeterInfo: Response = ${it.toHex()}")
                    appViewModel.insertGun2DCMeterInfoInDB(it)
                } else {
                    Log.e(TAG, "readGun2DCMeterInfo: Error Response - ${it.toHex()}")
                }
                lifecycleScope.launch {
                    delay(mCommonDelay)
                    Log.i(
                        TAG,
                        "readGun2DCMeterInfo: SELECTED GUN NUMBER = ${
                            prefHelper.getSelectedGunNumber(
                                SELECTED_GUN,
                                0
                            )
                        }"
                    )
                    chooseLocalStartStopOrAuthenticateMethod()
                }
            }, onReadStopped = {
                showReadStoppedUI()
                Log.e(TAG, "readGun2DCMeterInfo: OnReadStopped Called")
                chooseLocalStartStopOrAuthenticateMethod()
            })
    }

    private fun writeForPinAuthorization(enteredPin: String) {
        Log.i(TAG, "writeForPinAuthorization Request Started")
        lifecycleScope.launch(Dispatchers.IO) {
            delay(500)
            ReadWriteUtil.writeToMultipleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                75,
                enteredPin, {
                    Log.d(TAG, "writeForPinAuthorization: Response Got")
                    prefHelper.setStringValue(AUTH_PIN_VALUE, "")
                    lifecycleScope.launch {
                        startReading()
                    }
                }, {
                    prefHelper.setStringValue(AUTH_PIN_VALUE, "")
                })
        }
    }

    private fun authenticateGun(gunNumber: Int) {
        Log.i(TAG, "Gun $gunNumber authenticateGun Request Started")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                30,
                gunNumber, {
                    Log.d(TAG, "authenticateGun: Response Got")
                    lifecycleScope.launch {
                        if (prefHelper.getStringValue(AUTH_PIN_VALUE, "").isNotEmpty()) {
                            writeForPinAuthorization(prefHelper.getStringValue(AUTH_PIN_VALUE, ""))
                        } else {
                            startReading()
                        }
                    }
                }, {})
        }
    }

    private fun writeForDualSocketMode(mode: Int) {
        Log.i(TAG, "Gun $mode writeForDualSocketMode Request Started")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                86,
                mode, {
                    Log.d(TAG, "writeForDualSocketMode: Response Got")
                    lifecycleScope.launch {
                        readMiscInfo()
                    }
                }, {})
        }
    }

    private fun writeForLocalStartStop(gunsStartStopData: Int = 1) {
        //Guns Start/Stop cycle series = 10, 01, 11
        //Gun1 = 01 - 1
        //Gun2 = 10 - 2
        //GunBothStart = 11 - 3
        //GunBothStop = 00 - 0
        Log.i(TAG, "writeForLocalStartStop Request Started - $gunsStartStopData")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                48,
                gunsStartStopData, {
                    Log.d(TAG, "writeForLocalStartStop: Response Got")
                    lifecycleScope.launch {
                        startReading()
                    }
                }, {})
        }
    }

    private fun determineLocalStartStop(): Int {
        val gun1LocalStart = prefHelper.getBoolean(GUN_1_LOCAL_START, false)
        val gun2LocalStart = prefHelper.getBoolean(GUN_2_LOCAL_START, false)

        return when {
            gun1LocalStart && !gun2LocalStart -> {
                1
            }

            !gun1LocalStart && gun2LocalStart -> {
                2
            }

            gun1LocalStart && gun2LocalStart -> {
                3
            }

            else -> 0 // Default case or handle accordingly
        }
    }

    /**
     * This method will be called active/inactive test mode
     * */
    private fun writeForTestModeOnOff(isTestMode: Int = 0) {
        //1- Test Mode ON
        //0- Test Mode OFF
        Log.i(TAG, "writeForTestModeOnOff Request Started - $isTestMode")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                350,
                isTestMode, {
                    Log.d(TAG, "writeForTestModeOnOff: Response Got")
                    if (isTestMode != 1) {
                        lifecycleScope.launch {
                            startReading()
                        }
                    } else {
                        checkGunsTestModeValuesChanges()
                    }
                }, {})
        }
    }

    private fun checkGunsTestModeValuesChanges() {
        lifecycleScope.launch {
            //If any value written in test mode in guns then call here
            Log.d(
                TAG,
                "checkGunsTestModeValuesChanges: IS_OUTPUT_ON_OFF_VALUE_CHANGED = ${
                    prefHelper.getBoolean(
                        "IS_OUTPUT_ON_OFF_VALUE_CHANGED",
                        false
                    )
                }"
            )
            if (prefHelper.getBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)) {
                Log.d(
                    TAG,
                    "checkGunsTestModeValuesChanges: IS_OUTPUT_ON_OFF_VALUE_CHANGED IN IF"
                )
                if (prefHelper.getIntValue("SELECTED_GUN_IN_TEST_MODE", 1) == 1) {
                    writeForGunsRectifier(
                        356,
                        prefHelper.getIntValue("GUN1_OUTPUT_ON_OFF_VALUE", 0)
                    )
                } else {
                    writeForGunsRectifier(
                        359,
                        prefHelper.getIntValue("GUN2_OUTPUT_ON_OFF_VALUE", 0)
                    )
                }
            } else {
                startReading()
            }
        }
    }

    /**
     * This method will be called everytime after writing any value in guns rectifier
     * */
    private fun writeForUpdateTestMode() {
        //0 - By default
        //? -Random value
        Log.i(TAG, "writeForUpdateTestMode Request Started")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                351,
                generateRandomNumber(), {
                    Log.d(TAG, "writeForUpdateTestMode: Response Got")
                    lifecycleScope.launch {
                        if (prefHelper.getBoolean("IS_GUN_VOLTAGE_CHANGED", false)) {
                            if (prefHelper.getIntValue("SELECTED_GUN_IN_TEST_MODE", 1) == 1) {
                                writeForGunsRectifier(
                                    354,
                                    prefHelper.getIntValue("GUN1_VOLTAGE", 0)
                                )
                            } else {
                                writeForGunsRectifier(
                                    357,
                                    prefHelper.getIntValue("GUN2_VOLTAGE", 0)
                                )
                            }
                            prefHelper.setBoolean("IS_GUN_VOLTAGE_CHANGED", false)

                        } else if (prefHelper.getBoolean("IS_GUN_CURRENT_CHANGED", false)) {
                            if (prefHelper.getIntValue("SELECTED_GUN_IN_TEST_MODE", 1) == 1) {
                                writeForGunsRectifier(
                                    355,
                                    prefHelper.getIntValue("GUN1_CURRENT", 0)
                                )
                            } else {
                                writeForGunsRectifier(
                                    358,
                                    prefHelper.getIntValue("GUN2_CURRENT", 0)
                                )
                            }
                            prefHelper.setBoolean("IS_GUN_CURRENT_CHANGED", false)
                        } else {
                            startReading()
                        }
                    }
                }, {})
        }
    }

    /**
     * This method will be called for writing voltage, current, or output on/off in guns rectifier
     * */
    private fun writeForGunsRectifier(registerAddress: Int, registerValue: Int) {
        Log.i(TAG, "writeForGunsRectifier Request Started - $registerValue")
        lifecycleScope.launch(Dispatchers.IO) {
            delay(mCommonDelay)
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                registerAddress,
                registerValue, {
                    Log.d(TAG, "writeForGunsRectifier: Response Got")
                    lifecycleScope.launch {
                        delay(mCommonDelay)
                        writeForUpdateTestMode()
                    }
                }, {})
        }
    }

    companion object {
        private const val TAG = "SerialPortBaseActivityN"
    }

}