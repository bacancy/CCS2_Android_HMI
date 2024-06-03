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
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.adapters.ErrorCodesListAdapter
import com.bacancy.ccs2androidhmi.views.listener.DashboardActivityContract
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
        (requireActivity() as DashboardActivityContract).updateTopBar(false)
        observeAllErrorCodes()
        return binding.root
    }

    private fun observeAllErrorCodes() {
        appViewModel.allErrorCodes.observe(requireActivity()) { errorCodes ->
            if (errorCodes != null) {
                Log.d("ErrorCodes", "ErrorCodes: $errorCodes")
                val updatedErrorCodesList = mutableListOf<ErrorCodes>()

                errorCodes.forEachIndexed { index, tbErrorCodes ->
                    updatedErrorCodesList.add(
                        ErrorCodes(
                            id = index + 1,
                            errorCodeName = tbErrorCodes.sourceErrorCodes,
                            errorCodeStatus = "",
                            errorCodeSource = getErrorCodeSource(tbErrorCodes.sourceId),
                            errorCodeValue = tbErrorCodes.sourceErrorValue,
                            errorCodeDateTime = tbErrorCodes.sourceErrorDateTime
                        )
                    )
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    if (updatedErrorCodesList.isNotEmpty()) {
                        binding.tvNoDataFound.gone()
                        binding.rvVendorErrorCodeInfo.visible()
                        allErrorCodesListAdapter.submitList(updatedErrorCodesList)
                        binding.rvVendorErrorCodeInfo.smoothScrollToPosition(0)
                    } else {
                        binding.tvNoDataFound.visible()
                        binding.rvVendorErrorCodeInfo.gone()
                    }
                }
            }
        }
    }

    private fun getErrorCodeSource(sourceId: Int): String {
        return when (sourceId) {
            0 -> return "Charger"
            1 -> return "Gun 1"
            2 -> return "Gun 2"
            else -> "Unknown"
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