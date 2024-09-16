package com.bacancy.ccs2androidhmi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbFaultCounters")
data class TbFaultCounters(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var gun1TempCounter: Int = 0,
    var gun2TempCounter: Int = 0,
    var systemTempCounter: Int = 0,
    var mainsLowCounter: Int = 0,
    var mainsHighCounter: Int = 0
)