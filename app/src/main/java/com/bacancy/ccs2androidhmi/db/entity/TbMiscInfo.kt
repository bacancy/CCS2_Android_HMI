package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbMiscInfo")
data class TbMiscInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var serverConnectedWith: String,
    var ethernetStatus: String,
    var gsmLevel: Int,
    var wifiLevel: Int,
    var mcuFirmwareVersion: String,
    var ocppFirmwareVersion: String,
    var rfidFirmwareVersion: String,
    var ledFirmwareVersion: String,
    var plc1FirmwareVersion: String,
    var plc2FirmwareVersion: String,
    var plc1Fault: String,
    var plc2Fault: String,
    var rectifier1Fault: String,
    var rectifier2Fault: String,
    var rectifier3Fault: String,
    var rectifier4Fault: String,
    var communicationError: String,
    var devicePhysicalConnectionStatus: String,
    var unitPrice: Float,
    var emergencyButtonStatus: Int
)
