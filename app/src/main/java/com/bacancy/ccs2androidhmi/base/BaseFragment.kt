package com.bacancy.ccs2androidhmi.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android_serialport_api.SerialPort
import androidx.fragment.app.Fragment
import com.bacancy.ccs2androidhmi.HMIApp
import java.io.InputStream
import java.io.OutputStream

abstract class BaseFragment: Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScreenHeaderViews()
        setupViews()
        handleClicks()
    }

    abstract fun setScreenHeaderViews()

    abstract fun setupViews()

    abstract fun handleClicks()

}