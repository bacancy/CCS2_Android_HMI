package com.bacancy.ccs2androidhmi.db.model

data class ACMeterUserDefinedFields(
    val voltageV1N: Int = 0,
    val voltageV2N: Int = 0,
    val voltageV3N: Int = 0,
    val avgVoltageLN: Int = 0,
    val frequency: Int = 0,
    val avgPF: Int = 0,
    val currentL1: Int = 0,
    val currentL2: Int = 0,
    val currentL3: Int = 0,
    val avgCurrent: Int = 0,
    val activePower: Int = 0,
    val totalEnergy: Int = 0,
    val totalReactiveEnergy: Int = 0
)