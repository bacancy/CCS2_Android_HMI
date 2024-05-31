package com.bacancy.ccs2androidhmi.mqtt.models

import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.FAULT_ERRORS_ID

data class FaultErrorsBody(
    val configDateTime: String,
    val connectorId: Int,
    val errorMessage: String,
    val deviceMacAddress: String,
    val id: String = FAULT_ERRORS_ID
)
