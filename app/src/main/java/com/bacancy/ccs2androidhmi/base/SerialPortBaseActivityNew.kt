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
import com.bacancy.ccs2androidhmi.util.CommonUtils.ACCESS_PARAMETERS
import com.bacancy.ccs2androidhmi.util.CommonUtils.AC_METER_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.AC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.AUTH_PIN_VALUE
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_AC_METER_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_CHARGER_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_CONFIG_OPTION_ENTERED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_DC_METER_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_FAULT_DETECTION_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_RECTIFIERS_UPDATED
import com.bacancy.ccs2androidhmi.util.CommonUtils.CHARGER_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.CHARGER_OUTPUTS
import com.bacancy.ccs2androidhmi.util.CommonUtils.CHARGER_RATINGS
import com.bacancy.ccs2androidhmi.util.CommonUtils.DC_METER_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.DEVICE_MAC_ADDRESS
import com.bacancy.ccs2androidhmi.util.CommonUtils.FAULT_DETECTION_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_START_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_DC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_LAST_CHARGING_SUMMARY_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_LOCAL_START
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_START_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_DC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_LAST_CHARGING_SUMMARY_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_LOCAL_START
import com.bacancy.ccs2androidhmi.util.CommonUtils.INSIDE_LOCAL_START_STOP_SCREEN
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_APP_RESTARTED
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_CHARGER_ACTIVE
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_DUAL_SOCKET_MODE_SELECTED
import com.bacancy.ccs2androidhmi.util.CommonUtils.RECTIFIERS_DATA
import com.bacancy.ccs2androidhmi.util.CommonUtils.STORE_DATA_INTO_FLASH
import com.bacancy.ccs2androidhmi.util.CommonUtils.SYSTEM_AVAILABLE
import com.bacancy.ccs2androidhmi.util.CommonUtils.SYSTEM_UNAVAILABLE
import com.bacancy.ccs2androidhmi.util.CommonUtils.UNIT_PRICE
import com.bacancy.ccs2androidhmi.util.CommonUtils.addColonsToMacAddress
import com.bacancy.ccs2androidhmi.util.CommonUtils.fromJson
import com.bacancy.ccs2androidhmi.util.CommonUtils.generateRandomNumber
import com.bacancy.ccs2androidhmi.util.CommonUtils.getCleanedMacAddress
import com.bacancy.ccs2androidhmi.util.CommonUtils.toJsonString
import com.bacancy.ccs2androidhmi.util.DateTimeUtils
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.DATE_TIME_FORMAT
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.DATE_TIME_FORMAT_FROM_CHARGER
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.convertDateFormatToDesiredFormat
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.convertToUtc
import com.bacancy.ccs2androidhmi.util.DialogUtils.clearDialogFlags
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomDialog
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomLoadingDialog
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_AUTHENTICATION_DENIED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_AUTHENTICATION_SUCCESS
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_AUTHENTICATION_TIMEOUT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_COMMUNICATION_ERROR
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_COMPLETE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_EMERGENCY_STOP
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_ISOLATION_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_MAINS_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_PLC_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_PRECHARGE_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_RECTIFIER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_RESERVED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_UNAVAILABLE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_UNPLUGGED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLUGGED_IN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getGunChargingState
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getRangedArray
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.hexStringToDecimal
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.AUTHENTICATE_GUN
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.CDM_AC_METER
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.CDM_CHARGER
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.CDM_DC_METER
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.CDM_FAULT_DETECTION
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.CDM_RECTIFIER
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.CHARGER_ACTIVE_DEACTIVE
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.ENABLE_DISABLE_DUAL_SOCKET
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN1_CURRENT
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN1_OUTPUT_ON_OFF
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN1_SESSION_MODE
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN1_SESSION_MODE_VALUE
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN1_VOLTAGE
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN2_CURRENT
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN2_OUTPUT_ON_OFF
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN2_SESSION_MODE
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN2_SESSION_MODE_VALUE
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.GUN2_VOLTAGE
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.KEY_ACCESS_PARAMETER
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.LOCAL_START_STOP
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.PIN_AUTHORIZATION
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.TEST_MODE_ON_OFF
import com.bacancy.ccs2androidhmi.util.RegisterAddresses.UPDATE_TEST_MODE
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.viewmodel.MQTTViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.fragment.CDMConfigurationFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject


