package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentDcMeterBinding
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DcMeterFragment() : BaseFragment() {

    private lateinit var binding: FragmentDcMeterBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("DC_FRAG", "onAttach: CALLED")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDcMeterBinding.inflate(layoutInflater)
        return binding.root
    }

    fun startReading() {
        lifecycleScope.launch {
            Log.d("TAG", "startReading: VISIBLE")
            startReadingGun1Information()
        }
    }

    private fun startReadingGun1Information() {
        Log.d("TAG", "startReadingGun1Information: CALLED - START}")
        val gunRequestFrame: ByteArray = if (true) {
            ModbusRequestFrames.getGun1InfoRequestFrame()
        } else {
            ModbusRequestFrames.getGun2InfoRequestFrame()
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    ReadWriteUtil.startReading(
                        mOutputStream,
                        mInputStream,
                        ResponseSizes.GUN_INFORMATION_RESPONSE_SIZE,
                        gunRequestFrame
                    ) {
                        Log.d("TAG", "startReadingGun1Information: CALLED - ${it.toHex()}")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("DC_TAG", "onPause: CALLED")
    }

    override fun onResume() {
        super.onResume()
        Log.i("DC_TAG", "onResume: CALLED")

    }
}