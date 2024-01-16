package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.CommonTableRowBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentCommunicationFailureBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunicationFailureFragment : BaseFragment() {

    private lateinit var binding: FragmentCommunicationFailureBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunicationFailureBinding.inflate(layoutInflater)
        observeCommunicationFailure()
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_communication_failure)
    }

    override fun setupViews() {
        binding.incTableHeader.tvLabel1.text = getString(R.string.lbl_fault_type_information)
        binding.incTableHeader.tvLabel2.text = getString(R.string.lbl_status)

        val viewDataList = listOf(
            ViewData(
                binding.incPLCCommError,
                getString(R.string.lbl_plc_communication_error),
                null,
                R.color.black
            ),
            ViewData(
                binding.incRectifierCommError,
                getString(R.string.lbl_rectifier_communication_error),
                null,
                R.color.light_trans_sky_blue
            ),
            ViewData(
                binding.incOCPPCommError,
                getString(R.string.lbl_ocpp_communication_error),
                null,
                R.color.black
            ),
            ViewData(
                binding.incModbusMasterCommError,
                getString(R.string.lbl_modbus_master_communication_error),
                null,
                R.color.light_trans_sky_blue
            )
        )

        viewDataList.forEach { data ->
            data.viewBinding.tvRowTitle.text = data.title1

            data.backgroundColorResId?.let {
                data.viewBinding.root.setBackgroundColor(resources.getColor(it, null))
            }
        }
    }

    override fun handleClicks() {}

    data class ViewData(
        val viewBinding: CommonTableRowBinding,
        val title1: String,
        val title2: String?,
        @ColorRes val backgroundColorResId: Int?
    )

    private fun observeCommunicationFailure() {
        appViewModel.latestMiscInfo.observe(requireActivity()) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updateCommErrorUI(latestMiscInfo)
            }
        }
    }

    private fun updateCommErrorUI(latestMiscInfo: TbMiscInfo) {
        val commErrorCode = latestMiscInfo.communicationError

        val errorStatusMapping = mapOf(
            binding.incPLCCommError to commErrorCode[0].toString().toInt(),
            binding.incRectifierCommError to commErrorCode[1].toString().toInt(),
            binding.incOCPPCommError to commErrorCode[2].toString().toInt(),
            binding.incModbusMasterCommError to commErrorCode[3].toString().toInt()
        )

        errorStatusMapping.forEach { (errorView, errorCode) ->
            val resource = if (errorCode == 0) R.drawable.ic_green_dot else R.drawable.ic_red_dot
            errorView.ivStatus.setImageResource(resource)
        }
    }
}