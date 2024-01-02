package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbMiscInfo")
data class TbMiscInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var serverConnectedWith: String,
    var ethernetStatus: String,
    var gsmLevel: Int,
    var wifiLevel: Int
)
