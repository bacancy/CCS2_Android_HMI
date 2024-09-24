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
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenNewBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_CONFIG_OPTION_ENTERED
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_CHARGING_START_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_END_TIME
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_CHARGING_START_TIME
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
class GunsHomeScreenNwFragment : BaseFragment() {

    private lateinit var summaryDialogGun1: Dialog
    private lateinit var summaryDialogGun2: Dialog
    private var isGun1PluggedIn: Boolean = false
    private var isGun2PluggedIn: Boolean = false
    private var isGun1ChargingStarted: Boolean = false
    private var isGun2ChargingStarted: Boolean = false
    private var shouldShowGun1SummaryDialog: Boolean = false
    private var shouldShowGun2SummaryDialog: Boolean = false
    private lateinit var binding: FragmentGunsHomeScreenNewBinding
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
        binding = FragmentGunsHomeScreenNewBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(false)
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon(false)
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions(true)
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
                //binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                binding.ivGun1.setImageResource(R.drawable.ic_gun1_unplugged)
            }

            PLUGGED_IN -> {
                hideGunsSummaryDialog(true)
                isGun1PluggedIn = true
                //binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                binding.ivGun1.setImageResource(R.drawable.ic_gun1_plugged)
                //binding.tvGun1State.text = getString(R.string.lbl_plugged_in)
            }

            LBL_CHARGING -> {
                hideGunsSummaryDialog(true)
                isGun1PluggedIn = false
                //binding.tvGun1State.removeBlinking()
                isGun1ChargingStarted = true
                shouldShowGun1SummaryDialog = false
                //binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            LBL_PREPARING_FOR_CHARGING -> {
                hideGunsSummaryDialog(true)
                isGun1PluggedIn = false
                //binding.tvGun1State.removeBlinking()
                shouldShowGun1SummaryDialog = false
                //binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            LBL_COMPLETE -> {
                isGun1PluggedIn = false
                //binding.tvGun1State.removeBlinking()
                //binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging)
            }

            LBL_PLC_FAULT,
            LBL_RECTIFIER_FAULT,
            LBL_TEMPERATURE_FAULT,
            LBL_SPD_FAULT,
            LBL_SMOKE_FAULT,
            LBL_TAMPER_FAULT -> {
                isGun1PluggedIn = false
                //binding.tvGun1State.startBlinking(requireContext())
                //binding.ivGun1Half.setImageResource(R.drawable.img_gun1_fault)
            }

            LBL_EMERGENCY_STOP -> {
                isGun1PluggedIn = false
                //binding.tvGun1State.startBlinking(requireContext())
            }

            else -> {
                isGun1PluggedIn = false
                //binding.tvGun1State.startBlinking(requireContext())
                //binding.tvGun1State.text = "(${tbGunsChargingInfo.gunChargingStateToShow})"
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
                }
            }
        }
    }

    private fun updateGun2UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        if (isVisible) {
            prefHelper.setSelectedGunNumber(SELECTED_GUN, 0)
        }

        //binding.tvGun2State.text = "(${tbGunsChargingInfo.gunChargingStateToShow})"

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
                //binding.tvGun2State.removeBlinking()
                shouldShowGun2SummaryDialog = false
                binding.ivGun2.setImageResource(R.drawable.ic_gun2_unplugged)
            }

            PLUGGED_IN -> {
                hideGunsSummaryDialog(false)
                isGun2PluggedIn = true
                //binding.tvGun2State.removeBlinking()
                shouldShowGun2SummaryDialog = false
                binding.ivGun2.setImageResource(R.drawable.ic_gun2_plugged)
                //binding.tvGun2State.text = getString(R.string.lbl_plugged_in)
            }

            else -> {
                isGun2PluggedIn = false
                //binding.tvGun2State.startBlinking(requireContext())
                //binding.tvGun2State.text = "(${tbGunsChargingInfo.gunChargingStateToShow})"
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


    override fun handleClicks() {
        binding.ivGun1.setOnClickListener {
            openGunsMoreInfoFragment(1)
        }

        binding.ivGun2.setOnClickListener {
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