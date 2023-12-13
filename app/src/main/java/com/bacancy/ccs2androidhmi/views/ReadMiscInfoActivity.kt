package com.bacancy.ccs2androidhmi.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityReadMiscInfoBinding
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.bytesToAsciiString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.decimalArrayToHexArray
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.getIntValueFromByte
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import kotlinx.coroutines.Dispatchers
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
        Log.d("TAG", "onCreate: DEC TO HEX = ${
            decimalArrayToHexArray(listOf(224,226, 230, 48, 158, 115)).joinToString(":").uppercase(
                Locale.ROOT
            )
        }")
        startReadingMiscInformation()
    }

    private fun startReadingMiscInformation() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    observer = ModbusReadObserver()
                    observer.startObserving(
                        mOutputStream,
                        mInputStream, 256,
                        ModbusRequestFrames.getMiscInfoRequestFrame()
                    ) { responseFrameArray ->
                        onDataReceived(responseFrameArray)
                    }
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
        Log.d("TAG", "onDataReceived: MCU FIRMWARE VERSION = ${getMCUFirmwareVersion(buffer)}")
        Log.d("TAG", "onDataReceived: OCPP FIRMWARE VERSION = ${getOCPPFirmwareVersion(buffer)}")
        Log.d("TAG", "onDataReceived: RFID FIRMWARE VERSION = ${getRFIDFirmwareVersion(buffer)}")
        Log.d("TAG", "onDataReceived: LED FIRMWARE VERSION = ${getLEDModuleFirmwareVersion(buffer)}")
        Log.d("TAG", "onDataReceived: PLC1 Module FIRMWARE VERSION = ${getPLC1ModuleFirmwareVersion(buffer)}")
        Log.d("TAG", "onDataReceived: PLC2 Module FIRMWARE VERSION = ${getPLC2ModuleFirmwareVersion(buffer)}")
        Log.d("TAG", "onDataReceived: CHARGER SERIAL ID = ${getChargerSerialID(buffer)}")
        Log.d("TAG", "onDataReceived: Ethernet MAC Address = ${getEthernetMacAddress(buffer)}")
        Log.d("TAG", "onDataReceived: Bluetooth MAC Address = ${getBluetoothMacAddress(buffer)}")
        Log.d("TAG", "onDataReceived: Wifi Station Mode MAC Address = ${getWifiStationModeMacAddress(buffer)}")
        Log.d("TAG", "onDataReceived: Wifi AP Mode MAC Address = ${getWifiAPModeMacAddress(buffer)}")
        Log.d("TAG", "onDataReceived: RFID NUMBER = ${getRFIDNumber(buffer)}")

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
        //129-130 131-132 133-134
        val macAddressArray = response.copyOfRange(129, 134)
        Log.d("TAG", "getEthernetMacAddress: ${macAddressArray.toHex()}")
        val address1MSB = response[129].getIntValueFromByte()
        val address1LSB = response[130].getIntValueFromByte()

        val address2MSB = response[131].getIntValueFromByte()
        val address2LSB = response[132].getIntValueFromByte()

        val address3MSB = response[133].getIntValueFromByte()
        val address3LSB = response[134].getIntValueFromByte()
        return decimalArrayToHexArray(listOf(address1LSB,address1MSB, address2LSB, address2MSB, address3LSB, address3MSB)).joinToString(":").uppercase(
            Locale.ROOT
        )
    }

    private fun getBluetoothMacAddress(response: ByteArray): String {
        //135-136 137-138 139-140
        val address1MSB = response[135].getIntValueFromByte()
        val address1LSB = response[136].getIntValueFromByte()

        val address2MSB = response[137].getIntValueFromByte()
        val address2LSB = response[138].getIntValueFromByte()

        val address3MSB = response[139].getIntValueFromByte()
        val address3LSB = response[140].getIntValueFromByte()
        return decimalArrayToHexArray(listOf(address1LSB,address1MSB, address2LSB, address2MSB, address3LSB, address3MSB)).joinToString(":").uppercase(
            Locale.ROOT
        )
    }

    private fun getWifiStationModeMacAddress(response: ByteArray): String {
        //141-142 143-144 145-146
        val address1MSB = response[141].getIntValueFromByte()
        val address1LSB = response[142].getIntValueFromByte()

        val address2MSB = response[143].getIntValueFromByte()
        val address2LSB = response[144].getIntValueFromByte()

        val address3MSB = response[145].getIntValueFromByte()
        val address3LSB = response[146].getIntValueFromByte()
        return decimalArrayToHexArray(listOf(address1LSB,address1MSB, address2LSB, address2MSB, address3LSB, address3MSB)).joinToString(":").uppercase(
            Locale.ROOT
        )
    }

    private fun getWifiAPModeMacAddress(response: ByteArray): String {
        //147-148 149-150 151-152
        val address1MSB = response[147].getIntValueFromByte()
        val address1LSB = response[148].getIntValueFromByte()

        val address2MSB = response[149].getIntValueFromByte()
        val address2LSB = response[150].getIntValueFromByte()

        val address3MSB = response[151].getIntValueFromByte()
        val address3LSB = response[152].getIntValueFromByte()
        return decimalArrayToHexArray(listOf(address1LSB,address1MSB, address2LSB, address2MSB, address3LSB, address3MSB)).joinToString(":").uppercase(
            Locale.ROOT
        )
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
        val address1MSB = response[51].getIntValueFromByte()
        val address1LSB = response[52].getIntValueFromByte()

        val address2MSB = response[53].getIntValueFromByte()
        val address2LSB = response[54].getIntValueFromByte()

        val address3MSB = response[55].getIntValueFromByte()
        val address3LSB = response[56].getIntValueFromByte()

        val address4MSB = response[57].getIntValueFromByte()
        val address4LSB = response[58].getIntValueFromByte()
        return decimalArrayToHexArray(listOf(address1LSB,address1MSB, address2LSB, address2MSB, address3LSB, address3MSB, address4LSB, address4MSB)).joinToString(":").uppercase(
            Locale.ROOT
        )
    }

    override fun onPause() {
        super.onPause()
        observer.stopObserving()
    }
}