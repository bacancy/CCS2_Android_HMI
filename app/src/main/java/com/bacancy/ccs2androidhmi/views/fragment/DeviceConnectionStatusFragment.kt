package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.CommonTableRowBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentMiscErrorsBinding
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DeviceConnectionStatusFragment : BaseFragment() {

    private lateinit var binding: FragmentMiscErrorsBinding
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMiscErrorsBinding.inflate(layoutInflater)
        observeDevicePhysicalConnectionStatus()
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_device_connection_status)
    }

    override fun setupViews() {
        binding.incTableHeader.tvLabel1.text = getString(R.string.lbl_device_information)
        binding.incTableHeader.tvLabel2.text = getString(R.string.lbl_status)
        val viewDataList = listOf(
            ViewData(
                binding.incRFIDModule,
                getString(R.string.lbl_rfid_module),
                null,
                R.color.inverse_color
            ),
            ViewData(
                binding.incLEDModule,
                getString(R.string.lbl_led_module),
                null,
                R.color.light_trans_sky_blue
            ),
            ViewData(binding.incACMeter, getString(R.string.lbl_ac_meter), null, R.color.inverse_color),
            ViewData(
                binding.incDCMeter1,
                getString(R.string.lbl_dc_meter_1),
                null,
                R.color.light_trans_sky_blue
            ),
            ViewData(binding.incDCMeter2, getString(R.string.lbl_dc_meter_2), null, R.color.inverse_color)
        )

        viewDataList.forEach { data ->
            data.viewBinding.tvRowTitle.text = data.title1

            if (data.backgroundColorResId != null) {
                data.viewBinding.root.setBackgroundColor(
                    resources.getColor(
                        data.backgroundColorResId,
                        null
                    )
                )
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

    private fun observeDevicePhysicalConnectionStatus() {
        appViewModel.latestMiscInfo.observe(viewLifecycleOwner) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updateDeviceStatusUI(latestMiscInfo)
            }
        }
    }

    private fun updateDeviceStatusUI(latestMiscInfo: TbMiscInfo) {
        val deviceConnectionStatus = latestMiscInfo.devicePhysicalConnectionStatus

        val errorStatusMapping = mapOf(
            binding.incRFIDModule to deviceConnectionStatus[0].toString().toInt(),
            binding.incLEDModule to deviceConnectionStatus[1].toString().toInt(),
            binding.incACMeter to deviceConnectionStatus[2].toString().toInt(),
            binding.incDCMeter1 to deviceConnectionStatus[3].toString().toInt(),
            binding.incDCMeter2 to deviceConnectionStatus[4].toString().toInt()
        )

        errorStatusMapping.forEach { (errorView, errorCode) ->
            val resource = if (errorCode == 0) R.drawable.ic_green_dot else R.drawable.ic_red_dot
            errorView.ivStatus.setImageResource(resource)
        }
    }
}