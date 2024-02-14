package com.bacancy.ccs2androidhmi.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bacancy.ccs2androidhmi.db.dao.AppDao
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo

@Database(
    entities = [TbChargingHistory::class, TbAcMeterInfo::class, TbMiscInfo::class, TbGunsDcMeterInfo::class, TbGunsChargingInfo::class, TbGunsLastChargingSummary::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tbMiscInfo ADD COLUMN rfidTagState TEXT DEFAULT '' NOT NULL")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tbMiscInfo ADD COLUMN chargerErrorCodes TEXT DEFAULT '' NOT NULL")
                db.execSQL("ALTER TABLE tbGunsChargingInfo ADD COLUMN gunsErrorCodes TEXT DEFAULT '' NOT NULL")
            }
        }

    }

}