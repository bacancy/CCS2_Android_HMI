package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
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

    private var isGun1ChargingStarted: Boolean = false
    private var isGun2ChargingStarted: Boolean = false
    private var shouldShowGun1SummaryDialog: Boolean = false
    private var shouldShowGun2SummaryDialog: Boolean = false
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
        observeLatestMiscInfo()
        observeGunsChargingInfo()
        return binding.root
    }

    private fun observeLatestMiscInfo() {
        appViewModel.latestMiscInfo.observe(requireActivity()) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                Log.i("TAG", "LatestMiscInfo: Unit Price = Rs.${latestMiscInfo.unitPrice}/kwh")
                binding.tvUnitPrice.text = getString(R.string.lbl_unit_price_per_kw, latestMiscInfo.unitPrice)
            }
        }
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

        binding.tvGun1State.text = "(${tbGunsChargingInfo.gunChargingState})"

        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_unplugged)
            }

            PLUGGED_IN -> {
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_plugged)
                binding.tvGun1State.text = getString(R.string.lbl_plugged_in)
            }

            CHARGING -> {
                isGun1ChargingStarted = true
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
                showGunsChargingStatusUI(true, tbGunsChargingInfo)
            }

            PREPARING_FOR_CHARGING -> {
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            COMPLETE -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging)
                hideGunsChargingStatusUI(true)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_fault)
                hideGunsChargingStatusUI(true)
            }

            EMERGENCY_STOP -> {
                hideGunsChargingStatusUI(true)
            }

            else -> {
                binding.tvGun1State.text = "(${tbGunsChargingInfo.gunChargingState})"
                hideGunsChargingStatusUI(true)
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
                Log.d("TAG", "updateGun1UI##: $shouldShowGun1SummaryDialog && $isGun1ChargingStarted")
                if (!shouldShowGun1SummaryDialog && isGun1ChargingStarted) {
                    isGun1ChargingStarted = false
                    shouldShowGun1SummaryDialog = true
                    Log.d("TAG", "updateGun1UI##: Show Gun1 Dialog")
                    observeGunsLastChargingSummary(true)
                }
            }
        }
    }

    private fun updateGun2UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        if (isVisible) {
            prefHelper.setSelectedGunNumber(SELECTED_GUN, 0)
        }

        binding.tvGun2State.text = "(${tbGunsChargingInfo.gunChargingState})"

        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_unplugged)
            }

            PLUGGED_IN -> {
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_plugged)
                binding.tvGun2State.text = getString(R.string.lbl_plugged_in)
            }

            CHARGING -> {
                isGun2ChargingStarted = true
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
                showGunsChargingStatusUI(false, tbGunsChargingInfo)
            }

            PREPARING_FOR_CHARGING -> {
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
            }

            COMPLETE -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging)
                hideGunsChargingStatusUI(false)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_fault)
                hideGunsChargingStatusUI(false)
            }

            EMERGENCY_STOP -> {
                hideGunsChargingStatusUI(false)
            }

            else -> {
                binding.tvGun2State.text = "(${tbGunsChargingInfo.gunChargingState})"
                hideGunsChargingStatusUI(false)
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
                if (!shouldShowGun2SummaryDialog && isGun2ChargingStarted) {
                    isGun2ChargingStarted = false
                    shouldShowGun2SummaryDialog = true
                    observeGunsLastChargingSummary(false)
                }

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
        lifecycleScope.launch {
            delay(2000)
            appViewModel.getGunsLastChargingSummary(if (isGun1) 1 else 2).observe(requireActivity()) {
                Log.i("JAN25", "updateGun1UI##: Got Guns Last Charging Summary")
                it?.let {
                    if (isGun1) {
                        if (shouldShowGun1SummaryDialog) {
                            shouldShowGun1SummaryDialog = false
                            showChargingSummaryDialog(true, it) {}
                        }
                    } else {
                        if (shouldShowGun2SummaryDialog) {
                            shouldShowGun2SummaryDialog = false
                            showChargingSummaryDialog(false, it) {}
                        }
                    }
                }
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
            //showDemoDialogs()
            (requireActivity() as HMIDashboardActivity).showOrHideEmergencyStop(0)
            fragmentChangeListener?.replaceFragment(FirmwareVersionInfoFragment())
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
            //delay(5000)
            //showChargingSummaryDialog(false, sampleTbGunsLastChargingSummary2) {}
        }
    }

    private fun openGunsMoreInfoFragment(gunNumber: Int) {
        (requireActivity() as HMIDashboardActivity).showOrHideEmergencyStop(0)
        val bundle = Bundle()
        bundle.putInt(SELECTED_GUN, gunNumber)
        val fragment = GunsMoreInformationFragment()
        fragment.arguments = bundle
        fragmentChangeListener?.replaceFragment(fragment)
    }
}