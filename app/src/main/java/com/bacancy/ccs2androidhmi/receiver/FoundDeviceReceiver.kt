package com.bacancy.ccs2androidhmi.receiver

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

class FoundDeviceReceiver(private val onDeviceFound: (BluetoothDevice) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("FoundDeviceReceiver", "onReceive")
        when (intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                Log.d("FoundDeviceReceiver", "Device found")
                when (PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) -> {
                        Log.d("FoundDeviceReceiver", "Permission granted")
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val deviceName = device?.name
                        val deviceAddress = device?.address
                        val deviceRssi =
                            intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        val deviceClass = device?.bluetoothClass?.majorDeviceClass
                        Log.d(
                            "FoundDeviceReceiver",
                            "Device found: $deviceName, $deviceAddress, $deviceRssi, $deviceClass"
                        )
                        device?.let { onDeviceFound(it) }
                    }

                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) -> {
                        Log.d("FoundDeviceReceiver", "Permission granted")
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val deviceName = device?.name
                        val deviceAddress = device?.address
                        val deviceRssi =
                            intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        val deviceClass = device?.bluetoothClass?.majorDeviceClass
                        Log.d(
                            "FoundDeviceReceiver",
                            "Device found: $deviceName, $deviceAddress, $deviceRssi, $deviceClass"
                        )
                        device?.let { onDeviceFound(it) }
                    }

                    else -> {
                        Log.d("FoundDeviceReceiver", "Permission not granted")
                    }
                }
            }
        }
    }
}