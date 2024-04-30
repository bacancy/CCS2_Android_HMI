package com.bacancy.ccs2androidhmi.mqtt.models

import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.CONNECTOR_STATUS_ID

data class ConnectorStatusBody(
    val connectorId: Int,
    val connectorStatus: String,
    val deviceMacAddress: String,
    val id: String = CONNECTOR_STATUS_ID
)