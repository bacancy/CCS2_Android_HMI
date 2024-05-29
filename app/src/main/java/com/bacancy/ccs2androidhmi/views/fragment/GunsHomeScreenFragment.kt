package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.CommonUtils.INSIDE_LOCAL_START_STOP_SCREEN
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_DUAL_SOCKET_MODE_SELECTED
import com.bacancy.ccs2androidhmi.util.CommonUtils.UNIT_PRICE
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
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.RESERVED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.UNAVAILABLE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.UNPLUGGED
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.PrefHelper.Companion.IS_DARK_THEME
import com.bacancy.ccs2androidhmi.util.TextViewUtils.removeBlinking
import com.bacancy.ccs2androidhmi.util.TextViewUtils.startBlinking
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.viewmodel.MQTTViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GunsHomeScreenFragment : BaseFragment() {

    private var isGun1PluggedIn: Boolean = false
    private var isGun2PluggedIn: Boolean = false
    private var isGun1ChargingStarted: Boolean = false
    private var isGun2ChargingStarted: Boolean = false
    private var shouldShowGun1SummaryDialog: Boolean = false
    private var shouldShowGun2SummaryDialog: Boolean = false
    private lateinit var binding: FragmentGunsHomeScreenBinding
    private var fragmentChangeListener: FragmentChangeListener? = null
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    private val mqttViewModel: MQTTViewModel by activityViewModels()

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
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon(false)
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions(true)
        observeLatestMiscInfo()
        observeGunsChargingInfo()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as HMIDashboardActivity).updateDualSocketText("Dual Socket")
        prefHelper.setBoolean(IS_DUAL_SOCKET_MODE_SELECTED, false)
        prefHelper.setBoolean(INSIDE_LOCAL_START_STOP_SCREEN, false)
        prefHelper.setBoolean("IS_IN_TEST_MODE", false)
        prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)
    }

    private fun observeLatestMiscInfo() {
        appViewModel.latestMiscInfo.observe(viewLifecycleOwner) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                Log.i("TAG", "LatestMiscInfo: Unit Price = Rs.${latestMiscInfo.unitPrice}/kwh")
                try {
                    prefHelper.setStringValue(UNIT_PRICE, latestMiscInfo.unitPrice.toString())
                } catch (e: Exception) {
                    prefHelper.setStringValue(UNIT_PRICE, "0.0")
                }
                binding.tvUnitPrice.text =
                    getString(R.string.lbl_unit_price_per_kw, latestMiscInfo.unitPrice)
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

        //Send GUN 1 Charging State
        if (requireContext().isInternetConnected() && prefHelper.getStringValue(
                CommonUtils.DEVICE_MAC_ADDRESS,
                ""
            ).isNotEmpty()
        ) {
            mqttViewModel.sendGunStatusToMqtt(
                prefHelper.getStringValue(
                    CommonUtils.DEVICE_MAC_ADDRESS, ""
                ), 1, tbGunsChargingInfo.gunChargingState
            )
        }
        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                isGun1PluggedIn = false
                hideGunsChargingStatusUI(true)
                binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_unplugged)
            }

            PLUGGED_IN -> {
                isGun1PluggedIn = true
                hideGunsChargingStatusUI(true)
                binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_plugged)
                binding.tvGun1State.text = getString(R.string.lbl_plugged_in)
            }

            CHARGING -> {
                isGun1PluggedIn = false
                binding.tvGun1State.removeBlinking()
                isGun1ChargingStarted = true
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
                showGunsChargingStatusUI(true, tbGunsChargingInfo)
            }

            PREPARING_FOR_CHARGING -> {
                isGun1PluggedIn = false
                hideGunsChargingStatusUI(true)
                binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            COMPLETE -> {
                isGun1PluggedIn = false
                binding.tvGun1State.removeBlinking()
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging)
                hideGunsChargingStatusUI(true)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT -> {
                isGun1PluggedIn = false
                binding.tvGun1State.startBlinking(requireContext())
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_fault)
                hideGunsChargingStatusUI(true)
            }

            EMERGENCY_STOP -> {
                isGun1PluggedIn = false
                binding.tvGun1State.startBlinking(requireContext())
                hideGunsChargingStatusUI(true)
            }

            else -> {
                isGun1PluggedIn = false
                binding.tvGun1State.startBlinking(requireContext())
                binding.tvGun1State.text = "(${tbGunsChargingInfo.gunChargingState})"
                hideGunsChargingStatusUI(true)
            }
        }

        handleDualSocketButtonVisibility()

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
            UNAVAILABLE,
            RESERVED,
            EMERGENCY_STOP,
            -> {
                Log.d(
                    "TAG",
                    "updateGun1UI##: $shouldShowGun1SummaryDialog && $isGun1ChargingStarted"
                )
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

        //Send GUN 2 Charging State
        val chargerOutputs = prefHelper.getStringValue(CommonUtils.CHARGER_OUTPUTS, "")
        if (chargerOutputs == "2") {
            if (requireContext().isInternetConnected() && prefHelper.getStringValue(
                    CommonUtils.DEVICE_MAC_ADDRESS,
                    ""
                ).isNotEmpty()
            ) {
                mqttViewModel.sendGunStatusToMqtt(
                    prefHelper.getStringValue(
                        CommonUtils.DEVICE_MAC_ADDRESS, ""
                    ), 2, tbGunsChargingInfo.gunChargingState
                )
            }
        }
        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                isGun2PluggedIn = false
                hideGunsChargingStatusUI(false)
                binding.tvGun2State.removeBlinking()
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_unplugged)
            }

            PLUGGED_IN -> {
                isGun2PluggedIn = true
                hideGunsChargingStatusUI(false)
                binding.tvGun2State.removeBlinking()
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_plugged)
                binding.tvGun2State.text = getString(R.string.lbl_plugged_in)
            }

            CHARGING -> {
                isGun2PluggedIn = false
                binding.tvGun2State.removeBlinking()
                isGun2ChargingStarted = true
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
                showGunsChargingStatusUI(false, tbGunsChargingInfo)
            }

            PREPARING_FOR_CHARGING -> {
                isGun2PluggedIn = false
                hideGunsChargingStatusUI(false)
                binding.tvGun2State.removeBlinking()
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
            }

            COMPLETE -> {
                isGun2PluggedIn = false
                binding.tvGun2State.removeBlinking()
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging)
                hideGunsChargingStatusUI(false)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT -> {
                isGun2PluggedIn = false
                binding.tvGun2State.startBlinking(requireContext())
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_fault)
                hideGunsChargingStatusUI(false)
            }

            EMERGENCY_STOP -> {
                isGun2PluggedIn = false
                binding.tvGun2State.startBlinking(requireContext())
                hideGunsChargingStatusUI(false)
            }

            else -> {
                isGun2PluggedIn = false
                binding.tvGun2State.startBlinking(requireContext())
                binding.tvGun2State.text = "(${tbGunsChargingInfo.gunChargingState})"
                hideGunsChargingStatusUI(false)
            }
        }

        handleDualSocketButtonVisibility()

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
            UNAVAILABLE,
            RESERVED,
            EMERGENCY_STOP,
            -> {
                if (!shouldShowGun2SummaryDialog && isGun2ChargingStarted && !prefHelper.getBoolean(IS_DUAL_SOCKET_MODE_SELECTED, false)) {
                    isGun2ChargingStarted = false
                    shouldShowGun2SummaryDialog = true
                    observeGunsLastChargingSummary(false)
                }

            }
        }

    }

    private fun isBothGunsPluggedIn(): Boolean {
        return isGun1PluggedIn && isGun2PluggedIn
    }

    private fun handleDualSocketButtonVisibility() {
        if(isAdded){
            (requireActivity() as HMIDashboardActivity).manageDualSocketButtonUI(isBothGunsPluggedIn())
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
                    tvGun1ChargingSoc.tvLabel.text = "Charging SoC"
                    tvGun1ChargingSoc.tvValue.text = "$chargingSoc%"

                    tvGun1ChargingVoltage.tvLabel.text = "Charging Voltage"
                    tvGun1ChargingVoltage.tvValue.text = "$chargingVoltage V"

                    tvGun1ChargingCurrent.tvLabel.text = "Charging Current"
                    tvGun1ChargingCurrent.tvValue.text = "$chargingCurrent A"

                    tvGun1EnergyConsumption.tvLabel.text = "Total Energy"
                    tvGun1EnergyConsumption.tvValue.text =
                        getString(R.string.lbl_gun_energy_consumption, energyConsumption)

                    tvGun1Duration.tvLabel.text = "Duration (hh:mm)"
                    tvGun1Duration.tvValue.text = duration

                    tvGun1TotalCost.tvLabel.text = "Total Cost"
                    tvGun1TotalCost.tvValue.text =
                        getString(R.string.lbl_gun_total_cost_in_rs, totalCost)
                } else {
                    tvGun2ChargingSoc.tvLabel.text = "Charging SoC"
                    tvGun2ChargingSoc.tvValue.text = "$chargingSoc%"

                    tvGun2ChargingVoltage.tvLabel.text = "Charging Voltage"
                    tvGun2ChargingVoltage.tvValue.text = "$chargingVoltage V"

                    tvGun2ChargingCurrent.tvLabel.text = "Charging Current"
                    tvGun2ChargingCurrent.tvValue.text = "$chargingCurrent A"

                    tvGun2EnergyConsumption.tvLabel.text = "Total Energy"
                    tvGun2EnergyConsumption.tvValue.text =
                        getString(R.string.lbl_gun_energy_consumption, energyConsumption)

                    tvGun2Duration.tvLabel.text = "Duration (hh:mm)"
                    tvGun2Duration.tvValue.text = duration

                    tvGun2TotalCost.tvLabel.text = "Total Cost"
                    tvGun2TotalCost.tvValue.text =
                        getString(R.string.lbl_gun_total_cost_in_rs, totalCost)
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
            try {
                appViewModel.getGunsLastChargingSummary(if (isGun1) 1 else 2)
                    .observe(requireActivity()) {
                        it?.let {
                            val isDarkTheme = prefHelper.getBoolean(IS_DARK_THEME, false)
                            if (isGun1) {
                                if (shouldShowGun1SummaryDialog) {
                                    shouldShowGun1SummaryDialog = false
                                    requireContext().showChargingSummaryDialog(
                                        true,
                                        it,
                                        isDarkTheme
                                    ) {
                                        if(prefHelper.getBoolean(IS_DUAL_SOCKET_MODE_SELECTED, false)){
                                            isGun2ChargingStarted = false //To avoid showing gun-2 charging summary dialog when charging stopped in Dual Socket mode
                                            prefHelper.setBoolean(IS_DUAL_SOCKET_MODE_SELECTED, false)
                                            (requireActivity() as HMIDashboardActivity).goBack()
                                        }
                                    }
                                }
                            } else {
                                if (shouldShowGun2SummaryDialog) {
                                    shouldShowGun2SummaryDialog = false
                                    requireContext().showChargingSummaryDialog(
                                        false,
                                        it,
                                        isDarkTheme
                                    ) {}
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
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

        binding.tvGun1Label.setOnClickListener {

            /*mqttViewModel.publishMessageToTopic(
                TOPIC_A_TO_B,
                "{\"id\":\"T001\",\"chargerRating\":\"60KW\",\"chargerOutputs\":\"2\",\"deviceMacAddress\":\"AA:BB:CC:11:22:33\",\"configDateTime\":\"01-04-2024T04:00:00\"}"
            )*/
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