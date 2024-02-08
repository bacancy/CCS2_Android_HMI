package com.bacancy.ccs2androidhmi.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.CustomDialogBinding
import com.bacancy.ccs2androidhmi.databinding.DialogGunsChargingSummaryBinding
import com.bacancy.ccs2androidhmi.databinding.DialogPasswordPromptBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.util.CommonUtils.LOCAL_START_STOP_PIN


object DialogUtils {

    fun Context.showAlertDialog(
        title: String,
        message: String,
        ok: Pair<String, () -> Unit>,
        cancel: Pair<String, () -> Unit>? = null
    ) {

        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(ok.first) { _, _ -> ok.second() }

        cancel?.let {
            builder.setNegativeButton(it.first) { _, _ -> it.second() }
        }

        val alertDialog = builder.create()

        val uiFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        alertDialog.window?.let { window ->
            window.decorView.systemUiVisibility = uiFlags

            val layoutParams = window.attributes
            layoutParams.flags = layoutParams.flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            window.attributes = layoutParams
        }

        alertDialog.show()
    }

    fun Activity.showCustomDialog(message: String, onCloseClicked: () -> Unit) {
        // Show custom dialog without creating a new class
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val binding = CustomDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        // Initialize your custom views and handle interactions here
        binding.apply {
            tvMessage.text = message
            btnClose.setOnClickListener {
                dialog.dismiss()
                onCloseClicked()
            }
        }

        val uiFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        dialog.window?.let { window ->
            window.decorView.systemUiVisibility = uiFlags

            val layoutParams = window.attributes
            layoutParams.flags = layoutParams.flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            window.attributes = layoutParams
        }

        // Show the dialog
        dialog.show()
    }

    fun Context.showChargingSummaryDialog(
        isGun1: Boolean,
        tbGunsLastChargingSummary: TbGunsLastChargingSummary,
        onCloseClicked: () -> Unit
    ) {
        Log.i("JAN25", "showChargingSummaryDialog: CALLED - $isGun1")
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogGunsChargingSummaryBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(binding.root)

        binding.apply {
            incEVMacAddress.tvSummaryLabel.text = getString(R.string.lbl_ev_mac_address)
            incEVMacAddress.tvSummaryUnit.invisible()
            incEVMacAddress.tvSummaryValue.text = getString(R.string.hint_mac_address)
            incEVMacAddress.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incChargingDuration.tvSummaryLabel.text = getString(R.string.lbl_charging_duration)
            incChargingDuration.tvSummaryUnit.visible()
            incChargingDuration.tvSummaryUnit.text = getString(R.string.lbl_min)
            incChargingDuration.tvSummaryValue.text = getString(R.string.hint_0)
            incChargingDuration.root.setBackgroundColor(resources.getColor(R.color.black))

            incChargingStartDateTime.tvSummaryLabel.text =
                getString(R.string.lbl_charging_start_date_time)
            incChargingStartDateTime.tvSummaryUnit.invisible()
            incChargingStartDateTime.tvSummaryValue.text = getString(R.string.hint_date_time)
            incChargingStartDateTime.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incChargingEndDateTime.tvSummaryLabel.text =
                getString(R.string.lbl_charging_end_date_time)
            incChargingEndDateTime.tvSummaryUnit.invisible()
            incChargingEndDateTime.tvSummaryValue.text = getString(R.string.hint_date_time)
            incChargingEndDateTime.root.setBackgroundColor(resources.getColor(R.color.black))

            incStartSOC.tvSummaryLabel.text = getString(R.string.lbl_start_soc)
            incStartSOC.tvSummaryUnit.visible()
            incStartSOC.tvSummaryUnit.text = getString(R.string.lbl_percentage)
            incStartSOC.tvSummaryValue.text = getString(R.string.hint_0)
            incStartSOC.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incEndSOC.tvSummaryLabel.text = getString(R.string.lbl_end_soc)
            incEndSOC.tvSummaryUnit.visible()
            incEndSOC.tvSummaryUnit.text = getString(R.string.lbl_percentage)
            incEndSOC.tvSummaryValue.text = getString(R.string.hint_0)
            incEndSOC.root.setBackgroundColor(resources.getColor(R.color.black))

            incEnergyConsumption.tvSummaryLabel.text = getString(R.string.lbl_energy_consumption)
            incEnergyConsumption.tvSummaryUnit.visible()
            incEnergyConsumption.tvSummaryUnit.text = getString(R.string.lbl_kwh)
            incEnergyConsumption.tvSummaryValue.text = getString(R.string.hint_float)
            incEnergyConsumption.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incSessionEndReason.tvSummaryLabel.text = getString(R.string.lbl_session_end_reason)
            incSessionEndReason.tvSummaryUnit.invisible()
            incSessionEndReason.tvSummaryValue.text = ""
            incSessionEndReason.root.setBackgroundColor(resources.getColor(R.color.black))

            tvGunsHeader.text =
                if (isGun1) "Gun - 1 Charging Summary" else "Gun - 2 Charging Summary"
            tbGunsLastChargingSummary.apply {
                incEVMacAddress.tvSummaryValue.text = evMacAddress
                incChargingDuration.tvSummaryValue.text = chargingDuration
                incChargingStartDateTime.tvSummaryValue.text = chargingStartDateTime
                incChargingEndDateTime.tvSummaryValue.text = chargingEndDateTime
                incStartSOC.tvSummaryValue.text = startSoc
                incEndSOC.tvSummaryValue.text = endSoc
                incEnergyConsumption.tvSummaryValue.text = energyConsumption
                incSessionEndReason.tvSummaryValue.text = sessionEndReason
            }
            btnClose.setOnClickListener {
                dialog.dismiss()
                onCloseClicked()
            }
        }

        val uiFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        dialog.window?.let { window ->
            window.decorView.systemUiVisibility = uiFlags

            val layoutParams = window.attributes
            layoutParams.flags = layoutParams.flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window.attributes = layoutParams
        }

        dialog.show()
    }

    fun Activity.showPasswordPromptDialog(
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogPasswordPromptBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            btnSubmit.setOnClickListener {
                dialog.dismiss()
                val enteredPassword = edtPassword.text.toString()
                if (enteredPassword.length == 6 && enteredPassword == LOCAL_START_STOP_PIN) {
                    onSuccess()
                } else {
                    onFailed()
                }
            }
        }

        val uiFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        dialog.window?.let { window ->
            window.decorView.systemUiVisibility = uiFlags

            val layoutParams = window.attributes
            /*layoutParams.flags = layoutParams.flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL*/
            window.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window.attributes = layoutParams
        }

        dialog.show()
    }

}