package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsChargingInfo
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
        handleClicks()
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
            prefHelper.setSelectedGunNumber("SELECTED_GUN", 0)
        }
        val gunStateList = listOf(
            "Unplugged",
            "Plugged In & Waiting for Authentication",
            "Charging",
            "Complete",
            "Fault"
        )

        val gun1State = gunStateList[3]
        val gun2State = gunStateList[0]
        binding.tvGun1Label.text = "GUN - 1 \n(${tbGunsChargingInfo.gunChargingState})"

        when (tbGunsChargingInfo.gunChargingState) {
            "Unplugged" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_unplugged)
            }

            "Plugged In & Waiting for Authentication" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_plugged)
                binding.tvGun1Label.text = "GUN - 1 \n" + "(Plugged In)"
            }

            "Charging", "Preparing For Charging" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            "Complete" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging)
            }

            "PLC Fault", "Rectifier Fault", "Temperature Fault", "SPD Fault", "Smoke Fault", "Tamper Fault" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_fault)
            }

            "Emergency Stop" -> {
                binding.tvEmergencyStop.visible()
            }

            else -> {
                binding.tvGun1Label.text = "GUN - 1 \n(${tbGunsChargingInfo.gunChargingState})"
                binding.tvEmergencyStop.invisible()
            }
        }
    }

    private fun updateGun2UI(tbGunsChargingInfo: TbGunsChargingInfo) {
        if (isVisible) {
            prefHelper.setSelectedGunNumber("SELECTED_GUN", 0)
        }
        binding.tvGun2Label.text = "GUN - 2 \n" + "(${tbGunsChargingInfo.gunChargingState})"
        binding.tvEmergencyStop.invisible()
        when (tbGunsChargingInfo.gunChargingState) {
            "Unplugged" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_unplugged)
            }

            "Plugged In & Waiting for Authentication" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_plugged)
                binding.tvGun2Label.text = "GUN - 2 \n" + "(Plugged In)"
            }

            "Charging", "Preparing For Charging" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
            }

            "Complete" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging)
            }

            "PLC Fault", "Rectifier Fault", "Temperature Fault", "SPD Fault", "Smoke Fault", "Tamper Fault" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_fault)
            }

            "Emergency Stop" -> {
                binding.tvEmergencyStop.visible()
            }

            else -> {
                binding.tvGun2Label.text = "GUN - 2 \n(${tbGunsChargingInfo.gunChargingState})"
            }
        }
    }

    private fun handleClicks() {
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
        bundle.putInt("SELECTED_GUN", gunNumber)
        val fragment = GunsMoreInformationFragment()
        fragment.arguments = bundle
        fragmentChangeListener?.replaceFragment(fragment)
    }

    companion object {
    }
}