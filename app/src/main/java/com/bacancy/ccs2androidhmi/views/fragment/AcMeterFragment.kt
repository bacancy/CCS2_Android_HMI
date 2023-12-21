package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentAcMeterBinding
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class AcMeterFragment() : BaseFragment() {

    private lateinit var dcMeterFragment: DcMeterFragment
    private lateinit var binding: FragmentAcMeterBinding
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
        binding = FragmentAcMeterBinding.inflate(layoutInflater)
        dcMeterFragment = DcMeterFragment()
        binding.btnGoToDcMeter.setOnClickListener {
            /*mApplication!!.closeSerialPort()
            mSerialPort = null*/
            fragmentChangeListener?.replaceFragment(dcMeterFragment)
            dcMeterFragment.startReading()
        }
        return binding.root
    }

    fun startReadingMeterData() {
        Log.d("TAG", "startReadingMeterData: CALLED")
        lifecycleScope.launch {
            if (isVisible) {
                readAcMeterData()
            }
        }
    }

    private suspend fun readAcMeterData() {
        ReadWriteUtil.startReading(
            mOutputStream,
            mInputStream,
            ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getACMeterInfoRequestFrame()
        ) {
            //Step 5: Read response of ac meter data
            if (it.toHex().startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.d("AC_METER_FRAG", "AC METER DATA RESPONSE: ${it.toHex()}")
                val newResponse = ModBusUtils.parseInputRegistersResponse(it)
                Log.d("AC_METER_FRAG", "AC METER: ${newResponse.toList()}")
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.tvVoltageL1.text = newResponse[0].formatFloatToString()
                    binding.tvVoltageL2.text = newResponse[1].formatFloatToString()
                    binding.tvVoltageL3.text = newResponse[2].formatFloatToString()
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        Log.i("AC_TAG", "onPause: CALLED")
        mOutputStream?.flush()
        //mInputStream?.close()
    }

    override fun onResume() {
        super.onResume()
        Log.i("AC_TAG", "onResume: CALLED")
    }
}