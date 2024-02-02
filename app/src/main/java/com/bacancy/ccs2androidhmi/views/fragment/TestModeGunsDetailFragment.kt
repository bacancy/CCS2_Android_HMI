package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentTestModeGunsDetailBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentTestModeHomeBinding
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.hideKeyboard
import com.bacancy.ccs2androidhmi.util.showToast
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TestModeGunsDetailFragment : BaseFragment() {

    private var isOutputOn: Boolean = false
    private lateinit var binding: FragmentTestModeGunsDetailBinding
    private val appViewModel: AppViewModel by viewModels()
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
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(true)
        prefHelper.setBoolean("IS_IN_TEST_MODE", true)
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        prefHelper.setBoolean("IS_GUN_VOLTAGE_CHANGED", false)
        prefHelper.setBoolean("IS_GUN_CURRENT_CHANGED", false)
        prefHelper.setBoolean("IS_OUTPUT_ON_OFF_VALUE_CHANGED", false)
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

    override fun setupViews() {}

    override fun handleClicks() {

        binding.apply {

            edtGunVoltage.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    val edtGunVoltageValue = edtGunVoltage.text.toString().toIntOrNull()

                    if (edtGunVoltageValue in 200..1000) {
                        //showToast("Good ${edtGunVoltageValue ?: ""}")
                        edtGunVoltageValue?.let {
                            prefHelper.setBoolean("IS_GUN_VOLTAGE_CHANGED", true)
                            if (selectedGunNumber == 1) {
                                prefHelper.setIntValue("GUN1_VOLTAGE", it)
                            } else {
                                prefHelper.setIntValue("GUN2_VOLTAGE", it)
                            }
                        }
                    } else {
                        showToast("Please input voltage between 200 to 1000 V")
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
                        //showToast("Good ${enteredCurrentValue ?: ""}")
                        enteredCurrentValue?.let {
                            prefHelper.setBoolean("IS_GUN_CURRENT_CHANGED", true)
                            if (selectedGunNumber == 1) {
                                prefHelper.setIntValue("GUN1_CURRENT", it)
                            } else {
                                prefHelper.setIntValue("GUN2_CURRENT", it)
                            }
                        }
                    } else {
                        showToast("Please input current between 0 to 100 A")
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
                prefHelper.setIntValue("OUTPUT_ON_OFF_VALUE", value)
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