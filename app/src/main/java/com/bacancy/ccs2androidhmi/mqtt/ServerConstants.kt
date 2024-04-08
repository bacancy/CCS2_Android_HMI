package com.bacancy.ccs2androidhmi.mqtt

object ServerConstants {

   // const val MQTT_SERVER_URI = "tcp://broker.mqtt.cool:1883"
    const val MQTT_SERVER_URI = "tcp://66.94.110.161:1884"
    const val MQTT_CLIENT_ID = ""
    const val MQTT_USERNAME = "drMYsmO5c7O6DKkS8ra1"
    const val MQTT_PWD = "fdufUPnVWbLoMwwFaLre3"

   // const val TOPIC_A_TO_B = "UPS/1133557799/aTOb"
    /*const val TOPIC_A_TO_B = "bacancy-technology/e-commissioning/device/info"
    const val TOPIC_B_TO_A = "UPS/1133557799/bTOa"*/

    const val TOPIC_A_TO_B = "bt/e-comm/D2:D1:D4:D3:D6:D5/status"
    const val TOPIC_B_TO_A = "bt/e-comm/D2:D1:D4:D3:D6:D5/control"

    fun getTOPIC_A_TO_B(devId:String):String{
        return "bt/e-comm/${devId}/status"
    }

    fun getTOPIC_B_TO_A(devId:String):String{
        return "bt/e-comm/${devId}/control"
    }
}