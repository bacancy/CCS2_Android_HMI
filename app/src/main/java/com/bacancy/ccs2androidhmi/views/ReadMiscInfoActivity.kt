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
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.bytesToAsciiString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.decimalArrayToHexArray
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getActualIntValueFromHighAndLowBytes
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ResponseSizes.MISC_INFORMATION_RESPONSE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

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
                        ModbusRequestFrames.getMiscInfoRequestFrame()
                    ) { responseFrameArray ->
                        onDataReceived(responseFrameArray)
                    }
                    delay(5000)
                    observer.stopObserving()
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
                Log.d("TAG", "onDataReceived: Network Module Status = ${getNetworkModuleStatus(buffer)}")
                txtMCUFirmwareVersion.text = "MCU FIRMWARE VERSION = ${getMCUFirmwareVersion(buffer)}"
                txtOCPPFirmwareVersion.text = "OCPP FIRMWARE VERSION = ${getOCPPFirmwareVersion(buffer)}"
                txtRFIDFirmwareVersion.text = "RFID FIRMWARE VERSION = ${getRFIDFirmwareVersion(buffer)}"
                txtLEDFirmwareVersion.text = "LED FIRMWARE VERSION = ${getLEDModuleFirmwareVersion(buffer)}"
                txtPLC1ModuleFirmwareVersion.text = "PLC1 Module FIRMWARE VERSION = ${getPLC1ModuleFirmwareVersion(buffer)}"
                txtPLC2ModuleFirmwareVersion.text = "PLC2 Module FIRMWARE VERSION = ${getPLC2ModuleFirmwareVersion(buffer)}"
                txtChargerSerialID.text = "CHARGER SERIAL ID = ${getChargerSerialID(buffer)}"
                txtEthernetMacAddress.text = "Ethernet MAC Address = ${getEthernetMacAddress(buffer)}"
                txtBluetoohMacAddress.text = "Bluetooth MAC Address = ${getBluetoothMacAddress(buffer)}"
                txtWifiStationModeMacAddress.text = "Wifi Station Mode MAC Address = ${getWifiStationModeMacAddress(buffer)}"
                txtWifiAPModeMacAddress.text = "Wifi AP Mode MAC Address = ${getWifiAPModeMacAddress(buffer)}"
                txtRFIDNumber.text = "RFID NUMBER = ${getRFIDNumber(buffer)}"
            }
        }

    }

    private fun getNetworkModuleStatus(response: ByteArray): String {
        val statusNumber = getActualIntValueFromHighAndLowBytes(response[3].getIntValueFromByte(), response[4].getIntValueFromByte())
        return statusNumber.toString()
    }

    private fun getAmbientTemperature(response: ByteArray): String {
        val floatBytes = response.copyOfRange(5, 9)
        return "${ModbusTypeConverter.byteArrayToFloat(floatBytes)} c"
    }

    private fun getMCUFirmwareVersion(response: ByteArray):String {
        val reg3MSB = response[9].getIntValueFromByte()
        val reg3LSB = response[10].getIntValueFromByte()
        val reg4MSB = response[11].getIntValueFromByte()
        val reg4LSB = response[12].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getOCPPFirmwareVersion(response: ByteArray):String {
        val reg3MSB = response[65].getIntValueFromByte()
        val reg3LSB = response[66].getIntValueFromByte()
        val reg4MSB = response[67].getIntValueFromByte()
        val reg4LSB = response[68].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getRFIDFirmwareVersion(response: ByteArray):String {
        val reg3MSB = response[75].getIntValueFromByte()
        val reg3LSB = response[76].getIntValueFromByte()
        val reg4MSB = response[77].getIntValueFromByte()
        val reg4LSB = response[78].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getLEDModuleFirmwareVersion(response: ByteArray):String {
        val reg3MSB = response[79].getIntValueFromByte()
        val reg3LSB = response[80].getIntValueFromByte()
        val reg4MSB = response[81].getIntValueFromByte()
        val reg4LSB = response[82].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getPLC1ModuleFirmwareVersion(response: ByteArray):String {
        val reg3MSB = response[83].getIntValueFromByte()
        val reg3LSB = response[84].getIntValueFromByte()
        val reg4MSB = response[85].getIntValueFromByte()
        val reg4LSB = response[86].getIntValueFromByte()

        return "$reg3MSB.$reg4LSB.$reg4MSB"
    }

    private fun getPLC2ModuleFirmwareVersion(response: ByteArray):String {
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
        val macAddressArray = response.copyOfRange(129, 129+6)
        return getSwappedMacAddress(macAddressArray)
    }

    private fun getBluetoothMacAddress(response: ByteArray): String {
        val macAddressArray = response.copyOfRange(135, 135+6)
        return getSwappedMacAddress(macAddressArray)
    }

    private fun getWifiStationModeMacAddress(response: ByteArray): String {
        //141-142 143-144 145-146
        val macAddressArray = response.copyOfRange(141, 141+6)
        return getSwappedMacAddress(macAddressArray)
    }

    private fun getWifiAPModeMacAddress(response: ByteArray): String {
        //147-148 149-150 151-152
        val macAddressArray = response.copyOfRange(147, 147+6)
        return getSwappedMacAddress(macAddressArray)
    }

    private fun getPLC1Fault(response: ByteArray): String {
        val reg5MSB = response[13].getIntValueFromByte()
        val reg5LSB = response[14].getIntValueFromByte()

        val reg6MSB = response[15].getIntValueFromByte()
        val reg6LSB = response[16].getIntValueFromByte()

        val reg7MSB = response[17].getIntValueFromByte()
        val reg7LSB = response[18].getIntValueFromByte()

        val reg8MSB = response[19].getIntValueFromByte()
        val reg8LSB = response[20].getIntValueFromByte()
        return "$reg5MSB-$reg5LSB . $reg6MSB-$reg6LSB . $reg7MSB-$reg7LSB . $reg8MSB-$reg8LSB"
    }

    private fun getPLC2Fault(response: ByteArray): String {
        val reg5MSB = response[21].getIntValueFromByte()
        val reg5LSB = response[22].getIntValueFromByte()

        val reg6MSB = response[23].getIntValueFromByte()
        val reg6LSB = response[24].getIntValueFromByte()

        val reg7MSB = response[25].getIntValueFromByte()
        val reg7LSB = response[26].getIntValueFromByte()

        val reg8MSB = response[27].getIntValueFromByte()
        val reg8LSB = response[28].getIntValueFromByte()
        return "$reg5MSB-$reg5LSB . $reg6MSB-$reg6LSB . $reg7MSB-$reg7LSB . $reg8MSB-$reg8LSB"
    }

    private fun getRFIDNumber(response: ByteArray): String {
        val macAddressArray = response.copyOfRange(51, 51+8)
        return getSimpleMacAddress(macAddressArray)
    }

    override fun onPause() {
        super.onPause()
        observer.stopObserving()
    }
}