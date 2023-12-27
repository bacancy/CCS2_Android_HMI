package com.bacancy.ccs2androidhmi.base

import android.os.Bundle
import android.util.Log
import android_serialport_api.SerialPort
import androidx.fragment.app.Fragment
import com.bacancy.ccs2androidhmi.HMIApp
import java.io.InputStream
import java.io.OutputStream

abstract class BaseFragment: Fragment() {

    protected var mApplication: HMIApp? = null
    protected var mSerialPort: SerialPort? = null
    protected var mOutputStream: OutputStream? = null
    var mInputStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApplication = requireActivity().application as HMIApp
        try {
            mSerialPort = mApplication!!.getSerialPort()
            mOutputStream = mSerialPort!!.outputStream
            mInputStream = mSerialPort!!.inputStream
        } catch (e: Exception) {
            Log.d("TAG", "onCreate: Exception = ${e.toString()}")
        }

    }

    abstract fun setScreenHeaderViews()

    abstract fun setupViews()

}