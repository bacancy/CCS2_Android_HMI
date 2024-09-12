package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbRectifierTemperature")
data class TbRectifierTemperature(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var rectifier1Temp: String,
    var rectifier2Temp: String,
    var rectifier3Temp: String,
    var rectifier4Temp: String,
    var rectifier5Temp: String,
    var rectifier6Temp: String,
    var rectifier7Temp: String,
    var rectifier8Temp: String,
    var rectifier9Temp: String,
    var rectifier10Temp: String,
    var rectifier11Temp: String,
    var rectifier12Temp: String,
    var rectifier13Temp: String,
    var rectifier14Temp: String,
)
