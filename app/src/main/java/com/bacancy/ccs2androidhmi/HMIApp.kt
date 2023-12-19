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

    var mSerialPortFinder = SerialPortFinder()
    private var mSerialPort: SerialPort? = null

    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun getSerialPort(): SerialPort {
        /*if (mSerialPort == null) {
            *//* Read serial port parameters *//*
            val sp = getSharedPreferences("com.example.mydwindemoapp_preferences", MODE_PRIVATE)
            Log.d("TAG", "SharedPrefs: $sp")
            val path = sp.getString("DEVICE", "")
            Log.d("TAG", "Path: $path")
            val baudrate = sp.getString("BAUDRATE", "-1")?.let { Integer.decode(it) }
            Log.d("TAG", "baudrate: $baudrate")
            *//* Check parameters *//*
            if (path!!.length == 0 || baudrate == -1) {
                throw InvalidParameterException()
            }
            println("ysjie path = $path,baudrate = $baudrate")
            *//* Open the serial port *//*
            mSerialPort = baudrate?.let { SerialPort(File(path), it, 0) }
        }*/
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