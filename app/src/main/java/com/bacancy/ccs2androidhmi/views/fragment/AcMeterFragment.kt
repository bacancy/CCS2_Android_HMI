package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentAcMeterBinding
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import com.bacancy.ccs2androidhmi.views.listener.MiscDataListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AcMeterFragment() : BaseFragment(), MiscDataListener {

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
    }

    override fun onResume() {
        super.onResume()
        Log.i("AC_TAG", "onResume: CALLED")
    }

    override fun onMiscDataReceived() {
        Log.d("TAG", "IS FRAG VISIBLE - AC: $isVisible")
        lifecycleScope.launch {
            if (isVisible) {
                readAcMeterData()
            }
        }
    }
}