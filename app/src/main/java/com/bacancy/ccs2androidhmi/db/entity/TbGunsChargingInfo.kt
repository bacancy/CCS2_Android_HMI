package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbGunsChargingInfo")
data class TbGunsChargingInfo(
    @PrimaryKey(autoGenerate = true)
    var gunId: Int? = null,
    var gunChargingState: String,
    var initialSoc: Int,
    var chargingSoc: Int,
    var demandVoltage: Int,
    var demandCurrent: Int,
    var chargingVoltage: Int,
    var chargingCurrent: Int,
    var duration: String,
    var energyConsumption: Float
)
