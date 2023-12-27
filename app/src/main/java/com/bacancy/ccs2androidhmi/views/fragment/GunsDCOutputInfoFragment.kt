package com.bacancy.ccs2androidhmi.views.fragment

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
import com.bacancy.ccs2androidhmi.databinding.FragmentDcMeterBinding
import com.bacancy.ccs2androidhmi.databinding.FragmentGunsHomeScreenBinding
import com.bacancy.ccs2androidhmi.util.ModBusUtils
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.toHex
import com.bacancy.ccs2androidhmi.util.ReadWriteUtil
import com.bacancy.ccs2androidhmi.util.ResponseSizes
import com.bacancy.ccs2androidhmi.util.setValue
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.views.NewTestActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class GunsDCOutputInfoFragment : BaseFragment() {

    private val isGun1: Boolean = false
    private lateinit var binding: FragmentDcMeterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDcMeterBinding.inflate(layoutInflater)
        setScreenHeaderViews()
        setupViews()
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.apply {
            incHeader.tvHeader.text = getString(R.string.lbl_gun_1)
            incHeader.tvSubHeader.text = getString(R.string.lbl_dc_output_information)
            incHeader.tvSubHeader.visible()
        }
    }

    override fun setupViews() {
        binding.apply {
            incVoltage.tvLabel.text = getString(R.string.lbl_voltage)
            incVoltage.tvValueUnit.text = getString(R.string.lbl_v)

            incMaxVoltage.tvLabel.text = getString(R.string.lbl_max_voltage)
            incMaxVoltage.tvValueUnit.text = getString(R.string.lbl_v)

            incMinVoltage.tvLabel.text = getString(R.string.lbl_min_voltage)
            incMinVoltage.tvValueUnit.text = getString(R.string.lbl_v)

            incCurrent.tvLabel.text = getString(R.string.lbl_current)
            incCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incMaxCurrent.tvLabel.text = getString(R.string.lbl_max_current)
            incMaxCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incMinCurrent.tvLabel.text = getString(R.string.lbl_min_current)
            incMinCurrent.tvValueUnit.text = getString(R.string.lbl_a)

            incPower.tvLabel.text = getString(R.string.lbl_power)
            incPower.tvValueUnit.text = getString(R.string.lbl_kw)

            incImportEnergy.tvLabel.text = getString(R.string.lbl_import_energy)
            incImportEnergy.tvValueUnit.text = getString(R.string.lbl_kwh)

            incExportEnergy.tvLabel.text = getString(R.string.lbl_export_energy)
            incExportEnergy.tvValueUnit.text = getString(R.string.lbl_kwh)
        }
    }

    private suspend fun readGunsDCOutputInfo() {

        val gunsDcOutputInfoRequestFrame: ByteArray = if (isGun1) {
            ModbusRequestFrames.getGunOneDCMeterInfoRequestFrame()
        } else {
            ModbusRequestFrames.getGunTwoDCMeterInfoRequestFrame()
        }

        withContext(Dispatchers.IO) {
            ReadWriteUtil.startReading(
                mOutputStream,
                mInputStream,
                ResponseSizes.GUN_DC_METER_INFORMATION_RESPONSE_SIZE,
                gunsDcOutputInfoRequestFrame
            ) {
                if (it.toHex().startsWith(ModBusUtils.INPUT_REGISTERS_CORRECT_RESPONSE_BITS)) {
                    Log.d("DC_METER_FRAG", "DC METER DATA RESPONSE: ${it.toHex()}")
                    val newResponse = ModBusUtils.parseInputRegistersResponse(it)
                    Log.d("DC_METER_FRAG", "DC METER: ${newResponse.toList()}")
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.apply {
                            incVoltage.tvValue.setValue(0.0F)
                            incMaxVoltage.tvValue.setValue(0.0F)
                            incMinVoltage.tvValue.setValue(0.0F)
                            incCurrent.tvValue.setValue(0.0F)
                            incMaxCurrent.tvValue.setValue(0.0F)
                            incMinCurrent.tvValue.setValue(0.0F)
                            incPower.tvValue.setValue(0.0F)
                            incImportEnergy.tvValue.setValue(0.0F)
                            incExportEnergy.tvValue.setValue(0.0F)
                        }
                    }
                }
            }
        }
    }


}