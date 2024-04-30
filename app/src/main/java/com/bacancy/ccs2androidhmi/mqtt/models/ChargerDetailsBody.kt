package com.bacancy.ccs2androidhmi.mqtt.models

import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.CHARGER_DETAILS_ID

data class ChargerDetailsBody(
    val chargerOutputs: String,
    val chargerRating: String,
    val configDateTime: String,
    val unitPrice: String,
    val deviceMacAddress: String,
    val id: String = CHARGER_DETAILS_ID
)