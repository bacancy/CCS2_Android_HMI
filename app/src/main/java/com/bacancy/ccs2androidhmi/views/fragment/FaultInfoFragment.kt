package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentFaultInformationBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener

class FaultInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentFaultInformationBinding
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
        binding = FragmentFaultInformationBinding.inflate(layoutInflater)
        setScreenHeaderViews()
        setupViews()
        handleClicks()
        (requireActivity() as HMIDashboardActivity).showHideBackIcon(true)
        return binding.root
    }

    private fun handleClicks() {
        binding.apply {

            tvPLCFaults.setOnClickListener {
                fragmentChangeListener?.replaceFragment(PLCFaultsFragment())
            }

            tvRectifierFaults.setOnClickListener {
                fragmentChangeListener?.replaceFragment(RectifierFaultInfoFragment())
            }

            tvCommunicationError.setOnClickListener {
                fragmentChangeListener?.replaceFragment(CommunicationFailureFragment())
            }

            tvMiscError.setOnClickListener {
                fragmentChangeListener?.replaceFragment(DeviceConnectionStatusFragment())
            }

        }
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = getString(R.string.lbl_fault_information)
            incHeader.tvSubHeader.gone()
        }
    }

    override fun setupViews() {
    }
}