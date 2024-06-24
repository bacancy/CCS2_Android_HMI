package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbConfigurationParameters")
data class TbConfigurationParameters(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var chargeControlMode: Int,
    var selectedRectifier: Int,
    var numberOfRectifierPerGroup: Int,
    var rectifierMaxPower: Int,
    var rectifierMaxVoltage: Int,
    var rectifierMaxCurrent: Int,
    var selectedACMeter: Int,
    var isACMeterMandatory: Int,
    var selectedDCMeter: Int,
    var isDCMeterMandatory: Int,
)
