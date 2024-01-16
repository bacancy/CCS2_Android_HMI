package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.COMPLETE
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.EMERGENCY_STOP
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLC_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PLUGGED_IN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.PREPARING_FOR_CHARGING
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.RECTIFIER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SELECTED_GUN
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SMOKE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.SPD_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TAMPER_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.TEMPERATURE_FAULT
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils.UNPLUGGED
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.invisible
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GunsHomeScreenFragment : BaseFragment() {

    private lateinit var binding: FragmentGunsHomeScreenBinding
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
        binding = FragmentGunsHomeScreenBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(false)
        observeGunsChargingInfo()
        return binding.root
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

        binding.tvGun1Label.text = getString(R.string.gun_1_info, tbGunsChargingInfo.gunChargingState)

        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_unplugged)
            }

            PLUGGED_IN -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_plugged)
                binding.tvGun1Label.text = getString(R.string.lbl_gun1_plugged_in)
            }

            CHARGING, PREPARING_FOR_CHARGING -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            COMPLETE -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_fault)
            }

            EMERGENCY_STOP -> {
                binding.tvEmergencyStop.visible()
            }

            else -> {
                binding.tvGun1Label.text = getString(R.string.gun_1_info, tbGunsChargingInfo.gunChargingState)
                binding.tvEmergencyStop.invisible()
            }
        }
    }

    private fun updateGun2UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        if (isVisible) {
            prefHelper.setSelectedGunNumber(SELECTED_GUN, 0)
        }

        binding.tvEmergencyStop.invisible()

        binding.tvGun2Label.text = getString(R.string.gun_2_info, tbGunsChargingInfo.gunChargingState)

        when (tbGunsChargingInfo.gunChargingState) {
            UNPLUGGED -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_unplugged)
            }

            PLUGGED_IN -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_plugged)
                binding.tvGun2Label.text = getString(R.string.lbl_gun2_plugged_in)
            }

            CHARGING, PREPARING_FOR_CHARGING -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
            }

            COMPLETE -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging)
            }

            PLC_FAULT,
            RECTIFIER_FAULT,
            TEMPERATURE_FAULT,
            SPD_FAULT,
            SMOKE_FAULT,
            TAMPER_FAULT -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_fault)
            }

            EMERGENCY_STOP -> {
                binding.tvEmergencyStop.visible()
            }

            else -> {
                binding.tvGun2Label.text = getString(R.string.gun_2_info, tbGunsChargingInfo.gunChargingState)
                binding.tvEmergencyStop.invisible()
            }
        }

    }

    override fun handleClicks() {
        binding.tvGun1Actor.setOnClickListener {
            openGunsMoreInfoFragment(1)
        }

        binding.tvGun2Actor.setOnClickListener {
            openGunsMoreInfoFragment(2)
        }

        binding.ivScreenInfo.setOnClickListener {
            //showCustomDialog(getString(R.string.msg_dialog_home_screen)) {}
            fragmentChangeListener?.replaceFragment(FirmwareVersionInfoFragment())
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