package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentCdmConfigurationBinding
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CDMConfigurationFragment : BaseFragment() {

    private lateinit var binding: FragmentCdmConfigurationBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCdmConfigurationBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        observeConfigurationParameters()
        return binding.root
    }

    private fun observeConfigurationParameters() {
        appViewModel.getConfigurationParameters.observe(viewLifecycleOwner){
            Log.d("CDMConfigurationFragment", "observeConfigurationParameters: $it")
        }
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = "CDM Configuration"
        }
    }

    override fun setupViews() {}

    override fun handleClicks() {}

}