package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsStateInfoBinding
import com.bacancy.ccs2androidhmi.models.GunStatesInfo
import com.bacancy.ccs2androidhmi.util.StateAndModesUtils.getGunStates
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.adapters.GunStatesInfoListAdapter
import com.bacancy.ccs2androidhmi.views.listener.DashboardActivityContract

class GunsStateInfoFragment : BaseFragment() {

    private lateinit var gunStatesInfoListAdapter: GunStatesInfoListAdapter
    private lateinit var binding: FragmentGunsStateInfoBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsStateInfoBinding.inflate(layoutInflater)
        getGunStates()
        return binding.root
    }

    override fun handleClicks() {
        binding.btnClose.setOnClickListener {
            (requireActivity() as DashboardActivityContract).goBack()
        }
    }

    override fun setScreenHeaderViews() {
    }

    override fun setupViews() {
        gunStatesInfoListAdapter = GunStatesInfoListAdapter{}
        binding.apply {
            rvGunStates.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = gunStatesInfoListAdapter
            }
        }
        gunStatesInfoListAdapter.submitList(getGunStates())
    }
}