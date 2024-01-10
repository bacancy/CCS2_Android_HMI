package com.bacancy.ccs2androidhmi.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityDashboardBinding
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil.writeToSingleHoldingRegisterNew
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class Dashboard : SerialPortBaseActivity() {

    private var CHARGE_CONFIG: Int = 0
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var observer: ModbusReadObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        /*requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)*/
        /*window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )*/
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Dashboard"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        readChargeConfiguration()
    }

    private fun readChargeConfiguration() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val address = 436
                    val quantity = 1
                    observer = ModbusReadObserver()
                    observer.startObserving(
                        mOutputStream, mInputStream, 16,
                        ModBusUtils.createReadHoldingRegistersRequest(1, address, quantity),
                        { responseFrameArray ->
                            onDataReceived(responseFrameArray)
                        }, {
                            //OnFailure
                        })
                    //delay(10000)
                    //observer.stopObserving()
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
                    CHARGE_CONFIG = 1
                    binding.txtDataRead.text = "Charge Configuration = Single Gun"
                    binding.btnGun1.visibility = View.VISIBLE
                }

                2 -> {
                    CHARGE_CONFIG = 2
                    binding.txtDataRead.text = "Charge Configuration = Dual Gun"
                    binding.btnGun1.visibility = View.VISIBLE
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
        if (CHARGE_CONFIG == 2) {
            authenticateGun(1)
        } else {
            startActivity(
                Intent(this, Gun1InformationActivity::class.java).putExtra(
                    "IS_GUN1",
                    true
                )
            )
        }
    }

    fun goToUseGun2(view: View) {
        if (CHARGE_CONFIG == 2) {
            authenticateGun(2)
        } else {
            startActivity(
                Intent(this, Gun1InformationActivity::class.java).putExtra(
                    "IS_GUN1",
                    false
                )
            )
        }
    }

    private fun authenticateGun(gunNumber: Int) {
        /*lifecycleScope.launch(Dispatchers.IO) {
            writeToSingleHoldingRegisterNew(
                mOutputStream,
                mInputStream,
                30,
                gunNumber
            , { responseFrame ->
                    val decodeResponse =
                        ModBusUtils.convertModbusResponseFrameToString(responseFrame)
                    Log.d("TAG", "onDataReceived: $decodeResponse")
                    if (gunNumber == 1) {
                        startActivity(
                            Intent(this@Dashboard, Gun1InformationActivity::class.java).putExtra(
                                "IS_GUN1",
                                true
                            )
                        )
                    } else {
                        startActivity(
                            Intent(this@Dashboard, Gun1InformationActivity::class.java).putExtra(
                                "IS_GUN1",
                                false
                            )
                        )
                    }
            },{})
        }*/
    }

    fun goToAllChargingSummary(view: View) {
        startActivity(Intent(this, CheckAllChargingSummariesActivity::class.java))
    }

    fun goToTestScreen(view: View) {
        //startActivity(Intent(this,NewTestActivity::class.java))
    }


}