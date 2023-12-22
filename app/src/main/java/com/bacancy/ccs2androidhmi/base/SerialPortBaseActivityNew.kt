package com.bacancy.ccs2androidhmi.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android_serialport_api.SerialPort
import androidx.fragment.app.FragmentActivity
import com.bacancy.ccs2androidhmi.HMIApp
import java.io.InputStream
import java.io.OutputStream

abstract class SerialPortBaseActivityNew : FragmentActivity() {

    protected var mApplication: HMIApp? = null
    protected var mSerialPort: SerialPort? = null
    protected var mOutputStream: OutputStream? = null
    var mInputStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
        super.onCreate(savedInstanceState)
        mApplication = application as HMIApp
        try {
            mSerialPort = mApplication!!.getSerialPort()
            mOutputStream = mSerialPort!!.outputStream
            mInputStream = mSerialPort!!.inputStream
        } catch (e: Exception) {
            Log.d("TAG", "onCreate: Exception = ${e.toString()}")
        }
    }

    override fun onDestroy() {
        mApplication!!.closeSerialPort()
        mSerialPort = null
        super.onDestroy()
    }

}