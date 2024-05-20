package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbErrorCodes")
data class TbErrorCodes(
    @PrimaryKey
    var sourceId: Int,
    var sourceErrorCodes: String,
    var sourceErrorDateTime: String,
)
