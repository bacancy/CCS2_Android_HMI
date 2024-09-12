package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbRectifierFaults")
data class TbRectifierFaults(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var rectifier5Fault: String,
    var rectifier6Fault: String,
    var rectifier7Fault: String,
    var rectifier8Fault: String,
    var rectifier9Fault: String,
    var rectifier10Fault: String,
    var rectifier11Fault: String,
    var rectifier12Fault: String,
    var rectifier13Fault: String,
    var rectifier14Fault: String,
    var rectifier15Fault: String,
    var rectifier16Fault: String
)
