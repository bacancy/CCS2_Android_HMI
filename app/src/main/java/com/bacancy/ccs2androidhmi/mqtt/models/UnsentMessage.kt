package com.bacancy.ccs2androidhmi.mqtt.models

data class UnsentMessage(
    val topic: String,
    val message: String
)