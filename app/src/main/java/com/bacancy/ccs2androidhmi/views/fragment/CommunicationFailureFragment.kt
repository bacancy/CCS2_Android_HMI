package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding

class CommunicationFailureFragment : BaseFragment() {

    private lateinit var binding: FragmentGunsHomeScreenBinding
    override fun setScreenHeaderViews() {
        TODO("Not yet implemented")
    }

    override fun setupViews() {
        TODO("Not yet implemented")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGunsHomeScreenBinding.inflate(layoutInflater)
        return binding.root
    }
}