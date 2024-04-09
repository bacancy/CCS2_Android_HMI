package com.bacancy.ccs2androidhmi.mqtt

object ServerConstants {

    // const val MQTT_SERVER_URI = "tcp://broker.mqtt.cool:1883" //Test Server
    const val MQTT_SERVER_URI = "tcp://66.94.110.161:1884"
    const val MQTT_CLIENT_ID = ""
    const val MQTT_USERNAME = "drMYsmO5c7O6DKkS8ra1"
    const val MQTT_PWD = "fdufUPnVWbLoMwwFaLre3"

    //To send data from Android HMI to Backend
    const val TOPIC_A_TO_B = "bt/e-comm/D2D1D4D3D6D5/status"

    //To read data from Backend in Android HMI
    const val TOPIC_B_TO_A = "bt/e-comm/D2D1D4D3D6D5/control"

    fun getTopicAtoB(devId: String): String {
        return "bt/e-comm/${devId}/status"
    }

    fun getTopicBtoA(devId: String): String {
        return "bt/e-comm/${devId}/control"
    }
}