package com.bacancy.ccs2androidhmi.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityDashboardBinding
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class Dashboard : SerialPortBaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var observer: ModbusReadObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Dashboard"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        readChargeConfiguration()
    }
    fun readChargeConfiguration() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    var address = 436
                    var quantity = 1
                    observer = ModbusReadObserver()
                    observer.startObserving(
                        mOutputStream, mInputStream, 16,
                        ModBusUtils.createReadHoldingRegistersRequest(1, address, quantity)
                    ) { responseFrameArray ->
                        onDataReceived(responseFrameArray)
                    }
                    delay(3000)
                    observer.stopObserving()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onDataReceived(buffer: ByteArray) {
        val decodeResponse = ModBusUtils.convertModbusResponseFrameToStringSingleElement(buffer)
        Log.d("TAG", "onDataReceived: $decodeResponse")
        lifecycleScope.launch(Dispatchers.Main) {
            when (decodeResponse) {
                1 -> {
                    Log.d("TAG", "onDataReceived: SINGLE GUN")
                    binding.txtDataRead.text = "Charge Configuration = Single Gun"
                }
                2 -> {
                    Log.d("TAG", "onDataReceived: DUAL GUN")
                    binding.txtDataRead.text = "Charge Configuration = Dual Gun"
                }
            }
        }
    }

    fun goToReadInputRegisterScreen(view: View) {
        startActivity(Intent(this, ReadInputRegistersActivity::class.java))
    }
    fun goToReadHoldingRegistersScreen(view: View) {
        startActivity(Intent(this, ReadHoldingRegistersActivity::class.java))
    }
    fun goToWriteSingleHoldingRegisterScreen(view: View) {
        startActivity(Intent(this, WriteSingleHoldingRegisterActivity::class.java))
    }
    fun goToWriteMultipleHoldingRegistersScreen(view: View) {
        startActivity(Intent(this, WriteMultipleHoldingRegistersActivity::class.java))
    }

    fun goToReadACMeterInfoScreen(view: View) {
        startActivity(Intent(this, ReadACMeterInfoActivity::class.java))
    }

    fun goToReadMiscInfoScreen(view: View) {
        startActivity(Intent(this, ReadMiscInfoActivity::class.java))
    }

    fun goToGun1DCMeterInfoScreen(view: View) {
        startActivity(Intent(this, ReadGun1DCMeterInfoActivity::class.java))
    }

    fun goToGun2DCMeterInfoScreen(view: View) {
        startActivity(Intent(this, ReadGun2DCMeterInfoActivity::class.java))
    }

    override fun onPause() {
        super.onPause()
        observer.stopObserving()
    }

    fun goToViewNewScreens(view: View) {
        startActivity(Intent(this, NewScreensActivity::class.java))
    }

    fun goToReadACChargerACMeterInfoScreen(view: View) {
        startActivity(Intent(this, ReadACChargerACMeterInfoActivity::class.java))
    }

    fun goToUseGun1(view: View) {
        startActivity(Intent(this, Gun1InformationActivity::class.java).putExtra("IS_GUN1",true))
    }
    fun goToUseGun2(view: View) {
        startActivity(Intent(this, Gun1InformationActivity::class.java).putExtra("IS_GUN1",false))
    }
}