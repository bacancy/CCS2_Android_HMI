package com.bacancy.ccs2androidhmi

import android.app.Application
import android.util.Log
import android_serialport_api.SerialPort
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class HMIApp: Application() {

    private var mSerialPort: SerialPort? = null

    fun getSerialPort(): SerialPort? {
        try {
            if(mSerialPort == null){
                mSerialPort = SerialPort(File("/dev/ttyS8"), 115200, 0)
            }
            return mSerialPort as SerialPort
        }catch (e: Exception){
            Log.d("TAG", "onCreate: Exception = ${e.toString()}")
        }
        return null
    }

    fun closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort?.close()
            mSerialPort = null
        }
    }

}