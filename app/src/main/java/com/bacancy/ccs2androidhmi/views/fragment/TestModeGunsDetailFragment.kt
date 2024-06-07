package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentTestModeGunsDetailBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentTestModeHomeBinding
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.ToastUtils.showCustomToast
import com.bacancy.ccs2androidhmi.util.hideKeyboard
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TestModeGunsDetailFragment : BaseFragment() {

    private var isOutputOn: Boolean = false
    private lateinit var binding: FragmentTestModeGunsDetailBinding
    private var selectedGunNumber: Int = 1
    private var fragmentChangeListener: FragmentChangeListener? = null

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener) {
            fragmentChangeListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestModeGunsDetailBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        prefHelper.setBoolean("IS_IN_TEST_MODE", true)
        return binding.root
    }

    override fun setScreenHeaderViews() {
        selectedGunNumber = arguments?.getInt(GunsChargingInfoUtils.SELECTED_GUN)!!
        prefHelper.setIntValue("SELECTED_GUN_IN_TEST_MODE", selectedGunNumber)
        binding.apply {
            when (selectedGunNumber) {
                1 -> {
                    incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
                }

                2 -> {
                    incHeader.tvHeader.text = getString(R.string.lbl_gun_2)
                }
            }
        }
    }

    override fun setupViews() {
        selectedGunNumber = arguments?.getInt(GunsChargingInfoUtils.SELECTED_GUN)!!
        binding.apply {

            when (selectedGunNumber) {

                1 -> {
                    if (prefHelper.getIntValue("GUN1_OUTPUT_ON_OFF_VALUE", 0) == 1) {
                        isOutputOn = true
                        btnOutputOnOff.text = getString(R.string.lbl_output_off)
                    } else {
                        isOutputOn = false
                        btnOutputOnOff.text = getString(R.string.lbl_output_on)
                    }
                }

                2 -> {
                    if (prefHelper.getIntValue("GUN2_OUTPUT_ON_OFF_VALUE", 0) == 1) {
                        isOutputOn = true
                        btnOutputOnOff.text = getString(R.string.lbl_output_off)
                    } else {
                        isOutputOn = false
                        btnOutputOnOff.text = getString(R.string.lbl_output_on)
                    }
                }

            }

        }

    }

    override fun handleClicks() {

        binding.apply {

            edtGunVoltage.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    val edtGunVoltageValue = edtGunVoltage.text.toString().toIntOrNull()

                    if (edtGunVoltageValue in 200..1000) {
                        edtGunVoltageValue?.let {
                            prefHelper.setBoolean("IS_GUN_VOLTAGE_CHANGED", true)
                            if (selectedGunNumber == 1) {
                                prefHelper.setIntValue("GUN1_VOLTAGE", it)
                            } else {
                                prefHelper.setIntValue("GUN2_VOLTAGE", it)
                            }
                        }
                    } else {
                        requireContext().showCustomToast(getString(R.string.msg_please_input_voltage_between_200_to_1000_v), false)
                    }

                    edtGunVoltage.hideKeyboard(requireContext())
                    return@setOnEditorActionListener true
                }
                false
            }

            edtGunCurrent.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val enteredCurrentValue = edtGunCurrent.text.toString().toIntOrNull()
                    if (enteredCurrentValue in 0..100) {
                        enteredCurrentValue?.let {
                            prefHelper.setBoolean("IS_GUN_CURRENT_CHANGED", true)
                            if (selectedGunNumber == 1) {
                                prefHelper.setIntValue("GUN1_CURRENT", it)
                            } else {
                                prefHelper.setIntValue("GUN2_CURRENT", it)
                            }
                        }
                    } else {
                        requireContext().showCustomToast(getString(R.string.msg_please_input_current_between_0_to_100_a), false)
                    }

                    edtGunCurrent.hideKeyboard(requireContext())
                    return@setOnEditorActionListener true
                }
                false
            }

            btnOutputOnOff.setOnClickListener {
                prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", true)
                if (isOutputOn) {
                    isOutputOn = false
                    btnOutputOnOff.text = getString(R.string.lbl_output_on)
                } else {
                    isOutputOn = true
                    btnOutputOnOff.text = getString(R.string.lbl_output_off)
                }
                val value = if (isOutputOn) 1 else 0
                if (selectedGunNumber == 1) {
                    prefHelper.setIntValue("GUN1_OUTPUT_ON_OFF_VALUE", value)
                } else {
                    prefHelper.setIntValue("GUN2_OUTPUT_ON_OFF_VALUE", value)
                }
            }

            btnACMeterInfo.setOnClickListener {
                fragmentChangeListener?.replaceFragment(ACMeterInfoFragment())
            }

            btnDCMeterInfo.setOnClickListener {
                val gunsDCOutputInfoFragment = GunsDCOutputInfoFragment()
                gunsDCOutputInfoFragment.arguments = getBundleToPass()
                fragmentChangeListener?.replaceFragment(gunsDCOutputInfoFragment)
            }

        }

    }

    private fun getBundleToPass(): Bundle {
        val bundle = Bundle()
        bundle.putInt(GunsChargingInfoUtils.SELECTED_GUN, selectedGunNumber)
        return bundle
    }

}