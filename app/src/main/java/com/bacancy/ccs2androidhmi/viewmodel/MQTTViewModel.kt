package com.bacancy.ccs2androidhmi.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bacancy.ccs2androidhmi.models.ErrorCodes
import com.bacancy.ccs2androidhmi.mqtt.MQTTClient
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants
import com.bacancy.ccs2androidhmi.mqtt.models.ChargerDetailsBody
import com.bacancy.ccs2androidhmi.mqtt.models.ChargingHistoryBody
import com.bacancy.ccs2androidhmi.mqtt.models.ConnectorStatusBody
import com.bacancy.ccs2androidhmi.mqtt.models.FaultErrorsBody
import com.bacancy.ccs2androidhmi.util.CommonUtils.addColonsToMacAddress
import com.bacancy.ccs2androidhmi.util.CommonUtils.toJsonString
import com.bacancy.ccs2androidhmi.util.DateTimeUtils
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.convertDateFormat
import com.bacancy.ccs2androidhmi.util.DateTimeUtils.convertToUtc
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils
import com.bacancy.ccs2androidhmi.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

@HiltViewModel
class MQTTViewModel @Inject constructor(private val mqttClient: MQTTClient) : ViewModel() {

    private val _topicSubscriptionState = MutableStateFlow<Resource<Unit>>(Resource.Loading())
    val topicSubscriptionState: StateFlow<Resource<Unit>> = _topicSubscriptionState

    private val _topicUnSubscriptionState = MutableStateFlow<Resource<Unit>>(Resource.Loading())
    val topicUnSubscriptionState: StateFlow<Resource<Unit>> = _topicUnSubscriptionState

    private val _publishMessageState = MutableStateFlow<Resource<Unit>>(Resource.Loading())
    val publishMessageState: StateFlow<Resource<Unit>> = _publishMessageState

    private val _mqttDisconnectState = MutableStateFlow<Resource<Unit>>(Resource.Loading())
    val mqttDisconnectState: StateFlow<Resource<Unit>> = _mqttDisconnectState

    private val _mqttConnectState = MutableStateFlow<Resource<Unit>>(Resource.Loading())
    val mqttConnectState: StateFlow<Resource<Unit>> = _mqttConnectState

    private val _isMqttConnected = MutableStateFlow(false)
    private val _isChargerActive = MutableStateFlow(true)
    private val _publishMessageRequest = MutableStateFlow("" to "")
    private val _gun1LastChargingStatus = MutableStateFlow("")
    private val _gun2LastChargingStatus = MutableStateFlow("")

    val isMqttConnected = _isMqttConnected.asStateFlow()
    val isChargerActive = _isChargerActive.asStateFlow()
    val publishMessageRequest = _publishMessageRequest.asStateFlow()
    private val gun1LastChargingStatus = _gun1LastChargingStatus.asStateFlow()
    private val gun2LastChargingStatus = _gun2LastChargingStatus.asStateFlow()

    fun setIsMqttConnected(isConnected: Boolean) {
        _isMqttConnected.value = isConnected
    }

    fun setIsChargerActive(isChargerActive: Boolean) {
        _isChargerActive.value = isChargerActive
    }

    fun sendPublishMessageRequest(request: Pair<String, String>) {
        _publishMessageRequest.value = request
    }

    private fun updateGun1LastChargingStatus(lastStatus: String) {
        _gun1LastChargingStatus.value = lastStatus
    }

    private fun updateGun2LastChargingStatus(lastStatus: String) {
        _gun2LastChargingStatus.value = lastStatus
    }

