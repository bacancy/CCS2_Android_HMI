package com.bacancy.ccs2androidhmi.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bacancy.ccs2androidhmi.db.dao.AppDao
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary

@Database(entities = [ChargingSummary::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun appDao(): AppDao

}