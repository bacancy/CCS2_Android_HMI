package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.util.DialogUtils.showAlertDialog
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GunsHomeScreenFragment : BaseFragment() {

    private lateinit var binding: FragmentGunsHomeScreenBinding
    private var fragmentChangeListener: FragmentChangeListener? = null
    private val appViewModel: AppViewModel by viewModels()

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
        updateGunsUI()
        return binding.root
    }

    private fun updateGunsUI() {

        val gunStateList = listOf("Unplugged", "Plugged", "Charging", "Completed", "Fault")

        val gun1State = gunStateList[3]
        val gun2State = gunStateList[0]

        when (gun1State) {
            "Unplugged" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_unplugged)
            }

            "Plugged" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_plugged)
            }

            "Charging" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging)
            }

            "Completed" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_charging_completed)
            }

            "Fault" -> {
                binding.ivGun1Half.setImageResource(R.drawable.img_gun1_fault)
            }
        }

        when (gun2State) {
            "Unplugged" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_unplugged)
            }

            "Plugged" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_plugged)
            }

            "Charging" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging)
            }

            "Completed" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_charging_completed)
            }

            "Fault" -> {
                binding.ivGun2Half.setImageResource(R.drawable.img_gun2_fault)
            }
        }
    }

    private fun observeGunsChargingInfo() {

        appViewModel.getUpdatedGunsChargingInfo(1).observe(requireActivity()) {
            Log.d("GunsHomeScreen", "observeGunsChargingInfo: Gun 1 = ${Gson().toJson(it)}")
        }

        appViewModel.getUpdatedGunsChargingInfo(2).observe(requireActivity()) {
            Log.d("GunsHomeScreen", "observeGunsChargingInfo: Gun 2 = ${Gson().toJson(it)}")
        }

    }

    private fun handleClicks() {
        binding.btnClickHereForMore1.setOnClickListener {
            openGunsMoreInfoFragment(1)
        }

        binding.btnClickHereForMore2.setOnClickListener {
            openGunsMoreInfoFragment(2)
        }

        binding.btnFirmwareVersion.setOnClickListener {
            fragmentChangeListener?.replaceFragment(FirmwareVersionInfoFragment())
        }

        binding.ivScreenInfo.setOnClickListener {
            val title = "Info"
            val message = "To start the authentication process or view detailed parameter information, tap a specific gun icon."

            val okButton = Pair("Close") {
                // Ok button click action
                // Add your code here
            }

            val cancelButton = Pair("Cancel") {
                // Cancel button click action
                // Add your code here
            }

            requireContext().showAlertDialog(title, message, okButton)
        }
    }

    private fun openGunsMoreInfoFragment(gunNumber: Int) {
        val bundle = Bundle()
        bundle.putInt("SELECTED_GUN", gunNumber)
        val fragment = GunsMoreInformationFragment()
        fragment.arguments = bundle
        fragmentChangeListener?.replaceFragment(fragment)
    }
}