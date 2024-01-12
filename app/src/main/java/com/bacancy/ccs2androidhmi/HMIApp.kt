package com.bacancy.ccs2androidhmi

import android.app.Application
import android_serialport_api.SerialPort
import android_serialport_api.SerialPortFinder
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.IOException
import java.security.InvalidParameterException

@HiltAndroidApp
class HMIApp: Application() {

    private var mSerialPort: SerialPort? = null

    fun getSerialPort(): SerialPort {
        if(mSerialPort == null){
            mSerialPort = SerialPort(File("/dev/ttyS8"), 115200, 0)
        }
        return mSerialPort as SerialPort
    }

    fun closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort!!.close()
            mSerialPort = null
        }
    }

}