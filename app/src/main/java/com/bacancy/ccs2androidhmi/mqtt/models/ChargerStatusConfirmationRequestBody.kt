package com.bacancy.ccs2androidhmi.mqtt.models

import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.CHARGER_STATUS_CONFIRMATION_ID

data class ChargerStatusConfirmationRequestBody(
    val deviceMacAddress: String,
    val id: String = CHARGER_STATUS_CONFIRMATION_ID,
    val message: String,
    val statusDateTime: String
)