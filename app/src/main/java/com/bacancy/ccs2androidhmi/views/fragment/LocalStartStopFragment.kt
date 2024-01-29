package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentLocalStartStopBinding
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_1_LOCAL_START
import com.bacancy.ccs2androidhmi.util.CommonUtils.GUN_2_LOCAL_START
import com.bacancy.ccs2androidhmi.util.CommonUtils.INSIDE_LOCAL_START_STOP_SCREEN
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_GUN_1_CLICKED
import com.bacancy.ccs2androidhmi.util.CommonUtils.IS_GUN_2_CLICKED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocalStartStopFragment : BaseFragment() {

    private lateinit var binding: FragmentLocalStartStopBinding
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    private var isGun1Started = false
    private var isGun2Started = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalStartStopBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(true)
        prefHelper.setBoolean(INSIDE_LOCAL_START_STOP_SCREEN, true)
        observeGunsChargingInfo()
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_local_start_or_stop_charging)
    }

    override fun setupViews() {}

    override fun handleClicks() {

        binding.apply {

            btnStartStopGun1.setOnClickListener {
                prefHelper.setBoolean(IS_GUN_1_CLICKED, true)
                if (isGun1Started) {
                    isGun1Started = false
                    btnStartStopGun1.text = getString(R.string.lbl_start_gun_1)
                } else {
                    isGun1Started = true
                    btnStartStopGun1.text = getString(R.string.lbl_stop_gun_1)
                }
                prefHelper.setBoolean(GUN_1_LOCAL_START, isGun1Started)
            }

            btnStartStopGun2.setOnClickListener {
                prefHelper.setBoolean(IS_GUN_2_CLICKED, true)
                if (isGun2Started) {
                    isGun2Started = false
                    btnStartStopGun2.text = getString(R.string.lbl_start_gun_2)
                } else {
                    isGun2Started = true
                    btnStartStopGun2.text = getString(R.string.lbl_stop_gun_2)
                }
                prefHelper.setBoolean(GUN_2_LOCAL_START, isGun2Started)
            }

        }

    }

    private fun observeGunsChargingInfo() {

        appViewModel.getUpdatedGunsChargingInfo(1).observe(requireActivity()) {
            it?.let {
                when (it.gunChargingState) {

                    GunsChargingInfoUtils.PLUGGED_IN -> {
                        isGun1Started = false
                        binding.btnStartStopGun1.isEnabled  = true
                        binding.btnStartStopGun1.text = getString(R.string.lbl_start_gun_1)
                    }

                    GunsChargingInfoUtils.CHARGING -> {
                        isGun1Started = true
                        binding.btnStartStopGun1.isEnabled  = true
                        binding.btnStartStopGun1.text = getString(R.string.lbl_stop_gun_1)
                    }

                    else -> {
                        isGun1Started = false
                        binding.btnStartStopGun1.isEnabled  = false
                        binding.btnStartStopGun1.text = "Gun - 1 (${it.gunChargingState})"
                    }
                }
            }
        }

        appViewModel.getUpdatedGunsChargingInfo(2).observe(requireActivity()) {
            it?.let {
                when (it.gunChargingState) {
                    GunsChargingInfoUtils.PLUGGED_IN -> {
                        isGun2Started = false
                        binding.btnStartStopGun2.isEnabled  = true
                        binding.btnStartStopGun2.text = getString(R.string.lbl_start_gun_2)
                    }

                    GunsChargingInfoUtils.CHARGING -> {
                        isGun2Started = true
                        binding.btnStartStopGun2.isEnabled  = true
                        binding.btnStartStopGun2.text = getString(R.string.lbl_stop_gun_2)
                    }

                    else -> {
                        isGun2Started = false
                        binding.btnStartStopGun2.isEnabled  = false
                        binding.btnStartStopGun2.text = "Gun - 2 (${it.gunChargingState})"
                    }
                }
            }
        }

    }
}