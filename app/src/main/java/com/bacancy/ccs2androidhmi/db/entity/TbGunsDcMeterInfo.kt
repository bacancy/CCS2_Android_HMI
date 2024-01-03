package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbDcMeterInfo")
data class TbGunsDcMeterInfo(
    @PrimaryKey(autoGenerate = false)
    var gunId: Int? = null,
    var voltage: Float,
    var current: Float,
    var power: Float,
    var importEnergy: Float,
    var exportEnergy: Float,
    var maxVoltage: Float,
    var minVoltage: Float,
    var maxCurrent: Float,
    var minCurrent: Float,
)
