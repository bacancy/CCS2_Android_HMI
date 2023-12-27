package com.bacancy.ccs2androidhmi.repository

import com.bacancy.ccs2androidhmi.db.AppDatabase
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary
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

}