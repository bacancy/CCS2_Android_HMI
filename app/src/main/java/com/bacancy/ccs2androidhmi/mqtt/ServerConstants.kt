package com.bacancy.ccs2androidhmi.mqtt

object ServerConstants {
    const val MQTT_TEST_SERVER_URI = "tcp://broker.mqtt.cool:1883"
    const val MQTT_SERVER_URI = "tcp://66.94.110.161:1884"
    const val MQTT_CLIENT_ID = ""
    const val MQTT_USERNAME = "drMYsmO5c7O6DKkS8ra1"
    const val MQTT_PWD = "fdufUPnVWbLoMwwFaLre3"

    fun getTopicAtoB(devId: String) = "bt/e-comm/$devId/status"
    fun getTopicBtoA(devId: String) = "bt/e-comm/$devId/control"
}