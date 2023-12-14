package com.bacancy.ccs2androidhmi.util

object ModbusRequestFrames {

    fun getGunOneDCMeterInfoRequestFrame(
        startAddress: Int = 50,
        quantity: Int = 18
    ): ByteArray {
        return ModBusUtils.createReadInputRegistersRequest(startAddress, quantity)
    }

    fun getGunTwoDCMeterInfoRequestFrame(
        startAddress: Int = 100,
        quantity: Int = 18
    ): ByteArray {
        return ModBusUtils.createReadInputRegistersRequest(startAddress, quantity)
    }

    fun getACChargerACMeterInfoRequestFrame(
        startAddress: Int = 150,
        quantity: Int = 24
    ): ByteArray {
        return ModBusUtils.createReadInputRegistersRequest(startAddress, quantity)
    }

    fun getACMeterInfoRequestFrame(
        startAddress: Int = 0,
        quantity: Int = 24
    ): ByteArray {
        return ModBusUtils.createReadInputRegistersRequest(startAddress, quantity)
    }

    fun getMiscInfoRequestFrame(
        startAddress: Int = 0,
        quantity: Int = 75
    ): ByteArray {
        return ModBusUtils.createReadHoldingRegistersRequest(startAddress, quantity)
    }

    fun getGun1InfoRequestFrame(
        startAddress: Int = 100,
        quantity: Int = 18
    ): ByteArray {
        return ModBusUtils.createReadHoldingRegistersRequest(startAddress, quantity)
    }

    fun getGun2InfoRequestFrame(
        startAddress: Int = 200,
        quantity: Int = 18
    ): ByteArray {
        return ModBusUtils.createReadHoldingRegistersRequest(startAddress, quantity)
    }

    fun getGun1LastChargingSummaryRequestFrame(
        startAddress: Int = 150,
        quantity: Int = 21
    ): ByteArray {
        return ModBusUtils.createReadHoldingRegistersRequest(startAddress, quantity)
    }

    fun getGun2LastChargingSummaryRequestFrame(
        startAddress: Int = 250,
        quantity: Int = 21
    ): ByteArray {
        return ModBusUtils.createReadHoldingRegistersRequest(startAddress, quantity)
    }

}