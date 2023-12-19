package com.bacancy.ccs2androidhmi.views

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.base.SerialPortBaseActivity
import com.bacancy.ccs2androidhmi.databinding.ActivityReadAcmeterInfoBinding
import com.bacancy.ccs2androidhmi.models.ACMeterModel
import com.bacancy.ccs2androidhmi.util.ModBusUtils.parseInputRegistersResponse
import com.bacancy.ccs2androidhmi.util.ModbusReadObserver
import com.bacancy.ccs2androidhmi.util.ModbusRequestFrames
import com.bacancy.ccs2androidhmi.util.ResponseSizes.AC_METER_INFORMATION_RESPONSE_SIZE
import com.bacancy.ccs2androidhmi.views.adapters.ACMeterListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ReadACMeterInfoActivity : SerialPortBaseActivity() {

    private lateinit var observer: ModbusReadObserver
    private lateinit var acMeterListAdapter: ACMeterListAdapter
    private lateinit var binding: ActivityReadAcmeterInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadAcmeterInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "AC Meter Information"

        acMeterListAdapter = ACMeterListAdapter {}

        binding.apply {
            rvAcMeterInfo.layoutManager = LinearLayoutManager(this@ReadACMeterInfoActivity)
            rvAcMeterInfo.adapter = acMeterListAdapter
        }

        startReadingACMeterInformation()
    }

    private fun startReadingACMeterInformation() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    observer = ModbusReadObserver()
                    observer.startObserving(
                        mOutputStream,
                        mInputStream, AC_METER_INFORMATION_RESPONSE_SIZE,
                        ModbusRequestFrames.getACMeterInfoRequestFrame(), { responseFrameArray ->
                            onDataReceived(responseFrameArray)
                        }, {
                            //OnFailure
                        })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onDataReceived(buffer: ByteArray) {
        val newResponse = parseInputRegistersResponse(buffer)
        Log.d("TAG", "onDataReceived: ${newResponse.toList()}")
        lifecycleScope.launch(Dispatchers.Main) {
            if (newResponse.isNotEmpty()) {
                val acMeterModel = ACMeterModel(
                    1,
                    newResponse[0],
                    newResponse[1],
                    newResponse[2],
                    newResponse[3],
                    newResponse[4],
                    newResponse[5],
                    newResponse[6],
                    newResponse[7],
                    newResponse[8],
                    newResponse[9],
                    newResponse[10],
                    newResponse[11]
                )
                acMeterListAdapter.submitList(listOf(acMeterModel))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        observer.stopObserving()
    }
}