package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbGunsLastChargingSummary")
data class TbGunsLastChargingSummary(
    @PrimaryKey(autoGenerate = false)
    var gunId: Int? = null,
    var evMacAddress: String,
    var chargingDuration: String,
    var chargingStartDateTime: String,
    var chargingEndDateTime: String,
    var startSoc: String,
    var endSoc: String,
    var energyConsumption: String,
    var sessionEndReason: String
)
