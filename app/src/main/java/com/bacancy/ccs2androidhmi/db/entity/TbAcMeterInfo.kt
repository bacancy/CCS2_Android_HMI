package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbAcMeterInfo")
data class TbAcMeterInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var voltageV1N: Float,
    var voltageV2N: Float,
    var voltageV3N: Float,
    var averageVoltageLN: Float,
    var currentL1: Float,
    var currentL2: Float,
    var currentL3: Float,
    var averageCurrent: Float,
    var frequency: Float,
    var activePower: Float,
    var totalPower: Float
)
