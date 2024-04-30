package com.bacancy.ccs2androidhmi.mqtt.models

import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.ACTIVE_DEACTIVE_CHARGER_ID

data class ActiveDeactiveChargerMessageBody(
    val deviceMacAddress: String,
    val id: String = ACTIVE_DEACTIVE_CHARGER_ID,
    val message: String
)