package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentAcMeterInfoBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
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

class ACMeterInfoFragment : BaseFragment(), MiscDataListener {

    private lateinit var binding: FragmentAcMeterInfoBinding
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
        binding = FragmentAcMeterInfoBinding.inflate(layoutInflater)

        setScreenHeaderTitle()

        return binding.root
    }

    private fun setScreenHeaderTitle() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_ac_meter_information)


    }

    override fun onMiscDataReceived() {
        lifecycleScope.launch {
            if (isVisible) {
                readAcMeterData()
            }
        }
    }

    private suspend fun readAcMeterData() {
        Log.i("FRITAG", "readAcMeterData: Started")
        ReadWriteUtil.startReading(
            mOutputStream,
            mInputStream,
            ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE,
            ModbusRequestFrames.getACMeterInfoRequestFrame()
        ) {
            //Step 5: Read response of ac meter data
            if (it.toHex().startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)) {
                Log.i("FRITAG", "readAcMeterData: Response Got")
                Log.d("AC_METER_FRAG", "AC METER DATA RESPONSE: ${it.toHex()}")
                val newResponse = ModBusUtils.parseInputRegistersResponse(it)
                Log.d("AC_METER_FRAG", "AC METER: ${newResponse.toList()}")
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.incVoltageV1N.tvLabel.text = getString(R.string.lbl_voltage_v1n)
                    binding.incVoltageV1N.tvValue.text = newResponse[0].formatFloatToString()
                    binding.incVoltageV1N.tvValueUnit.text = "V"

                    binding.incVoltageV2N.tvLabel.text = getString(R.string.lbl_voltage_v2n)
                    binding.incVoltageV2N.tvValue.text = newResponse[1].formatFloatToString()
                    binding.incVoltageV2N.tvValueUnit.text = "V"

                    binding.incVoltageV3N.tvLabel.text = getString(R.string.lbl_voltage_v3n)
                    binding.incVoltageV3N.tvValue.text = newResponse[2].formatFloatToString()
                    binding.incVoltageV3N.tvValueUnit.text = "V"

                    binding.incAvgVoltageLN.tvLabel.text = getString(R.string.lbl_avg_voltage_ln)
                    binding.incAvgVoltageLN.tvValue.text = newResponse[3].formatFloatToString()
                    binding.incAvgVoltageLN.tvValueUnit.text = "V"

                    binding.incCurrentI1.tvLabel.text = getString(R.string.lbl_current_i1)
                    binding.incCurrentI1.tvValue.text = newResponse[4].formatFloatToString()
                    binding.incCurrentI1.tvValueUnit.text = "A"

                    binding.incCurrentI2.tvLabel.text = getString(R.string.lbl_current_i2)
                    binding.incCurrentI2.tvValue.text = newResponse[5].formatFloatToString()
                    binding.incCurrentI2.tvValueUnit.text = "A"

                    binding.incCurrentI3.tvLabel.text = getString(R.string.lbl_current_i3)
                    binding.incCurrentI3.tvValue.text = newResponse[6].formatFloatToString()
                    binding.incCurrentI3.tvValueUnit.text = "A"

                    binding.incAvgCurrent.tvLabel.text = getString(R.string.lbl_avg_current)
                    binding.incAvgCurrent.tvValue.text = newResponse[7].formatFloatToString()
                    binding.incAvgCurrent.tvValueUnit.text = "A"

                    binding.incFrequency.tvLabel.text = getString(R.string.lbl_frequency_hz)
                    binding.incFrequency.tvValue.text = newResponse[10].formatFloatToString()
                    binding.incFrequency.tvValueUnit.text = "Hz"

                    binding.incActivePower.tvLabel.text = getString(R.string.lbl_active_power)
                    binding.incActivePower.tvValue.text = newResponse[11].formatFloatToString()
                    binding.incActivePower.tvValueUnit.text = "Kw"

                    binding.incTotalPower.tvLabel.text = getString(R.string.lbl_total_power)
                    binding.incTotalPower.tvValue.text = newResponse[9].formatFloatToString()
                    binding.incTotalPower.tvValueUnit.text = "kwh"
                }
            }
        }

    }
}