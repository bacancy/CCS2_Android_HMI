package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsMoreInfoScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.util.DialogUtils.showPinAuthorizationDialog
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.showToast
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GunsMoreInformationFragment : BaseFragment() {

    private var selectedGunNumber: Int = 1
    private lateinit var binding: FragmentGunsMoreInfoScreenBinding
    private lateinit var acMeterInfoFragment: ACMeterInfoFragment
    private lateinit var gunsDCOutputInfoFragment: GunsDCOutputInfoFragment
    private lateinit var gunsLastChargingSummaryFragment: GunsLastChargingSummaryFragment
    private lateinit var gunsChargingHistoryFragment: GunsChargingHistoryFragment
    private lateinit var faultInfoFragment: FaultInfoFragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsMoreInfoScreenBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        return binding.root
    }

    private fun observeGunsChargingInfo() {
        appViewModel.getUpdatedGunsChargingInfo(selectedGunNumber).observe(viewLifecycleOwner) {
            it?.let { gunInfo ->
                if (gunInfo.gunId == selectedGunNumber) {
                    updateGunsChargingUI(gunInfo)
                }
            }
        }
    }

    private fun updateGunsChargingUI(tbGunsChargingInfo: TbGunsChargingInfo) {
        binding.apply {
            tbGunsChargingInfo.apply {

                when (gunChargingState) {
                    GunsChargingInfoUtils.PLUGGED_IN,
                    GunsChargingInfoUtils.CHARGING -> {
                        ivPinAuthorization.visible()
                    }

                    else -> {
                        ivPinAuthorization.gone()
                    }
                }

                when (selectedGunNumber) {
                    1 -> {
                        incHeader.tvHeader.text =
                            getString(R.string.lbl_gun_1) + " ($gunChargingState)"
                    }

                    2 -> {
                        incHeader.tvHeader.text =
                            getString(R.string.lbl_gun_2) + " ($gunChargingState)"
                    }
                }

                incInitialSoc.tvValue.text = initialSoc.toString()
                incDemandVoltage.tvValue.text = demandVoltage.toString()
                incDemandCurrent.tvValue.text = demandCurrent.toString()
                incChargingVoltage.tvValue.text = chargingVoltage.toString()
                incChargingCurrent.tvValue.text = chargingCurrent.toString()
                incChargingSoc.tvValue.text = chargingSoc.toString()
                incDuration.tvValue.text = duration
                incEnergyConsumption.tvValue.text = energyConsumption.toString()
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

            ivGunStateInfo.setOnClickListener {
                fragmentChangeListener?.replaceFragment(GunsStateInfoFragment())
            }

            ivPinAuthorization.setOnClickListener {
                showPinAuthorizationDialog({
                    showToast(it)
                }, {
                    showToast("Invalid PIN")
                })
            }

        }
    }

    private fun getBundleToPass(): Bundle {
        val bundle = Bundle()
        bundle.putInt(SELECTED_GUN, selectedGunNumber)
        return bundle
    }

    override fun setScreenHeaderViews() {
        selectedGunNumber = arguments?.getInt(SELECTED_GUN)!!
        prefHelper.setSelectedGunNumber(SELECTED_GUN, selectedGunNumber)
        observeGunsChargingInfo()
        binding.apply {
            when (selectedGunNumber) {
                1 -> {
                    incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
                }

                2 -> {
                    incHeader.tvHeader.text = getString(R.string.lbl_gun_2)
                }
            }
        }
    }

    override fun setupViews() {
        binding.apply {
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
            incEnergyConsumption.tvValueUnit.text = getString(R.string.lbl_kw)
        }
    }
}