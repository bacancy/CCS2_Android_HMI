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
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

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
        setScreenHeaderViews()
        setupViews()
        handleClicks()
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(true)
        observeGunsChargingInfo()
        return binding.root
    }

    private fun observeGunsChargingInfo() {
        appViewModel.getUpdatedGunsChargingInfo(selectedGunNumber).observe(requireActivity()) {
            Log.d("GunsMoreInfo", "observeGunsChargingInfo: Gun $selectedGunNumber = ${Gson().toJson(it)}")
        }
    }

    private fun handleClicks() {
        binding.apply {

            btnACMeterInfo.setOnClickListener {
                acMeterInfoFragment = ACMeterInfoFragment()
                fragmentChangeListener?.replaceFragment(acMeterInfoFragment)
            }

            btnDCMeterInfo.setOnClickListener {
                gunsDCOutputInfoFragment = GunsDCOutputInfoFragment()
                fragmentChangeListener?.replaceFragment(gunsDCOutputInfoFragment)
            }

            btnChargingSummary.setOnClickListener {
                gunsLastChargingSummaryFragment = GunsLastChargingSummaryFragment()
                fragmentChangeListener?.replaceFragment(gunsLastChargingSummaryFragment)
            }

            btnChargingHistory.setOnClickListener {
                gunsChargingHistoryFragment = GunsChargingHistoryFragment()
                fragmentChangeListener?.replaceFragment(gunsChargingHistoryFragment)
            }

            btnFaultIndication.setOnClickListener {
                faultInfoFragment = FaultInfoFragment()
                fragmentChangeListener?.replaceFragment(faultInfoFragment)
            }

            ivGunStateInfo.setOnClickListener {
                fragmentChangeListener?.replaceFragment(GunsStateInfoFragment())
            }

        }
    }

    override fun setScreenHeaderViews() {
        selectedGunNumber = arguments?.getInt("SELECTED_GUN")!!
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
            incDuration.tvValue.text = "00:00"
            incDuration.tvValueUnit.gone()

            incEnergyConsumption.tvLabel.text = getString(R.string.lbl_energy_consumption)
            incEnergyConsumption.tvValueUnit.text = getString(R.string.lbl_kw)
        }
    }
}