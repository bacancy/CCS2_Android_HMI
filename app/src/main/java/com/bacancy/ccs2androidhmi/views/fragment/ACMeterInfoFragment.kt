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
import com.bacancy.ccs2androidhmi.databinding.FragmentAcMeterInfoBinding
import com.bacancy.ccs2androidhmi.util.setValue
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ACMeterInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentAcMeterInfoBinding
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
        binding = FragmentAcMeterInfoBinding.inflate(layoutInflater)
        setScreenHeaderViews()
        setupViews()
        getLatestAcMeterInfo()
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(true)
        return binding.root
    }

    private fun getLatestAcMeterInfo() {
        appViewModel.latestAcMeterInfo.observe(requireActivity()) { latestAcMeterInfo ->
            if(latestAcMeterInfo!=null){
                Log.d("TAG", "getLatestAcMeterInfo: ${Gson().toJson(latestAcMeterInfo)}")
                lifecycleScope.launch(Dispatchers.Main) {

                    latestAcMeterInfo.apply {
                        binding.incVoltageV1N.tvValue.setValue(voltageV1N)
                        binding.incVoltageV2N.tvValue.setValue(voltageV2N)
                        binding.incVoltageV3N.tvValue.setValue(voltageV3N)
                        binding.incAvgVoltageLN.tvValue.setValue(averageVoltageLN)
                        binding.incCurrentI1.tvValue.setValue(currentL1)
                        binding.incCurrentI2.tvValue.setValue(currentL2)
                        binding.incCurrentI3.tvValue.setValue(currentL3)
                        binding.incAvgCurrent.tvValue.setValue(averageCurrent)
                        binding.incFrequency.tvValue.setValue(frequency)
                        binding.incActivePower.tvValue.setValue(activePower)
                        binding.incTotalPower.tvValue.setValue(totalPower)
                    }

                }
            }
        }
    }

    override fun setupViews() {

        binding.apply {

            incVoltageV1N.tvLabel.text = getString(R.string.lbl_voltage_v1n)
            incVoltageV1N.tvValueUnit.text = getString(R.string.lbl_v)

            incVoltageV2N.tvLabel.text = getString(R.string.lbl_voltage_v2n)
            incVoltageV2N.tvValueUnit.text = getString(R.string.lbl_v)

            incVoltageV3N.tvLabel.text = getString(R.string.lbl_voltage_v3n)
            incVoltageV3N.tvValueUnit.text = getString(R.string.lbl_v)

            incAvgVoltageLN.tvLabel.text = getString(R.string.lbl_avg_voltage_ln)
            incAvgVoltageLN.tvValueUnit.text = getString(R.string.lbl_v)

            incCurrentI1.tvLabel.text = getString(R.string.lbl_current_i1)
            incCurrentI1.tvValueUnit.text = getString(R.string.lbl_a)

            incCurrentI2.tvLabel.text = getString(R.string.lbl_current_i2)
            incCurrentI2.tvValueUnit.text = getString(R.string.lbl_a)

            incCurrentI3.tvLabel.text = getString(R.string.lbl_current_i3)
            incCurrentI3.tvValueUnit.text = getString(R.string.lbl_a)

            incAvgCurrent.tvLabel.text = getString(R.string.lbl_avg_current)
            incAvgCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incFrequency.tvLabel.text = getString(R.string.lbl_frequency_hz)
            incFrequency.tvValueUnit.text = getString(R.string.lbl_hz)

            incActivePower.tvLabel.text = getString(R.string.lbl_active_power)
            incActivePower.tvValueUnit.text = getString(R.string.lbl_kw)

            incTotalPower.tvLabel.text = getString(R.string.lbl_total_power)
            incTotalPower.tvValueUnit.text = getString(R.string.lbl_kwh)
        }

    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_ac_meter_information)
    }

}