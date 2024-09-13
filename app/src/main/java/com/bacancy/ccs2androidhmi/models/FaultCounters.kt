package com.bacancy.ccs2androidhmi.models

data class FaultCounters(
    val gun1TempCounter: Int = 0,
    val gun2TempCounter: Int = 0,
    val systemTempCounter: Int = 0,
    val mainsLowCounter: Int = 0,
    val mainsHighCounter: Int = 0
)
