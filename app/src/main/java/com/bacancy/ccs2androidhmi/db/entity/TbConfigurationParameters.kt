package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bacancy.ccs2androidhmi.db.model.ACMeterUserDefinedFields
import com.bacancy.ccs2androidhmi.db.model.DCMeterUserDefinedFields

@Entity(tableName = "tbConfigurationParameters")
data class TbConfigurationParameters(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var chargeControlMode: Int,
    var selectedRectifier: Int,
    var numberOfRectifierPerGroup: Int,
    var maxDCOutputPowerCapacity: Int,
    var rectifierMaxPower: Int,
    var rectifierMaxVoltage: Int,
    var rectifierMaxCurrent: Int,
    var selectedACMeter: Int,
    var acMeterDataConfiguration: String,
    var isACMeterMandatory: Int,
    var selectedDCMeter: Int,
    var dcMeterDataConfiguration: String,
    var isDCMeterMandatory: Int,
    var spdFaultDetection: Int,
    var smokeFaultDetection: Int,
    var tamperFaultDetection: Int,
    var ledModuleFaultDetection: Int,
    var gunTempFaultDetection: Int,
    var isolationFaultDetection: Int,
    var gunTemperatureThresholdValue: Int,
    var phaseLowDetectionVoltage: Int,
    var phaseHighDetectionVoltage: Int,
    var acMeterUserDefinedFields: ACMeterUserDefinedFields?,
    var dcMeterUserDefinedFields: DCMeterUserDefinedFields?
)
