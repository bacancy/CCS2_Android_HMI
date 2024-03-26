package com.bacancy.ccs2androidhmi.mqtt.models

import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.CHARGING_HISTORY_ID

data class ChargingHistoryBody(
    val chargingEndTime: String,
    val chargingStartTime: String,
    val connectorId: Int,
    val customSessionEndReason: String,
    val endSoc: String,
    val energyConsumption: String,
    val evMacAddress: String,
    val id: String = CHARGING_HISTORY_ID,
    val sessionEndReason: String,
    val startSoc: String,
    val totalChargingTime: String,
    val totalCost: String
)