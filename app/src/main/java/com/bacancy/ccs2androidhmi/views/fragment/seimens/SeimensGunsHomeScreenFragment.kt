package com.bacancy.ccs2androidhmi.views.fragment.seimens

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
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentSeimensGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.util.CommonUtils.INSIDE_LOCAL_START_STOP_SCREEN
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
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PREPARING_FOR_CHARGING
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
import com.bacancy.ccs2androidhmi.util.TextViewUtils.removeBlinking
import com.bacancy.ccs2androidhmi.util.TextViewUtils.startBlinking
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
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
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        prefHelper.setBoolean(INSIDE_LOCAL_START_STOP_SCREEN, false)
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
    }

    private fun updateGun1UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                binding.ivSingleGun.setImageResource(R.drawable.ic_single_gun_unplugged)
            }

            PLUGGED_IN -> {
                binding.ivSingleGun.setImageResource(R.drawable.ic_single_gun_plugged)
            }

            CHARGING -> {
                binding.ivSingleGun.setImageResource(R.drawable.ic_single_gun_charging_in_process)
            }

            COMPLETE -> {
                binding.ivSingleGun.setImageResource(R.drawable.ic_single_gun_charging_completed)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT,
            EMERGENCY_STOP -> {
                binding.ivSingleGun.setImageResource(R.drawable.ic_single_gun_fault)
            }
        }
    }

    private fun observeGunsLastChargingSummary() {
        lifecycleScope.launch {
            delay(2000)
            try {
                appViewModel.getGunsLastChargingSummary(1)
                    .observe(requireActivity()) {
                        it?.let {
                            val isDarkTheme = prefHelper.getBoolean(IS_DARK_THEME, false)
                            if (shouldShowGun1SummaryDialog) {
                                shouldShowGun1SummaryDialog = false
                                requireContext().showChargingSummaryDialog(
                                    true,
                                    it,
                                    isDarkTheme
                                ) {}
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun handleClicks() {
        binding.ivSingleGun.setOnClickListener {
            openGunsMoreInfoFragment()
        }
    }

    private fun openGunsMoreInfoFragment() {
        val bundle = Bundle()
        bundle.putInt(SELECTED_GUN, 1)
        val fragment = GunsMoreInformationFragment()
        fragment.arguments = bundle
        fragmentChangeListener?.replaceFragment(fragment)
    }

}