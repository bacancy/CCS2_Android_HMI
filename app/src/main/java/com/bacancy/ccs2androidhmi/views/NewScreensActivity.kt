package com.bacancy.ccs2androidhmi.views

import android.os.Bundle
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityNewScreensBinding

class NewScreensActivity : SerialPortBaseActivity() {
    private lateinit var binding: ActivityNewScreensBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewScreensBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}