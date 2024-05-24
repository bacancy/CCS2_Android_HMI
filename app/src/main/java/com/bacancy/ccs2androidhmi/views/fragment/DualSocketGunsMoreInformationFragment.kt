package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
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
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.NetworkUtils.isInternetConnected
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.viewmodel.MQTTViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DualSocketGunsMoreInformationFragment : BaseFragment() {

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
        (requireActivity() as HMIDashboardActivity).updateDualSocketText("Single Socket")
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
                    GunsChargingInfoUtils.PLUGGED_IN,
                    GunsChargingInfoUtils.CHARGING -> {
                        if (SHOW_PIN_AUTHORIZATION) {
                            //ivPinAuthorization.visible()
                        }
                    }

                    else -> {
                        //ivPinAuthorization.gone()
                    }
                }

                if (selectedGunNumber == 1) {
                    incHeaderGun1.tvHeader.text =
                        getString(R.string.lbl_gun_1) + " ($gunChargingState)"
                    incInitialSoc.tvValue.text = initialSoc.toString()
                    incDemandVoltage.tvValue.text = demandVoltage.toString()
                    incDemandCurrent.tvValue.text = demandCurrent.toString()
                    incChargingVoltage.tvValue.text = chargingVoltage.toString()
                    incChargingCurrent.tvValue.text = chargingCurrent.toString()
                    incChargingSoc.tvValue.text = chargingSoc.toString()
                    incDuration.tvValue.text = duration
                    incEnergyConsumption.tvValue.text = energyConsumption.toString()
                } else {
                    incHeaderGun2.tvHeader.text =
                        getString(R.string.lbl_gun_2) + " ($gunChargingState)"
                    incInitialSoc2.tvValue.text = initialSoc.toString()
                    incDemandVoltage2.tvValue.text = demandVoltage.toString()
                    incDemandCurrent2.tvValue.text = demandCurrent.toString()
                    incChargingVoltage2.tvValue.text = chargingVoltage.toString()
                    incChargingCurrent2.tvValue.text = chargingCurrent.toString()
                    incChargingSoc2.tvValue.text = chargingSoc.toString()
                    incDuration2.tvValue.text = duration
                    incEnergyConsumption2.tvValue.text = energyConsumption.toString()
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
            }

            ivPinAuthorization.setOnClickListener {
                showPinAuthorizationDialog({
                    prefHelper.setStringValue(AUTH_PIN_VALUE, it)
                }, {
                    requireContext().showCustomToast(
                        getString(R.string.message_invalid_empty_pin),
                        false
                    )
                })
            }*/

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
            incInitialSoc.tvLabel.text = getString(R.string.lbl_initial_soc)
            incInitialSoc.tvValueUnit.text = getString(R.string.lbl_percentage)

            incDemandVoltage.tvLabel.text = getString(R.string.lbl_demand_voltage)
            incDemandVoltage.tvValueUnit.text = getString(R.string.lbl_v)

            incDemandCurrent.tvLabel.text = getString(R.string.lbl_demand_current)
            incDemandCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incChargingVoltage.tvLabel.text = getString(R.string.lbl_charging_voltage)
            incChargingVoltage.tvValueUnit.text = getString(R.string.lbl_v)

            incChargingCurrent.tvLabel.text = getString(R.string.lbl_charging_current)
            incChargingCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incChargingSoc.tvLabel.text = getString(R.string.lbl_charging_soc)
            incChargingSoc.tvValueUnit.text = getString(R.string.lbl_percentage)

            incDuration.tvLabel.text = getString(R.string.lbl_duration_hh_mm)
            incDuration.tvValue.text = getString(R.string.hint_00_00)
            incDuration.tvValueUnit.invisible()

            incEnergyConsumption.tvLabel.text = getString(R.string.lbl_energy_consumption)
            incEnergyConsumption.tvValueUnit.text = getString(R.string.lbl_kwh)

            //For Gun2
            incInitialSoc2.tvLabel.text = getString(R.string.lbl_initial_soc)
            incInitialSoc2.tvValueUnit.text = getString(R.string.lbl_percentage)

            incDemandVoltage2.tvLabel.text = getString(R.string.lbl_demand_voltage)
            incDemandVoltage2.tvValueUnit.text = getString(R.string.lbl_v)

            incDemandCurrent2.tvLabel.text = getString(R.string.lbl_demand_current)
            incDemandCurrent2.tvValueUnit.text = getString(R.string.lbl_a)

            incChargingVoltage2.tvLabel.text = getString(R.string.lbl_charging_voltage)
            incChargingVoltage2.tvValueUnit.text = getString(R.string.lbl_v)

            incChargingCurrent2.tvLabel.text = getString(R.string.lbl_charging_current)
            incChargingCurrent2.tvValueUnit.text = getString(R.string.lbl_a)

            incChargingSoc2.tvLabel.text = getString(R.string.lbl_charging_soc)
            incChargingSoc2.tvValueUnit.text = getString(R.string.lbl_percentage)

            incDuration2.tvLabel.text = getString(R.string.lbl_duration_hh_mm)
            incDuration2.tvValue.text = getString(R.string.hint_00_00)
            incDuration2.tvValueUnit.invisible()

            incEnergyConsumption2.tvLabel.text = getString(R.string.lbl_energy_consumption)
            incEnergyConsumption2.tvValueUnit.text = getString(R.string.lbl_kwh)

        }
    }
}