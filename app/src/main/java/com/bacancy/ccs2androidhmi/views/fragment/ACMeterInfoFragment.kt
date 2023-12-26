package com.bacancy.ccs2androidhmi.views.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.bacancy.ccs2androidhmi.views.NewTestActivity
import com.bacancy.ccs2androidhmi.views.listener.FragmentChangeListener
import com.bacancy.ccs2androidhmi.views.listener.MiscDataListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

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
        setupViews()

        return binding.root
    }

    private fun setupViews() {

        binding.incVoltageV1N.tvLabel.text = getString(R.string.lbl_voltage_v1n)
        binding.incVoltageV1N.tvValueUnit.text = getString(R.string.lbl_v)

        binding.incVoltageV2N.tvLabel.text = getString(R.string.lbl_voltage_v2n)
        binding.incVoltageV2N.tvValueUnit.text = getString(R.string.lbl_v)

        binding.incVoltageV3N.tvLabel.text = getString(R.string.lbl_voltage_v3n)
        binding.incVoltageV3N.tvValueUnit.text = getString(R.string.lbl_v)

        binding.incAvgVoltageLN.tvLabel.text = getString(R.string.lbl_avg_voltage_ln)
        binding.incAvgVoltageLN.tvValueUnit.text = getString(R.string.lbl_v)

        binding.incCurrentI1.tvLabel.text = getString(R.string.lbl_current_i1)
        binding.incCurrentI1.tvValueUnit.text = getString(R.string.lbl_a)

        binding.incCurrentI2.tvLabel.text = getString(R.string.lbl_current_i2)
        binding.incCurrentI2.tvValueUnit.text = getString(R.string.lbl_a)

        binding.incCurrentI3.tvLabel.text = getString(R.string.lbl_current_i3)
        binding.incCurrentI3.tvValueUnit.text = getString(R.string.lbl_a)

        binding.incAvgCurrent.tvLabel.text = getString(R.string.lbl_avg_current)
        binding.incAvgCurrent.tvValueUnit.text = getString(R.string.lbl_a)

        binding.incFrequency.tvLabel.text = getString(R.string.lbl_frequency_hz)
        binding.incFrequency.tvValueUnit.text = getString(R.string.lbl_hz)

        binding.incActivePower.tvLabel.text = getString(R.string.lbl_active_power)
        binding.incActivePower.tvValueUnit.text = getString(R.string.lbl_kw)

        binding.incTotalPower.tvLabel.text = getString(R.string.lbl_total_power)
        binding.incTotalPower.tvValueUnit.text = getString(R.string.lbl_kwh)

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
        Log.e("FRITAG", "readAcMeterData: Started")
        withContext(Dispatchers.IO){
            ReadWriteUtil.startReading(
                mOutputStream,
                mInputStream,
                ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE,
                ModbusRequestFrames.getACMeterInfoRequestFrame()
            ) {
                //Step 5: Read response of ac meter data
                if (it.toHex().startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)) {
                    lifecycleScope.launch { delay(TimeUnit.SECONDS.toMillis(1)) }
                    (requireActivity() as NewTestActivity).startReadingMiscInformation(true)

                    Log.e("FRITAG", "readAcMeterData: Response Got")
                    Log.d("AC_METER_FRAG", "AC METER DATA RESPONSE: ${it.toHex()}")
                    val newResponse = ModBusUtils.parseInputRegistersResponse(it)
                    Log.d("AC_METER_FRAG", "AC METER: ${newResponse.toList()}")
                    lifecycleScope.launch(Dispatchers.Main) {

                        binding.incVoltageV1N.tvValue.setValue(newResponse[0])
                        binding.incVoltageV2N.tvValue.setValue(newResponse[1])
                        binding.incVoltageV3N.tvValue.setValue(newResponse[2])
                        binding.incAvgVoltageLN.tvValue.setValue(newResponse[3])
                        binding.incCurrentI1.tvValue.setValue(newResponse[4])
                        binding.incCurrentI2.tvValue.setValue(newResponse[5])
                        binding.incCurrentI3.tvValue.setValue(newResponse[6])
                        binding.incAvgCurrent.tvValue.setValue(newResponse[7])
                        binding.incFrequency.tvValue.setValue(newResponse[10])
                        binding.incActivePower.tvValue.setValue(newResponse[11])
                        binding.incTotalPower.tvValue.setValue(newResponse[9])

                    }
                }
            }

        }

    }

    fun TextView.setValue(value: Float){
        this.text = value.formatFloatToString()
    }
}