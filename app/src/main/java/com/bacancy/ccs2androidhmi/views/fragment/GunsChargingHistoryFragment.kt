package com.bacancy.ccs2androidhmi.views.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.bacancy.ccs2androidhmi.models.ChargingHistoryDomainModel
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_EXPORT_CHARGING_HISTORY
import com.bacancy.ccs2androidhmi.util.CSVExporter.exportCSVInCustomDirectory
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomDialogForAreYouSure
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
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
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        selectedGunNumber = arguments?.getInt(SELECTED_GUN)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleViewsVisibility()
    }

    private fun handleViewsVisibility(hasHistoryData: Boolean = false) {
        binding.apply {
            if (SHOW_EXPORT_CHARGING_HISTORY && hasHistoryData) {
                ivExportChargingHistory.gone()
            } else {
                ivExportChargingHistory.gone()
            }
        }
    }

    private fun getSampleHistory(): MutableList<ChargingHistoryDomainModel> {
        val historyList = mutableListOf<ChargingHistoryDomainModel>()
        for (i in 10..1) {
            val chargingSummary = ChargingHistoryDomainModel(
                summaryId = i,
                gunNumber = if (i % 2 == 0) 1 else 2,
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
        getAllChargingHistory()
    }

    private fun getAllChargingHistory() {
        lifecycleScope.launch {
            appViewModel.getChargingHistoryByGunNumber(selectedGunNumber).collect {
                if (it.isNotEmpty()) {
                    binding.rvChargingHistory.visible()
                    binding.tvNoDataFound.gone()
                    val updatedList = it.mapIndexed { index, tbChargingHistory ->
                        ChargingHistoryDomainModel(
                            summaryId = index + 1,
                            gunNumber = tbChargingHistory.gunNumber,
                            evMacAddress = tbChargingHistory.evMacAddress,
                            chargingStartTime = tbChargingHistory.chargingStartTime,
                            chargingEndTime = tbChargingHistory.chargingEndTime,
                            totalChargingTime = tbChargingHistory.totalChargingTime,
                            startSoc = tbChargingHistory.startSoc,
                            endSoc = tbChargingHistory.endSoc,
                            energyConsumption = tbChargingHistory.energyConsumption,
                            sessionEndReason = tbChargingHistory.sessionEndReason,
                            customSessionEndReason = tbChargingHistory.customSessionEndReason,
                            totalCost = tbChargingHistory.totalCost
                        )
                    }
                    chargingHistoryAdapter.submitList(updatedList)
                } else {
                    binding.rvChargingHistory.gone()
                    binding.tvNoDataFound.visible()
                }
                handleViewsVisibility(it.isNotEmpty())
            }
        }
    }

    private val storageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedFolderUri = result.data?.data ?: return@registerForActivityResult
            lifecycleScope.launch {
                requireContext().exportCSVInCustomDirectory(
                    chargingHistoryAdapter.currentList,
                    selectedFolderUri
                ) { message ->
                    when (message) {
                        getString(R.string.msg_file_saved_successfully) -> {
                            requireActivity().showCustomDialogForAreYouSure(getString(R.string.msg_are_you_sure_to_delete),isCancelable = false,
                                {
                                    //Yes - Delete charging history from local DB for selected gun
                                    appViewModel.deleteChargingHistoryByGunId(selectedGunNumber)
                                },
                                {
                                    //No - Do nothing and close the dialog
                                })
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun openFoldersStructure() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
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