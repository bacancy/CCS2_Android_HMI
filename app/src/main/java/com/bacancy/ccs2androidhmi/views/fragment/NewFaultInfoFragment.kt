package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentNewFaultInformationBinding
import com.bacancy.ccs2androidhmi.models.ErrorCodes
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.adapters.ErrorCodesListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewFaultInfoFragment : BaseFragment() {

    private lateinit var chargingHistoryAdapter: ErrorCodesListAdapter
    private lateinit var binding: FragmentNewFaultInformationBinding
    private val appViewModel: AppViewModel by viewModels()
    val abnormalErrorCodesList = mutableListOf<StateAndModesUtils.GunsErrorCode>()
    val finalErrorCodesList = mutableListOf<ErrorCodes>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewFaultInformationBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()

        return binding.root
    }

    private fun observeGunsInfo() {
        appViewModel.getUpdatedGunsChargingInfo(1).observe(requireActivity()) {
            it?.let {
                Log.i(
                    "NEWFAULT",
                    "observeMiscInfo: Gun1 Error Code - ${getAbnormalErrorCodesList(it.gunsErrorCodes)}"
                )
                abnormalErrorCodesList.addAll(getAbnormalErrorCodesList(it.gunsErrorCodes))
                chargingHistoryAdapter.submitList(changeToFinalList(abnormalErrorCodesList))
            }
        }

        appViewModel.getUpdatedGunsChargingInfo(2).observe(requireActivity()) {
            it?.let {
                Log.i(
                    "NEWFAULT",
                    "observeMiscInfo: Gun2 Error Code - ${getAbnormalErrorCodesList(it.gunsErrorCodes)}"
                )
                abnormalErrorCodesList.addAll(getAbnormalErrorCodesList(it.gunsErrorCodes))
                chargingHistoryAdapter.submitList(changeToFinalList(abnormalErrorCodesList))
            }
        }

    }

    private fun changeToFinalList(abnormalErrorCodesList: MutableList<StateAndModesUtils.GunsErrorCode>): MutableList<ErrorCodes>? {
        val newErrorCodesList = mutableListOf<ErrorCodes>()
        Log.i("TAG", "changeToFinalList: AbnormalList $abnormalErrorCodesList")
        abnormalErrorCodesList.forEachIndexed { index, gunsErrorCode ->
            newErrorCodesList.add(ErrorCodes(index, gunsErrorCode.name, "Abnormal"))
        }
        Log.i("TAG", "changeToFinalList: NewList $newErrorCodesList")
        return newErrorCodesList
    }

    private fun observeMiscInfo() {
        appViewModel.latestMiscInfo.observe(requireActivity()) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                Log.i(
                    "NEWFAULT",
                    "observeMiscInfo: Charger Error Code - ${
                        getAbnormalErrorCodesList(
                            latestMiscInfo.chargerErrorCodes
                        )
                    }"
                )
                abnormalErrorCodesList.addAll(getAbnormalErrorCodesList(latestMiscInfo.chargerErrorCodes))
                chargingHistoryAdapter.submitList(changeToFinalList(abnormalErrorCodesList))
            }
        }
    }

    private fun getAbnormalErrorCodesList(errorCodeString: String): List<StateAndModesUtils.GunsErrorCode> {

        // Reverse the string so that the LSB (Least Significant Bit) corresponds to the first index
        val reversedString = errorCodeString.reversed()

        val abnormalErrors = mutableListOf<StateAndModesUtils.GunsErrorCode>()
        val normalErrors = mutableListOf<StateAndModesUtils.GunsErrorCode>()

        for (index in StateAndModesUtils.GunsErrorCode.values().indices) {
            val char = if (index < reversedString.length) reversedString[index] else '0'
            val errorCode = StateAndModesUtils.GunsErrorCode.values()[index]
            if (char == '1') {
                abnormalErrors.add(errorCode)
            } else {
                normalErrors.add(errorCode)
            }
        }

        //allErrors.filter { it -> it in abnormalErrors }.forEach { println(it) }
        //allErrors.filter { it -> it in normalErrors }.forEach { println(it) }
        return abnormalErrors
    }

    private fun getSampleErrorCodesList(): MutableList<ErrorCodes> {
        val historyList = mutableListOf<ErrorCodes>()
        for (i in 1..5) {
            val chargingSummary = ErrorCodes(
                id = i,
                errorCodeName = "PLC_COMM_FAIL",
                errorCodeStatus = "Abnormal"
            )
            historyList.add(chargingSummary)
        }
        return historyList
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = getString(R.string.lbl_fault_information)
        }
    }

    override fun setupViews() {
        chargingHistoryAdapter = ErrorCodesListAdapter {}
        binding.apply {
            rvVendorErrorCodeInfo.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = chargingHistoryAdapter
            }
        }
        observeMiscInfo()
        observeGunsInfo()
    }

    override fun handleClicks() {}

}