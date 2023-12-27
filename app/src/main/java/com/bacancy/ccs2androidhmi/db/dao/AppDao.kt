package com.bacancy.ccs2androidhmi.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(chargingSummary: ChargingSummary): Long

    @Query("SELECT * FROM lastChargingSummary ORDER BY summaryId DESC")
    suspend fun getAllChargingSummaries(): List<ChargingSummary>

    @Query("SELECT * FROM lastChargingSummary WHERE gunNumber = :gunNumber ORDER BY summaryId DESC")
    suspend fun getGunsChargingHistory(gunNumber: Int): List<ChargingSummary>
}