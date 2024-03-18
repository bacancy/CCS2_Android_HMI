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
import com.bacancy.ccs2androidhmi.views.listener.DashboardActivityContract
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocalStartStopFragment : BaseFragment() {

    private var isGun1ChargingStarted: Boolean = false
    private var isGun2ChargingStarted: Boolean = false
    private lateinit var binding: FragmentLocalStartStopBinding
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalStartStopBinding.inflate(layoutInflater)
        (requireActivity() as DashboardActivityContract).updateTopBar(false)
        prefHelper.setBoolean(INSIDE_LOCAL_START_STOP_SCREEN, true)
        observeGunsChargingInfo()
        prefHelper.setBoolean(IS_GUN_1_CLICKED, false)
        prefHelper.setBoolean(IS_GUN_2_CLICKED, false)
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
                prefHelper.setBoolean(
                    GUN_1_LOCAL_START,
                    !prefHelper.getBoolean(GUN_1_LOCAL_START, false)
                )
                btnStartStopGun1.text = "Gun - 1 (Loading...)"
                binding.btnStartStopGun1.isEnabled = false
                binding.btnStartStopGun1.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.grey)
            }

            btnStartStopGun2.setOnClickListener {
                prefHelper.setBoolean(IS_GUN_2_CLICKED, true)
                prefHelper.setBoolean(
                    GUN_2_LOCAL_START,
                    !prefHelper.getBoolean(GUN_2_LOCAL_START, false)
                )
                btnStartStopGun2.text = "Gun - 2 (Loading...)"
                btnStartStopGun2.isEnabled = false
                btnStartStopGun2.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.grey)
            }

        }

    }

    private fun observeGunsChargingInfo() {

        appViewModel.getUpdatedGunsChargingInfo(1).observe(viewLifecycleOwner) {
            it?.let {
                when (it.gunChargingState) {

                    GunsChargingInfoUtils.PLUGGED_IN -> {
                        if (prefHelper.getBoolean(IS_GUN_1_CLICKED, false)) {
                            binding.btnStartStopGun1.isEnabled = false
                            binding.btnStartStopGun1.backgroundTintList =
                                ContextCompat.getColorStateList(requireContext(), R.color.grey)
                            binding.btnStartStopGun1.text = getString(R.string.lbl_gun_1_loading)
                        } else {
                            binding.btnStartStopGun1.isEnabled = true
                            binding.btnStartStopGun1.backgroundTintList = null
                            binding.btnStartStopGun1.text = getString(R.string.lbl_start_gun_1)
                        }

                    }

                    GunsChargingInfoUtils.CHARGING -> {
                        isGun1ChargingStarted = true
                        if (prefHelper.getBoolean(IS_GUN_1_CLICKED, false)) {
                            prefHelper.setBoolean(IS_GUN_1_CLICKED, false)
                            binding.btnStartStopGun1.isEnabled = false
                            binding.btnStartStopGun1.backgroundTintList =
                                ContextCompat.getColorStateList(requireContext(), R.color.grey)
                            binding.btnStartStopGun1.text = getString(R.string.lbl_gun_1_loading)
                        } else {
                            binding.btnStartStopGun1.isEnabled = true
                            binding.btnStartStopGun1.backgroundTintList = null
                            binding.btnStartStopGun1.text = getString(R.string.lbl_stop_gun_1)
                        }

                    }

                    else -> {
                        if (isGun1ChargingStarted) {
                            isGun1ChargingStarted = false
                            prefHelper.setBoolean(IS_GUN_1_CLICKED, false)
                        }
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
                        if (prefHelper.getBoolean(IS_GUN_2_CLICKED, false)) {
                            binding.btnStartStopGun2.isEnabled = false
                            binding.btnStartStopGun2.backgroundTintList =
                                ContextCompat.getColorStateList(requireContext(), R.color.grey)
                            binding.btnStartStopGun2.text = getString(R.string.lbl_gun_2_loading)
                        } else {
                            binding.btnStartStopGun2.isEnabled = true
                            binding.btnStartStopGun2.backgroundTintList = null
                            binding.btnStartStopGun2.text = getString(R.string.lbl_start_gun_2)
                        }

                    }

                    GunsChargingInfoUtils.CHARGING -> {
                        isGun2ChargingStarted = true
                        if (prefHelper.getBoolean(IS_GUN_2_CLICKED, false)) {
                            prefHelper.setBoolean(IS_GUN_2_CLICKED, false)
                            binding.btnStartStopGun2.isEnabled = false
                            binding.btnStartStopGun2.backgroundTintList =
                                ContextCompat.getColorStateList(requireContext(), R.color.grey)
                            binding.btnStartStopGun2.text = getString(R.string.lbl_gun_2_loading)
                        } else {
                            binding.btnStartStopGun2.isEnabled = true
                            binding.btnStartStopGun2.backgroundTintList = null
                            binding.btnStartStopGun2.text = getString(R.string.lbl_stop_gun_2)
                        }

                    }

                    else -> {
                        if (isGun2ChargingStarted) {
                            isGun2ChargingStarted = false
                            prefHelper.setBoolean(IS_GUN_2_CLICKED, false)
                        }
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