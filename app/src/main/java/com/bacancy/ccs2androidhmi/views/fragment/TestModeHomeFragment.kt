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
import com.bacancy.ccs2androidhmi.databinding.FragmentTestModeHomeBinding
import com.bacancy.ccs2androidhmi.util.GunsChargingInfoUtils
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TestModeHomeFragment : BaseFragment() {

    private lateinit var binding: FragmentTestModeHomeBinding
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    private var fragmentChangeListener: FragmentChangeListener? = null

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
        binding = FragmentTestModeHomeBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.i("TAG", "onResume: TestMode Called")
        prefHelper.setBoolean("IS_IN_TEST_MODE", true)
    }

    override fun onPause() {
        super.onPause()
        Log.i("TAG", "onPause: TestMode Called")
        prefHelper.setBoolean("IS_IN_TEST_MODE", false)
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_test_mode)
    }

    override fun setupViews() {}

    override fun handleClicks() {

        binding.apply {

            btnGun1.setOnClickListener {
                openTestModeGunsDetailFragment(1)
            }

            btnGun2.setOnClickListener {
                openTestModeGunsDetailFragment(2)
            }

        }

    }

    private fun openTestModeGunsDetailFragment(gunNumber: Int) {
        val bundle = Bundle()
        bundle.putInt(GunsChargingInfoUtils.SELECTED_GUN, gunNumber)
        val fragment = TestModeGunsDetailFragment()
        fragment.arguments = bundle
        fragmentChangeListener?.replaceFragment(fragment)
    }

}