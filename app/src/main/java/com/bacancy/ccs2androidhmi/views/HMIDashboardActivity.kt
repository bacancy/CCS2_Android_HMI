package com.bacancy.ccs2androidhmi.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivityNew
import com.bacancy.ccs2androidhmi.databinding.ActivityHmiDashboardBinding
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.NO_STATE
import com.bacancy.ccs2androidhmi.util.MiscInfoUtils.TOKEN_ID_NONE
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.showToast
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.views.fragment.GunsHomeScreenFragment
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HMIDashboardActivity : SerialPortBaseActivityNew(), FragmentChangeListener,
    OnBackPressedDispatcherOwner {

    private lateinit var gunsHomeScreenFragment: GunsHomeScreenFragment
    private lateinit var binding: ActivityHmiDashboardBinding
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        binding = ActivityHmiDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gunsHomeScreenFragment = GunsHomeScreenFragment()
        addNewFragment(gunsHomeScreenFragment)

        handleClicks()

        handleBackStackChanges()

        startClockTimer()

        observeLatestMiscInfo()

        showHideBackIcon()

        showHideHomeIcon()

    }

    private fun handleClicks() {
        binding.incToolbar.imgBack.setOnClickListener {
            goBack()
        }
        binding.incToolbar.imgHome.setOnClickListener {
            prefHelper.setBoolean("IS_IN_TEST_MODE", false)
            prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)
            addNewFragment(GunsHomeScreenFragment(), true)
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
        if (binding.incToolbar.imgBack.isVisible) {
            binding.incToolbar.tvEmergencyStop.gone()
        }
    }

    fun showHideHomeIcon(showHomeIcon: Boolean = true) {
        if (showHomeIcon) {
            binding.incToolbar.imgHome.visible()
        } else {
            binding.incToolbar.imgHome.invisible()
        }
        if (binding.incToolbar.imgHome.isVisible) {
            binding.incToolbar.tvEmergencyStop.gone()
        }
    }

    private fun observeLatestMiscInfo() {
        appViewModel.latestMiscInfo.observe(this) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updateServerStatus(latestMiscInfo.serverConnectedWith)
                updateEthernetStatus(latestMiscInfo.ethernetStatus)
                adjustGSMLevel(latestMiscInfo.gsmLevel)
                adjustWifiLevel(latestMiscInfo.wifiLevel)
                showOrHideEmergencyStop(latestMiscInfo.emergencyButtonStatus)
                if (latestMiscInfo.rfidTagState.isNotEmpty() && latestMiscInfo.rfidTagState != TOKEN_ID_NONE && latestMiscInfo.rfidTagState != NO_STATE) {
                    showToast(latestMiscInfo.rfidTagState)
                }
            }
        }
    }

    fun showOrHideEmergencyStop(emergencyButtonStatus: Int) {
        if (emergencyButtonStatus == 1) {
            binding.incToolbar.tvEmergencyStop.visible()
        } else {
            binding.incToolbar.tvEmergencyStop.gone()
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
        supportFragmentManager.addOnBackStackChangedListener {
            // Perform actions based on the current state of the back stack
            // For example, update the UI or perform specific logic
        }
    }

    @Deprecated("Deprecated in Java", replaceWith = ReplaceWith(""))
    override fun onBackPressed() {
        Log.d("TAG", "onBackPressed Count: ${supportFragmentManager.backStackEntryCount}")
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
            super.onBackPressed()
        }
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
            "Not Connected" -> binding.incToolbar.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_disconnected_new)
            "Connected" -> binding.incToolbar.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_connected_new)
            else -> binding.incToolbar.imgEthernetStatus.setImageResource(R.drawable.ic_ethernet_disconnected_new)
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
}