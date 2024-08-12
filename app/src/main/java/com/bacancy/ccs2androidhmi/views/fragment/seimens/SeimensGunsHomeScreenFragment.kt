package com.bacancy.ccs2androidhmi.views.fragment.seimens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentSeimensGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.util.AppConfig.IS_SINGLE_GUN
import com.bacancy.ccs2androidhmi.util.DialogUtils.showChargingSummaryDialog
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_DENIED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.AUTHENTICATION_TIMEOUT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMMUNICATION_ERROR
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMPLETE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.EMERGENCY_STOP
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.ISOLATION_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.MAINS_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLC_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLUGGED_IN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PRECHARGE_FAIL
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.RECTIFIER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.RESERVED
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.UNAVAILABLE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.UNPLUGGED
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.PrefHelper.Companion.IS_DARK_THEME
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.fragment.GunsMoreInformationFragment
import com.bacancy.ccs2androidhmi.views.listener.DashboardActivityContract
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SeimensGunsHomeScreenFragment : BaseFragment() {

    private var isGun1ChargingStarted: Boolean = false
    private var isGun2ChargingStarted: Boolean = false
    private var shouldShowGun1SummaryDialog: Boolean = false
    private var shouldShowGun2SummaryDialog: Boolean = false
    private lateinit var binding: FragmentSeimensGunsHomeScreenBinding
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

    override fun setScreenHeaderViews() {
    }

    override fun setupViews() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeimensGunsHomeScreenBinding.inflate(layoutInflater)
        (requireActivity() as DashboardActivityContract).updateTopBar(true)
        observeLatestMiscInfo()
        observeGunsChargingInfo()
        if (IS_SINGLE_GUN) {
            binding.tvModelName.text = getString(R.string.lbl_controller_model_name)
        } else {
            binding.tvModelName.text = getString(R.string.lbl_controller_model_name_dual_gun)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        prefHelper.setBoolean("IS_IN_TEST_MODE", false)
        prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)
    }

    private fun observeLatestMiscInfo() {
        appViewModel.latestMiscInfo.observe(viewLifecycleOwner) { latestMiscInfo ->
            if (latestMiscInfo != null) {
                binding.tvUnitCost.text = getString(R.string.lbl_unit_cost, latestMiscInfo.unitPrice.toString())
            }
        }
    }

    private fun observeGunsChargingInfo() {
        appViewModel.getUpdatedGunsChargingInfo(1).observe(requireActivity()) {
            it?.let {
                updateGun1UI(it)
            }
        }
        appViewModel.getUpdatedGunsChargingInfo(2).observe(requireActivity()) {
            it?.let {
                updateGun2UI(it)
            }
        }
    }

    private fun updateGun1UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        if (isVisible) {
            prefHelper.setSelectedGunNumber(SELECTED_GUN, 0)
        }
        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                shouldShowGun1SummaryDialog = false
                binding.ivGun1.setImageResource(R.drawable.ic_single_gun_unplugged)
            }

            PLUGGED_IN -> {
                shouldShowGun1SummaryDialog = false
                binding.ivGun1.setImageResource(R.drawable.ic_single_gun_plugged)
            }

            CHARGING -> {
                shouldShowGun1SummaryDialog = false
                isGun1ChargingStarted = true
                binding.ivGun1.setImageResource(R.drawable.ic_single_gun_charging_in_process)
            }

            COMPLETE -> {
                binding.ivGun1.setImageResource(R.drawable.ic_single_gun_charging_completed)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT,
            EMERGENCY_STOP -> {
                binding.ivGun1.setImageResource(R.drawable.ic_single_gun_fault)
            }
        }

        when (tbGunsChargingInfo.gunChargingState) {
            COMPLETE,
            COMMUNICATION_ERROR,
            AUTHENTICATION_TIMEOUT,
            PLC_FAULT,
            RECTIFIER_FAULT,
            AUTHENTICATION_DENIED,
            PRECHARGE_FAIL,
            ISOLATION_FAIL,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT,
            MAINS_FAIL,
            UNAVAILABLE,
            RESERVED,
            EMERGENCY_STOP,
            -> {
                if (!shouldShowGun1SummaryDialog && isGun1ChargingStarted) {
                    isGun1ChargingStarted = false
                    shouldShowGun1SummaryDialog = true
                    observeGunsLastChargingSummary(1)
                }

            }
        }
    }

    private fun updateGun2UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                shouldShowGun2SummaryDialog = false
                binding.ivGun2.setImageResource(R.drawable.ic_single_gun_unplugged)
            }

            PLUGGED_IN -> {
                shouldShowGun2SummaryDialog = false
                binding.ivGun2.setImageResource(R.drawable.ic_single_gun_plugged)
            }

            CHARGING -> {
                shouldShowGun2SummaryDialog = false
                isGun2ChargingStarted = true
                binding.ivGun2.setImageResource(R.drawable.ic_single_gun_charging_in_process)
            }

            COMPLETE -> {
                binding.ivGun2.setImageResource(R.drawable.ic_single_gun_charging_completed)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT,
            EMERGENCY_STOP -> {
                binding.ivGun2.setImageResource(R.drawable.ic_single_gun_fault)
            }
        }

        when (tbGunsChargingInfo.gunChargingState) {
            COMPLETE,
            COMMUNICATION_ERROR,
            AUTHENTICATION_TIMEOUT,
            PLC_FAULT,
            RECTIFIER_FAULT,
            AUTHENTICATION_DENIED,
            PRECHARGE_FAIL,
            ISOLATION_FAIL,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT,
            MAINS_FAIL,
            UNAVAILABLE,
            RESERVED,
            EMERGENCY_STOP,
            -> {
                if (!shouldShowGun2SummaryDialog && isGun2ChargingStarted) {
                    isGun2ChargingStarted = false
                    shouldShowGun2SummaryDialog = true
                    observeGunsLastChargingSummary(2)
                }

            }
        }
    }

    private fun observeGunsLastChargingSummary(gunNumber: Int) {
        lifecycleScope.launch {
            delay(2000)
            try {
                appViewModel.getGunsLastChargingSummary(gunNumber)
                    .observe(requireActivity()) {
                        it?.let {
                            val isDarkTheme = prefHelper.getBoolean(IS_DARK_THEME, false)
                            when(gunNumber){
                                1 -> {
                                    if (shouldShowGun1SummaryDialog) {
                                        shouldShowGun1SummaryDialog = false
                                        requireContext().showChargingSummaryDialog(
                                            true,
                                            it,
                                            isDarkTheme
                                        ) {}
                                    }
                                }
                                2 -> {
                                    if (shouldShowGun2SummaryDialog) {
                                        shouldShowGun2SummaryDialog = false
                                        requireContext().showChargingSummaryDialog(
                                            true,
                                            it,
                                            isDarkTheme
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun handleClicks() {
        binding.frmLayoutGun1.setOnClickListener {
            openGunsMoreInfoFragment(1)
        }
        binding.frmLayoutGun2.setOnClickListener {
            openGunsMoreInfoFragment(2)
        }
    }

    private fun openGunsMoreInfoFragment(gunNumber: Int) {
        val bundle = Bundle()
        bundle.putInt(SELECTED_GUN, gunNumber)
        val fragment = GunsMoreInformationFragment()
        fragment.arguments = bundle
        fragmentChangeListener?.replaceFragment(fragment)
    }

}