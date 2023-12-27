package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsChargingSummaryBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible

class GunsLastChargingSummaryFragment : BaseFragment() {

    private lateinit var binding: FragmentGunsChargingSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsChargingSummaryBinding.inflate(layoutInflater)
        setScreenHeaderViews()
        setupViews()
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
            incHeader.tvSubHeader.text = getString(R.string.lbl_charging_summary)
            incHeader.tvSubHeader.visible()
        }
    }

    override fun setupViews() {
        binding.apply {
            incEVMacAddress.tvSummaryLabel.text = getString(R.string.lbl_ev_mac_address)
            incEVMacAddress.tvSummaryUnit.gone()
            incEVMacAddress.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incChargingDuration.tvSummaryLabel.text = getString(R.string.lbl_charging_duration)
            incChargingDuration.tvSummaryUnit.visible()
            incChargingDuration.tvSummaryUnit.text = getString(R.string.lbl_min)
            incChargingDuration.root.setBackgroundColor(resources.getColor(R.color.black))

            incChargingStartDateTime.tvSummaryLabel.text =
                getString(R.string.lbl_charging_start_date_time)
            incChargingStartDateTime.tvSummaryUnit.gone()
            incChargingStartDateTime.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incChargingEndDateTime.tvSummaryLabel.text =
                getString(R.string.lbl_charging_end_date_time)
            incChargingEndDateTime.tvSummaryUnit.gone()
            incChargingEndDateTime.root.setBackgroundColor(resources.getColor(R.color.black))

            incStartSOC.tvSummaryLabel.text = getString(R.string.lbl_start_soc)
            incStartSOC.tvSummaryUnit.visible()
            incStartSOC.tvSummaryUnit.text = getString(R.string.lbl_percentage)
            incStartSOC.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incEndSOC.tvSummaryLabel.text = getString(R.string.lbl_end_soc)
            incEndSOC.tvSummaryUnit.visible()
            incEndSOC.tvSummaryUnit.text = getString(R.string.lbl_percentage)
            incEndSOC.root.setBackgroundColor(resources.getColor(R.color.black))

            incEnergyConsumption.tvSummaryLabel.text = getString(R.string.lbl_energy_consumption)
            incEnergyConsumption.tvSummaryUnit.visible()
            incEnergyConsumption.tvSummaryUnit.text = getString(R.string.lbl_kwh)
            incEnergyConsumption.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incSessionEndReason.tvSummaryLabel.text = getString(R.string.lbl_session_end_reason)
            incSessionEndReason.tvSummaryUnit.gone()
            incSessionEndReason.root.setBackgroundColor(resources.getColor(R.color.black))


        }
    }
}