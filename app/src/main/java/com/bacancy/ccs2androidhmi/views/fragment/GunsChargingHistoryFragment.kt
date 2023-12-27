package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsChargingHistoryBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.ChargingSummary
import com.bacancy.ccs2androidhmi.util.LastChargingSummaryUtils
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.adapters.ChargingHistoryListAdapter
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GunsChargingHistoryFragment : BaseFragment() {

    private lateinit var chargingHistoryAdapter: ChargingHistoryListAdapter
    private lateinit var binding: FragmentGunsChargingHistoryBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsChargingHistoryBinding.inflate(layoutInflater)
        setScreenHeaderViews()
        setupViews()
        return binding.root
    }

    private fun getSampleHistory(): MutableList<ChargingSummary> {
        val historyList = mutableListOf<ChargingSummary>()
        for (i in 1..5) {
            val chargingSummary = ChargingSummary(
                summaryId = i,
                gunNumber = 1,
                evMacAddress = "00-00-00-01-87-OF-66-30",
                chargingStartTime = "27/12/2023 15:50:10",
                chargingEndTime = "27/12/2023 15:55:10",
                totalChargingTime = "5",
                startSoc = "50",
                endSoc = "85",
                energyConsumption = "15.60",
                sessionEndReason = "Emergency",
                customSessionEndReason = "",
                totalCost = ""
            )
            historyList.add(chargingSummary)
        }
        return historyList
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
            incHeader.tvSubHeader.text = getString(R.string.lbl_charging_history)
            incHeader.tvSubHeader.visible()
        }
    }

    override fun setupViews() {
        chargingHistoryAdapter = ChargingHistoryListAdapter {}
        binding.apply {
            rvChargingHistory.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = chargingHistoryAdapter
            }
        }
        chargingHistoryAdapter.submitList(getSampleHistory())
        //getAllChargingHistory()
    }

    private fun getAllChargingHistory() {

        lifecycleScope.launch {

            appViewModel.chargingSummariesList.observe(requireActivity()){
                Log.d("TAG", "getAllChargingSummaries: ${Gson().toJson(it)}")
                chargingHistoryAdapter.submitList(it)

            }

        }

    }
}