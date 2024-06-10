package com.bacancy.ccs2androidhmi.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bacancy.ccs2androidhmi.db.dao.AppDao
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbErrorCodes
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.db.entity.TbNotifications

@Database(
    entities = [TbChargingHistory::class, TbAcMeterInfo::class, TbMiscInfo::class, TbGunsDcMeterInfo::class, TbGunsChargingInfo::class, TbGunsLastChargingSummary::class,
               TbErrorCodes::class, TbNotifications::class],
    version = 6
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
                db.execSQL("CREATE TABLE IF NOT EXISTS `tbErrorCodes` (`sourceId` INTEGER NOT NULL, `sourceErrorCodes` TEXT NOT NULL, PRIMARY KEY(`sourceId`))")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tbGunsChargingInfo ADD COLUMN gunTemperatureDCPositive REAL DEFAULT 0.0 NOT NULL")
                db.execSQL("ALTER TABLE tbGunsChargingInfo ADD COLUMN gunTemperatureDCNegative REAL DEFAULT 0.0 NOT NULL")
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tbGunsLastChargingSummary ADD COLUMN totalCost TEXT DEFAULT '' NOT NULL")
            }
        }

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tbGunsChargingInfo ADD COLUMN gunChargingStateToSave TEXT DEFAULT '' NOT NULL")
                db.execSQL("ALTER TABLE tbGunsChargingInfo ADD COLUMN gunChargingStateToShow TEXT DEFAULT '' NOT NULL")
            }
        }


    }

}