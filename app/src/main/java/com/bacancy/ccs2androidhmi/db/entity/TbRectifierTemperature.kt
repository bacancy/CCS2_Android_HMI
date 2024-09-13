package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbRectifierTemperature")
data class TbRectifierTemperature(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var rectifier1Temp: Int,
    var rectifier2Temp: Int,
    var rectifier3Temp: Int,
    var rectifier4Temp: Int,
    var rectifier5Temp: Int,
    var rectifier6Temp: Int,
    var rectifier7Temp: Int,
    var rectifier8Temp: Int,
    var rectifier9Temp: Int,
    var rectifier10Temp: Int,
    var rectifier11Temp: Int,
    var rectifier12Temp: Int,
    var rectifier13Temp: Int,
    var rectifier14Temp: Int,
    var rectifier15Temp: Int,
    var rectifier16Temp: Int
)
