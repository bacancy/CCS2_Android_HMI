package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import com.bacancy.ccs2androidhmi.views.fragment.AcMeterFragment
import com.bacancy.ccs2androidhmi.views.fragment.DcMeterFragment
import com.bacancy.ccs2androidhmi.views.fragment.FragmentChangeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class NewTestActivity : SerialPortBaseActivityNew(), FragmentChangeListener,
    OnBackPressedDispatcherOwner {

    private lateinit var acMeterFragment: AcMeterFragment
    private lateinit var binding: ActivityNewTestBinding
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startReadingMiscInformation()
        acMeterFragment = AcMeterFragment()
        addNewFragment(acMeterFragment)

        binding.incToolbar.imgBack.setOnClickListener {
            Toast.makeText(this, "HELLO", Toast.LENGTH_SHORT).show()
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }

        handleBackStackChanges()
        //handler.post(runnable)
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

    private fun startReadingMiscInformation() {
        lifecycleScope.launch {
            while (true) {
                withContext(Dispatchers.IO) {
                    //Step 1: Request for misc data
                    readMiscData()
                }
                //Step 6: Now start Step 1 again with a delay of 3 seconds
                delay(3000)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    suspend fun readMiscData() {
        lifecycleScope.launch(Dispatchers.IO) {
            ReadWriteUtil.startReading(
                mOutputStream,
                mInputStream,
                ResponseSizes.MISC_INFORMATION_RESPONSE_SIZE,
                ModbusRequestFrames.getMiscInfoRequestFrame()
            ) {
                //Step 2: Read response of misc data
                if (it.toHex().startsWith(ModBusUtils.HOLDING_REGISTERS_CORRECT_RESPONSE_BITS)) {

                    lifecycleScope.launch {
                        Log.d("TAG", "MISC DATA RESPONSE: ${it.toHex()}")
                        val networkStatusBits =
                            ModbusTypeConverter.byteArrayToBinaryString(it.copyOfRange(3, 5)).reversed()
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
                            adjustWifiLevel(
                                StateAndModesUtils.checkWifiNetworkStrength(
                                    wifiNetworkStrengthBits
                                ).toInt()
                            )
                        }
                        Log.d("TAG", "miscDataRecd: CALLED IN NEW TEST ACTIVITY")
                        //Step 3: Delay for 3 seconds
                        delay(3000)
                        //Step 4: Request for ac meter data
                        acMeterFragment.startReadingMeterData()
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
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
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

}
