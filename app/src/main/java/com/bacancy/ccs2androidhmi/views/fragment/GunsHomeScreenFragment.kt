package com.bacancy.ccs2androidhmi.views.fragment

import android.app.Dialog
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
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_CONFIG_OPTION_ENTERED
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_START_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_START_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.INSIDE_LOCAL_START_STOP_SCREEN
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_DUAL_SOCKET_MODE_SELECTED
import com.bacancy.ccs2androidhmi.util.CommonUtils.UNIT_PRICE
import com.bacancy.ccs2androidhmi.util.DialogUtils.clearDialogFlags
import com.bacancy.ccs2androidhmi.util.DialogUtils.showChargingSummaryDialog
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_AUTHENTICATION_DENIED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_AUTHENTICATION_TIMEOUT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_COMMUNICATION_ERROR
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_COMPLETE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_EMERGENCY_STOP
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_ISOLATION_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_MAINS_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_PLC_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLUGGED_IN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_PRECHARGE_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_PREPARING_FOR_CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_RECTIFIER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_RESERVED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_UNAVAILABLE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.LBL_UNPLUGGED
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

    private lateinit var summaryDialogGun1: Dialog
    private lateinit var summaryDialogGun2: Dialog
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
        prefHelper.setBoolean(CDM_CONFIG_OPTION_ENTERED, false)
        (requireActivity() as HMIDashboardActivity).updateDualSocketText(getString(R.string.lbl_dual_socket))
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

        binding.tvGun1State.text = "(${tbGunsChargingInfo.gunChargingStateToShow})"

        //Send GUN 1 Charging State
        if (requireContext().isInternetConnected() && prefHelper.getStringValue(
                CommonUtils.DEVICE_MAC_ADDRESS,
                ""
            ).isNotEmpty()
        ) {
            mqttViewModel.sendGunStatusToMqtt(
                prefHelper.getStringValue(
                    CommonUtils.DEVICE_MAC_ADDRESS, ""
                ), 1, tbGunsChargingInfo.gunChargingStateToSave
            )
        }
        when (tbGunsChargingInfo.gunChargingStateToSave) {
            LBL_UNPLUGGED -> {
                hideGunsSummaryDialog(true)
                isGun1PluggedIn = false
                hideGunsChargingStatusUI(true)
                binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_unplugged)
            }

            PLUGGED_IN -> {
                hideGunsSummaryDialog(true)
                isGun1PluggedIn = true
                hideGunsChargingStatusUI(true)
                binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_plugged)
                binding.tvGun1State.text = getString(R.string.lbl_plugged_in)
            }

            LBL_CHARGING -> {
                hideGunsSummaryDialog(true)
                isGun1PluggedIn = false
                binding.tvGun1State.removeBlinking()
                isGun1ChargingStarted = true
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
                showGunsChargingStatusUI(true, tbGunsChargingInfo)
            }

            LBL_PREPARING_FOR_CHARGING -> {
                hideGunsSummaryDialog(true)
                isGun1PluggedIn = false
                hideGunsChargingStatusUI(true)
                binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            LBL_COMPLETE -> {
                isGun1PluggedIn = false
                binding.tvGun1State.removeBlinking()
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging)
                hideGunsChargingStatusUI(true)
            }

            LBL_PLC_FAULT,
            LBL_RECTIFIER_FAULT,
            LBL_TEMPERATURE_FAULT,
            LBL_SPD_FAULT,
            LBL_SMOKE_FAULT,
            LBL_TAMPER_FAULT -> {
                isGun1PluggedIn = false
                binding.tvGun1State.startBlinking(requireContext())
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_fault)
                hideGunsChargingStatusUI(true)
            }

            LBL_EMERGENCY_STOP -> {
                isGun1PluggedIn = false
                binding.tvGun1State.startBlinking(requireContext())
                hideGunsChargingStatusUI(true)
            }

            else -> {
                isGun1PluggedIn = false
                binding.tvGun1State.startBlinking(requireContext())
                binding.tvGun1State.text = "(${tbGunsChargingInfo.gunChargingStateToShow})"
                hideGunsChargingStatusUI(true)
            }
        }

        handleDualSocketButtonVisibility()

        when (tbGunsChargingInfo.gunChargingStateToSave) {
            LBL_COMPLETE,
            LBL_COMMUNICATION_ERROR,
            LBL_AUTHENTICATION_TIMEOUT,
            LBL_PLC_FAULT,
            LBL_RECTIFIER_FAULT,
            LBL_AUTHENTICATION_DENIED,
            LBL_PRECHARGE_FAIL,
            LBL_ISOLATION_FAIL,
            LBL_TEMPERATURE_FAULT,
            LBL_SPD_FAULT,
            LBL_SMOKE_FAULT,
            LBL_TAMPER_FAULT,
            LBL_MAINS_FAIL,
            LBL_UNAVAILABLE,
            LBL_RESERVED,
            LBL_EMERGENCY_STOP,
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

        binding.tvGun2State.text = "(${tbGunsChargingInfo.gunChargingStateToShow})"

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
                    ), 2, tbGunsChargingInfo.gunChargingStateToSave
                )
            }
        }
        when (tbGunsChargingInfo.gunChargingStateToSave) {
            LBL_UNPLUGGED -> {
                hideGunsSummaryDialog(false)
                isGun2PluggedIn = false
                hideGunsChargingStatusUI(false)
                binding.tvGun2State.removeBlinking()
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_unplugged)
            }

            PLUGGED_IN -> {
                hideGunsSummaryDialog(false)
                isGun2PluggedIn = true
                hideGunsChargingStatusUI(false)
                binding.tvGun2State.removeBlinking()
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_plugged)
                binding.tvGun2State.text = getString(R.string.lbl_plugged_in)
            }

            LBL_CHARGING -> {
                hideGunsSummaryDialog(false)
                isGun2PluggedIn = false
                binding.tvGun2State.removeBlinking()
                isGun2ChargingStarted = true
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
                showGunsChargingStatusUI(false, tbGunsChargingInfo)
            }

            LBL_PREPARING_FOR_CHARGING -> {
                hideGunsSummaryDialog(false)
                isGun2PluggedIn = false
                hideGunsChargingStatusUI(false)
                binding.tvGun2State.removeBlinking()
                shouldShowGun2SummaryDialog = false
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
            }

            LBL_COMPLETE -> {
                isGun2PluggedIn = false
                binding.tvGun2State.removeBlinking()
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging)
                hideGunsChargingStatusUI(false)
            }

            LBL_PLC_FAULT,
            LBL_RECTIFIER_FAULT,
            LBL_TEMPERATURE_FAULT,
            LBL_SPD_FAULT,
            LBL_SMOKE_FAULT,
            LBL_TAMPER_FAULT -> {
                isGun2PluggedIn = false
                binding.tvGun2State.startBlinking(requireContext())
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_fault)
                hideGunsChargingStatusUI(false)
            }

            LBL_EMERGENCY_STOP -> {
                isGun2PluggedIn = false
                binding.tvGun2State.startBlinking(requireContext())
                hideGunsChargingStatusUI(false)
            }

            else -> {
                isGun2PluggedIn = false
                binding.tvGun2State.startBlinking(requireContext())
                binding.tvGun2State.text = "(${tbGunsChargingInfo.gunChargingStateToShow})"
                hideGunsChargingStatusUI(false)
            }
        }

        handleDualSocketButtonVisibility()

        when (tbGunsChargingInfo.gunChargingStateToSave) {
            LBL_COMPLETE,
            LBL_COMMUNICATION_ERROR,
            LBL_AUTHENTICATION_TIMEOUT,
            LBL_PLC_FAULT,
            LBL_RECTIFIER_FAULT,
            LBL_AUTHENTICATION_DENIED,
            LBL_PRECHARGE_FAIL,
            LBL_ISOLATION_FAIL,
            LBL_TEMPERATURE_FAULT,
            LBL_SPD_FAULT,
            LBL_SMOKE_FAULT,
            LBL_TAMPER_FAULT,
            LBL_MAINS_FAIL,
            LBL_UNAVAILABLE,
            LBL_RESERVED,
            LBL_EMERGENCY_STOP,
            -> {
                if (!shouldShowGun2SummaryDialog && isGun2ChargingStarted && !prefHelper.getBoolean(
                        IS_DUAL_SOCKET_MODE_SELECTED,
                        false
                    )
                ) {
                    isGun2ChargingStarted = false
                    shouldShowGun2SummaryDialog = true
                    observeGunsLastChargingSummary(false)
                }

            }
        }

    }

    private fun hideGunsSummaryDialog(isGun1: Boolean) {
        if (isGun1 && this::summaryDialogGun1.isInitialized && summaryDialogGun1.isShowing) {
            summaryDialogGun1.dismiss()
        } else {
            if (this::summaryDialogGun2.isInitialized && summaryDialogGun2.isShowing) {
                summaryDialogGun2.dismiss()
            }
        }
    }

    private fun isBothGunsPluggedIn(): Boolean {
        return isGun1PluggedIn && isGun2PluggedIn
    }

    private fun handleDualSocketButtonVisibility() {
        if (isAdded) {
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
                    tvGun1ChargingSoc.tvLabel.text = getString(R.string.lbl_charging_soc)
                    tvGun1ChargingSoc.tvValue.text = "$chargingSoc%"

                    tvGun1ChargingVoltage.tvLabel.text = getString(R.string.lbl_charging_voltage)
                    tvGun1ChargingVoltage.tvValue.text = "$chargingVoltage V"

                    tvGun1ChargingCurrent.tvLabel.text = getString(R.string.lbl_charging_current)
                    tvGun1ChargingCurrent.tvValue.text = "$chargingCurrent A"

                    tvGun1EnergyConsumption.tvLabel.text = getString(R.string.lbl_total_energy)
                    tvGun1EnergyConsumption.tvValue.text =
                        getString(R.string.lbl_gun_energy_consumption, energyConsumption)

                    tvGun1Duration.tvLabel.text = getString(R.string.lbl_duration_hh_mm)
                    tvGun1Duration.tvValue.text = duration

                    tvGun1TotalCost.tvLabel.text = getString(R.string.lbl_total_cost)
                    tvGun1TotalCost.tvValue.text =
                        getString(R.string.lbl_gun_total_cost_in_rs, totalCost)
                } else {
                    tvGun2ChargingSoc.tvLabel.text = getString(R.string.lbl_charging_soc)
                    tvGun2ChargingSoc.tvValue.text = "$chargingSoc%"

                    tvGun2ChargingVoltage.tvLabel.text = getString(R.string.lbl_charging_voltage)
                    tvGun2ChargingVoltage.tvValue.text = "$chargingVoltage V"

                    tvGun2ChargingCurrent.tvLabel.text = getString(R.string.lbl_charging_current)
                    tvGun2ChargingCurrent.tvValue.text = "$chargingCurrent A"

                    tvGun2EnergyConsumption.tvLabel.text = getString(R.string.lbl_total_energy)
                    tvGun2EnergyConsumption.tvValue.text =
                        getString(R.string.lbl_gun_energy_consumption, energyConsumption)

                    tvGun2Duration.tvLabel.text = getString(R.string.lbl_duration_hh_mm)
                    tvGun2Duration.tvValue.text = duration

                    tvGun2TotalCost.tvLabel.text = getString(R.string.lbl_total_cost)
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
                                    summaryDialogGun1 = requireContext().showChargingSummaryDialog(
                                        true,
                                        it,
                                        isDarkTheme
                                    ) {
                                        if (prefHelper.getBoolean(
                                                IS_DUAL_SOCKET_MODE_SELECTED,
                                                false
                                            )
                                        ) {
                                            isGun2ChargingStarted =
                                                false //To avoid showing gun-2 charging summary dialog when charging stopped in Dual Socket mode
                                            prefHelper.setBoolean(
                                                IS_DUAL_SOCKET_MODE_SELECTED,
                                                false
                                            )
                                            (requireActivity() as HMIDashboardActivity).goBack()
                                        }
                                        prefHelper.setStringValue(GUN_1_CHARGING_START_TIME, "")
                                        prefHelper.setStringValue(GUN_1_CHARGING_END_TIME, "")
                                    }
                                    summaryDialogGun1.show()
                                    if(isAdded){
                                        requireActivity().clearDialogFlags(summaryDialogGun1)
                                    }
                                }
                            } else {
                                if (shouldShowGun2SummaryDialog) {
                                    shouldShowGun2SummaryDialog = false
                                    summaryDialogGun2 = requireContext().showChargingSummaryDialog(
                                        false,
                                        it,
                                        isDarkTheme
                                    ) {
                                        prefHelper.setStringValue(GUN_2_CHARGING_START_TIME, "")
                                        prefHelper.setStringValue(GUN_2_CHARGING_END_TIME, "")
                                    }
                                    summaryDialogGun2.show()
                                    if(isAdded){
                                        requireActivity().clearDialogFlags(summaryDialogGun1)
                                    }
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
    }

    private fun openGunsMoreInfoFragment(gunNumber: Int) {
        val bundle = Bundle()
        bundle.putInt(SELECTED_GUN, gunNumber)
        val fragment = GunsMoreInformationFragment()
        fragment.arguments = bundle
        fragmentChangeListener?.replaceFragment(fragment)
    }

}