    fun connectToMQTT() {
        _mqttConnectState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            mqttClient.connect(
                ServerConstants.MQTT_USERNAME,
                ServerConstants.MQTT_PWD, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        _mqttConnectState.value = Resource.Success(Unit)
                        setIsMqttConnected(true)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        setIsMqttConnected(false)
                        _mqttConnectState.value =
                            Resource.Error("MQTTWorker - Failed to connect to server")
                    }

                }, object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        setIsMqttConnected(false)
                        _mqttConnectState.value = Resource.Error("MQTTWorker - Connection Lost")
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        Log.i(
                            "checkMessageArrived",
                            "MQTT  ${topic.toString()}  message: ${message.toString()}"
                        )
                        _mqttConnectState.value = Resource.IncomingMessage(topic, message)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        _mqttConnectState.value = Resource.DeliveryComplete(Unit)
                    }
                })
        }

    }

    fun disconnectWithMQTT() {
        _mqttDisconnectState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect(object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        _mqttDisconnectState.value = Resource.Success(Unit)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        _mqttDisconnectState.value = Resource.Error("Unable to disconnect")
                    }
                })
            }
        }
    }

    fun subscribeTopic(topicName: String) {
        _topicSubscriptionState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            if (mqttClient.isConnected()) {
                mqttClient.subscribe(topicName, 1, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        _topicSubscriptionState.value = Resource.Success(Unit)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        _topicSubscriptionState.value =
                            Resource.Error("Failed to subscribe to topic: $topicName")
                    }
                })
            }
        }
    }

    fun unsubscribeTopic(topicName: String) {
        _topicUnSubscriptionState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            if (mqttClient.isConnected()) {
                mqttClient.unsubscribe(topicName, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        _topicUnSubscriptionState.value = Resource.Success(Unit)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        _topicUnSubscriptionState.value =
                            Resource.Error("Failed to unsubscribe to topic: $topicName")
                    }
                })
            }
        }
    }

    fun publishMessageToTopic(topicName: String, message: String) {
        _publishMessageState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            if (mqttClient.isConnected()) {
                mqttClient.publish(topicName, message, 1, false, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        _publishMessageState.value = Resource.Success(Unit)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        _publishMessageState.value =
                            Resource.Error("Failed to publish message: $message to topic: $topicName")
                    }
                })
            }
        }
    }

    fun getInitialChargerDetails(
        devId: String,
        chargerRatings: String,
        chargerOutputs: String,
        unitPrice: String
    ): Pair<String, String> {
        return ServerConstants.getTopicAtoB(devId) to
                ChargerDetailsBody(
                    chargerOutputs = chargerOutputs,
                    chargerRating = "${chargerRatings}KW",
                    configDateTime = DateTimeUtils.getCurrentDateTime()?.convertToUtc().orEmpty(),
                    unitPrice = unitPrice,
                    deviceMacAddress = devId.addColonsToMacAddress()
                ).toJsonString()

    }

    fun convertByteArrayToPublishRequest(
        deviceMacAddress: String,
        connectorId: Int,
        it: ByteArray
    ): Pair<String, String> {
        val chargingHistoryBody = ChargingHistoryBody(
            connectorId = connectorId,
            evMacAddress = LastChargingSummaryUtils.getEVMacAddress(it),
            chargingStartTime = LastChargingSummaryUtils.getChargingStartTime(it)
                .convertDateFormat().convertToUtc().orEmpty(),
            chargingEndTime = LastChargingSummaryUtils.getChargingEndTime(it).convertDateFormat().convertToUtc().orEmpty(),
            totalChargingTime = LastChargingSummaryUtils.getTotalChargingTime(it),
            startSoc = LastChargingSummaryUtils.getStartSoc(it),
            endSoc = LastChargingSummaryUtils.getEndSoc(it),
            energyConsumption = LastChargingSummaryUtils.getEnergyConsumption(it),
            sessionEndReason = LastChargingSummaryUtils.getSessionEndReason(it),
            customSessionEndReason = "",
            totalCost = LastChargingSummaryUtils.getTotalCost(it),
            deviceMacAddress = deviceMacAddress.addColonsToMacAddress()
        )
        return ServerConstants.getTopicAtoB(deviceMacAddress) to chargingHistoryBody.toJsonString()
    }

    fun sendGunStatusToMqtt(
        deviceMacAddress: String,
        selectedGunNumber: Int,
        gunChargingState: String
    ) {
        if (isMqttConnected.value) {
            val lastChargingStatus = if (selectedGunNumber == 1) {
                gun1LastChargingStatus.value
            } else {
                gun2LastChargingStatus.value
            }

            if (lastChargingStatus != gunChargingState) {
                publishMessageToTopic(
                    ServerConstants.getTopicAtoB(
                        deviceMacAddress
                    ),
                    ConnectorStatusBody(
                        connectorId = selectedGunNumber,
                        connectorStatus = gunChargingState,
                        deviceMacAddress = deviceMacAddress.addColonsToMacAddress()
                    ).toJsonString()
                )
                if (selectedGunNumber == 1) {
                    updateGun1LastChargingStatus(gunChargingState)
                } else {
                    updateGun2LastChargingStatus(gunChargingState)
                }
            }
        }
    }

    fun sendErrorToServer(
        deviceMacAddress: String,
        updatedErrorCodesList: MutableList<ErrorCodes>
    ) {
        if(updatedErrorCodesList.isNotEmpty()){
            updatedErrorCodesList.forEach { error ->
                val connectorId = when (error.errorCodeStatus) {
                    "Charger" -> 0
                    "Gun 1" -> 1
                    "Gun 2" -> 2
                    else -> -1
                }
                if (connectorId != -1) {
                    val faultErrorsBody = FaultErrorsBody(
                        connectorId = connectorId,
                        configDateTime = DateTimeUtils.getCurrentDateTime()?.convertToUtc().orEmpty(),
                        errorMessage = error.errorCodeName,
                        deviceMacAddress = deviceMacAddress
                    )
                    publishMessageToTopic(
                        ServerConstants.getTopicAtoB(deviceMacAddress),
                        faultErrorsBody.toJsonString()
                    )
                }
            }
        }
    }
}