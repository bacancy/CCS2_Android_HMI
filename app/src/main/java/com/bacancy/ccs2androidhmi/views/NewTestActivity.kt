package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivityNew
import com.bacancy.ccs2androidhmi.databinding.ActivityNewTestBinding
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import com.bacancy.ccs2androidhmi.views.fragment.ACMeterInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.DcMeterFragment
import com.bacancy.ccs2androidhmi.views.fragment.GunsDCOutputInfoFragment
import com.bacancy.ccs2androidhmi.views.fragment.GunsMoreInformationFragment
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import com.bacancy.ccs2androidhmi.views.listener.MiscDataListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class NewTestActivity : SerialPortBaseActivityNew(), FragmentChangeListener,
    OnBackPressedDispatcherOwner, MiscDataListener {

    private lateinit var acMeterFragment: ACMeterInfoFragment
    private lateinit var gunsDCOutputInfoFragment: GunsDCOutputInfoFragment
    private lateinit var gunsMoreInformationFragment: GunsMoreInformationFragment
    private lateinit var binding: ActivityNewTestBinding
    val handler = Handler(Looper.getMainLooper())
    private var miscDataListener: MiscDataListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        miscDataListener = this
        //startReadingMiscInformation(true)
        //testMethod()
        acMeterFragment = ACMeterInfoFragment()
        gunsDCOutputInfoFragment = GunsDCOutputInfoFragment()
        gunsMoreInformationFragment = GunsMoreInformationFragment()
        addNewFragment(gunsMoreInformationFragment)
        binding.incToolbar.imgBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }

        handleBackStackChanges()

        //For starting clock timer
        handler.post(runnable)
        //handler.post(registersRunnable)
    }

    private val runnable = object : Runnable {
        override fun run() {
            startTimer()
            // Post the handler again to run in 1 second
            handler.postDelayed(this, 1000) // Delay of 1 second
        }
    }

    private fun startTimer() {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy EEE HH:mm:ss", Locale.ENGLISH)
        val formattedDate = dateFormat.format(currentTime)
        Log.d("TAG", "Current Date & Time = $formattedDate")
        binding.incToolbar.tvDateTime.text = formattedDate.uppercase()
    }

    fun startReadingMiscInformation(isChildResponseReceived: Boolean = false) {
        lifecycleScope.launch {
            while (true) {
                Log.i("FRITAG", "startReadingMiscInformation: CALLED")

                if(isChildResponseReceived){
                    withContext(Dispatchers.IO) {
                        //Step 1: Request for misc data
                        readMiscData()
                    }
                    //Step 6: Now start Step 1 again with a delay of 3 seconds
                    delay(3000)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    suspend fun readMiscData() {
        withContext(Dispatchers.IO) {
            val startTIme = System.currentTimeMillis()
            Log.i("FRITAG", "readMiscData: Started - $startTIme")
            ReadWriteUtil.startReading(
                mOutputStream,
                mInputStream,
                ResponseSizes.MISC_INFORMATION_RESPONSE_SIZE,
                ModbusRequestFrames.getMiscInfoRequestFrame()
            ) {
                //Step 2: Read response of misc data
                if (it.toHex().startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)) {
                    //Step 3: Delay for 3 seconds
                    //delay(100)
                    miscDataListener?.onMiscDataReceived()
                    val endTime = System.currentTimeMillis()

                    // Calculate the time difference
                    val elapsedTime = endTime - startTIme
                    Log.i("FRITAG", "readMiscData: Response Got - $elapsedTime ms")
                    lifecycleScope.launch {
                        Log.d("TAG", "MISC DATA RESPONSE: ${it.toHex()}")
                        val networkStatusBits =
                            ModbusTypeConverter.byteArrayToBinaryString(it.copyOfRange(3, 5))
                                .reversed()
                                .substring(0, 11)
                        val arrayOfNetworkStatusBits = networkStatusBits.toCharArray()
                        val wifiNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(0, 3)
                        val gsmNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(3, 7)
                        val ethernetConnectedBits = arrayOfNetworkStatusBits.copyOfRange(7, 8)
                        val serverConnectedWithBits = arrayOfNetworkStatusBits.copyOfRange(8, 11)
                        lifecycleScope.launch(Dispatchers.Main) {
                            updateServerStatus(
                                StateAndModesUtils.checkServerConnectedWith(
                                    serverConnectedWithBits
                                )
                            )
                            updateEthernetStatus(
                                StateAndModesUtils.checkIfEthernetIsConnected(
                                    ethernetConnectedBits
                                )
                            )
                            adjustGSMLevel(
                                StateAndModesUtils.checkGSMNetworkStrength(
                                    gsmNetworkStrengthBits
                                ).toInt()
                            )
                            adjustWifiLevel(
                                StateAndModesUtils.checkWifiNetworkStrength(
                                    wifiNetworkStrengthBits
                                ).toInt()
                            )
                        }
                        Log.d("TAG", "miscDataRecd: CALLED IN NEW TEST ACTIVITY")

                        //Step 4: Request for ac meter data

                        //acMeterFragment.startReadingMeterData()
                    }
                }
            }
       }

    }

    private fun handleBackStackChanges() {
        supportFragmentManager.addOnBackStackChangedListener {
            // Perform actions based on the current state of the back stack
            // For example, update the UI or perform specific logic
            Log.d("TAG", "handleBackStackChanges: CALLED")
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

    private fun addNewFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentContainer.id, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun adjustWifiLevel(wifiLevel: Int) {
        when (wifiLevel) {
            1 -> binding.incToolbar.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_1)
            2 -> binding.incToolbar.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_2)
            3 -> binding.incToolbar.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_3)
            else -> binding.incToolbar.imgWifiLevel.setImageResource(R.drawable.ic_wifi_level_0)
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
            "Ethernet" -> binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_with_ethernet)
            "GSM" -> binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_with_gsm)
            "Wifi" -> binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_with_wifi)
            else -> binding.incToolbar.imgServerStatus.setImageResource(R.drawable.ic_server_with_nothing)
        }
    }

    private fun adjustGSMLevel(level: Int) {
        when (level) {
            1 -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_1)
            2 -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_2)
            3 -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_3)
            4 -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_4)
            else -> binding.incToolbar.imgGSMLevel.setImageResource(R.drawable.ic_gsm_level_0)
        }
    }

    override fun onPause() {
        super.onPause()
        mApplication!!.closeSerialPort()
        handler.removeCallbacks(runnable)
    }

    override fun replaceFragment(fragment: Fragment?) {
        if (fragment != null) {
            addNewFragment(fragment)
        }
    }

    override fun onMiscDataReceived() {
        Log.d("TAG", "onMiscDataReceived: CALLED IN NEW TEST ACTIVITY")
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        //Just add new fragments in which you want to start respective screen readings after misc data received
        if (fragment is ACMeterInfoFragment) {
            fragment.onMiscDataReceived()
        } else if (fragment is DcMeterFragment) {
            fragment.onMiscDataReceived()
        }
    }

}
