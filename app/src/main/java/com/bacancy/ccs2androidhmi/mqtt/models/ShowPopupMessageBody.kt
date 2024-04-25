package com.bacancy.ccs2androidhmi.mqtt.models

import com.bacancy.ccs2androidhmi.mqtt.MQTTUtils.SHOW_POPUP_ID

data class ShowPopupMessageBody(
    val deviceMacAddress: String,
    val dialogDuration: String,
    val dialogMessage: String,
    val dialogType: String,
    val senderFirstName: String,
    val senderLastName: String,
    val senderId: String,
    val id: String = SHOW_POPUP_ID
)