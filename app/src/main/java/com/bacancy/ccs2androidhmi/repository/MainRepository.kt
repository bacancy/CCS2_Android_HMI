package com.bacancy.ccs2androidhmi.repository

import androidx.lifecycle.LiveData
import com.bacancy.ccs2androidhmi.db.AppDatabase
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbErrorCodes
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.db.entity.TbNotifications
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepository @Inject constructor(private val appDatabase: AppDatabase) {

    suspend fun insertChargingSummary(chargingSummary: TbChargingHistory){
        appDatabase.appDao().insertSummary(chargingSummary)
    }

    suspend fun getAllChargingSummaries(): List<TbChargingHistory> {
        return appDatabase.appDao().getAllChargingSummaries()
    }

    fun getGunsChargingHistory(gunNumber: Int): Flow<List<TbChargingHistory>> {
        return appDatabase.appDao().getGunsChargingHistory(gunNumber)
    }

    suspend fun deleteChargingHistoryByGunId(gunNumber: Int) {
        return appDatabase.appDao().deleteChargingHistoryByGunId(gunNumber)
    }

    suspend fun insertAcMeterInfo(acMeterInfo: TbAcMeterInfo){
        appDatabase.appDao().insertAcMeterInfo(acMeterInfo)
    }

    fun getLatestAcMeterInfo(): LiveData<TbAcMeterInfo> {
        return appDatabase.appDao().getLatestAcMeterInfo()
    }

    suspend fun insertMiscInfo(tbMiscInfo: TbMiscInfo){
        appDatabase.appDao().insertMiscInfo(tbMiscInfo)
    }

    fun getLatestMiscInfo(): LiveData<TbMiscInfo> {
        return appDatabase.appDao().getLatestMiscInfo()
    }

    suspend fun insertGunsChargingInfo(tbGunsChargingInfo: TbGunsChargingInfo){
        appDatabase.appDao().insertGunChargingInfo(tbGunsChargingInfo)
    }

    fun getGunsChargingInfoByGunNumber(gunNumber: Int): LiveData<TbGunsChargingInfo>{
        return appDatabase.appDao().getGunsChargingInfo(gunNumber)
    }
    suspend fun insertGunsDCMeterInfo(tbGunsDcMeterInfo: TbGunsDcMeterInfo){
        appDatabase.appDao().insertGunsDCMeterInfo(tbGunsDcMeterInfo)
    }

    fun getGunsDCMeterInfoByGunNumber(gunNumber: Int): Flow<TbGunsDcMeterInfo?> {
        return appDatabase.appDao().getGunsDCMeterInfo(gunNumber)
    }

    suspend fun insertGunsLastChargingSummary(tbGunsLastChargingSummary: TbGunsLastChargingSummary){
        appDatabase.appDao().insertGunsLastChargingSummary(tbGunsLastChargingSummary)
    }

    fun getGunsLastChargingSummary(gunNumber: Int): LiveData<TbGunsLastChargingSummary>{
        return appDatabase.appDao().getGunsLastChargingSummary(gunNumber)
    }

    suspend fun insertErrorCode(tbErrorCodes: TbErrorCodes){
        appDatabase.appDao().insertErrorCode(tbErrorCodes)
    }

    fun getAllErrorCodes(): LiveData<List<TbErrorCodes>> {
        return appDatabase.appDao().getAllErrorCodes()
    }

    suspend fun insertNotifications(tbNotifications: TbNotifications){
        appDatabase.appDao().insertNotifications(tbNotifications)
    }

    fun getAllNotifications(): LiveData<List<TbNotifications>> {
        return appDatabase.appDao().getAllNotifications()
    }

}