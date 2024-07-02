package com.bacancy.ccs2androidhmi.db.model

data class DCMeterUserDefinedFields(
    val voltageParameter: String,
    val currentParameter: String,
    val powerParameter: String,
    val importEnergyParameter: String,
    val exportEnergyParameter: String,
    val maxVoltageParameter: String,
    val minVoltageParameter: String,
    val maxCurrent: String,
    val minCurrent: String
)
