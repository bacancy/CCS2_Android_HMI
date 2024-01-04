package com.bacancy.ccs2androidhmi.views

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityCheckAllChargingSummariesBinding
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.adapters.ChargingSummaryListAdapter
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckAllChargingSummariesActivity : SerialPortBaseActivity() {

    private lateinit var summaryAdapter: ChargingSummaryListAdapter
    private lateinit var binding: ActivityCheckAllChargingSummariesBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckAllChargingSummariesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Charging Summary"
        setupList()
        appViewModel.getChargingHistoryByGunNumber(1)
        getAllChargingSummaries()
    }

    private fun setupList() {
        summaryAdapter = ChargingSummaryListAdapter{}
        binding.rvChargingSummary.apply {
            adapter = summaryAdapter
            layoutManager = LinearLayoutManager(this@CheckAllChargingSummariesActivity)
        }
    }

    private fun getAllChargingSummaries() {

        lifecycleScope.launch {

            appViewModel.chargingSummariesList.observe(this@CheckAllChargingSummariesActivity){

                Log.d("TAG", "getAllChargingSummaries: ${Gson().toJson(it)}")
                summaryAdapter.submitList(it)

            }

        }

    }
}