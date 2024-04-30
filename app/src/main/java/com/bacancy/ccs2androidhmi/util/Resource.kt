package com.bacancy.ccs2androidhmi.util

import org.eclipse.paho.client.mqttv3.MqttMessage

sealed class Resource<out T> {
    data class Loading(val message: String = "Loading...") : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data class IncomingMessage(val topic: String?, val message: MqttMessage?): Resource<Nothing>()
    data class DeliveryComplete<T>(val data: T): Resource<T>()

}
