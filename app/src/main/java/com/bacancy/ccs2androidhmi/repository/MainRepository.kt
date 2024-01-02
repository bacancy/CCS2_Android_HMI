package com.bacancy.ccs2androidhmi.repository

import androidx.lifecycle.LiveData
import com.bacancy.ccs2androidhmi.db.AppDatabase
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import javax.inject.Inject

class MainRepository @Inject constructor(private val appDatabase: AppDatabase) {

    suspend fun insertChargingSummary(chargingSummary: ChargingSummary){
        appDatabase.appDao().insertSummary(chargingSummary)
    }

    suspend fun getAllChargingSummaries(): List<ChargingSummary> {
        return appDatabase.appDao().getAllChargingSummaries()
    }

    suspend fun getGunsChargingHistory(gunNumber: Int): List<ChargingSummary> {
        return appDatabase.appDao().getGunsChargingHistory(gunNumber)
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
}