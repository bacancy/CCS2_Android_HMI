package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsChargingSummaryBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.setBackgroundColorBasedOnTheme
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GunsLastChargingSummaryFragment : BaseFragment() {

    private var selectedGunNumber: Int = 1
    private lateinit var binding: FragmentGunsChargingSummaryBinding
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsChargingSummaryBinding.inflate(layoutInflater)
        selectedGunNumber = arguments?.getInt(SELECTED_GUN)!!
        observeGunsLastChargingSummary()
        return binding.root
    }

    private fun observeGunsLastChargingSummary() {
        appViewModel.getGunsLastChargingSummary(selectedGunNumber).observe(viewLifecycleOwner) {
            it?.let {
                updateGunsLastChargingSummaryUI(it)
            }
        }
    }

    private fun updateGunsLastChargingSummaryUI(tbGunsLastChargingSummary: TbGunsLastChargingSummary) {
        binding.apply {
            tbGunsLastChargingSummary.apply {
                incEVMacAddress.tvSummaryValue.text = evMacAddress
                incChargingDuration.tvSummaryValue.text = chargingDuration
                incChargingStartDateTime.tvSummaryValue.text = chargingStartDateTime
                incChargingEndDateTime.tvSummaryValue.text = chargingEndDateTime
                incStartSOC.tvSummaryValue.text = startSoc
                incEndSOC.tvSummaryValue.text = endSoc
                incEnergyConsumption.tvSummaryValue.text = energyConsumption
                incSessionEndReason.tvSummaryValue.text = sessionEndReason
            }
        }
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            if (selectedGunNumber == 1) {
                incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
            } else {
                incHeader.tvHeader.text = getString(R.string.lbl_gun_2)
            }
            incHeader.tvSubHeader.text = getString(R.string.lbl_charging_summary)
            incHeader.tvSubHeader.visible()
        }
    }

    override fun onResume() {
        super.onResume()
        if (selectedGunNumber == 1) {
            prefHelper.setScreenVisible(CommonUtils.GUN_1_LAST_CHARGING_SUMMARY_FRAG, true)
        } else {
            prefHelper.setScreenVisible(CommonUtils.GUN_2_LAST_CHARGING_SUMMARY_FRAG, true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (selectedGunNumber == 1) {
            prefHelper.setScreenVisible(CommonUtils.GUN_1_LAST_CHARGING_SUMMARY_FRAG, false)
        } else {
            prefHelper.setScreenVisible(CommonUtils.GUN_2_LAST_CHARGING_SUMMARY_FRAG, false)
        }
    }

    override fun setupViews() {
        binding.apply {
            incEVMacAddress.tvSummaryLabel.text = getString(R.string.lbl_ev_mac_address)
            incEVMacAddress.tvSummaryUnit.invisible()
            incEVMacAddress.tvSummaryValue.text = getString(R.string.hint_mac_address)
            incEVMacAddress.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incChargingDuration.tvSummaryLabel.text = getString(R.string.lbl_charging_duration)
            incChargingDuration.tvSummaryUnit.visible()
            incChargingDuration.tvSummaryUnit.text = getString(R.string.lbl_min)
            incChargingDuration.tvSummaryValue.text = getString(R.string.hint_0)
            incChargingDuration.root.setBackgroundColorBasedOnTheme()

            incChargingStartDateTime.tvSummaryLabel.text =
                getString(R.string.lbl_charging_start_date_time)
            incChargingStartDateTime.tvSummaryUnit.invisible()
            incChargingStartDateTime.tvSummaryValue.text = getString(R.string.hint_date_time)
            incChargingStartDateTime.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incChargingEndDateTime.tvSummaryLabel.text =
                getString(R.string.lbl_charging_end_date_time)
            incChargingEndDateTime.tvSummaryUnit.invisible()
            incChargingEndDateTime.tvSummaryValue.text = getString(R.string.hint_date_time)
            incChargingEndDateTime.root.setBackgroundColorBasedOnTheme()

            incStartSOC.tvSummaryLabel.text = getString(R.string.lbl_start_soc)
            incStartSOC.tvSummaryUnit.visible()
            incStartSOC.tvSummaryUnit.text = getString(R.string.lbl_percentage)
            incStartSOC.tvSummaryValue.text = getString(R.string.hint_0)
            incStartSOC.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incEndSOC.tvSummaryLabel.text = getString(R.string.lbl_end_soc)
            incEndSOC.tvSummaryUnit.visible()
            incEndSOC.tvSummaryUnit.text = getString(R.string.lbl_percentage)
            incEndSOC.tvSummaryValue.text = getString(R.string.hint_0)
            incEndSOC.root.setBackgroundColorBasedOnTheme()

            incEnergyConsumption.tvSummaryLabel.text = getString(R.string.lbl_energy_consumption)
            incEnergyConsumption.tvSummaryUnit.visible()
            incEnergyConsumption.tvSummaryUnit.text = getString(R.string.lbl_kwh)
            incEnergyConsumption.tvSummaryValue.text = getString(R.string.hint_float)
            incEnergyConsumption.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incSessionEndReason.tvSummaryLabel.text = getString(R.string.lbl_session_end_reason)
            incSessionEndReason.tvSummaryUnit.invisible()
            incSessionEndReason.tvSummaryValue.text = ""
            incSessionEndReason.root.setBackgroundColorBasedOnTheme()


        }
    }

    override fun handleClicks() {}
}