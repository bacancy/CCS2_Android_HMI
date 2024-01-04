package com.bacancy.ccs2androidhmi.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bacancy.ccs2androidhmi.db.dao.AppDao
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo

@Database(
    entities = [TbChargingHistory::class, TbAcMeterInfo::class, TbMiscInfo::class, TbGunsDcMeterInfo::class, TbGunsChargingInfo::class, TbGunsLastChargingSummary::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

}