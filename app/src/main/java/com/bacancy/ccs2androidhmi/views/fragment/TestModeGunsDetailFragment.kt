package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentTestModeHomeBinding
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TestModeGunsDetailFragment : BaseFragment() {

    private lateinit var binding: FragmentTestModeHomeBinding
    private val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestModeHomeBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(true)
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_test_mode)
    }

    override fun setupViews() {}

    override fun handleClicks() {

        binding.apply {

        }

    }

}