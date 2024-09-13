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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        setupFaultAlarmCounters()
    }

    private fun setupFaultAlarmLabels() {
        binding.apply {
            incGun1TempAlarm.tvFaultName.text = "Gun 1 Temperature"
            incGun2TempAlarm.tvFaultName.text = "Gun 2 Temperature"
            incSystemTempHighAlarm.tvFaultName.text = "System Temperature"
            incMainsLowAlarm.tvFaultName.text = "Mains Low"
            incMainsHighAlarm.tvFaultName.text = "Mains High"
        }
    }

    private fun setupFaultAlarmCounters() {
        Log.d("FRITAG", "Lifecycle state when setting observer: ${viewLifecycleOwner.lifecycle.currentState}")

        /*appViewModel.faultCounters.observe(viewLifecycleOwner) {
            Log.d("FRITAG", "setupFaultAlarmCounters: $it")
            *//*binding.apply {
                incGun1TempAlarm.tvFaultCounter.text = it.gun1TempCounter.toString()
                incGun2TempAlarm.tvFaultCounter.text = it.gun2TempCounter.toString()
                incSystemTempHighAlarm.tvFaultCounter.text = it.systemTempCounter.toString()
                incMainsLowAlarm.tvFaultCounter.text = it.mainsLowCounter.toString()
                incMainsHighAlarm.tvFaultCounter.text = it.mainsHighCounter.toString()
            }*//*
        }*/

        appViewModel.basicNumber.observe(viewLifecycleOwner) { it ->
            Log.d("WINTAG","$it")
        }
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_faults_alarm_counter)
    }

    override fun setupViews() {}

    override fun handleClicks() {}
}