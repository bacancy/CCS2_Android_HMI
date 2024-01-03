package com.bacancy.ccs2androidhmi.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(chargingSummary: ChargingSummary): Long

    @Query("SELECT * FROM lastChargingSummary ORDER BY summaryId DESC")
    suspend fun getAllChargingSummaries(): List<ChargingSummary>

    @Query("SELECT * FROM lastChargingSummary WHERE gunNumber = :gunNumber ORDER BY summaryId DESC")
    suspend fun getGunsChargingHistory(gunNumber: Int): List<ChargingSummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcMeterInfo(tbAcMeterInfo: TbAcMeterInfo): Long

    @Query("SELECT * FROM tbAcMeterInfo WHERE id = 1")
    fun getLatestAcMeterInfo(): LiveData<TbAcMeterInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMiscInfo(tbMiscInfo: TbMiscInfo): Long

    @Query("SELECT * FROM TbMiscInfo WHERE id = 1")
    fun getLatestMiscInfo(): LiveData<TbMiscInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGunChargingInfo(tbGunsChargingInfo: TbGunsChargingInfo): Long

    @Query("SELECT * FROM tbGunsChargingInfo WHERE gunId = :gunNumber")
    fun getGunsChargingInfo(gunNumber: Int): LiveData<TbGunsChargingInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGunsDCMeterInfo(tbGunsDcMeterInfo: TbGunsDcMeterInfo): Long

    @Query("SELECT * FROM tbDcMeterInfo WHERE gunId = :gunNumber")
    fun getGunsDCMeterInfo(gunNumber: Int): LiveData<TbGunsDcMeterInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGunsLastChargingSummary(tbGunsLastChargingSummary: TbGunsLastChargingSummary): Long

    @Query("SELECT * FROM tbGunsLastChargingSummary WHERE gunId = :gunNumber")
    fun getGunsLastChargingSummary(gunNumber: Int): LiveData<TbGunsLastChargingSummary>

}