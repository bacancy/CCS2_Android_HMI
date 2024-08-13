package com.bacancy.ccs2androidhmi.views.activity.seimens

import android.app.Dialog
import android.app.UiModeManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivityNew
import com.bacancy.ccs2androidhmi.databinding.ActivityHmiDashboardBinding
import com.bacancy.ccs2androidhmi.databinding.ActivitySeimensHmiDashboardBinding
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbNotifications
import com.bacancy.ccs2androidhmi.models.ErrorCodes
import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants
import com.bacancy.ccs2androidhmi.mqtt.models.ActiveDeactiveChargerMessageBody
import com.bacancy.ccs2androidhmi.mqtt.models.ShowPopupMessageBody
import com.bacancy.ccs2androidhmi.util.AppConfig
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_LOCAL_START_STOP
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_TEST_MODE
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.CommonUtils.fromJson
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
import com.bacancy.ccs2androidhmi.views.fragment.FirmwareVersionInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.GunsHomeScreenFragment
import com.bacancy.ccs2androidhmi.views.fragment.LocalStartStopFragment
import com.bacancy.ccs2androidhmi.views.fragment.NewFaultInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.TestModeHomeFragment
import com.bacancy.ccs2androidhmi.views.fragment.seimens.SeimensGunsHomeScreenFragment
import com.bacancy.ccs2androidhmi.views.listener.DashboardActivityContract
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class SeimensHMIDashboardActivity : SerialPortBaseActivityNew(), FragmentChangeListener,
    DashboardActivityContract {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var gunsHomeScreenFragment: SeimensGunsHomeScreenFragment
    private lateinit var binding: ActivitySeimensHmiDashboardBinding
    val handler = Handler(Looper.getMainLooper())
    private val mqttViewModel: MQTTViewModel by viewModels()
    private var sentErrorsList = mutableListOf<ErrorCodes>()
    private val TAG = "SeimensHMIDashboardActivity"
    private lateinit var serverPopup: Dialog

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        binding = ActivitySeimensHmiDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefHelper.setBoolean(CommonUtils.IS_APP_RESTARTED, true)
        gunsHomeScreenFragment = SeimensGunsHomeScreenFragment()
        addNewFragment(gunsHomeScreenFragment)

        handleViewsVisibility()

        handleClicks()

        handleBackStackChanges()

        startClockTimer()

        observeLatestMiscInfo()

        insertSampleChargingHistory()

        startMQTTConnection()

        observeMqttOperations()

        observeAllErrorCodes()

        observeChargerActiveDeactiveStates()
    }

    private fun observeChargerActiveDeactiveStates() {
        prefHelper.setBoolean(
            CommonUtils.CHARGER_ACTIVE_DEACTIVE_MESSAGE_RECD,
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
    }

    private fun observeDeviceInternetStates(){
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
        connectivityManager.registerNetworkCallback(networkRequest,networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun observeAllErrorCodes() {
        appViewModel.allErrorCodes.observe(this) { errorCodes ->
            errorCodes?.let { codes ->
                val savedMacAddress = prefHelper.getStringValue(CommonUtils.DEVICE_MAC_ADDRESS, "")

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
                val abnormalErrorsList = errorCodeDomainList.filter { it.errorCodeValue == 1 }.toMutableList()
                val resolvedErrorsList = errorCodeDomainList.filter { it.errorCodeValue == 0 }.toMutableList()

                if (abnormalErrorsList.size == resolvedErrorsList.size || abnormalErrorsList.isEmpty()) {
                    sentErrorsList = mutableListOf()
                } else {
                    val uniqueErrorsList =
                        CommonUtils.getUniqueItems(abnormalErrorsList, sentErrorsList)
                    if (uniqueErrorsList.isNotEmpty()) {
                        mqttViewModel.sendErrorToServer(savedMacAddress, uniqueErrorsList)
                        sentErrorsList.addAll(uniqueErrorsList)
                    }
                }
            }
        }
    }

    private fun getErrorSource(sourceId: Int): String {
        return when(sourceId){
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
                    prefHelper.setStringValue(CommonUtils.DEVICE_MAC_ADDRESS, deviceMacAddress)
                    if (isInternetConnected()) {
                        mqttViewModel.subscribeTopic(ServerConstants.getTopicAtoB(deviceMacAddress))
                        mqttViewModel.subscribeTopic(ServerConstants.getTopicBtoA(deviceMacAddress))
                    }
                }
            }
        }
    }

    private fun startMQTTConnection() {
        lifecycleScope.launch {
            if (isInternetConnected()) {
                Log.d(TAG, "startMQTTConnection: Internet Connected")
                mqttViewModel.connectToMQTT()
            }else{
                Log.d(TAG, "startMQTTConnection: Internet Not Connected")
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
                        prefHelper.getStringValue(CommonUtils.DEVICE_MAC_ADDRESS, "").let { savedMacAddress ->
                            when (it.topic) {
                                ServerConstants.getTopicBtoA(savedMacAddress) -> {
                                    val jsonMessage = it.message.toString()
                                    when {
                                        jsonMessage.contains(MQTTUtils.ACTIVE_DEACTIVE_CHARGER_ID) -> {
                                            it.message.toString()
                                                .fromJson<ActiveDeactiveChargerMessageBody>()
                                                .let { messageBody ->
                                                    Log.d(
                                                        TAG,
                                                        "observeConnectState: IncomingMessage - ACTIVE_DEACTIVE_CHARGER_ID - $messageBody"
                                                    )
                                                    prefHelper.setBoolean(
                                                        CommonUtils.IS_CHARGER_ACTIVE,
                                                        messageBody.message == "ACTIVE"
                                                    )
                                                    prefHelper.setBoolean(
                                                        CommonUtils.CHARGER_ACTIVE_DEACTIVE_MESSAGE_RECD,
                                                        true
                                                    )
                                                }
                                        }

                                        jsonMessage.contains(MQTTUtils.SHOW_POPUP_ID) -> {
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
                                                    withContext(Dispatchers.Main){
                                                        serverPopup =
                                                            showCustomDialog(messageBody.dialogMessage, messageBody.dialogType.lowercase()) {
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
        val deviceMacAddress = prefHelper.getStringValue(CommonUtils.DEVICE_MAC_ADDRESS, "")
        if (deviceMacAddress.isNotEmpty()) {
            mqttViewModel.publishMessageToTopic(ServerConstants.getTopicAtoB(deviceMacAddress), ackMessage.toJsonString())
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

    private fun insertSampleChargingHistory() {
        for (i in 1..20) {
            val chargingSummary = TbChargingHistory(
                summaryId = i,
                gunNumber = if (i % 2 == 0) 1 else 2,
                evMacAddress = "00-00-00-02-88-AF-56-39",
                chargingStartTime = "01/03/2024 17:59:10",
                chargingEndTime = "01/03/2024 18:59:10",
                totalChargingTime = "60",
                startSoc = "50",
                endSoc = "85",
                energyConsumption = "15.60",
                sessionEndReason = "Emergency",
                customSessionEndReason = "",
                totalCost = ""
            )
            lifecycleScope.launch(Dispatchers.IO) {
                appViewModel.insertChargingSummary(chargingSummary)
                delay(500)
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

    private fun handleClicks() {

        binding.incToolbar.ivSwitchDarkMode.setOnClickListener {
            toggleTheme()
        }

        binding.incToolbar.imgBack.setOnClickListener {
            goBack()
        }
        binding.incToolbar.imgHome.setOnClickListener {
            prefHelper.setBoolean("IS_IN_TEST_MODE", false)
            prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)
            if (AppConfig.IS_SEIMENS_CLIENT) {
                addNewFragment(SeimensGunsHomeScreenFragment(), true)
            } else {
                addNewFragment(GunsHomeScreenFragment(), true)
            }
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
    }

    //For starting clock timer
    private fun startClockTimer() {
        handler.post(runnable)
    }

    private fun showHideBackIcon(show: Boolean) {
        if (show) {
            binding.incToolbar.imgBack.visible()
        } else {
            binding.incToolbar.imgBack.invisible()
        }
    }

    private fun showHideSettingOptions(show: Boolean) {
        if (show) {
            binding.incToolbar.lnrOptions.visible()
        } else {
            binding.incToolbar.lnrOptions.gone()
        }
    }

    private fun showHideHomeIcon(show: Boolean) {
        if (show) {
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

    override fun goBack() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        }
    }

    override fun updateTopBar(isHomeScreen: Boolean) {
        if (!isHomeScreen) {
            showHideBackIcon(true)
            showHideHomeIcon(true)
            showHideSettingOptions(false)
        } else {
            showHideBackIcon(false)
            showHideHomeIcon(false)
            showHideSettingOptions(true)
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
        val dateFormat = SimpleDateFormat(CommonUtils.CLOCK_DATE_AND_TIME_FORMAT, Locale.ENGLISH)
        val formattedDate = dateFormat.format(currentTime)
        binding.incToolbar.tvDateTime.text = formattedDate.uppercase()
    }

    private fun handleBackStackChanges() {
        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                Log.d("TAG", "onBackPressed Count: ${supportFragmentManager.backStackEntryCount}")
                if (supportFragmentManager.backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
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
            "Ethernet" -> {
                binding.incToolbar.tvServerStatus.text = getString(R.string.online)
                binding.incToolbar.tvServerStatus.background =
                    AppCompatResources.getDrawable(this, R.drawable.ic_server_online_gradient)
                binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_connected)
            }

            "GSM" -> {
                binding.incToolbar.tvServerStatus.text = getString(R.string.online)
                binding.incToolbar.tvServerStatus.background =
                    AppCompatResources.getDrawable(this, R.drawable.ic_server_online_gradient)
                binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_connected)
            }

            "Wifi" -> {
                binding.incToolbar.tvServerStatus.text = getString(R.string.online)
                binding.incToolbar.tvServerStatus.background =
                    AppCompatResources.getDrawable(this, R.drawable.ic_server_online_gradient)
                binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_connected)
            }

            else -> {
                binding.incToolbar.tvServerStatus.text = getString(R.string.offline)
                binding.incToolbar.tvServerStatus.background =
                    AppCompatResources.getDrawable(this, R.drawable.ic_server_offline_gradient)
                binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_disconnected)
            }
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
    }

    override fun onStop() {
        super.onStop()
        prefHelper.setBoolean("IS_GUN_VOLTAGE_CHANGED", false)
        prefHelper.setBoolean("IS_GUN_CURRENT_CHANGED", false)
        prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)
        prefHelper.setBoolean("IS_IN_TEST_MODE", false)

    }

    override fun replaceFragment(fragment: Fragment?, shouldMoveToHomeScreen: Boolean) {
        if (fragment != null) {
            addNewFragment(fragment, shouldMoveToHomeScreen)
        }
    }
}