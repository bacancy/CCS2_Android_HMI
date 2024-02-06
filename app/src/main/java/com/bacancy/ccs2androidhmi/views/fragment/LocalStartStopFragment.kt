package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalStartStopBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
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
                if (prefHelper.getBoolean(GUN_1_LOCAL_START, false)) {
                    prefHelper.setBoolean(GUN_1_LOCAL_START, false)
                    btnStartStopGun1.text = getString(R.string.lbl_start_gun_1)
                } else {
                    prefHelper.setBoolean(GUN_1_LOCAL_START, true)
                    btnStartStopGun1.text = getString(R.string.lbl_stop_gun_1)
                }

            }

            btnStartStopGun2.setOnClickListener {
                prefHelper.setBoolean(IS_GUN_2_CLICKED, true)
                if (prefHelper.getBoolean(GUN_2_LOCAL_START, false)) {
                    prefHelper.setBoolean(GUN_2_LOCAL_START, false)
                    btnStartStopGun2.text = getString(R.string.lbl_start_gun_2)
                } else {
                    prefHelper.setBoolean(GUN_2_LOCAL_START, true)
                    btnStartStopGun2.text = getString(R.string.lbl_stop_gun_2)
                }
            }

        }

    }

    private fun observeGunsChargingInfo() {

        appViewModel.getUpdatedGunsChargingInfo(1).observe(viewLifecycleOwner) {
            it?.let {
                when (it.gunChargingState) {

                    GunsChargingInfoUtils.PLUGGED_IN -> {
                        binding.btnStartStopGun1.isEnabled = true
                        binding.btnStartStopGun1.backgroundTintList = null
                        binding.btnStartStopGun1.text = getString(R.string.lbl_start_gun_1)
                    }

                    GunsChargingInfoUtils.CHARGING -> {
                        binding.btnStartStopGun1.isEnabled = true
                        binding.btnStartStopGun1.backgroundTintList = null
                        binding.btnStartStopGun1.text = getString(R.string.lbl_stop_gun_1)
                    }

                    else -> {
                        binding.btnStartStopGun1.isEnabled = false
                        binding.btnStartStopGun1.backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), R.color.grey)
                        binding.btnStartStopGun1.text = "Gun - 1 (${it.gunChargingState})"
                    }
                }
            }
        }

        appViewModel.getUpdatedGunsChargingInfo(2).observe(viewLifecycleOwner) {
            it?.let {
                when (it.gunChargingState) {
                    GunsChargingInfoUtils.PLUGGED_IN -> {
                        binding.btnStartStopGun2.isEnabled = true
                        binding.btnStartStopGun2.backgroundTintList = null
                        binding.btnStartStopGun2.text = getString(R.string.lbl_start_gun_2)
                    }

                    GunsChargingInfoUtils.CHARGING -> {
                        binding.btnStartStopGun2.isEnabled = true
                        binding.btnStartStopGun2.backgroundTintList = null
                        binding.btnStartStopGun2.text = getString(R.string.lbl_stop_gun_2)
                    }

                    else -> {
                        binding.btnStartStopGun2.isEnabled = false
                        binding.btnStartStopGun2.backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), R.color.grey)
                        binding.btnStartStopGun2.text = "Gun - 2 (${it.gunChargingState})"
                    }
                }
            }
        }

    }
}