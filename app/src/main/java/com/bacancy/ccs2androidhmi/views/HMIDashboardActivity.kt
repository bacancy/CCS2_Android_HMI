package com.bacancy.ccs2androidhmi.views

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.UiModeManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import androidx.core.content.ContextCompat
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
import com.bacancy.ccs2androidhmi.receiver.MyDeviceAdminReceiver
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_DUAL_SOCKET
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_LOCAL_START_STOP
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_TEST_MODE
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.CommonUtils.APP_SETTINGS_PIN
import com.bacancy.ccs2androidhmi.util.CommonUtils.CHARGER_ACTIVE_DEACTIVE_MESSAGE_RECD
import com.bacancy.ccs2androidhmi.util.CommonUtils.DEVICE_MAC_ADDRESS
import com.bacancy.ccs2androidhmi.util.CommonUtils.EVSE_APP_PACKAGE_NAME
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_APP_PINNED
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_APP_RESTARTED
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_CHARGER_ACTIVE
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_DUAL_SOCKET_MODE_SELECTED
import com.bacancy.ccs2androidhmi.util.CommonUtils.fromJson
import com.bacancy.ccs2androidhmi.util.CommonUtils.getUniqueItems
import com.bacancy.ccs2androidhmi.util.CommonUtils.toJsonString
import com.bacancy.ccs2androidhmi.util.DateTimeUtils
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.convertToUtc
import com.bacancy.ccs2androidhmi.util.DialogUtils.clearDialogFlags
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomDialog
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomDialogForAreYouSure
import com.bacancy.ccs2androidhmi.util.DialogUtils.showPasswordPromptDialog
import com.bacancy.ccs2androidhmi.util.LanguageConfig.getAppLanguage
import com.bacancy.ccs2androidhmi.util.LanguageConfig.setAppLanguage
import com.bacancy.ccs2androidhmi.util.LogUtils
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.NO_STATE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.TOKEN_ID_NONE
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.bacancy.ccs2androidhmi.util.PrefHelper.Companion.IS_DARK_THEME
import com.bacancy.ccs2androidhmi.util.Resource
import com.bacancy.ccs2androidhmi.util.ToastUtils.showCustomToast
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.showToast
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.MQTTViewModel
import com.bacancy.ccs2androidhmi.views.fragment.AppNotificationsFragment
import com.bacancy.ccs2androidhmi.views.fragment.AppSettingsFragment
import com.bacancy.ccs2androidhmi.views.fragment.DualSocketGunsMoreInformationFragment
import com.bacancy.ccs2androidhmi.views.fragment.FirmwareVersionInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.GunsHomeScreenFragment
import com.bacancy.ccs2androidhmi.views.fragment.LocalStartStopFragment
import com.bacancy.ccs2androidhmi.views.fragment.NewFaultInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.TestModeHomeFragment
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class HMIDashboardActivity : SerialPortBaseActivityNew(), FragmentChangeListener {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var serverPopup: Dialog
    private lateinit var gunsHomeScreenFragment: GunsHomeScreenFragment
    private lateinit var binding: ActivityHmiDashboardBinding
    val handler = Handler(Looper.getMainLooper())
    private var sentErrorsList = mutableListOf<ErrorCodes>()
    private val mqttViewModel: MQTTViewModel by viewModels()
    private val TAG = "HMIDashboardActivity"

    override fun onResumeFragments() {
        super.onResumeFragments()
        //TEST COMMIT
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment == null) {
            lifecycleScope.launch {
                gunsHomeScreenFragment = GunsHomeScreenFragment()
                addNewFragment(gunsHomeScreenFragment)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        setAppLanguage(getAppLanguage(prefHelper), prefHelper)
        binding = ActivityHmiDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefHelper.setBoolean(IS_APP_RESTARTED, true)

        handleClicks()

        handleBackStackChanges()

        observeLatestMiscInfo()

        showHideBackIcon()

        showHideHomeIcon()

        showHideDualSocketButton()

        startMQTTConnection()

        observeMqttOperations()

        observeAllErrorCodes()

        observeChargerActiveDeactiveStates()
    }

    private fun loadClientLogoFromDownloads() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        } else {
            loadImage()
        }
    }

    private fun loadImage(imageName: String = "ccs2_hmi_logo") {
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val potentialFilePaths = listOf(
            File(downloadsDirectory, "$imageName.jpg"),
            File(downloadsDirectory, "$imageName.png"),
            File(downloadsDirectory, "$imageName.webp")
            // Add other extensions as needed
        )

        // Check each potential file path for existence
        for (file in potentialFilePaths) {
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.incToolbar.ivLogo.setImageBitmap(bitmap)
                return
            }
        }

        binding.incToolbar.ivLogo.setImageResource(R.drawable.ic_statiq_logo)//replace with sample_logo
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadImage()
        } else {
            showToast("Enable permission to load client logo")
        }
    }

    override fun onResume() {
        super.onResume()
        loadClientLogoFromDownloads()//remove this when kiosk mode app is built
        observeDeviceInternetStates()
        startClockTimer()
        //manageKioskMode()
    }

    private fun manageKioskMode() {
        if (!prefHelper.getBoolean(IS_APP_PINNED, false)) {
            requestDeviceAdminPermissions()
        } else {
            startLockTask()
            loadClientLogoFromDownloads()
        }
    }

    private fun requestDeviceAdminPermissions() {
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
        deviceAdminLauncher.launch(intent)
    }

    private val deviceAdminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            prefHelper.setBoolean(IS_APP_PINNED, true)
            startLockTask()
            loadClientLogoFromDownloads()
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

    private fun observeAllErrorCodes() {
        appViewModel.allErrorCodes.observe(this) { errorCodes ->
            errorCodes?.let { codes ->
                val savedMacAddress = prefHelper.getStringValue(DEVICE_MAC_ADDRESS, "")

                val errorCodeDomainList = mutableListOf<ErrorCodes>()
                codes.forEach {
                    errorCodeDomainList.add(
                        ErrorCodes(
                            id = it.id,
                            errorCodeStatus = "",
                            errorCodeValue = it.sourceErrorValue,
                            errorCodeName = it.sourceErrorCodes,
                            errorCodeSource = getErrorSource(it.sourceId),
                            errorCodeDateTime = it.sourceErrorDateTime
                        )
                    )
                }
                val abnormalErrorsList =
                    errorCodeDomainList.filter { it.errorCodeValue == 1 }.toMutableList()
                val resolvedErrorsList =
                    errorCodeDomainList.filter { it.errorCodeValue == 0 }.toMutableList()

                if (abnormalErrorsList.size == resolvedErrorsList.size || abnormalErrorsList.isEmpty()) {
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

    private fun getErrorSource(sourceId: Int): String {
        return when (sourceId) {
            0 -> "Charger"
            1 -> "Gun 1"
            2 -> "Gun 2"
            else -> "Charger"
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
                        sendUnsentMessages()
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
                                                                messageBody.dialogType.lowercase(),
                                                                isCancelable = false
                                                            ) {
                                                                popupHandler.removeCallbacks(
                                                                    dismissDialogRunnable
                                                                )
                                                            }
                                                        serverPopup.show()
                                                        clearDialogFlags(serverPopup)
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

    private fun sendUnsentMessages() {
        lifecycleScope.launch {
            mqttViewModel.getUnsentMessages().forEach { unsentMessage ->
                mqttViewModel.publishMessageToTopic(
                    unsentMessage.topic,
                    unsentMessage.message,
                    true
                )
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

    fun toggleTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        lifecycleScope.launch {
            resetPorts()
            delay(1000)
            setupPortsAndStartReading()
        }
    }

    private fun handleClicks() {

        binding.tvDualSocket.setOnClickListener {
            if (binding.tvDualSocket.tag == "DISABLED") {
                val dialog = showCustomDialog(
                    getString(R.string.msg_to_inform_about_dual_socket),
                    "info"
                ) {}
                dialog.show()
                clearDialogFlags(dialog)
            } else {
                if (binding.tvDualSocket.text == getString(R.string.lbl_dual_socket)) {
                    showCustomDialogForAreYouSure(
                        getString(R.string.msg_to_confirm_to_switch_to_dual_socket),
                        isCancelable = false,
                        {
                            prefHelper.setBoolean(IS_DUAL_SOCKET_MODE_SELECTED, true)
                            addNewFragment(DualSocketGunsMoreInformationFragment())
                        },
                        {})
                } else if (binding.tvDualSocket.text == getString(R.string.single_socket)) {
                    showCustomDialogForAreYouSure(
                        getString(R.string.msg_to_confirm_to_switch_to_single_socket),
                        isCancelable = false,
                        {
                            goBack()
                        },
                        {})
                }
            }

        }

        binding.lnrChargerInoperative.setOnClickListener {}

        binding.incToolbar.ivSwitchDarkMode.setOnClickListener {
            toggleTheme()
        }

        binding.incToolbar.ivSettings.setOnClickListener {
            addNewFragment(AppSettingsFragment())
            /*showPasswordPromptDialog(getString(R.string.title_authorize_for_settings),isCancelable = true, {
                addNewFragment(AppSettingsFragment())
            }, {
                showCustomToast(getString(R.string.msg_invalid_password), false)
            }, password = APP_SETTINGS_PIN)*/
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
            showPasswordPromptDialog(
                getString(R.string.title_authorize_for_local_start_stop),
                isCancelable = true,
                {
                    addNewFragment(LocalStartStopFragment())
                },
                {
                    showCustomToast(getString(R.string.msg_invalid_password), false)
                })
        }

        binding.incToolbar.ivTestMode.setOnClickListener {
            showPasswordPromptDialog(
                getString(R.string.title_authorize_for_test_mode),
                isCancelable = true,
                {
                    addNewFragment(TestModeHomeFragment())
                },
                {
                    showCustomToast(getString(R.string.msg_invalid_password), false)
                })
        }

        binding.incToolbar.ivFaultInfo.setOnClickListener {
            addNewFragment(NewFaultInfoFragment())
        }

        binding.incToolbar.ivNotifications.setOnClickListener {
            addNewFragment(AppNotificationsFragment())
        }

        binding.incToolbar.ivLogo.setOnClickListener {
            prefHelper.setBoolean(IS_APP_PINNED, false)
            stopLockTask()
        }
    }

    fun openEVSEApp() {
        try {
            val launchIntent: Intent? =
                packageManager.getLaunchIntentForPackage(EVSE_APP_PACKAGE_NAME)
            if (launchIntent != null) {
                startActivity(launchIntent, null)
            } else {
                showToast(getString(R.string.msg_evseready_app_not_installed))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(getString(R.string.msg_evseready_app_not_installed))
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

    private fun showHideDualSocketButton() {
        if (SHOW_DUAL_SOCKET) {
            binding.tvDualSocket.visible()
        } else {
            binding.tvDualSocket.invisible()
        }
    }

    fun updateDualSocketText(updatedLabel: String = getString(R.string.lbl_dual_socket)) {
        binding.tvDualSocket.text = updatedLabel
    }

    fun manageDualSocketButtonUI(isBothGunsPluggedIn: Boolean) {
        if (isBothGunsPluggedIn) {
            binding.tvDualSocket.tag = "ENABLED"
            binding.tvDualSocket.setBackgroundResource(R.drawable.bg_rect_half_rounded)
        } else {
            binding.tvDualSocket.tag = "DISABLED"
            binding.tvDualSocket.setBackgroundResource(R.drawable.bg_rect_half_rounded_disabled)
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
        val locale =
            if (resources.configuration.locales.size() > 0) resources.configuration.locales[0] else Locale.ENGLISH
        val dateFormat = SimpleDateFormat(CommonUtils.CLOCK_DATE_AND_TIME_FORMAT, locale)
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

    fun addNewFragment(fragment: Fragment, shouldMoveToHomeScreen: Boolean = false) {
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