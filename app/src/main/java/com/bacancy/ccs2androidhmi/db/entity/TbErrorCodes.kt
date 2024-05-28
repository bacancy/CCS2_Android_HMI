package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbErrorCodes")
data class TbErrorCodes(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var sourceId: Int,//0,1,2
    var sourceErrorCodes: String,//EMG_PRESSED, etc
    var sourceErrorValue: Int,//1,0
    var sourceErrorDateTime: String,//2021-09-01 12:00:00
)
