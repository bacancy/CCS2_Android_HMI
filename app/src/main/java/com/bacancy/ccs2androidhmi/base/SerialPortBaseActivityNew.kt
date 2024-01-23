package com.bacancy.ccs2androidhmi.base

import android.os.Bundle
import android.util.Log
import android_serialport_api.SerialPort
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.HMIApp
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.util.CommonUtils.AC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_DC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_LAST_CHARGING_SUMMARY_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_DC_METER_FRAG
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_LAST_CHARGING_SUMMARY_FRAG
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_DENIED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_TIMEOUT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMMUNICATION_ERROR
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMPLETE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.EMERGENCY_STOP
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.ISOLATION_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.MAINS_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLC_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLUGGED_IN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PRECHARGE_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.RECTIFIER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getChargingCurrent
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getChargingDuration
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getChargingEnergyConsumption
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getChargingSoc
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getChargingVoltage
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getDemandCurrent
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getDemandVoltage
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getGunChargingState
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getInitialSoc
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.getTotalCost
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getCommunicationErrorCodes
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getDevicePhysicalConnectionStatus
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getLEDModuleFirmwareVersion
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getMCUFirmwareVersion
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getOCPPFirmwareVersion
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getPLC1Fault
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getPLC1ModuleFirmwareVersion
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getPLC2Fault
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getPLC2ModuleFirmwareVersion
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getRFIDFirmwareVersion
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getRectifier1Code
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getRectifier2Code
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getRectifier3Code
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.getRectifier4Code
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject


@OptIn(DelicateCoroutinesApi::class)
@AndroidEntryPoint
abstract class SerialPortBaseActivityNew : FragmentActivity() {

    private var isGun1PluggedIn: Boolean = false
    private var isGun2PluggedIn: Boolean = false
    protected var mApplication: HMIApp? = null
    private var mSerialPort: SerialPort? = null
    private var mOutputStream: OutputStream? = null
    private var mInputStream: InputStream? = null
    private val mCommonDelay = 300L

