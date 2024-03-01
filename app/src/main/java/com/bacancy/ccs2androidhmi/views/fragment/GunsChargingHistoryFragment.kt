package com.bacancy.ccs2androidhmi.views.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsChargingHistoryBinding
import com.bacancy.ccs2androidhmi.db.entity.TbChargingHistory
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_EXPORT_CHARGING_HISTORY
import com.bacancy.ccs2androidhmi.util.CSVExporter.exportCSVInCustomDirectory
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.adapters.ChargingHistoryListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GunsChargingHistoryFragment : BaseFragment() {

    private var selectedGunNumber: Int = 1
    private lateinit var chargingHistoryAdapter: ChargingHistoryListAdapter
    private lateinit var binding: FragmentGunsChargingHistoryBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsChargingHistoryBinding.inflate(layoutInflater)
        selectedGunNumber = arguments?.getInt(SELECTED_GUN)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleViewsVisibility()
    }

    private fun handleViewsVisibility() {
        binding.apply {
            if (SHOW_EXPORT_CHARGING_HISTORY) {
                ivExportChargingHistory.visible()
            } else {
                ivExportChargingHistory.gone()
            }
        }
    }

    private fun getSampleHistory(): MutableList<TbChargingHistory> {
        val historyList = mutableListOf<TbChargingHistory>()
        for (i in 1..5) {
            val chargingSummary = TbChargingHistory(
                summaryId = i,
                gunNumber = 1*i,
                evMacAddress = "00-00-00-02-88-AF-56-39",
                chargingStartTime = "01/03/2024 17:59:10",
                chargingEndTime = "01/03/2024 18:59:10",
                totalChargingTime = "60",
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
            if (selectedGunNumber == 1) {
                incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
            } else {
                incHeader.tvHeader.text = getString(R.string.lbl_gun_2)
            }
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
        //chargingHistoryAdapter.submitList(getSampleHistory())
        appViewModel.getChargingHistoryByGunNumber(selectedGunNumber)
        getAllChargingHistory()
    }

    private fun getAllChargingHistory() {
        lifecycleScope.launch {
            appViewModel.chargingSummariesList.observe(viewLifecycleOwner) {
                Log.d("FRITAG", "getChargingHistory: ${it.size}")
                if (it.isNotEmpty()) {
                    binding.rvChargingHistory.visible()
                    binding.tvNoDataFound.gone()
                    chargingHistoryAdapter.submitList(it)
                } else {
                    binding.rvChargingHistory.gone()
                    binding.tvNoDataFound.visible()
                }
            }
        }
    }

    private val storageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedFolderUri = result.data?.data ?: return@registerForActivityResult
            requireContext().exportCSVInCustomDirectory(chargingHistoryAdapter.currentList, selectedFolderUri)
        }
    }

    private fun openFoldersStructure() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        storageLauncher.launch(intent)
    }

    override fun handleClicks() {
        binding.apply {
            ivExportChargingHistory.setOnClickListener {
                openFoldersStructure()
            }
        }
    }
}