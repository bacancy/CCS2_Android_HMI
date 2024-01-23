package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.util.DialogUtils.showChargingSummaryDialog
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomDialog
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_DENIED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_TIMEOUT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMMUNICATION_ERROR
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMPLETE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.EMERGENCY_STOP
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.ISOLATION_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.MAINS_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLC_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLUGGED_IN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PRECHARGE_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PREPARING_FOR_CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.RECTIFIER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.UNPLUGGED
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GunsHomeScreenFragment : BaseFragment() {

    private lateinit var binding: FragmentGunsHomeScreenBinding
    private var fragmentChangeListener: FragmentChangeListener? = null
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener) {
            fragmentChangeListener = context
        }
    }

    override fun setScreenHeaderViews() {
    }

    override fun setupViews() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsHomeScreenBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(false)
        observeGunsChargingInfo()
        return binding.root
    }

    private fun observeGunsChargingInfo() {

        appViewModel.getUpdatedGunsChargingInfo(1).observe(requireActivity()) {
            it?.let {
                updateGun1UI(it)
            }
        }

        appViewModel.getUpdatedGunsChargingInfo(2).observe(requireActivity()) {
            it?.let {
                updateGun2UI(it)
            }
        }

    }

    private fun updateGun1UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        if (isVisible) {
            prefHelper.setSelectedGunNumber(SELECTED_GUN, 0)
        }

        binding.tvGun1Label.text =
            getString(R.string.gun_1_info, tbGunsChargingInfo.gunChargingState)

        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_unplugged)
            }

            PLUGGED_IN -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_plugged)
                binding.tvGun1Label.text = getString(R.string.lbl_gun1_plugged_in)
            }

            CHARGING, PREPARING_FOR_CHARGING -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            COMPLETE -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging)
                showGunsChargingStatusUI(true, tbGunsChargingInfo)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_fault)
            }

            EMERGENCY_STOP -> {
                binding.tvEmergencyStop.visible()
            }

            else -> {
                binding.tvGun1Label.text =
                    getString(R.string.gun_1_info, tbGunsChargingInfo.gunChargingState)
                binding.tvEmergencyStop.invisible()
            }
        }

        when (tbGunsChargingInfo.gunChargingState) {
            COMPLETE,
            COMMUNICATION_ERROR,
            AUTHENTICATION_TIMEOUT,
            PLC_FAULT,
            RECTIFIER_FAULT,
            AUTHENTICATION_DENIED,
            PRECHARGE_FAIL,
            ISOLATION_FAIL,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT,
            MAINS_FAIL,
            EMERGENCY_STOP,
            -> {
                observeGunsLastChargingSummary(true)
            }
        }
    }

    private fun updateGun2UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        if (isVisible) {
            prefHelper.setSelectedGunNumber(SELECTED_GUN, 0)
        }

        binding.tvEmergencyStop.invisible()

        binding.tvGun2Label.text =
            getString(R.string.gun_2_info, tbGunsChargingInfo.gunChargingState)

        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_unplugged)
            }

            PLUGGED_IN -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_plugged)
                binding.tvGun2Label.text = getString(R.string.lbl_gun2_plugged_in)
            }

            CHARGING, PREPARING_FOR_CHARGING -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
            }

            COMPLETE -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging)
                showGunsChargingStatusUI(false, tbGunsChargingInfo)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_fault)
            }

            EMERGENCY_STOP -> {
                binding.tvEmergencyStop.visible()
            }

            else -> {
                binding.tvGun2Label.text =
                    getString(R.string.gun_2_info, tbGunsChargingInfo.gunChargingState)
                binding.tvEmergencyStop.invisible()
            }
        }

        when (tbGunsChargingInfo.gunChargingState) {
            COMPLETE,
            COMMUNICATION_ERROR,
            AUTHENTICATION_TIMEOUT,
            PLC_FAULT,
            RECTIFIER_FAULT,
            AUTHENTICATION_DENIED,
            PRECHARGE_FAIL,
            ISOLATION_FAIL,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT,
            MAINS_FAIL,
            EMERGENCY_STOP,
            -> {
                observeGunsLastChargingSummary(false)
            }
        }

    }

    private fun showGunsChargingStatusUI(isGun1: Boolean, tbGunsChargingInfo: TbGunsChargingInfo) {
        if (isGun1) {
            binding.lnrGun1ChargingStatus.visible()
            setValuesInStatusUI(tbGunsChargingInfo, true)
        }

        if (!isGun1) {
            binding.lnrGun2ChargingStatus.visible()
            setValuesInStatusUI(tbGunsChargingInfo, false)
        }
    }

    private fun setValuesInStatusUI(tbGunsChargingInfo: TbGunsChargingInfo, isGun1: Boolean) {
        binding.apply {
            tbGunsChargingInfo.apply {
                if (isGun1) {
                    tvGun1ChargingSoc.text = getString(R.string.lbl_gun_charging_soc, chargingSoc)
                    tvGun1Duration.text = getString(R.string.lbl_gun_charging_duration, duration)
                    tvGun1EnergyConsumption.text =
                        getString(R.string.lbl_gun_energy_consumption, energyConsumption)
                    tvGun1TotalCost.text = getString(R.string.lbl_gun_total_cost, totalCost)
                } else {
                    tvGun2ChargingSoc.text = getString(R.string.lbl_gun_charging_soc, chargingSoc)
                    tvGun2Duration.text = getString(R.string.lbl_gun_charging_duration, duration)
                    tvGun2EnergyConsumption.text =
                        getString(R.string.lbl_gun_energy_consumption, energyConsumption)
                    tvGun2TotalCost.text = getString(R.string.lbl_gun_total_cost, totalCost)
                }
            }
        }
    }

    private fun hideGunsChargingStatusUI(isGun1: Boolean) {
        if (isGun1) {
            binding.lnrGun1ChargingStatus.gone()
        }

        if (!isGun1) {
            binding.lnrGun2ChargingStatus.gone()
        }
    }

    private fun observeGunsLastChargingSummary(isGun1: Boolean) {
        appViewModel.getGunsLastChargingSummary(if (isGun1) 1 else 2).observe(requireActivity()) {
            it?.let {
                showChargingSummaryDialog(isGun1, it) {}
            }
        }
    }

    override fun handleClicks() {
        binding.tvGun1Actor.setOnClickListener {
            openGunsMoreInfoFragment(1)
        }

        binding.tvGun2Actor.setOnClickListener {
            openGunsMoreInfoFragment(2)
        }

        binding.ivScreenInfo.setOnClickListener {
            //showCustomDialog(getString(R.string.msg_dialog_home_screen)) {}
            //fragmentChangeListener?.replaceFragment(FirmwareVersionInfoFragment())
            showDemoDialogs()
        }
    }

    private fun showDemoDialogs() {
        val sampleTbGunsLastChargingSummary1 = TbGunsLastChargingSummary(
            gunId = 1,
            evMacAddress = "00:11:22:33:44:55",
            chargingDuration = "2 hours",
            chargingStartDateTime = "2024-01-23 10:00 AM",
            chargingEndDateTime = "2024-01-23 12:00 PM",
            startSoc = "30%",
            endSoc = "80%",
            energyConsumption = "50 kWh",
            sessionEndReason = "Completed"
        )
        val sampleTbGunsLastChargingSummary2 = TbGunsLastChargingSummary(
            gunId = 2,
            evMacAddress = "00:11:22:33:44:55",
            chargingDuration = "4 hours",
            chargingStartDateTime = "2024-01-25 10:00 AM",
            chargingEndDateTime = "2024-01-25 12:00 PM",
            startSoc = "10%",
            endSoc = "60%",
            energyConsumption = "150 kWh",
            sessionEndReason = "Remote Stop"
        )

        lifecycleScope.launch {
            showChargingSummaryDialog(true, sampleTbGunsLastChargingSummary1) {}
            delay(5000)
            showChargingSummaryDialog(false, sampleTbGunsLastChargingSummary2) {}
        }
    }

    private fun openGunsMoreInfoFragment(gunNumber: Int) {
        val bundle = Bundle()
        bundle.putInt(SELECTED_GUN, gunNumber)
        val fragment = GunsMoreInformationFragment()
        fragment.arguments = bundle
        fragmentChangeListener?.replaceFragment(fragment)
    }
}