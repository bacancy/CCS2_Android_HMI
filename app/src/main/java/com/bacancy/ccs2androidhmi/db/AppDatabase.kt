package com.bacancy.ccs2androidhmi.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bacancy.ccs2androidhmi.db.dao.AppDao
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo

@Database(entities = [ChargingSummary::class, TbAcMeterInfo::class, TbMiscInfo::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun appDao(): AppDao

}