package com.bacancy.ccs2androidhmi.db.model

data class ACMeterUserDefinedFields(
    val voltageV1N: String,
    val voltageV2N: String,
    val voltageV3N: String,
    val avgVoltageLN: String,
    val frequency: String,
    val avgPF: String,
    val currentL1: String,
    val currentL2: String,
    val currentL3: String,
    val avgCurrent: String,
    val activePower: String,
    val totalEnergy: String,
    val totalReactiveEnergy: String
)