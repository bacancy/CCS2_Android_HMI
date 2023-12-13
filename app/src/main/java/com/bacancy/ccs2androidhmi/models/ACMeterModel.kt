package com.bacancy.ccs2androidhmi.models

data class ACMeterModel(
    val id: Int,
    val voltageL1: Float,
    val voltageL2: Float,
    val voltageL3: Float,
    val voltageAverage: Float,
    val currentL1: Float,
    val currentL2: Float,
    val currentL3: Float,
    val currentAverage: Float,
    val totalKW: Float,
    val totalKWH: Float,
    val frequency: Float,
    val averagePowerFactor: Float
)
