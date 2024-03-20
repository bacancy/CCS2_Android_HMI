package com.bacancy.ccs2androidhmi.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.mqtt.MQTTClient
import com.bacancy.ccs2androidhmi.mqtt.ResponseModel
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants.MQTT_PWD
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants.MQTT_USERNAME
import com.bacancy.ccs2androidhmi.util.LogUtils
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

@HiltWorker
class MQTTWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val mqttClient: MQTTClient
) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        if (mqttClient.isConnected().not()) {
            connectToMQTT()
        } else {
            publishMessageToTopic("CHARGER", "HELLO CHARGER")
        }
        return Result.success()
    }

    private fun connectToMQTT() {
        if (context.isInternetConnected()) {
            mqttClient.connect(
                MQTT_USERNAME,
                MQTT_PWD, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        LogUtils.debugLog("MQTTWorker - Connect onSuccess")
                        /*publishMessageToTopic("CCS2", Gson().toJson(SampleModel(2,"Preparing for charging")))*/
                        subscribeTopic("CHARGER")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        LogUtils.errorLog("MQTTWorker - Connect onFailure - ${exception?.printStackTrace()}")
                    }

                }, object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        LogUtils.errorLog("MQTTWorker - Connect Connection Lost")
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        LogUtils.debugLog("MQTTWorker - Connect Data Arrived from Topic=$topic Message=$message")
                        if (topic == "CHARGER") {
                            val messageInModel =
                                Gson().fromJson(message.toString(), ResponseModel::class.java)
                            LogUtils.debugLog("MQTTWorker - MESSAGE IN MODEL = $messageInModel")
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        LogUtils.debugLog("MQTTWorker - Connect Delivery Complete")
                    }
                })
        } else {
            LogUtils.errorLog(context.getString(R.string.msg_internet_connection_unavailable))
        }
    }

    private fun disconnectWithMQTT() {
        if (context.isInternetConnected()) {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect(object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        LogUtils.debugLog("MQTTWorker - Disconnect onSuccess")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        LogUtils.errorLog("MQTTWorker - Disconnect onFailure")
                    }
                })
            }
        } else {
            LogUtils.errorLog(context.getString(R.string.msg_internet_connection_unavailable))
        }
    }

    private fun subscribeTopic(topicName: String) {
        if (context.isInternetConnected()) {
            if (mqttClient.isConnected()) {
                mqttClient.subscribe(topicName, 1, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        LogUtils.debugLog("MQTTWorker - Subscribe onSuccess - $asyncActionToken")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        LogUtils.errorLog("MQTTWorker - Subscribe onFailure")
                    }
                })
            }
        } else {
            LogUtils.errorLog(context.getString(R.string.msg_internet_connection_unavailable))
        }
    }

    private fun unsubscribeTopic(topicName: String) {
        if (context.isInternetConnected()) {
            if (mqttClient.isConnected()) {
                mqttClient.unsubscribe(topicName, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        LogUtils.debugLog("MQTTWorker - UnSubscribe onFailure")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        LogUtils.errorLog("MQTTWorker - UnSubscribe onFailure")
                    }
                })
            }
        } else {
            LogUtils.errorLog(context.getString(R.string.msg_internet_connection_unavailable))
        }
    }

    private fun publishMessageToTopic(topicName: String, message: String) {
        if (context.isInternetConnected()) {
            if (mqttClient.isConnected()) {
                mqttClient.publish(topicName, message, 1, false, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        LogUtils.debugLog("MQTTWorker - Publish onSuccess")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        LogUtils.errorLog("MQTTWorker - Publish onFailure")
                    }
                })
            }
        } else {
            LogUtils.errorLog(context.getString(R.string.msg_internet_connection_unavailable))
        }
    }
}