package com.bacancy.ccs2androidhmi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbErrorCodes
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {

    val latestAcMeterInfo: LiveData<TbAcMeterInfo> = mainRepository.getLatestAcMeterInfo()

    val latestMiscInfo: LiveData<TbMiscInfo> = mainRepository.getLatestMiscInfo()

    val allErrorCodes: LiveData<List<TbErrorCodes>> = mainRepository.getAllErrorCodes()

    fun getUpdatedGunsChargingInfo(gunNumber: Int): LiveData<TbGunsChargingInfo> =
        mainRepository.getGunsChargingInfoByGunNumber(gunNumber)

    fun getUpdatedGunsDCMeterInfo(gunNumber: Int): Flow<TbGunsDcMeterInfo?> =
        mainRepository.getGunsDCMeterInfoByGunNumber(gunNumber)

    fun getGunsLastChargingSummary(gunNumber: Int): LiveData<TbGunsLastChargingSummary> =
        mainRepository.getGunsLastChargingSummary(gunNumber)

    fun insertChargingSummary(chargingSummary: TbChargingHistory) {
        viewModelScope.launch {
            mainRepository.insertChargingSummary(chargingSummary)
        }
    }

    fun getChargingHistoryByGunNumber(gunNumber: Int) = mainRepository.getGunsChargingHistory(gunNumber)

    fun deleteChargingHistoryByGunId(gunNumber: Int) {
        viewModelScope.launch {
            mainRepository.deleteChargingHistoryByGunId(gunNumber)
        }
    }

    fun insertAcMeterInfo(acMeterInfo: TbAcMeterInfo) {
        viewModelScope.launch {
            mainRepository.insertAcMeterInfo(acMeterInfo)
        }
    }

    fun insertMiscInfo(tbMiscInfo: TbMiscInfo) {
        viewModelScope.launch {
            mainRepository.insertMiscInfo(tbMiscInfo)
        }
    }

    fun insertGunsChargingInfo(tbGunsChargingInfo: TbGunsChargingInfo) {
        viewModelScope.launch {
            mainRepository.insertGunsChargingInfo(tbGunsChargingInfo)
        }
    }

    fun insertGunsDCMeterInfo(tbGunsDcMeterInfo: TbGunsDcMeterInfo) {
        viewModelScope.launch {
            mainRepository.insertGunsDCMeterInfo(tbGunsDcMeterInfo)
        }
    }

    fun insertGunsLastChargingSummary(tbGunsLastChargingSummary: TbGunsLastChargingSummary) {
        viewModelScope.launch {
            mainRepository.insertGunsLastChargingSummary(tbGunsLastChargingSummary)
        }
    }

    fun insertErrorCode(tbErrorCodes: TbErrorCodes) {
        viewModelScope.launch {
            mainRepository.insertErrorCode(tbErrorCodes)
        }
    }

}