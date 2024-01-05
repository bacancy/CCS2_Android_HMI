package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsStateInfoBinding
import com.bacancy.ccs2androidhmi.models.GunStatesInfo
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.adapters.GunStatesInfoListAdapter

class GunsStateInfoFragment : BaseFragment() {

    private lateinit var gunStatesInfoListAdapter: GunStatesInfoListAdapter
    private lateinit var binding: FragmentGunsStateInfoBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsStateInfoBinding.inflate(layoutInflater)
        setScreenHeaderViews()
        setupViews()
        getGunStates()
        handleClicks()
        return binding.root
    }

    private fun handleClicks() {
        binding.btnClose.setOnClickListener {
            (requireActivity() as HMIDashboardActivity).goBack()
        }
    }

    private fun getGunStates(): MutableList<GunStatesInfo> {
        return mutableListOf(
            GunStatesInfo(1, "Unplugged", "Gun Not Connected", "White"),
            GunStatesInfo(2, "Plugged In", "Gun Connected with EV", "Yellow"),
            GunStatesInfo(
                3,
                "Authentication",
                "Authentication using RFID/OCPP with in 55 Sec of start this state",
                "White"
            ),
            GunStatesInfo(
                4,
                "Authentication Timeout",
                "Authentication not done within the time interval",
                "Red"
            ),
            GunStatesInfo(
                5,
                "Authentication Denied",
                "Authentication rejected by the Server",
                "Red"
            ),
            GunStatesInfo(
                6,
                "Authentication Success",
                "Authentication Success Response from Server",
                "Green"
            ),
            GunStatesInfo(7, "Isolation Fail", "Isolation Test Fail", "Red"),
            GunStatesInfo(8, "Preparing For Charging", "Initializing for charging", "Blue"),
            GunStatesInfo(9, "Precharge Fail", "Precharge Test Fail", "Red"),
            GunStatesInfo(10, "Charging", "EV Charge in Progress", "Blue"),
            GunStatesInfo(11, "Charging Complete", "EV charge Completed", "Green"),
            GunStatesInfo(12, "PLC Fault", "Fault Occurred in PLC Module", "Red"),
            GunStatesInfo(13, "Rectifier Fault", "Fault Occurred in Rectifier Module", "Red"),
            GunStatesInfo(14, "Communication Error", "Communication Break with EV", "Red"),
            GunStatesInfo(15, "Emergency Stop", "Emergency Stop Triggered by user", "Red")
        )
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