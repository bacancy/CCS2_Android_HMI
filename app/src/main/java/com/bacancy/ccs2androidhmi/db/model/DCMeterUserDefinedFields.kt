package com.bacancy.ccs2androidhmi.db.model

data class DCMeterUserDefinedFields(
    val voltageParameter: Int = 0,
    val currentParameter: Int = 0,
    val powerParameter: Int = 0,
    val importEnergyParameter: Int = 0,
    val exportEnergyParameter: Int = 0,
    val maxVoltageParameter: Int = 0,
    val minVoltageParameter: Int = 0,
    val maxCurrent: Int = 0,
    val minCurrent: Int = 0
)
