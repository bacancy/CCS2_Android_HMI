package com.bacancy.ccs2androidhmi.views.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentDualSocketGunsMoreInfoScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.util.AppConfig.SHOW_PIN_AUTHORIZATION
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.CommonUtils.AUTH_PIN_VALUE
import com.bacancy.ccs2androidhmi.util.DialogUtils.clearDialogFlags
import com.bacancy.ccs2androidhmi.util.DialogUtils.showCustomDialog
import com.bacancy.ccs2androidhmi.util.DialogUtils.showPinAuthorizationDialog
import com.bacancy.ccs2androidhmi.util.DialogUtils.showSessionModeDialog
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.ToastUtils.showCustomToast
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.viewmodel.MQTTViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DualSocketGunsMoreInformationFragment : BaseFragment() {

    private var isSessionModeDialogShownOnce: Boolean = false
    private lateinit var sessionModeDialog: Dialog
    private lateinit var binding: FragmentDualSocketGunsMoreInfoScreenBinding
    private lateinit var acMeterInfoFragment: ACMeterInfoFragment
    private lateinit var gunsDCOutputInfoFragment: GunsDCOutputInfoFragment
    private lateinit var gunsLastChargingSummaryFragment: GunsLastChargingSummaryFragment
    private lateinit var gunsChargingHistoryFragment: GunsChargingHistoryFragment
    private lateinit var faultInfoFragment: FaultInfoFragment
    private var fragmentChangeListener: FragmentChangeListener? = null
    private val appViewModel: AppViewModel by viewModels()
    private val mqttViewModel: MQTTViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener) {
            fragmentChangeListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDualSocketGunsMoreInfoScreenBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(false)
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon(false)
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        (requireActivity() as HMIDashboardActivity).updateDualSocketText(getString(R.string.single_socket))
        sessionModeDialog =
            requireActivity().showSessionModeDialog { selectedRadioButton, sessionModeValue ->
                Log.d("TAG", "updateGunsChargingUI - selectedRadioButton: $selectedRadioButton")
                Log.d("TAG", "updateGunsChargingUI - sessionModeValue: $sessionModeValue")
                prefHelper.setBoolean(CommonUtils.IS_GUN_1_SESSION_MODE_SELECTED, true)
                prefHelper.setIntValue(CommonUtils.GUN_1_SELECTED_SESSION_MODE, selectedRadioButton)
                prefHelper.setStringValue(CommonUtils.GUN_1_SELECTED_SESSION_MODE_VALUE, sessionModeValue)
                val dialog = requireActivity().showCustomDialog(getString(R.string.msg_convey_user_to_start_charging_session)) {}
                dialog.show()
                requireActivity().clearDialogFlags(dialog)
            }
        return binding.root
    }

    private fun observeGunsChargingInfo() {
        appViewModel.getUpdatedGunsChargingInfo(1).observe(requireActivity()) {
            it?.let {
                updateGunsChargingUI(1, it)
            }
        }

        appViewModel.getUpdatedGunsChargingInfo(2).observe(requireActivity()) {
            it?.let {
                updateGunsChargingUI(2, it)
            }
        }
    }

    private fun updateGunsChargingUI(
        selectedGunNumber: Int,
        tbGunsChargingInfo: TbGunsChargingInfo
    ) {
        binding.apply {
            tbGunsChargingInfo.apply {

                //Send GUN 1/2 Charging State
                val chargerOutputs = prefHelper.getStringValue(CommonUtils.CHARGER_OUTPUTS, "")
                if (selectedGunNumber == 1 || (selectedGunNumber == 2 && chargerOutputs == "2")) {
                    if (requireContext().isInternetConnected() && prefHelper.getStringValue(
                            CommonUtils.DEVICE_MAC_ADDRESS,
                            ""
                        ).isNotEmpty()
                    ) {
                        mqttViewModel.sendGunStatusToMqtt(
                            prefHelper.getStringValue(
                                CommonUtils.DEVICE_MAC_ADDRESS, ""
                            ), selectedGunNumber, gunChargingState
                        )
                    }
                }

                when (gunChargingState) {
                    GunsChargingInfoUtils.PLUGGED_IN -> {
                        ivSessionMode.visible()
                        if (!isSessionModeDialogShownOnce && !sessionModeDialog.isShowing) {
                            val sessionModeKey = when (selectedGunNumber) {
                                1 -> CommonUtils.IS_GUN_1_SESSION_MODE_SELECTED
                                2 -> CommonUtils.IS_GUN_1_SESSION_MODE_SELECTED
                                else -> null
                            }

                            if (sessionModeKey != null && !prefHelper.getBoolean(sessionModeKey, false)) {
                                isSessionModeDialogShownOnce = true
                                sessionModeDialog.show()
                                requireActivity().clearDialogFlags(sessionModeDialog)
                            }
                        }
                        if (SHOW_PIN_AUTHORIZATION) {
                            ivPinAuthorization.visible()
                        }
                    }
                    GunsChargingInfoUtils.CHARGING -> {
                        ivSessionMode.gone()
                        isSessionModeDialogShownOnce = false
                        if (SHOW_PIN_AUTHORIZATION) {
                            ivPinAuthorization.visible()
                        }
                    }

                    else -> {
                        ivSessionMode.gone()
                        isSessionModeDialogShownOnce = false
                        ivPinAuthorization.gone()
                    }
                }

                if (selectedGunNumber == 1) {
                    if(gunChargingState == GunsChargingInfoUtils.PLUGGED_IN) {
                        incHeaderGun1.tvSubHeader.text = getString(R.string.lbl_plugged_in)
                    } else {
                        incHeaderGun1.tvSubHeader.text = "($gunChargingState)"
                    }
                    initialSoc1.tvValue.text = initialSoc.toString()
                    demandVoltage1.tvValue.text = demandVoltage.toString()
                    demandCurrent1.tvValue.text = demandCurrent.toString()
                    chargingVoltage1.tvValue.text = chargingVoltage.toString()
                    chargingCurrent1.tvValue.text = chargingCurrent.toString()
                    chargingSoc1.tvValue.text = chargingSoc.toString()
                    chargingDuration1.tvValue.text = duration
                    energyConsumption1.tvValue.text = energyConsumption.toString()
                } else {
                    if(gunChargingState == GunsChargingInfoUtils.PLUGGED_IN) {
                        incHeaderGun2.tvSubHeader.text = getString(R.string.lbl_plugged_in)
                    } else {
                        incHeaderGun2.tvSubHeader.text = "($gunChargingState)"
                    }
                    initialSoc2.tvValue.text = initialSoc.toString()
                    demandVoltage2.tvValue.text = demandVoltage.toString()
                    demandCurrent2.tvValue.text = demandCurrent.toString()
                    chargingVoltage2.tvValue.text = chargingVoltage.toString()
                    chargingCurrent2.tvValue.text = chargingCurrent.toString()
                    chargingSoc2.tvValue.text = chargingSoc.toString()
                    chargingDuration2.tvValue.text = duration
                    energyConsumption2.tvValue.text = energyConsumption.toString()
                }

            }
        }
    }

    override fun handleClicks() {
        binding.apply {

            btnACMeterInfo.setOnClickListener {
                acMeterInfoFragment = ACMeterInfoFragment()
                fragmentChangeListener?.replaceFragment(acMeterInfoFragment)
            }

            btnDCMeterInfo.setOnClickListener {
                gunsDCOutputInfoFragment = GunsDCOutputInfoFragment()
                gunsDCOutputInfoFragment.arguments = getBundleToPass()
                fragmentChangeListener?.replaceFragment(gunsDCOutputInfoFragment)
            }

            btnChargingSummary.setOnClickListener {
                gunsLastChargingSummaryFragment = GunsLastChargingSummaryFragment()
                gunsLastChargingSummaryFragment.arguments = getBundleToPass()
                fragmentChangeListener?.replaceFragment(gunsLastChargingSummaryFragment)
            }

            btnChargingHistory.setOnClickListener {
                gunsChargingHistoryFragment = GunsChargingHistoryFragment()
                gunsChargingHistoryFragment.arguments = getBundleToPass()
                fragmentChangeListener?.replaceFragment(gunsChargingHistoryFragment)
            }

            btnFaultIndication.setOnClickListener {
                faultInfoFragment = FaultInfoFragment()
                fragmentChangeListener?.replaceFragment(faultInfoFragment)
            }

            /*ivGunStateInfo.setOnClickListener {
                fragmentChangeListener?.replaceFragment(GunsStateInfoFragment())
            }*/

            ivPinAuthorization.setOnClickListener {
                showPinAuthorizationDialog({
                    prefHelper.setStringValue(AUTH_PIN_VALUE, it)
                }, {
                    requireContext().showCustomToast(
                        getString(R.string.message_invalid_empty_pin),
                        false
                    )
                })
            }

            ivSessionMode.setOnClickListener {
                if(sessionModeDialog.isShowing.not()){
                    sessionModeDialog.show()
                    requireActivity().clearDialogFlags(sessionModeDialog)
                }
            }
        }
    }

    private fun getBundleToPass(): Bundle {
        val bundle = Bundle()
        bundle.putInt(SELECTED_GUN, 1)
        return bundle
    }

    override fun setScreenHeaderViews() {
        prefHelper.setSelectedGunNumber(SELECTED_GUN, 1)
        observeGunsChargingInfo()
        binding.apply {
            incHeaderGun1.tvHeader.text = getString(R.string.lbl_gun_1)
            incHeaderGun2.tvHeader.text = getString(R.string.lbl_gun_2)
        }
    }

    override fun setupViews() {
        binding.apply {
            //For Gun1
            initialSoc1.tvValueUnit.text = getString(R.string.lbl_percentage)

            demandVoltage1.tvValueUnit.text = getString(R.string.lbl_v)

            demandCurrent1.tvValueUnit.text = getString(R.string.lbl_a)

            chargingVoltage1.tvValueUnit.text = getString(R.string.lbl_v)

            chargingCurrent1.tvValueUnit.text = getString(R.string.lbl_a)

            chargingSoc1.tvValueUnit.text = getString(R.string.lbl_percentage)

            chargingDuration1.tvValue.text = getString(R.string.hint_00_00)
            chargingDuration1.tvValueUnit.invisible()

            energyConsumption1.tvValueUnit.text = getString(R.string.lbl_kwh)

            //For Gun2
            initialSoc2.tvValueUnit.text = getString(R.string.lbl_percentage)

            demandVoltage2.tvValueUnit.text = getString(R.string.lbl_v)

            demandCurrent2.tvValueUnit.text = getString(R.string.lbl_a)

            chargingVoltage2.tvValueUnit.text = getString(R.string.lbl_v)

            chargingCurrent2.tvValueUnit.text = getString(R.string.lbl_a)

            chargingSoc2.tvValueUnit.text = getString(R.string.lbl_percentage)

            chargingDuration2.tvValue.text = getString(R.string.hint_00_00)
            chargingDuration2.tvValueUnit.invisible()

            energyConsumption2.tvValueUnit.text = getString(R.string.lbl_kwh)

        }
    }
}