package com.bacancy.ccs2androidhmi.models

data class ChargingHistoryDomainModel(
    var summaryId: Int? = null,
    var gunNumber: Int,
    var evMacAddress: String,
    var chargingStartTime: String,
    var chargingEndTime: String,
    var totalChargingTime: String,
    var startSoc: String,
    var endSoc: String,
    var energyConsumption: String,
    var sessionEndReason: String,
    var customSessionEndReason: String,
    var totalCost: String
)