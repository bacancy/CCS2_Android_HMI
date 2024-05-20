package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentNewFaultInformationBinding
import com.bacancy.ccs2androidhmi.db.entity.TbErrorCodes
import com.bacancy.ccs2androidhmi.models.ErrorCodes
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.adapters.ErrorCodesListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewFaultInfoFragment : BaseFragment() {

    private lateinit var allErrorCodesListAdapter: ErrorCodesListAdapter
    private lateinit var binding: FragmentNewFaultInformationBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewFaultInformationBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        observeAllErrorCodes()
        return binding.root
    }

    private fun observeAllErrorCodes() {
        appViewModel.allErrorCodes.observe(requireActivity()) { errorCodes ->
            if (errorCodes != null) {
                Log.d("ErrorCodes", "ErrorCodes: $errorCodes")
                createCommonAbnormalErrorsList(errorCodes)
            }
        }
    }

    private fun createCommonAbnormalErrorsList(errorCodes: List<TbErrorCodes>) {
        lifecycleScope.launch(Dispatchers.Main) {
            val updatedErrorCodesList = mutableListOf<ErrorCodes>()
            errorCodes.forEach { tbErrorCodes ->
                updatedErrorCodesList.addAll(
                    appViewModel.getAbnormalErrorCodesList(
                        tbErrorCodes.sourceErrorCodes,
                        tbErrorCodes.sourceId,
                        tbErrorCodes.sourceErrorDateTime
                    )
                )
            }
            if (updatedErrorCodesList.isNotEmpty()) {
                binding.tvNoDataFound.gone()
                binding.rvVendorErrorCodeInfo.visible()
                allErrorCodesListAdapter.submitList(updatedErrorCodesList)
            } else {
                binding.tvNoDataFound.visible()
                binding.rvVendorErrorCodeInfo.gone()
            }
        }
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = getString(R.string.lbl_fault_information)
        }
    }

    override fun setupViews() {
        allErrorCodesListAdapter = ErrorCodesListAdapter {}
        binding.apply {
            rvVendorErrorCodeInfo.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = allErrorCodesListAdapter
                itemAnimator = null //To remove item insertion or removal animation of the list
            }
        }
    }

    override fun handleClicks() {}

}