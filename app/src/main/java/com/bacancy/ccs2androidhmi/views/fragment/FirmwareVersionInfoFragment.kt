package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentFirmwareVersionInfoBinding
import com.bacancy.ccs2androidhmi.db.entity.TbMiscInfo
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirmwareVersionInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentFirmwareVersionInfoBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirmwareVersionInfoBinding.inflate(layoutInflater)
        observeFirmwareVersionInfo()
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_firmware_version_information)
    }

    override fun setupViews() {}

    override fun handleClicks() {}

    private fun observeFirmwareVersionInfo() {
        appViewModel.latestMiscInfo.observe(viewLifecycleOwner) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                updateFirmwareVersionUI(latestMiscInfo)
            }
        }
    }

    private fun updateFirmwareVersionUI(latestMiscInfo: TbMiscInfo) {
        binding.apply {
            tvMCUVersion.text = latestMiscInfo.mcuFirmwareVersion
            tvOCPPVersion.text = latestMiscInfo.ocppFirmwareVersion
            tvRFIDVersion.text = latestMiscInfo.rfidFirmwareVersion
            tvLEDVersion.text = latestMiscInfo.ledFirmwareVersion
            tvPLC1Version.text = latestMiscInfo.plc1FirmwareVersion
            tvPLC2Version.text = latestMiscInfo.plc2FirmwareVersion
            tvChargerSerialID.text = getString(R.string.lbl_charger_serial_id, latestMiscInfo.chargerSerialID)
            tvBluetoothMacAddress.text = getString(R.string.lbl_bluetooth_mac_address, latestMiscInfo.bluetoothMacAddress)
        }
    }
}