@AndroidEntryPoint
abstract class SerialPortBaseActivityNew : AppCompatActivity() {

    private lateinit var statusCheckingDialog: Dialog
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
            Log.d("TAG", "onCreate: Exception = $e")
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

    private suspend fun readConfigAccessParamsState() {
        Log.i(
            "###CDMCONFIG",
            "readConfigAccessParamsState: Request Sent - ${
                ModbusRequestFrames.getConfigAccessParamsStateRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.SINGLE_REGISTER_RESPONSE_SIZE,
            ModbusRequestFrames.getConfigAccessParamsStateRequestFrame(),
            onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    //010302 (2222/1111/1234/4321/5678) 20fd
                    //1111 - system available for configuration
                    //2222 - system not available for configuration
                    //1234 - to start accessing config parameters
                    //4321 - Store data and access parameters
                    //5678 - CDM default configuration parameters
                    Log.d("###CDMCONFIG", "readConfigAccessParamsState: Response = ${it.toHex()}")
                    prefHelper.setBoolean(CDM_CONFIG_OPTION_ENTERED, false)
                    val chargingEndTimeArray = it.getRangedArray(3..4)
                    val mappedArray = chargingEndTimeArray.map { it2 -> it2.getIntValueFromByte() }
                    val accessData = ModbusTypeConverter.decimalArrayToHexArray(mappedArray)
                        .joinToString { it2 -> it2 }.replace(", ", "")
                    Log.d("###CDMCONFIG", "readConfigAccessParamsState: CDM Status = $accessData")
                    when (accessData) {
                        SYSTEM_UNAVAILABLE -> {
                            //system not available for configuration
                            lifecycleScope.launch(Dispatchers.Main) {
                                statusCheckingDialog.dismiss()
                                val dialog = showCustomDialog(getString(R.string.msg_system_busy), isCancelable = false){
                                    lifecycleScope.launch {
                                        readMiscInfo()
                                    }
                                }
                                dialog.show()
                                clearDialogFlags(dialog)
                            }
                        }

                        SYSTEM_AVAILABLE -> {
                            statusCheckingDialog.dismiss()
                            (this as HMIDashboardActivity).addNewFragment(CDMConfigurationFragment())
                            //system available for configuration
                            //write "1234" to start accessing config parameters
                            lifecycleScope.launch {
                                writeForConfigAccessParamsState(ACCESS_PARAMETERS)
                            }
                        }

                        else -> {
                            statusCheckingDialog.dismiss()
                            lifecycleScope.launch {
                                readMiscInfo()
                            }
                        }
                    }
                } else {
                    statusCheckingDialog.dismiss()
                    Log.e("TUE_TAG", "readConfigAccessParamsState: Error Response - ${it.toHex()}")
                }
            }, onReadStopped = {
                statusCheckingDialog.dismiss()
                showReadStoppedUI()
                Log.e("TUE_TAG", "readConfigAccessParamsState: OnReadStopped Called")
                lifecycleScope.launch {
                    startReading()
                }
            })
    }

    private suspend fun writeForConfigAccessParamsState(accessStartCode: String) {
        Log.i(
            "###CDMCONFIG",
            "writeForConfigAccessParamsState: Request Code - $accessStartCode"
        )
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                KEY_ACCESS_PARAMETER,
                accessStartCode.hexStringToDecimal(), {
                    Log.d("###CDMCONFIG", "writeForConfigAccessParamsState: Response Got")
                    lifecycleScope.launch {
                        getConfigurationParameters()
                    }
                }, {
                    lifecycleScope.launch {
                        startReading()
                    }
                })
        }
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

            if (prefHelper.getBoolean(CDM_CONFIG_OPTION_ENTERED, false)) {
                statusCheckingDialog = showCustomLoadingDialog(getString(R.string.msg_checking_cdm_status))
                statusCheckingDialog.show()
                clearDialogFlags(statusCheckingDialog)
                readConfigAccessParamsState()
            } else if (prefHelper.getBoolean(CDM_CHARGER_UPDATED, false)) {
                delay(mCommonDelay)
                if (prefHelper.getStringValue(CHARGER_DATA, "").isNotEmpty()) {
                    writeForCDMFields(1,
                        prefHelper.getStringValue(CHARGER_DATA, "").fromJson<List<Int>>()
                    )
                }
            } else if (prefHelper.getBoolean(CDM_RECTIFIERS_UPDATED, false)) {
                delay(mCommonDelay)
                if (prefHelper.getStringValue(RECTIFIERS_DATA, "").isNotEmpty()) {
                    writeForCDMFields(2,
                        prefHelper.getStringValue(RECTIFIERS_DATA, "").fromJson<List<Int>>()
                    )
                }
            } else if (prefHelper.getBoolean(CDM_AC_METER_UPDATED, false)) {
                delay(mCommonDelay)
                if (prefHelper.getStringValue(AC_METER_DATA, "").isNotEmpty()) {
                    writeForCDMFields(3,
                        prefHelper.getStringValue(AC_METER_DATA, "").fromJson<List<Int>>()
                    )
                }
            } else if (prefHelper.getBoolean(CDM_DC_METER_UPDATED, false)) {
                delay(mCommonDelay)
                if (prefHelper.getStringValue(DC_METER_DATA, "").isNotEmpty()) {
                    writeForCDMFields(4,
                        prefHelper.getStringValue(DC_METER_DATA, "").fromJson<List<Int>>()
                    )
                }
            } else if (prefHelper.getBoolean(CDM_FAULT_DETECTION_UPDATED, false)) {
                delay(mCommonDelay)
                if (prefHelper.getStringValue(FAULT_DETECTION_DATA, "").isNotEmpty()) {
                    writeForCDMFields(5,
                        prefHelper.getStringValue(FAULT_DETECTION_DATA, "").fromJson<List<Int>>()
                    )
                }
            } else {
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
                    writeForDualSocketMode(
                        if (prefHelper.getBoolean(
                                IS_DUAL_SOCKET_MODE_SELECTED,
                                false
                            )
                        ) 1 else 0
                    )
                }
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
                CHARGER_ACTIVE_DEACTIVE,
                if (isChargerActive) 1 else 0, {
                    Log.d(TAG, "writeForChargerActiveDeactive: Response Got")
                    sendChargerStatusConfirmation(isChargerActive)
                    lifecycleScope.launch {
                        //readChargerActiveDeactiveState()
                        writeForDualSocketMode(
                            if (prefHelper.getBoolean(
                                    IS_DUAL_SOCKET_MODE_SELECTED,
                                    false
                                )
                            ) 1 else 0
                        )
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

    private suspend fun getConfigurationParameters() {
        Log.i(
            "###CDMCONFIG",
            "getConfigurationParameters: Request Sent - ${
                ModbusRequestFrames.getConfigurationParametersRequestFrame().toHex()
            }"
        )
        ReadWriteUtil.writeRequestAndReadResponse(
            mOutputStream,
            mInputStream,
            ResponseSizes.CONFIGURATION_PARAMETERS_RESPONSE_SIZE,
            ModbusRequestFrames.getConfigurationParametersRequestFrame(),
            onDataReceived = {
                if (it.toHex()
                        .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                ) {
                    resetReadStopCount()
                    appViewModel.insertConfigurationParametersInDB(it)
                    lifecycleScope.launch {
                        startReading()
                    }
                    Log.d("###CDMCONFIG", "getConfigurationParameters: Response = ${it.toHex()}")
                } else {
                    lifecycleScope.launch {
                        startReading()
                    }
                    Log.e("###CDMCONFIG", "getConfigurationParameters: Error Response - ${it.toHex()}")
                }
            }, onReadStopped = {
                Log.e("###CDMCONFIG", "getConfigurationParameters: OnReadStopped Called")
                showReadStoppedUI()
                lifecycleScope.launch {
                    startReading()
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
        if (this::dialog.isInitialized && dialog.isShowing) {
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
                if (isInternetConnected()) {
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
                dialog = showCustomDialog(getString(R.string.message_device_communication_error)) {
                    resetReadStopCount()
                }
                dialog.show()
                clearDialogFlags(dialog)
            }
            resetReadStopCount()
        } else {
            if (this::dialog.isInitialized && dialog.isShowing) {
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
                    appViewModel.insertGun1InfoInDB(it, this)
                    Log.d(
                        TAG,
                        "readGun1Info: Gun Current State: ${getGunChargingState(it).descriptionToSave}"
                    )
                    when (getGunChargingState(it).descriptionToSave) {
                        LBL_UNPLUGGED -> {
                            isGun1PluggedIn = false
                            openGun1LastChargingSummary()
                        }

                        PLUGGED_IN,
                        LBL_AUTHENTICATION_SUCCESS -> {
                            prefHelper.setStringValue(GUN_1_CHARGING_START_TIME, "")
                            prefHelper.setStringValue(GUN_1_CHARGING_END_TIME, "")
                            isGun1PluggedIn = true
                            openGun1LastChargingSummary()
                        }

                        LBL_CHARGING -> {
                            if (prefHelper.getStringValue(GUN_1_CHARGING_START_TIME, "")
                                    .isEmpty()
                            ) {
                                prefHelper.setStringValue(
                                    GUN_1_CHARGING_START_TIME,
                                    DateTimeUtils.getCurrentDateTime()
                                        .convertDateFormatToDesiredFormat(
                                            DATE_TIME_FORMAT, DATE_TIME_FORMAT_FROM_CHARGER
                                        )
                                )
                            }
                            isGun1PluggedIn = true
                            openGun1LastChargingSummary()
                        }

                        LBL_COMPLETE,
                        LBL_COMMUNICATION_ERROR,
                        LBL_AUTHENTICATION_TIMEOUT,
                        LBL_PLC_FAULT,
                        LBL_RECTIFIER_FAULT,
                        LBL_AUTHENTICATION_DENIED,
                        LBL_PRECHARGE_FAIL,
                        LBL_ISOLATION_FAIL,
                        LBL_TEMPERATURE_FAULT,
                        LBL_SPD_FAULT,
                        LBL_SMOKE_FAULT,
                        LBL_TAMPER_FAULT,
                        LBL_MAINS_FAIL,
                        LBL_UNAVAILABLE,
                        LBL_RESERVED,
                        LBL_EMERGENCY_STOP,
                        -> {
                            if (isGun1PluggedIn) {
                                isGun1PluggedIn = false
                                if (prefHelper.getStringValue(GUN_1_CHARGING_END_TIME, "")
                                        .isEmpty()
                                ) {
                                    prefHelper.setStringValue(
                                        GUN_1_CHARGING_END_TIME,
                                        DateTimeUtils.getCurrentDateTime()
                                            .convertDateFormatToDesiredFormat(
                                                DATE_TIME_FORMAT, DATE_TIME_FORMAT_FROM_CHARGER
                                            )
                                    )
                                }
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
                        val topic = ServerConstants.getTopicAtoB(
                            prefHelper.getStringValue(
                                DEVICE_MAC_ADDRESS, ""
                            )
                        )
                        val history = mqttViewModel.convertByteArrayToPublishRequest(
                            prefHelper.getStringValue(
                                DEVICE_MAC_ADDRESS, ""
                            ),
                            1,
                            it
                        ).second
                        if (mqttViewModel.isMqttConnected.value && isInternetConnected()) {
                            mqttViewModel.publishMessageToTopic(
                                topic, history, isChargingHistory = true
                            )
                        } else {
                            mqttViewModel.storeUnsentMessages(topic, history)
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

                    appViewModel.insertGun2InfoInDB(it, this)
                    Log.d(
                        TAG,
                        "readGun2Info: Gun Current State: ${getGunChargingState(it).descriptionToSave}"
                    )
                    when (getGunChargingState(it).descriptionToSave) {
                        LBL_UNPLUGGED -> {
                            isGun2PluggedIn = false
                            openGun2LastChargingSummary()
                        }

                        PLUGGED_IN,
                        LBL_AUTHENTICATION_SUCCESS -> {
                            prefHelper.setStringValue(GUN_2_CHARGING_START_TIME, "")
                            prefHelper.setStringValue(GUN_2_CHARGING_END_TIME, "")
                            isGun2PluggedIn = true
                            openGun2LastChargingSummary()
                        }

                        LBL_CHARGING -> {
                            if (prefHelper.getStringValue(GUN_2_CHARGING_START_TIME, "")
                                    .isEmpty()
                            ) {
                                prefHelper.setStringValue(
                                    GUN_2_CHARGING_START_TIME,
                                    DateTimeUtils.getCurrentDateTime()
                                        .convertDateFormatToDesiredFormat(
                                            DATE_TIME_FORMAT, DATE_TIME_FORMAT_FROM_CHARGER
                                        )
                                )
                            }
                            isGun2PluggedIn = true
                            openGun2LastChargingSummary()
                        }

                        LBL_COMPLETE,
                        LBL_COMMUNICATION_ERROR,
                        LBL_AUTHENTICATION_TIMEOUT,
                        LBL_PLC_FAULT,
                        LBL_RECTIFIER_FAULT,
                        LBL_AUTHENTICATION_DENIED,
                        LBL_PRECHARGE_FAIL,
                        LBL_ISOLATION_FAIL,
                        LBL_TEMPERATURE_FAULT,
                        LBL_SPD_FAULT,
                        LBL_SMOKE_FAULT,
                        LBL_TAMPER_FAULT,
                        LBL_MAINS_FAIL,
                        LBL_UNAVAILABLE,
                        LBL_RESERVED,
                        LBL_EMERGENCY_STOP,
                        -> {
                            if (isGun2PluggedIn) {
                                if (prefHelper.getStringValue(GUN_2_CHARGING_END_TIME, "")
                                        .isEmpty()
                                ) {
                                    prefHelper.setStringValue(
                                        GUN_2_CHARGING_END_TIME,
                                        DateTimeUtils.getCurrentDateTime()
                                            .convertDateFormatToDesiredFormat(
                                                DATE_TIME_FORMAT, DATE_TIME_FORMAT_FROM_CHARGER
                                            )
                                    )
                                }
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
                        val topic = ServerConstants.getTopicAtoB(
                            prefHelper.getStringValue(
                                DEVICE_MAC_ADDRESS, ""
                            )
                        )
                        val history = mqttViewModel.convertByteArrayToPublishRequest(
                            prefHelper.getStringValue(
                                DEVICE_MAC_ADDRESS, ""
                            ),
                            2,
                            it
                        ).second
                        if (mqttViewModel.isMqttConnected.value && isInternetConnected()) {
                            mqttViewModel.publishMessageToTopic(
                                topic, history, isChargingHistory = true
                            )
                        } else {
                            mqttViewModel.storeUnsentMessages(topic, history)
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
                PIN_AUTHORIZATION,
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

    private fun writeForCDMFields(fieldCode: Int, values: List<Int>) {
        Log.i("###CDMCONFIG", "writeForCDMFields Request Started - $fieldCode")
        lifecycleScope.launch(Dispatchers.IO) {
            delay(500)
            ReadWriteUtil.writeToMultipleHoldingRegister(
                mOutputStream,
                mInputStream,
                getCDMFieldStartingAddress(fieldCode),
                values, {
                    Log.d("###CDMCONFIG", "writeForCDMFields: Response Got - $fieldCode")
                    resetCDMPrefs(fieldCode)
                    lifecycleScope.launch {
                        //write config access key as 4321 to save the updated value to mcu
                        writeForConfigAccessParamsState(STORE_DATA_INTO_FLASH)
                    }
                }, {
                    Log.d("###CDMCONFIG", "writeForCDMFields: Error Response")
                    lifecycleScope.launch {
                        startReading()
                    }
                })
        }
    }

    private fun getCDMFieldStartingAddress(fieldCode: Int): Int {
        return when (fieldCode) {
            1 -> CDM_CHARGER
            2 -> CDM_RECTIFIER
            3 -> CDM_AC_METER
            4 -> CDM_DC_METER
            5 -> CDM_FAULT_DETECTION
            else -> 0
        }
    }

    private fun resetCDMPrefs(fieldCode: Int) {
        when (fieldCode) {
            1 -> {
                prefHelper.setBoolean(CDM_CHARGER_UPDATED, false)
                prefHelper.setStringValue(CHARGER_DATA, "")
            }

            2 -> {
                prefHelper.setBoolean(CDM_RECTIFIERS_UPDATED, false)
                prefHelper.setStringValue(RECTIFIERS_DATA, "")
            }

            3 -> {
                prefHelper.setBoolean(CDM_AC_METER_UPDATED, false)
                prefHelper.setStringValue(AC_METER_DATA, "")
            }

            4 -> {
                prefHelper.setBoolean(CDM_DC_METER_UPDATED, false)
                prefHelper.setStringValue(DC_METER_DATA, "")
            }

            5 -> {
                prefHelper.setBoolean(CDM_FAULT_DETECTION_UPDATED, false)
                prefHelper.setStringValue(FAULT_DETECTION_DATA, "")
            }

            else -> {}
        }
    }

    private fun authenticateGun(gunNumber: Int) {
        Log.i(TAG, "Gun $gunNumber authenticateGun Request Started")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                AUTHENTICATE_GUN,
                gunNumber, {
                    Log.d(TAG, "authenticateGun: Response Got")
                    lifecycleScope.launch {
                        if (prefHelper.getBoolean(
                                CommonUtils.IS_GUN_1_SESSION_MODE_SELECTED,
                                false
                            )
                        ) {
                            writeForSelectedSessionMode(
                                prefHelper.getIntValue(
                                    CommonUtils.GUN_1_SELECTED_SESSION_MODE,
                                    0
                                ), true
                            )
                        } else if (prefHelper.getBoolean(
                                CommonUtils.IS_GUN_2_SESSION_MODE_SELECTED,
                                false
                            )
                        ) {
                            writeForSelectedSessionMode(
                                prefHelper.getIntValue(
                                    CommonUtils.GUN_2_SELECTED_SESSION_MODE,
                                    0
                                ), false
                            )
                        } else if (prefHelper.getStringValue(AUTH_PIN_VALUE, "").isNotEmpty()) {
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
                ENABLE_DISABLE_DUAL_SOCKET,
                mode, {
                    Log.d(TAG, "writeForDualSocketMode: Response Got")
                    lifecycleScope.launch {
                        readMiscInfo()
                    }
                }, {})
        }
    }

    private fun writeForSelectedSessionMode(selectedSessionMode: Int, isGun1: Boolean) {
        Log.i(TAG, "Gun $selectedSessionMode writeForSelectedSessionMode Request Started")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                if (isGun1) GUN1_SESSION_MODE else GUN2_SESSION_MODE,
                selectedSessionMode, {
                    Log.d(TAG, "writeForSelectedSessionMode: Response Got")
                    lifecycleScope.launch {
                        if (isGun1) {
                            writeForSelectedSessionModeValue(
                                prefHelper.getStringValue(
                                    CommonUtils.GUN_1_SELECTED_SESSION_MODE_VALUE,
                                    ""
                                ), true
                            )
                        } else {
                            writeForSelectedSessionModeValue(
                                prefHelper.getStringValue(
                                    CommonUtils.GUN_2_SELECTED_SESSION_MODE_VALUE,
                                    ""
                                ), false
                            )
                        }
                    }
                }, {})
        }
    }

    private fun writeForSelectedSessionModeValue(
        selectedSessionModeValue: String,
        isGun1: Boolean
    ) {
        val floatBytes: ByteArray = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
            .putFloat(selectedSessionModeValue.toFloat()).array()
        Log.i(TAG, "Gun $selectedSessionModeValue writeForSelectedSessionModeValue Request Started")
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                if (isGun1) GUN1_SESSION_MODE_VALUE else GUN2_SESSION_MODE_VALUE,
                selectedSessionModeValue.toInt(), {
                    Log.d(TAG, "writeForSelectedSessionModeValue: Response Got")
                    lifecycleScope.launch {
                        if (isGun1) {
                            prefHelper.setBoolean(CommonUtils.IS_GUN_1_SESSION_MODE_SELECTED, false)
                            prefHelper.setIntValue(CommonUtils.GUN_1_SELECTED_SESSION_MODE, 0)
                            prefHelper.setStringValue(
                                CommonUtils.GUN_1_SELECTED_SESSION_MODE_VALUE,
                                ""
                            )
                        } else {
                            prefHelper.setBoolean(CommonUtils.IS_GUN_2_SESSION_MODE_SELECTED, false)
                            prefHelper.setIntValue(CommonUtils.GUN_2_SELECTED_SESSION_MODE, 0)
                            prefHelper.setStringValue(
                                CommonUtils.GUN_2_SELECTED_SESSION_MODE_VALUE,
                                ""
                            )
                        }
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
                LOCAL_START_STOP,
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
                TEST_MODE_ON_OFF,
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
                        GUN1_OUTPUT_ON_OFF,
                        prefHelper.getIntValue("GUN1_OUTPUT_ON_OFF_VALUE", 0)
                    )
                } else {
                    writeForGunsRectifier(
                        GUN2_OUTPUT_ON_OFF,
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
                UPDATE_TEST_MODE,
                generateRandomNumber(), {
                    Log.d(TAG, "writeForUpdateTestMode: Response Got")
                    lifecycleScope.launch {
                        if (prefHelper.getBoolean("IS_GUN_VOLTAGE_CHANGED", false)) {
                            if (prefHelper.getIntValue("SELECTED_GUN_IN_TEST_MODE", 1) == 1) {
                                writeForGunsRectifier(
                                    GUN1_VOLTAGE,
                                    prefHelper.getIntValue("GUN1_VOLTAGE", 0)
                                )
                            } else {
                                writeForGunsRectifier(
                                    GUN2_VOLTAGE,
                                    prefHelper.getIntValue("GUN2_VOLTAGE", 0)
                                )
                            }
                            prefHelper.setBoolean("IS_GUN_VOLTAGE_CHANGED", false)

                        } else if (prefHelper.getBoolean("IS_GUN_CURRENT_CHANGED", false)) {
                            if (prefHelper.getIntValue("SELECTED_GUN_IN_TEST_MODE", 1) == 1) {
                                writeForGunsRectifier(
                                    GUN1_CURRENT,
                                    prefHelper.getIntValue("GUN1_CURRENT", 0)
                                )
                            } else {
                                writeForGunsRectifier(
                                    GUN2_CURRENT,
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