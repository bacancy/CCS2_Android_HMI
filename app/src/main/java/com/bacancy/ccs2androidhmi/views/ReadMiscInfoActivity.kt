package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityReadMiscInfoBinding
import com.bacancy.ccs2androidhmi.util.CommonUtils.getSimpleMacAddress
import com.bacancy.ccs2androidhmi.util.CommonUtils.getSwappedMacAddress
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.byteArrayToBinaryString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.bytesToAsciiString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getActualIntValueFromHighAndLowBytes
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ResponseSizes.MISC_INFORMATION_RESPONSE_SIZE
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils.checkGSMNetworkStrength
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils.checkIfEthernetIsConnected
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils.checkServerConnectedWith
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils.checkWifiNetworkStrength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ReadMiscInfoActivity : SerialPortBaseActivity() {

    private lateinit var observer: ModbusReadObserver
    private lateinit var binding: ActivityReadMiscInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMiscInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Misc Information"
        startReadingMiscInformation()
    }

    private fun startReadingMiscInformation() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    observer = ModbusReadObserver()
                    observer.startObserving(
                        mOutputStream,
                        mInputStream, MISC_INFORMATION_RESPONSE_SIZE,
                        ModbusRequestFrames.getMiscInfoRequestFrame(), { responseFrameArray ->
                            onDataReceived(responseFrameArray)
                        }, {
                            //OnFailure
                        })
                    /*delay(5000)
                    observer.stopObserving()*/
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onDataReceived(buffer: ByteArray) {
        Log.d("TAG", "onDataReceived: ${buffer.toHex()}")
        lifecycleScope.launch(Dispatchers.Main) {
            binding.apply {
                val networkStatusBits =
                    byteArrayToBinaryString(buffer.copyOfRange(3, 5)).reversed()
                        .substring(0, 11)
                val arrayOfNetworkStatusBits = networkStatusBits.toCharArray()
                val wifiNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(0, 3)
                val gsmNetworkStrengthBits = arrayOfNetworkStatusBits.copyOfRange(3, 7)
                val ethernetConnectedBits = arrayOfNetworkStatusBits.copyOfRange(7, 8)
                val serverConnectedWithBits = arrayOfNetworkStatusBits.copyOfRange(8, 11)

                txtServerConnection.text =
                    "Server connection = ${checkServerConnectedWith(serverConnectedWithBits)}"
                txtEthernetConnection.text =
                    "Ethernet = ${checkIfEthernetIsConnected(ethernetConnectedBits)}"
                txtGSMStrength.text =
                    "GSM Strength = ${checkGSMNetworkStrength(gsmNetworkStrengthBits)}"
                txtWifiStrength.text =
                    "Wifi Strength = ${checkWifiNetworkStrength(wifiNetworkStrengthBits)}"

                txtMCUFirmwareVersion.text =
                    "MCU FIRMWARE VERSION = ${getMCUFirmwareVersion(buffer)}"
                txtOCPPFirmwareVersion.text =
                    "OCPP FIRMWARE VERSION = ${getOCPPFirmwareVersion(buffer)}"
                txtRFIDFirmwareVersion.text =
                    "RFID FIRMWARE VERSION = ${getRFIDFirmwareVersion(buffer)}"
                txtLEDFirmwareVersion.text =
                    "LED FIRMWARE VERSION = ${getLEDModuleFirmwareVersion(buffer)}"
                txtPLC1ModuleFirmwareVersion.text =
                    "PLC1 Module FIRMWARE VERSION = ${getPLC1ModuleFirmwareVersion(buffer)}"
                txtPLC2ModuleFirmwareVersion.text =
                    "PLC2 Module FIRMWARE VERSION = ${getPLC2ModuleFirmwareVersion(buffer)}"

                txtChargerSerialID.text = "CHARGER SERIAL ID = ${getChargerSerialID(buffer)}"
                txtEthernetMacAddress.text =
                    "Ethernet MAC Address = ${getEthernetMacAddress(buffer)}"
                txtBluetoohMacAddress.text =
                    "Bluetooth MAC Address = ${getBluetoothMacAddress(buffer)}"
                txtWifiStationModeMacAddress.text =
                    "Wifi Station Mode MAC Address = ${getWifiStationModeMacAddress(buffer)}"
                txtWifiAPModeMacAddress.text =
                    "Wifi AP Mode MAC Address = ${getWifiAPModeMacAddress(buffer)}"
                txtRFIDNumber.text = "RFID NUMBER = ${getRFIDNumber(buffer)}"

                //0-NoError
                //1-Error
                Log.d("TAG", "onDataReceived: PLC1 Fault = ${byteArrayToBinaryString(buffer.copyOfRange(13, 21)).reversed().substring(0, 1)}")

                //0-NoError
                //1-Error
                Log.d("TAG", "onDataReceived: PLC2 Fault = ${byteArrayToBinaryString(buffer.copyOfRange(21, 29)).reversed().substring(0, 1)}")

                Log.d("TAG", "onDataReceived: Rectifier1 Fault = ${buffer.copyOfRange(29, 33).toHex()}")
                Log.d("TAG", "onDataReceived: Rectifier2 Fault = ${buffer.copyOfRange(33, 37).toHex()}")
                Log.d("TAG", "onDataReceived: Rectifier3 Fault = ${buffer.copyOfRange(37, 41).toHex()}")
                Log.d("TAG", "onDataReceived: Rectifier4 Fault = ${buffer.copyOfRange(41, 45).toHex()}")

                //0-Working
                //1-Failure
                Log.d("TAG", "onDataReceived: Comm Error = ${byteArrayToBinaryString(buffer.copyOfRange(45, 49)).reversed().substring(0, 4)}")

                //0-NotConnected
                //1-Connected
                Log.d("TAG", "onDataReceived: DPCS = ${byteArrayToBinaryString(buffer.copyOfRange(59, 61)).reversed().substring(0, 6)}")
            }
        }
    }

    private fun getAmbientTemperature(response: ByteArray): String {
        val floatBytes = response.copyOfRange(5, 9)
        return "${ModbusTypeConverter.byteArrayToFloat(floatBytes)} c"
    }

    private fun getMCUFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[9].getIntValueFromByte()
        val reg3LSB = response[10].getIntValueFromByte()
        val reg4MSB = response[11].getIntValueFromByte()
        val reg4LSB = response[12].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getOCPPFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[65].getIntValueFromByte()
        val reg3LSB = response[66].getIntValueFromByte()
        val reg4MSB = response[67].getIntValueFromByte()
        val reg4LSB = response[68].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getRFIDFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[75].getIntValueFromByte()
        val reg3LSB = response[76].getIntValueFromByte()
        val reg4MSB = response[77].getIntValueFromByte()
        val reg4LSB = response[78].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getLEDModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[79].getIntValueFromByte()
        val reg3LSB = response[80].getIntValueFromByte()
        val reg4MSB = response[81].getIntValueFromByte()
        val reg4LSB = response[82].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getPLC1ModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[83].getIntValueFromByte()
        val reg3LSB = response[84].getIntValueFromByte()
        val reg4MSB = response[85].getIntValueFromByte()
        val reg4LSB = response[86].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getPLC2ModuleFirmwareVersion(response: ByteArray): String {
        val reg3MSB = response[87].getIntValueFromByte()
        val reg3LSB = response[88].getIntValueFromByte()
        val reg4MSB = response[89].getIntValueFromByte()
        val reg4LSB = response[90].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getChargerSerialID(response: ByteArray): String {
        val serialIDBytesArray = response.copyOfRange(103, 128)
        return bytesToAsciiString(serialIDBytesArray)
    }

    private fun getEthernetMacAddress(response: ByteArray): String {
        val macAddressArray = response.copyOfRange(129, 129 + 6)
        return getSwappedMacAddress(macAddressArray)
    }

    private fun getBluetoothMacAddress(response: ByteArray): String {
        val macAddressArray = response.copyOfRange(135, 135 + 6)
        return getSwappedMacAddress(macAddressArray)
    }

    private fun getWifiStationModeMacAddress(response: ByteArray): String {
        //141-142 143-144 145-146
        val macAddressArray = response.copyOfRange(141, 141 + 6)
        return getSwappedMacAddress(macAddressArray)
    }

    private fun getWifiAPModeMacAddress(response: ByteArray): String {
        //147-148 149-150 151-152
        val macAddressArray = response.copyOfRange(147, 147 + 6)
        return getSwappedMacAddress(macAddressArray)
    }

    private fun getRFIDNumber(response: ByteArray): String {
        val macAddressArray = response.copyOfRange(51, 59)
        return getSimpleMacAddress(macAddressArray)
    }

    override fun onPause() {
        super.onPause()
        observer.stopObserving()
    }
}