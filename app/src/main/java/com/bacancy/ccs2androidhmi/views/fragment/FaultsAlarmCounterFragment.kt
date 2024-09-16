package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentFaultsAlarmCounterBinding
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FaultsAlarmCounterFragment : BaseFragment() {

    private lateinit var binding: FragmentFaultsAlarmCounterBinding
    val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFaultsAlarmCounterBinding.inflate(layoutInflater)
        setupFaultAlarmLabels()
        setupFaultAlarmCounters()
        return binding.root
    }

    private fun setupFaultAlarmLabels() {
        binding.apply {
            incGun1TempAlarm.tvFaultName.text = getString(R.string.lbl_gun_1_temperature)
            incGun2TempAlarm.tvFaultName.text = getString(R.string.lbl_gun_2_temperature)
            incSystemTempHighAlarm.tvFaultName.text = getString(R.string.lbl_system_temperature)
            incMainsLowAlarm.tvFaultName.text = getString(R.string.lbl_mains_low)
            incMainsHighAlarm.tvFaultName.text = getString(R.string.lbl_mains_high)
        }
    }

    private fun setupFaultAlarmCounters() {
        appViewModel.faultCounters.observe(requireActivity()) {
            binding.apply {
                if(it!=null){
                    incGun1TempAlarm.tvFaultCounter.text = it.gun1TempCounter.toString()
                    incGun2TempAlarm.tvFaultCounter.text = it.gun2TempCounter.toString()
                    incSystemTempHighAlarm.tvFaultCounter.text = it.systemTempCounter.toString()
                    incMainsLowAlarm.tvFaultCounter.text = it.mainsLowCounter.toString()
                    incMainsHighAlarm.tvFaultCounter.text = it.mainsHighCounter.toString()
                }
            }
        }
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_faults_alarm_counter)
    }

    override fun setupViews() {}

    override fun handleClicks() {}
}