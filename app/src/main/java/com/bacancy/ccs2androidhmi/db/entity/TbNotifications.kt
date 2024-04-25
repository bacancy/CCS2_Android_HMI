package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbNotifications")
data class TbNotifications(
    @PrimaryKey(autoGenerate = true)
    var notificationId: Int? = null,
    var notificationMessage: String,
    var notificationReceiveTime: String
)
