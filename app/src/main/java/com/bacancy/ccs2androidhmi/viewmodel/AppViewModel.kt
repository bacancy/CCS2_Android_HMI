package com.bacancy.ccs2androidhmi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbAcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsDcMeterInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {

    private val _chargingSummariesList = MutableLiveData<List<ChargingSummary>>()
    val chargingSummariesList: LiveData<List<ChargingSummary>> = _chargingSummariesList

    val latestAcMeterInfo: LiveData<TbAcMeterInfo> = mainRepository.getLatestAcMeterInfo()
    val latestMiscInfo: LiveData<TbMiscInfo> = mainRepository.getLatestMiscInfo()
    fun getUpdatedGunsChargingInfo(gunNumber: Int): LiveData<TbGunsChargingInfo> =
        mainRepository.getGunsChargingInfoByGunNumber(gunNumber)

    fun getUpdatedGunsDCMeterInfo(gunNumber: Int): LiveData<TbGunsDcMeterInfo> =
        mainRepository.getGunsDCMeterInfoByGunNumber(gunNumber)

    fun getGunsLastChargingSummary(gunNumber: Int): LiveData<TbGunsLastChargingSummary> =
        mainRepository.getGunsLastChargingSummary(gunNumber)

    fun insertChargingSummary(chargingSummary: ChargingSummary) {
        viewModelScope.launch {
            mainRepository.insertChargingSummary(chargingSummary)
        }
    }

    fun getAllChargingSummaries() {
        viewModelScope.launch {
            _chargingSummariesList.value = mainRepository.getAllChargingSummaries()
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

}