    val appViewModel: AppViewModel by viewModels()

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
            Log.d("TAG", "onCreate: Exception = ${e.toString()}")
        }
    }

    override fun onResume() {
        super.onResume()
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

    override fun onDestroy() {
        Log.e(TAG, "onDestroy: CALLED")
        mApplication!!.closeSerialPort()
        mSerialPort = null
        super.onDestroy()
    }

    private fun startReading() {
        lifecycleScope.launch {
            delay(mCommonDelay)
            readMiscInfo()
        }
    }

    private suspend fun readMiscInfo() {
        withTimeout(1000) {
            try {
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
                            Log.d(TAG, "readMiscInfo: Response = ${it.toHex()}")

                            lifecycleScope.launch {
                                insertMiscInfoInDB(it)
                            }
                        } else {
                            Log.e(TAG, "readMiscInfo: Error Response - ${it.toHex()}")
                        }
                        openAcMeterInfo()
                    }, onReadStopped = {
                        startReading()
                    })
            } catch (te: TimeoutCancellationException) {
                Log.e(TAG, "readMiscInfo: Timeout Occurred", te.cause)
                startReading()
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

    private fun insertMiscInfoInDB(it: ByteArray) {
        val gunSelectionResponse = it.copyOfRange(63, 65).toHex()
        Log.i(TAG, "insertMiscInfoInDB: gunSelectionResponse = $gunSelectionResponse")
        val networkStatusBits =
            ModbusTypeConverter.byteArrayToBinaryString(it.copyOfRange(3, 5))
                .reversed()
                .substring(0, 11)
        val arrayOfNetworkStatusBits = networkStatusBits.toCharArray()
        val wifiNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(0, 3)
        val gsmNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(3, 7)
        val ethernetConnectedBits = arrayOfNetworkStatusBits.copyOfRange(7, 8)
        val serverConnectedWithBits =
            arrayOfNetworkStatusBits.copyOfRange(8, 11)

        //Insert into DB
        appViewModel.insertMiscInfo(
            TbMiscInfo(
                1,
                serverConnectedWith = StateAndModesUtils.checkServerConnectedWith(
                    serverConnectedWithBits
                ),
                ethernetStatus = StateAndModesUtils.checkIfEthernetIsConnected(
                    ethernetConnectedBits
                ),
                gsmLevel = StateAndModesUtils.checkGSMNetworkStrength(
                    gsmNetworkStrengthBits
                ).toInt(),
                wifiLevel = StateAndModesUtils.checkWifiNetworkStrength(
                    wifiNetworkStrengthBits
                ).toInt(),
                mcuFirmwareVersion = getMCUFirmwareVersion(it),
                ocppFirmwareVersion = getOCPPFirmwareVersion(it),
                rfidFirmwareVersion = getRFIDFirmwareVersion(it),
                ledFirmwareVersion = getLEDModuleFirmwareVersion(it),
                plc1FirmwareVersion = getPLC1ModuleFirmwareVersion(it),
                plc2FirmwareVersion = getPLC2ModuleFirmwareVersion(it),
                plc1Fault = getPLC1Fault(it),
                plc2Fault = getPLC2Fault(it),
                rectifier1Fault = getRectifier1Code(it),
                rectifier2Fault = getRectifier2Code(it),
                rectifier3Fault = getRectifier3Code(it),
                rectifier4Fault = getRectifier4Code(it),
                communicationError = getCommunicationErrorCodes(it),
                devicePhysicalConnectionStatus = getDevicePhysicalConnectionStatus(
                    it
                )
            )
        )
    }

    private suspend fun readAcMeterInfo() {
        Log.i(
            TAG,
            "readAcMeterInfo: Request Sent - ${
                ModbusRequestFrames.getACMeterInfoRequestFrame().toHex()
            }"
        )
        withTimeout(1000) {
            try {
                ReadWriteUtil.writeRequestAndReadResponse(
                    mOutputStream,
                    mInputStream,
                    ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE,
                    ModbusRequestFrames.getACMeterInfoRequestFrame(), onDataReceived = {
                        if (it.toHex()
                                .startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
                        ) {
                            Log.d(TAG, "readAcMeterInfo: Response = ${it.toHex()}")
                            insertACMeterInfoInDB(it)
                        } else {
                            Log.e(TAG, "readAcMeterInfo: Error Response - ${it.toHex()}")
                        }
                        lifecycleScope.launch {
                            delay(mCommonDelay)
                            readGun1Info()
                        }
                    },onReadStopped = {
                        startReading()
                    })
            } catch (te: TimeoutCancellationException) {
                Log.e(TAG, "readMiscInfo: Timeout Occurred", te.cause)
                startReading()
            }
        }


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
                activePower = newResponse[11],
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
        withTimeout(1000) {
            try {
                ReadWriteUtil.writeRequestAndReadResponse(
                    mOutputStream,
                    mInputStream,
                    ResponseSizes.GUN_INFORMATION_RESPONSE_SIZE,
                    ModbusRequestFrames.getGun1InfoRequestFrame(), onDataReceived = {
                        if (it.toHex()
                                .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                        ) {
                            Log.d(TAG, "readGun1Info: Response = ${it.toHex()}")
                            insertGun1InfoInDB(it)
                            Log.d(
                                TAG,
                                "readGun1Info: Gun Current State: ${getGunChargingState(it).description}"
                            )
                            when (getGunChargingState(it).description) {
                                PLUGGED_IN -> {
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
                        startReading()
                    })
            } catch (te: TimeoutCancellationException) {
                Log.e(TAG, "readMiscInfo: Timeout Occurred", te.cause)
                startReading()
            }
        }

    }

    private fun openGun1LastChargingSummary(shouldSave: Boolean = false) {
        lifecycleScope.launch {
            delay(mCommonDelay)
            if (shouldSave) {
                readGun1LastChargingSummaryInfo(shouldSave)
            } else {
                if (prefHelper.getScreenVisible(
                        GUN_1_LAST_CHARGING_SUMMARY_FRAG,
                        false
                    )
                ) {
                    readGun1LastChargingSummaryInfo()
                } else {
                    openGun1DCMeterInfo()
                }
            }

        }
    }

    private fun openGun2LastChargingSummary(shouldSave: Boolean = false) {
        lifecycleScope.launch {
            delay(mCommonDelay)
            if (shouldSave) {
                readGun2LastChargingSummaryInfo(shouldSave)
            } else {
                if (prefHelper.getScreenVisible(
                        GUN_2_LAST_CHARGING_SUMMARY_FRAG,
                        false
                    )
                ) {
                    readGun2LastChargingSummaryInfo()
                } else {
                    openGun2DCMeterInfo()
                }
            }
        }
    }

    private fun insertGun1InfoInDB(it: ByteArray) {
        appViewModel.insertGunsChargingInfo(
            TbGunsChargingInfo(
                gunId = 1,
                gunChargingState = getGunChargingState(it).description,
                initialSoc = getInitialSoc(it),
                chargingSoc = getChargingSoc(it),
                demandVoltage = getDemandVoltage(it),
                demandCurrent = getDemandCurrent(it),
                chargingVoltage = getChargingVoltage(it),
                chargingCurrent = getChargingCurrent(it),
                duration = getChargingDuration(it),
                energyConsumption = getChargingEnergyConsumption(it),
                totalCost = getTotalCost(it)
            )
        )
    }

    private suspend fun readGun1LastChargingSummaryInfo(shouldSaveLastChargingSummary: Boolean = false) {
        Log.i(
            "SAVER",
            "readGun1LastChargingSummaryInfo: Request Sent - ${
                ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame().toHex()
            }"
        )
        withTimeout(1000) {
            try {
                ReadWriteUtil.writeRequestAndReadResponse(
                    mOutputStream,
                    mInputStream,
                    ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE,
                    ModbusRequestFrames.getGun1LastChargingSummaryRequestFrame(), onDataReceived = {
                        if (it.toHex()
                                .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                        ) {
                            Log.d(
                                "SAVER",
                                "readGun1LastChargingSummaryInfo: Response = ${it.toHex()}"
                            )
                            if (shouldSaveLastChargingSummary) {
                                Log.w("SAVER", "INSERT LCS IN DB")
                                insertGun1LastChargingSummaryInDB(it)
                                insertGun1ChargingHistoryInDB(it)
                            }
                        } else {
                            Log.e(
                                TAG,
                                "readGun1LastChargingSummaryInfo: Error Response - ${it.toHex()}"
                            )
                        }
                        openGun1DCMeterInfo()
                    }, onReadStopped = {
                        startReading()
                    })
            } catch (te: TimeoutCancellationException) {
                Log.e(TAG, "readMiscInfo: Timeout Occurred", te.cause)
                startReading()
            }
        }

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
                val selectedGunNumber =
                    prefHelper.getSelectedGunNumber(SELECTED_GUN, 0)
                if (selectedGunNumber != 0) {
                    authenticateGun(selectedGunNumber)
                } else {
                    startReading()
                }
            }
        }
    }

    private fun insertGun1ChargingHistoryInDB(it: ByteArray) {
        val chargingSummary = TbChargingHistory(
            gunNumber = 1,
            evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
            chargingStartTime = LastChargingSummaryUtils.getChargingStartTime(it),
            chargingEndTime = LastChargingSummaryUtils.getChargingEndTime(it),
            totalChargingTime = LastChargingSummaryUtils.getTotalChargingTime(it),
            startSoc = LastChargingSummaryUtils.getStartSoc(it),
            endSoc = LastChargingSummaryUtils.getEndSoc(it),
            energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(it),
            sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(it),
            customSessionEndReason = "NA",
            totalCost = LastChargingSummaryUtils.getTotalCost(it)
        )
        appViewModel.insertChargingSummary(chargingSummary)
    }

    private fun insertGun1LastChargingSummaryInDB(it: ByteArray) {
        appViewModel.insertGunsLastChargingSummary(
            TbGunsLastChargingSummary(
                gunId = 1,
                evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
                chargingDuration = LastChargingSummaryUtils.getTotalChargingTime(
                    it
                ),
                chargingStartDateTime = LastChargingSummaryUtils.getChargingStartTime(
                    it
                ),
                chargingEndDateTime = LastChargingSummaryUtils.getChargingEndTime(
                    it
                ),
                startSoc = LastChargingSummaryUtils.getStartSoc(it),
                endSoc = LastChargingSummaryUtils.getEndSoc(it),
                energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(
                    it
                ),
                sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(
                    it
                )
            )
        )
    }

    private suspend fun readGun1DCMeterInfo() {
        Log.i(
            TAG,
            "readGun1DCMeterInfo: Request Sent - ${
                ModbusRequestFrames.getGunOneDCMeterInfoRequestFrame().toHex()
            }"
        )

        withTimeout(1000) {
            try {
                ReadWriteUtil.writeRequestAndReadResponse(
                    mOutputStream,
                    mInputStream,
                    ResponseSizes.GUN_DC_METER_INFORMATION_RESPONSE_SIZE,
                    ModbusRequestFrames.getGunOneDCMeterInfoRequestFrame(), onDataReceived = {
                        if (it.toHex()
                                .startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
                        ) {
                            Log.d(TAG, "readGun1DCMeterInfo: Response = ${it.toHex()}")
                            insertGun1DCMeterInfoInDB(it)

                        } else {
                            Log.e(TAG, "readGun1DCMeterInfo: Error Response - ${it.toHex()}")
                        }
                        lifecycleScope.launch {
                            delay(mCommonDelay)
                            readGun2Info()
                        }
                    }, onReadStopped = {
                        startReading()
                    })
            } catch (te: TimeoutCancellationException) {
                Log.e(TAG, "readMiscInfo: Timeout Occurred", te.cause)
                startReading()
            }
        }

    }

    private fun insertGun1DCMeterInfoInDB(it: ByteArray) {
        val newResponse = ModBusUtils.parseInputRegistersResponse(it)
        Log.i(TAG, "insertGun1DCMeterInfoInDB: DC LIST = ${newResponse.toList()}")
        if (newResponse.isNotEmpty()) {
            appViewModel.insertGunsDCMeterInfo(
                TbGunsDcMeterInfo(
                    gunId = 1,
                    voltage = newResponse[0],
                    current = newResponse[1],
                    power = newResponse[2],
                    importEnergy = newResponse[3],
                    exportEnergy = newResponse[4],
                    maxVoltage = newResponse[5],
                    minVoltage = newResponse[6],
                    maxCurrent = newResponse[7],
                    minCurrent = newResponse[8]
                )
            )
        }
    }

    private suspend fun readGun2Info() {
        Log.i(
            TAG,
            "readGun2Info: Request Sent - ${ModbusRequestFrames.getGun2InfoRequestFrame().toHex()}"
        )
        withTimeout(1000) {
            try {
                ReadWriteUtil.writeRequestAndReadResponse(
                    mOutputStream,
                    mInputStream,
                    ResponseSizes.GUN_INFORMATION_RESPONSE_SIZE,
                    ModbusRequestFrames.getGun2InfoRequestFrame(), onDataReceived = {
                        if (it.toHex()
                                .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                        ) {
                            Log.d(TAG, "readGun2Info: Response = ${it.toHex()}")

                            insertGun2InfoInDB(it)

                            when (getGunChargingState(it).description) {
                                PLUGGED_IN -> {
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
                        startReading()
                    })
            } catch (te: TimeoutCancellationException) {
                Log.e(TAG, "readMiscInfo: Timeout Occurred", te.cause)
                startReading()
            }
        }

    }

    private fun insertGun2InfoInDB(it: ByteArray) {
        appViewModel.insertGunsChargingInfo(
            TbGunsChargingInfo(
                gunId = 2,
                gunChargingState = getGunChargingState(it).description,
                initialSoc = getInitialSoc(it),
                chargingSoc = getChargingSoc(it),
                demandVoltage = getDemandVoltage(it),
                demandCurrent = getDemandCurrent(it),
                chargingVoltage = getChargingVoltage(it),
                chargingCurrent = getChargingCurrent(it),
                duration = getChargingDuration(it),
                energyConsumption = getChargingEnergyConsumption(it),
                totalCost = getTotalCost(it)
            )
        )
    }

    private suspend fun readGun2LastChargingSummaryInfo(shouldSaveLastChargingSummary: Boolean = false) {
        Log.i(
            TAG,
            "readGun2LastChargingSummaryInfo: Request Sent - ${
                ModbusRequestFrames.getGun2LastChargingSummaryRequestFrame().toHex()
            }"
        )
        withTimeout(1000) {
            try {
                ReadWriteUtil.writeRequestAndReadResponse(
                    mOutputStream,
                    mInputStream,
                    ResponseSizes.LAST_CHARGING_SUMMARY_RESPONSE_SIZE,
                    ModbusRequestFrames.getGun2LastChargingSummaryRequestFrame(), onDataReceived = {
                        if (it.toHex()
                                .startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)
                        ) {
                            Log.d(TAG, "readGun2LastChargingSummaryInfo: Response = ${it.toHex()}")
                            if (shouldSaveLastChargingSummary) {
                                insertGun2LastChargingSummaryInDB(it)
                                insertGun2ChargingHistoryInDB(it)
                            }
                        } else {
                            Log.e(
                                TAG,
                                "readGun2LastChargingSummaryInfo: Error Response - ${it.toHex()}"
                            )
                        }
                        openGun2DCMeterInfo()
                    }, onReadStopped = {
                        startReading()
                    })
            } catch (te: TimeoutCancellationException) {
                Log.e(TAG, "readMiscInfo: Timeout Occurred", te.cause)
                startReading()
            }
        }

    }


    private fun insertGun2ChargingHistoryInDB(it: ByteArray) {
        val chargingSummary = TbChargingHistory(
            gunNumber = 2,
            evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
            chargingStartTime = LastChargingSummaryUtils.getChargingStartTime(it),
            chargingEndTime = LastChargingSummaryUtils.getChargingEndTime(it),
            totalChargingTime = LastChargingSummaryUtils.getTotalChargingTime(it),
            startSoc = LastChargingSummaryUtils.getStartSoc(it),
            endSoc = LastChargingSummaryUtils.getEndSoc(it),
            energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(it),
            sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(it),
            customSessionEndReason = "NA",
            totalCost = LastChargingSummaryUtils.getTotalCost(it)
        )
        appViewModel.insertChargingSummary(chargingSummary)
    }

    private fun insertGun2LastChargingSummaryInDB(it: ByteArray) {
        appViewModel.insertGunsLastChargingSummary(
            TbGunsLastChargingSummary(
                gunId = 2,
                evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
                chargingDuration = LastChargingSummaryUtils.getTotalChargingTime(
                    it
                ),
                chargingStartDateTime = LastChargingSummaryUtils.getChargingStartTime(
                    it
                ),
                chargingEndDateTime = LastChargingSummaryUtils.getChargingEndTime(
                    it
                ),
                startSoc = LastChargingSummaryUtils.getStartSoc(it),
                endSoc = LastChargingSummaryUtils.getEndSoc(it),
                energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(
                    it
                ),
                sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(
                    it
                )
            )
        )
    }

    private suspend fun readGun2DCMeterInfo() {
        Log.i(
            TAG,
            "readGun2DCMeterInfo: Request Sent - ${
                ModbusRequestFrames.getGunTwoDCMeterInfoRequestFrame().toHex()
            }"
        )
        withTimeout(1000) {
            try {
                ReadWriteUtil.writeRequestAndReadResponse(
                    mOutputStream,
                    mInputStream,
                    ResponseSizes.GUN_DC_METER_INFORMATION_RESPONSE_SIZE,
                    ModbusRequestFrames.getGunTwoDCMeterInfoRequestFrame(),onDataReceived = {
                        if (it.toHex()
                                .startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)
                        ) {
                            Log.d(TAG, "readGun2DCMeterInfo: Response = ${it.toHex()}")
                            insertGun2DCMeterInfoInDB(it)
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
                            val selectedGunNumber =
                                prefHelper.getSelectedGunNumber(SELECTED_GUN, 0)
                            if (selectedGunNumber != 0) {
                                authenticateGun(selectedGunNumber)
                            } else {
                                startReading()
                            }
                        }
                    }, onReadStopped = {
                        startReading()
                    })
            } catch (te: TimeoutCancellationException) {
                Log.e(TAG, "readMiscInfo: Timeout Occurred", te.cause)
                startReading()
            }
        }

    }

    private fun insertGun2DCMeterInfoInDB(it: ByteArray) {
        val newResponse = ModBusUtils.parseInputRegistersResponse(it)
        if (newResponse.isNotEmpty()) {
            appViewModel.insertGunsDCMeterInfo(
                TbGunsDcMeterInfo(
                    gunId = 2,
                    voltage = newResponse[0],
                    current = newResponse[1],
                    power = newResponse[2],
                    importEnergy = newResponse[3],
                    exportEnergy = newResponse[4],
                    maxVoltage = newResponse[5],
                    minVoltage = newResponse[6],
                    maxCurrent = newResponse[7],
                    minCurrent = newResponse[8]
                )
            )
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
                        startReading()
                    }

                }, {})
        }
    }

    companion object {
        private const val TAG = "SerialPortBaseActivityN"
    }

}