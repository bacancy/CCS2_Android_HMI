package com.bacancy.ccs2androidhmi.views

import android.app.UiModeManager
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivityNew
import com.bacancy.ccs2androidhmi.databinding.ActivityHmiDashboardBinding
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.mqtt.MQTTClient
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants.TOPIC_A_TO_B
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants.TOPIC_B_TO_A
import com.bacancy.ccs2androidhmi.mqtt.models.ChargerDetailsBody
import com.bacancy.ccs2androidhmi.mqtt.models.ConnectorStatusBody
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_LOCAL_START_STOP
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_TEST_MODE
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.getCurrentDateTime
import com.bacancy.ccs2androidhmi.util.DialogUtils.showPasswordPromptDialog
import com.bacancy.ccs2androidhmi.util.LogUtils
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.NO_STATE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.TOKEN_ID_NONE
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.bacancy.ccs2androidhmi.util.PrefHelper.Companion.IS_DARK_THEME
import com.bacancy.ccs2androidhmi.util.ToastUtils.showCustomToast
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.MQTTWorkerViewModel
import com.bacancy.ccs2androidhmi.views.fragment.FirmwareVersionInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.GunsHomeScreenFragment
import com.bacancy.ccs2androidhmi.views.fragment.LocalStartStopFragment
import com.bacancy.ccs2androidhmi.views.fragment.NewFaultInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.TestModeHomeFragment
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class HMIDashboardActivity : SerialPortBaseActivityNew(), FragmentChangeListener {

    private lateinit var gunsHomeScreenFragment: GunsHomeScreenFragment
    private lateinit var binding: ActivityHmiDashboardBinding
    val handler = Handler(Looper.getMainLooper())

    private val mqttWorkerViewModel: MQTTWorkerViewModel by viewModels()

    @Inject
    lateinit var mqttClient: MQTTClient

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

        startClockTimer()

        observeLatestMiscInfo()

        showHideBackIcon()

        showHideHomeIcon()

        insertSampleChargingHistory()

        startMQTTConnection()

        observePublishRequest()
    }

    private fun observePublishRequest() {
        lifecycleScope.launch {
            mqttWorkerViewModel.publishMessageRequest.collectLatest {
                publishMessageToTopic(it.first, it.second)
            }
        }
    }

    private fun startMQTTConnection() {
        if (mqttClient.isConnected().not()) {
            connectToMQTT()
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

    private fun connectToMQTT() {
        if (isInternetConnected()) {
            lifecycleScope.launch(Dispatchers.IO) {
                mqttClient.connect(
                    ServerConstants.MQTT_USERNAME,
                    ServerConstants.MQTT_PWD, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            LogUtils.debugLog("MQTTWorker - Connect onSuccess")
                            subscribeTopic(TOPIC_A_TO_B)
                            subscribeTopic(TOPIC_B_TO_A)
                            sendInitialChargerDetails()
                            sendConnectorStatus()
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            LogUtils.errorLog("MQTTWorker - Connect onFailure - ${exception?.printStackTrace()}")
                        }

                    }, object : MqttCallback {
                        override fun connectionLost(cause: Throwable?) {
                            LogUtils.errorLog("MQTTWorker - Connect Connection Lost")
                        }

                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            LogUtils.debugLog("MQTTWorker - Connect Data Arrived from Topic=$topic Message=$message")
                            when (topic) {
                                TOPIC_A_TO_B -> {
                                    /*val messageInModel = Gson().fromJson(message.toString(), ResponseModel::class.java)
                                      LogUtils.debugLog("MQTTWorker - MESSAGE IN MODEL = $messageInModel")*/
                                }

                                TOPIC_B_TO_A -> {
                                    /*val messageInModel = Gson().fromJson(message.toString(), ResponseModel::class.java)
                                      LogUtils.debugLog("MQTTWorker - MESSAGE IN MODEL = $messageInModel")*/
                                }
                            }
                        }

                        override fun deliveryComplete(token: IMqttDeliveryToken?) {
                            LogUtils.debugLog("MQTTWorker - Connect Delivery Complete")
                        }
                    })
            }
        } else {
            LogUtils.errorLog(getString(R.string.msg_internet_connection_unavailable))
        }
    }

    private fun sendInitialChargerDetails() {
        publishMessageToTopic(
            TOPIC_A_TO_B,
            Gson().toJson(
                ChargerDetailsBody(
                    chargerOutputs = "2",
                    chargerRating = "120KW",
                    configDateTime = getCurrentDateTime().orEmpty(),
                    deviceMacAddress = "1133557799"
                )
            )
        )
    }

    private fun sendConnectorStatus() {
        lifecycleScope.launch {
            delay(500)
            publishMessageToTopic(
                TOPIC_A_TO_B,
                Gson().toJson(ConnectorStatusBody(connectorId = 1, connectorStatus = "Unplugged"))
            )
            delay(500)
            publishMessageToTopic(
                TOPIC_A_TO_B,
                Gson().toJson(ConnectorStatusBody(connectorId = 2, connectorStatus = "Unplugged"))
            )
        }
    }

    fun disconnectWithMQTT() {
        if (isInternetConnected()) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect(object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            LogUtils.debugLog("MQTTWorker - Disconnect onSuccess")
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            LogUtils.errorLog("MQTTWorker - Disconnect onFailure")
                        }
                    })
                }
            }
        } else {
            LogUtils.errorLog(getString(R.string.msg_internet_connection_unavailable))
        }
    }

    fun subscribeTopic(topicName: String) {
        if (isInternetConnected()) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (mqttClient.isConnected()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        mqttClient.subscribe(topicName, 1, object : IMqttActionListener {
                            override fun onSuccess(asyncActionToken: IMqttToken?) {
                                LogUtils.debugLog("MQTTWorker - Subscribe to $topicName onSuccess")
                            }

                            override fun onFailure(
                                asyncActionToken: IMqttToken?,
                                exception: Throwable?
                            ) {
                                LogUtils.errorLog("MQTTWorker - Subscribe to $topicName onFailure")
                            }
                        })
                    }
                }
            }
        } else {
            LogUtils.errorLog(getString(R.string.msg_internet_connection_unavailable))
        }
    }

    fun unsubscribeTopic(topicName: String) {
        if (isInternetConnected()) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (mqttClient.isConnected()) {
                    mqttClient.unsubscribe(topicName, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            LogUtils.debugLog("MQTTWorker - UnSubscribe onFailure")
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            LogUtils.errorLog("MQTTWorker - UnSubscribe onFailure")
                        }
                    })
                }
            }
        } else {
            LogUtils.errorLog(getString(R.string.msg_internet_connection_unavailable))
        }
    }

    private fun publishMessageToTopic(topicName: String, message: String) {
        if (isInternetConnected()) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (mqttClient.isConnected()) {
                    mqttClient.publish(topicName, message, 1, false, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            LogUtils.debugLog("MQTTWorker - Publish onSuccess")
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            LogUtils.errorLog("MQTTWorker - Publish onFailure")
                        }
                    })
                }
            }
        } else {
            LogUtils.errorLog(getString(R.string.msg_internet_connection_unavailable))
        }
    }
}