package com.bacancy.ccs2androidhmi.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.UiModeManager
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivityNew
import com.bacancy.ccs2androidhmi.databinding.ActivityHmiDashboardBinding
import com.bacancy.ccs2androidhmi.db.entity.TbNotifications
import com.bacancy.ccs2androidhmi.models.ErrorCodes
import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.ACTIVE_DEACTIVE_CHARGER_ID
import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.SHOW_POPUP_ID
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants.getTopicAtoB
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants.getTopicBtoA
import com.bacancy.ccs2androidhmi.mqtt.models.ActiveDeactiveChargerMessageBody
import com.bacancy.ccs2androidhmi.mqtt.models.ShowPopupMessageBody
import com.bacancy.ccs2androidhmi.receiver.FoundDeviceReceiver
import com.bacancy.ccs2androidhmi.receiver.MyDeviceAdminReceiver
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_LOCAL_START_STOP
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_TEST_MODE
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.CommonUtils.CHARGER_ACTIVE_DEACTIVE_MESSAGE_RECD
import com.bacancy.ccs2androidhmi.util.CommonUtils.DEVICE_MAC_ADDRESS
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_CHARGER_ACTIVE
import com.bacancy.ccs2androidhmi.util.CommonUtils.fromJson
import com.bacancy.ccs2androidhmi.util.CommonUtils.getUniqueItems
import com.bacancy.ccs2androidhmi.util.CommonUtils.hasLocationPermissions
import com.bacancy.ccs2androidhmi.util.CommonUtils.hasPermission
import com.bacancy.ccs2androidhmi.util.CommonUtils.isAndroidVersionMoreThan11
import com.bacancy.ccs2androidhmi.util.CommonUtils.requestLocationPermissions
import com.bacancy.ccs2androidhmi.util.CommonUtils.toJsonString
import com.bacancy.ccs2androidhmi.util.DateTimeUtils
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.convertToUtc
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomDialog
import com.bacancy.ccs2androidhmi.util.DialogUtils.showPasswordPromptDialog
import com.bacancy.ccs2androidhmi.util.LogUtils
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.NO_STATE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.TOKEN_ID_NONE
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.bacancy.ccs2androidhmi.util.PrefHelper.Companion.IS_DARK_THEME
import com.bacancy.ccs2androidhmi.util.Resource
import com.bacancy.ccs2androidhmi.util.ToastUtils.showCustomToast
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.MQTTViewModel
import com.bacancy.ccs2androidhmi.views.fragment.AppNotificationsFragment
import com.bacancy.ccs2androidhmi.views.fragment.FirmwareVersionInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.GunsHomeScreenFragment
import com.bacancy.ccs2androidhmi.views.fragment.LocalStartStopFragment
import com.bacancy.ccs2androidhmi.views.fragment.NewFaultInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.TestModeHomeFragment
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class HMIDashboardActivity : SerialPortBaseActivityNew(), FragmentChangeListener {

    private lateinit var CHARACTERISTIC_UUID: String
    private lateinit var SERVICE_UUID: String
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var serverPopup: Dialog
    private lateinit var gunsHomeScreenFragment: GunsHomeScreenFragment
    private lateinit var binding: ActivityHmiDashboardBinding
    val handler = Handler(Looper.getMainLooper())
    private var sentErrorsList = mutableListOf<ErrorCodes>()
    private val mqttViewModel: MQTTViewModel by viewModels()
    private val TAG = "HMIDashboardActivity"

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        binding = ActivityHmiDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gunsHomeScreenFragment = GunsHomeScreenFragment()
        addNewFragment(gunsHomeScreenFragment)

        handleViewsVisibility()

        handleClicks()

        handleBackStackChanges()

        observeLatestMiscInfo()

        showHideBackIcon()

        showHideHomeIcon()

        startMQTTConnection()

        observeMqttOperations()

        observeAllErrorCodes()

        observeChargerActiveDeactiveStates()

        /*if (!prefHelper.getBoolean("IS_APP_PINNED", false)) {
            requestDeviceAdminPermissions()
        } else {
            startLockTask()
        }*/

        setupBluetooth()
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Bluetooth enabled")
            registerFoundDeviceReceiverAndStartDiscovery()
        } else {
            Log.d(TAG, "Bluetooth not enabled")
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val canEnableBluetooth = if (isAndroidVersionMoreThan11()) {
            perms[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            perms[Manifest.permission.BLUETOOTH] == true &&
                    perms[Manifest.permission.BLUETOOTH_ADMIN] == true
        }

        if (canEnableBluetooth && !isBluetoothEnabled) {
            Log.d(TAG, "Enabling Bluetooth")
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            Log.d(TAG, "Bluetooth already enabled")
            registerFoundDeviceReceiverAndStartDiscovery()
        }
    }

    private fun setupBluetooth() {
        val permissions = if (isAndroidVersionMoreThan11()) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        permissionLauncher.launch(permissions)
    }

    private fun registerFoundDeviceReceiverAndStartDiscovery() {
        if (isAndroidVersionMoreThan11()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    1
                )
            } else {
                Log.d(TAG, "setupBluetooth: Registering FoundDeviceReceiver")
                lifecycleScope.launch(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        this@HMIDashboardActivity.registerReceiver(
                            foundDeviceReceiver,
                            IntentFilter(BluetoothDevice.ACTION_FOUND), RECEIVER_EXPORTED
                        )
                    }
                    bluetoothAdapter?.startDiscovery()
                }
            }
        } else {
            if (hasLocationPermissions()) {
                Log.d(TAG, "setupBluetooth: Starting Bluetooth discovery")
                if (hasPermission(Manifest.permission.BLUETOOTH_ADMIN)
                ) {
                    Log.d(TAG, "setupBluetooth: Registering FoundDeviceReceiver")
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            this@HMIDashboardActivity.registerReceiver(
                                foundDeviceReceiver,
                                IntentFilter(BluetoothDevice.ACTION_FOUND), RECEIVER_EXPORTED
                            )
                        }
                        bluetoothAdapter?.startDiscovery()
                    }
                } else {
                    Log.d(TAG, "setupBluetooth: Requesting Bluetooth Admin permission")
                    requestPermissions(
                        arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                        1
                    )
                }
            } else {
                Log.d(TAG, "setupBluetooth: Requesting Bluetooth Scan permission")
                requestLocationPermissions()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        Log.d(TAG, "setupBluetooth foundDeviceReceiver: Called")
        if (isAndroidVersionMoreThan11()) {
            if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
            ) {
                Log.d(
                    TAG,
                    "setupBluetooth foundDeviceReceiver1: Device found: ${device.name}, ${device.address}"
                )
            } else {
                Log.d(TAG, "setupBluetooth foundDeviceReceiver1: Permission not granted")
                requestPermissions(
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    1
                )
            }
        } else {
            if (hasLocationPermissions()) {
                Log.d(
                    TAG,
                    "setupBluetooth foundDeviceReceiver2: Device found: ${device.name}, ${device.address}"
                )
                if (device.name?.startsWith("CCS2_") == true || device.name?.startsWith("BAC_") == true) {
                    Log.d(TAG, "setupBluetooth foundDeviceReceiver2: CCS2 DEVICE FOUND FINALLY")
                    connectToDevice(device.address)
                }
            } else {
                Log.d(TAG, "setupBluetooth foundDeviceReceiver2: Permission not granted")
                requestLocationPermissions()
            }
        }
    }

    private lateinit var gatt: BluetoothGatt

    @SuppressLint("MissingPermission")
    private fun connectToDevice(deviceMacAddress: String?) {
        val device: BluetoothDevice = bluetoothAdapter!!.getRemoteDevice(deviceMacAddress)
        SERVICE_UUID = "000000ff-0000-1000-8000-00805f9b34fb"
        CHARACTERISTIC_UUID = "0000ff01-0000-1000-8000-00805f9b34fb"
        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT) || hasLocationPermissions()) {
            gatt = device.connectGatt(this, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    super.onConnectionStateChange(gatt, status, newState)
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        Log.d("THU_TAG", "connectToDevice: Connected to device")
                        gatt.discoverServices()
                    } else {
                        Log.d("THU_TAG", "connectToDevice: Disconnected from device")
                    }
                }

                @SuppressLint("NewApi")
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    super.onServicesDiscovered(gatt, status)
                    Log.d("THU_TAG", "connectToDevice: Services discovered")
                    val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                    val characteristic =
                        service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))

                    characteristic?.value =
                        "{\"md\":1,\"data\":{\"ssid\":\"\",\"pwd\":\"\",\"security\":\"0\",\"app_mode\":\"0\",\"apn\":\"cmnet\",\"Ethernet_Mode\":0,\"static_ip\":\"\",\"gateway_ip\":\"\",\"subnet_mask\":\"\"}}".toByteArray()
                    gatt?.writeCharacteristic(characteristic!!)

                    gatt?.readCharacteristic(characteristic)
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray,
                    status: Int
                ) {
                    super.onCharacteristicRead(gatt, characteristic, value, status)
                    Log.d("THU_TAG", "connectToDevice: Characteristic read")
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val value = characteristic.value // Read value here
                        // Handle the received value
                        Log.d(
                            "THU_TAG",
                            "connectToDevice: Characteristic read value - ${String(value)}"
                        )
                    } else {
                        // Handle read failure
                        Log.d("THU_TAG", "connectToDevice: Characteristic read failed")
                    }
                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?,
                    status: Int
                ) {
                    super.onCharacteristicWrite(gatt, characteristic, status)
                    Log.d("THU_TAG", "connectToDevice: Characteristic write")
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Characteristic write successful
                        Log.d(
                            "THU_TAG", "connectToDevice: Characteristic write successful - ${
                                java.lang.String(
                                    characteristic?.value
                                )
                            }"
                        )
                    } else {
                        // Handle write failure
                        Log.d("THU_TAG", "connectToDevice: Characteristic write failed")
                    }
                }
            })
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )
        }
    }

    private fun observeDeviceInternetStates() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.i(TAG, "###Network onAvailable: Called")
                startMQTTConnection()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.i(TAG, "###Network onLost: Called")
            }
        }
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
            // Consume the long press event to prevent the OVERVIEW screen from showing up
            true
        } else {
            super.onKeyLongPress(keyCode, event)
        }
    }

    private fun observeChargerActiveDeactiveStates() {
        prefHelper.setBoolean(
            CHARGER_ACTIVE_DEACTIVE_MESSAGE_RECD,
            true
        )//Called initially to get active state of charger
        lifecycleScope.launch {
            mqttViewModel.isChargerActive.collect { isChargerActive ->
                if (isChargerActive) {
                    showUIForActiveCharger()
                } else {
                    showUIForDeactiveCharger()
                }
            }
        }
    }

    private fun showUIForDeactiveCharger() {
        binding.apply {
            lnrChargerInoperative.visible()
        }
    }

    private fun showUIForActiveCharger() {
        binding.apply {
            lnrChargerInoperative.gone()
        }
    }

    override fun onResume() {
        super.onResume()
        observeDeviceInternetStates()
        startClockTimer()
    }

    private fun isActiveAdmin(): Boolean {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE)
                as DevicePolicyManager
        return devicePolicyManager.isDeviceOwnerApp(packageName)
    }

    private fun requestDeviceAdminPermissions() {
        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                prefHelper.setBoolean("IS_APP_PINNED", true)
                startLockTask()
            }
        }

        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                MyDeviceAdminReceiver.getComponentName(this@HMIDashboardActivity)
            )
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Need to make this app as Device Admin to provide better experience."
            )
        }
        launcher.launch(intent)
    }

    private fun observeAllErrorCodes() {
        appViewModel.allErrorCodes.observe(this) { errorCodes ->
            errorCodes?.let { codes ->
                val savedMacAddress = prefHelper.getStringValue(DEVICE_MAC_ADDRESS, "")

                val abnormalErrorsList = codes.flatMap { tbErrorCodes ->
                    appViewModel.getAbnormalErrorCodesList(
                        tbErrorCodes.sourceErrorCodes,
                        tbErrorCodes.sourceId
                    )
                }.toMutableList()

                if (abnormalErrorsList.isEmpty()) {
                    sentErrorsList = mutableListOf()
                } else {
                    val uniqueErrorsList = getUniqueItems(abnormalErrorsList, sentErrorsList)
                    if (uniqueErrorsList.isNotEmpty()) {
                        mqttViewModel.sendErrorToServer(savedMacAddress, uniqueErrorsList)
                        sentErrorsList.addAll(uniqueErrorsList)
                    }
                }
            }
        }
    }

    private fun observeDeviceMacAddress() {
        lifecycleScope.launch {
            appViewModel.deviceMacAddress.collect { deviceMacAddress ->
                if (deviceMacAddress.isNotEmpty()) {
                    prefHelper.setStringValue(DEVICE_MAC_ADDRESS, deviceMacAddress)
                    if (isInternetConnected()) {
                        mqttViewModel.subscribeTopic(getTopicAtoB(deviceMacAddress))
                        mqttViewModel.subscribeTopic(getTopicBtoA(deviceMacAddress))
                    }
                }
            }
        }
    }

    private fun startMQTTConnection() {
        lifecycleScope.launch {
            if (isInternetConnected()) {
                mqttViewModel.connectToMQTT()
            }
        }
    }

    private fun observeMqttOperations() {
        lifecycleScope.launch {

            observeConnectState()
            observeDisconnectState()
            observeTopicSubscribeState()
            observeTopicUnsubscribeState()
            observePublishState()

            mqttViewModel.publishMessageRequest.collect {
                if (isInternetConnected()) {
                    mqttViewModel.publishMessageToTopic(it.first, it.second)
                }
            }
        }
    }

    private fun observeConnectState() {
        lifecycleScope.launch {
            mqttViewModel.mqttConnectState.collect {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        LogUtils.debugLog("MQTTWorker - Connect onSuccess")
                        observeDeviceMacAddress()
                    }

                    is Resource.Error -> {
                        LogUtils.errorLog(it.message)
                    }

                    is Resource.IncomingMessage -> {
                        LogUtils.debugLog("MQTTWorker - Connect Data Arrived from Topic=${it.topic} Message=${it.message}")
                        prefHelper.getStringValue(DEVICE_MAC_ADDRESS, "").let { savedMacAddress ->
                            when (it.topic) {
                                getTopicBtoA(savedMacAddress) -> {
                                    val jsonMessage = it.message.toString()
                                    when {
                                        jsonMessage.contains(ACTIVE_DEACTIVE_CHARGER_ID) -> {
                                            it.message.toString()
                                                .fromJson<ActiveDeactiveChargerMessageBody>()
                                                .let { messageBody ->
                                                    Log.d(
                                                        TAG,
                                                        "observeConnectState: IncomingMessage - ACTIVE_DEACTIVE_CHARGER_ID - $messageBody"
                                                    )
                                                    prefHelper.setBoolean(
                                                        IS_CHARGER_ACTIVE,
                                                        messageBody.message == "ACTIVE"
                                                    )
                                                    prefHelper.setBoolean(
                                                        CHARGER_ACTIVE_DEACTIVE_MESSAGE_RECD,
                                                        true
                                                    )
                                                }
                                        }

                                        jsonMessage.contains(SHOW_POPUP_ID) -> {
                                            it.message.toString()
                                                .fromJson<ShowPopupMessageBody>()
                                                .let { messageBody ->
                                                    Log.d(
                                                        TAG,
                                                        "observeConnectState: IncomingMessage - SHOW_POPUP_ID - $messageBody"
                                                    )
                                                    withContext(Dispatchers.IO) {
                                                        appViewModel.insertNotifications(
                                                            TbNotifications(
                                                                notificationMessage = messageBody.dialogMessage,
                                                                notificationReceiveTime =
                                                                DateTimeUtils.getCurrentDateTime()
                                                                    .convertToUtc().orEmpty()
                                                            )
                                                        )
                                                        sendPopupAcknowledgementToServer(messageBody)
                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        serverPopup =
                                                            showCustomDialog(
                                                                messageBody.dialogMessage,
                                                                messageBody.dialogType.lowercase()
                                                            ) {
                                                                popupHandler.removeCallbacks(
                                                                    dismissDialogRunnable
                                                                )
                                                            }
                                                        serverPopup.show()
                                                        if (messageBody.dialogDuration.isNotEmpty() && messageBody.dialogDuration.toInt() > 0) {
                                                            hideServerPopupAfterGivenSeconds(
                                                                messageBody.dialogDuration.toInt()
                                                            )
                                                        }
                                                    }
                                                }
                                        }

                                        else -> {}
                                    }
                                }

                                else -> {}
                            }
                        }
                    }

                    is Resource.DeliveryComplete -> {
                        LogUtils.debugLog("MQTTWorker - Connect Delivery Complete")
                    }
                }
            }
        }
    }

    private fun sendPopupAcknowledgementToServer(messageBody: ShowPopupMessageBody) {
        val ackMessage = messageBody.copy(dialogStatus = "RECEIVED")
        val deviceMacAddress = prefHelper.getStringValue(DEVICE_MAC_ADDRESS, "")
        if (deviceMacAddress.isNotEmpty()) {
            mqttViewModel.publishMessageToTopic(
                getTopicAtoB(deviceMacAddress),
                ackMessage.toJsonString()
            )
        }
    }

    private val popupHandler = Handler(Looper.getMainLooper())
    private val dismissDialogRunnable = Runnable { serverPopup.dismiss() }

    private fun hideServerPopupAfterGivenSeconds(seconds: Int) {
        popupHandler.postDelayed(dismissDialogRunnable, seconds * 1000L)
    }

    private fun observeDisconnectState() {
        lifecycleScope.launch {
            mqttViewModel.mqttDisconnectState.collect {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {}
                    is Resource.Error -> {
                        LogUtils.errorLog(it.message)
                    }

                    is Resource.DeliveryComplete -> {}
                    is Resource.IncomingMessage -> {}
                }
            }
        }
    }

    private fun observePublishState() {
        lifecycleScope.launch {
            mqttViewModel.publishMessageState.collect {
                when (it) {
                    is Resource.Loading -> {
                        Log.d(TAG, "observePublishState: Loading")
                    }

                    is Resource.Success -> {
                        Log.d(TAG, "observePublishState: Success")
                    }

                    is Resource.Error -> {
                        Log.d(TAG, "observePublishState: Error - $it.message")
                        LogUtils.errorLog(it.message)
                    }

                    is Resource.DeliveryComplete -> {
                        Log.d(TAG, "observePublishState: DeliveryComplete")
                    }

                    is Resource.IncomingMessage -> {
                        Log.d(TAG, "observePublishState: IncomingMessage")
                    }
                }
            }
        }
    }

    private fun observeTopicSubscribeState() {
        lifecycleScope.launch {
            mqttViewModel.topicSubscriptionState.collect {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        Log.e(TAG, "topicSubscribeSuccess:  ${it.data.toJsonString()}")
                    }

                    is Resource.Error -> {
                        Log.e(TAG, "topicSubscribeError:  ${it.message.toJsonString()}")
                    }

                    is Resource.DeliveryComplete -> {
                        Log.e(TAG, "topicSubscribeDeliveryComplete:  ${it.data.toJsonString()}")
                    }

                    is Resource.IncomingMessage -> {
                        Log.e(
                            TAG,
                            "topicSubscribeTopic:  ${it.topic?.toJsonString()}  topicSubscribeMqttMessage:${
                                it.message?.toJsonString()
                            }"
                        )
                    }
                }
            }
        }
    }

    private fun observeTopicUnsubscribeState() {
        lifecycleScope.launch {
            mqttViewModel.topicUnSubscriptionState.collect {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {}
                    is Resource.Error -> {
                        LogUtils.errorLog(it.message)
                    }

                    is Resource.DeliveryComplete -> {}
                    is Resource.IncomingMessage -> {}
                }
            }
        }
    }

    private fun handleViewsVisibility() {
        binding.apply {
            if (SHOW_LOCAL_START_STOP) {
                binding.incToolbar.ivLocalStartStop.visible()
            } else {
                binding.incToolbar.ivLocalStartStop.gone()
            }

            if (SHOW_TEST_MODE) {
                binding.incToolbar.ivTestMode.visible()
            } else {
                binding.incToolbar.ivTestMode.gone()
            }
        }
    }

    private fun toggleTheme() {
        if (isAndroidVersionMoreThan11()) {
            // Device API level is 31 or higher
            val newNightMode = if (prefHelper.getBoolean(
                    IS_DARK_THEME,
                    false
                )
            ) UiModeManager.MODE_NIGHT_NO else UiModeManager.MODE_NIGHT_YES
            val uiManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
            uiManager.setApplicationNightMode(newNightMode)
        } else {
            // Device API level is 30 or lower
            val newNightMode = if (prefHelper.getBoolean(
                    IS_DARK_THEME,
                    false
                )
            ) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(newNightMode)
        }
        prefHelper.setBoolean(IS_DARK_THEME, !prefHelper.getBoolean(IS_DARK_THEME, false))
    }

    private fun handleClicks() {

        binding.lnrChargerInoperative.setOnClickListener {}

        binding.incToolbar.ivSwitchDarkMode.setOnClickListener {
            toggleTheme()
        }

        binding.incToolbar.imgBack.setOnClickListener {
            goBack()
        }
        binding.incToolbar.imgHome.setOnClickListener {
            prefHelper.setBoolean("IS_IN_TEST_MODE", false)
            prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)
            addNewFragment(GunsHomeScreenFragment(), true)
        }
        binding.incToolbar.ivScreenInfo.setOnClickListener {
            addNewFragment(FirmwareVersionInfoFragment())
        }

        binding.incToolbar.ivLocalStartStop.setOnClickListener {
            showPasswordPromptDialog({
                addNewFragment(LocalStartStopFragment())
            }, {
                showCustomToast(getString(R.string.msg_invalid_password), false)
            })
        }

        binding.incToolbar.ivTestMode.setOnClickListener {
            addNewFragment(TestModeHomeFragment())
        }

        binding.incToolbar.ivFaultInfo.setOnClickListener {
            addNewFragment(NewFaultInfoFragment())
        }

        binding.incToolbar.ivNotifications.setOnClickListener {
            addNewFragment(AppNotificationsFragment())
        }

        binding.incToolbar.ivLogo.setOnClickListener {
            prefHelper.setBoolean("IS_APP_PINNED", false)
            stopLockTask()
        }
    }

    //For starting clock timer
    private fun startClockTimer() {
        handler.post(runnable)
    }

    fun showHideBackIcon(showBackIcon: Boolean = true) {
        if (showBackIcon) {
            binding.incToolbar.imgBack.visible()
        } else {
            binding.incToolbar.imgBack.invisible()
        }
    }

    fun showHideSettingOptions(showSettingOptions: Boolean = false) {
        if (showSettingOptions) {
            binding.incToolbar.lnrOptions.visible()
        } else {
            binding.incToolbar.lnrOptions.gone()
        }
    }

    fun showHideHomeIcon(showHomeIcon: Boolean = true) {
        if (showHomeIcon) {
            binding.incToolbar.imgHome.visible()
        } else {
            binding.incToolbar.imgHome.invisible()
        }
    }

    private fun observeLatestMiscInfo() {
        appViewModel.latestMiscInfo.observe(this) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updateServerStatus(latestMiscInfo.serverConnectedWith)
                updateEthernetStatus(latestMiscInfo.ethernetStatus)
                adjustGSMLevel(latestMiscInfo.gsmLevel)
                adjustWifiLevel(latestMiscInfo.wifiLevel)
                if (latestMiscInfo.rfidTagState.isNotEmpty() && latestMiscInfo.rfidTagState != TOKEN_ID_NONE && latestMiscInfo.rfidTagState != NO_STATE) {
                    if (latestMiscInfo.rfidTagState.endsWith("Invalid")) {
                        showCustomToast(latestMiscInfo.rfidTagState, false)
                    } else {
                        showCustomToast(latestMiscInfo.rfidTagState, true)
                    }
                }
            }
        }
    }

    fun goBack() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            updateTimerUI()
            handler.postDelayed(this, 1000)
        }
    }

    private fun updateTimerUI() {
        val currentTime = Calendar.getInstance().time
        val dateFormat =
            SimpleDateFormat(CommonUtils.CLOCK_DATE_AND_TIME_FORMAT, Locale.ENGLISH)
        val formattedDate = dateFormat.format(currentTime)
        binding.incToolbar.tvDateTime.text = formattedDate.uppercase()
    }

    private fun handleBackStackChanges() {
        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                Log.d(
                    "TAG",
                    "onBackPressed Count: ${supportFragmentManager.backStackEntryCount}"
                )
                if (supportFragmentManager.backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun addNewFragment(fragment: Fragment, shouldMoveToHomeScreen: Boolean = false) {
        val fragmentManager: FragmentManager = supportFragmentManager
        if (shouldMoveToHomeScreen) {
            //To remove all the fragments in-between calling and first fragment
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentContainer.id, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun adjustWifiLevel(wifiLevel: Int) {
        when (wifiLevel) {
            1 -> binding.incToolbar.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_1_new)
            2 -> binding.incToolbar.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_2_new)
            3 -> binding.incToolbar.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_3_new)
            else -> binding.incToolbar.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_0_new)
        }
    }

    private fun updateEthernetStatus(status: String) {
        when (status) {
            "Not Connected" -> binding.incToolbar.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_disconnected)
            "Connected" -> binding.incToolbar.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_connected)
            else -> binding.incToolbar.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_disconnected)
        }
    }

    private fun updateServerStatus(serverStatus: String) {
        when (serverStatus) {
            "Ethernet" -> binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_connected)
            "GSM" -> binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_connected)
            "Wifi" -> binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_connected)
            else -> binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_disconnected)
        }
    }

    private fun adjustGSMLevel(level: Int) {
        when (level) {
            1 -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_1_new)
            2 -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_2_new)
            3 -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_3_new)
            4 -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_4_new)
            else -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_0_new)
        }
    }

    override fun onPause() {
        super.onPause()
        mApplication!!.closeSerialPort()
        handler.removeCallbacks(runnable)
        unregisterNetworkCallback()
    }

    override fun onStop() {
        super.onStop()
        prefHelper.setBoolean("IS_GUN_VOLTAGE_CHANGED", false)
        prefHelper.setBoolean("IS_GUN_CURRENT_CHANGED", false)
        prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)
        prefHelper.setBoolean("IS_IN_TEST_MODE", false)
        prefHelper.setBoolean(CommonUtils.GUN_1_LOCAL_START, false)
        prefHelper.setBoolean(CommonUtils.GUN_2_LOCAL_START, false)

    }

    override fun replaceFragment(fragment: Fragment?, shouldMoveToHomeScreen: Boolean) {
        if (fragment != null) {
            addNewFragment(fragment, shouldMoveToHomeScreen)
        }
    }

}