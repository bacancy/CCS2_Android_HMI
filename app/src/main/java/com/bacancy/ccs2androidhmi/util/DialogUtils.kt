package com.bacancy.ccs2androidhmi.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.CustomDialogAreYouSureBinding
import com.bacancy.ccs2androidhmi.databinding.CustomDialogBinding
import com.bacancy.ccs2androidhmi.databinding.DialogGunsChargingSummaryBinding
import com.bacancy.ccs2androidhmi.databinding.DialogPasswordPromptBinding
import com.bacancy.ccs2androidhmi.databinding.DialogPinAuthorizationBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.util.CommonUtils.LOCAL_START_STOP_PIN

object DialogUtils {

    fun Activity.showCustomDialog(
        message: String,
        messageType: String = "info",
        isCancelable: Boolean = true,
        onCloseClicked: () -> Unit
    ): Dialog {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val binding = CustomDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            tvMessage.text = message
            when (messageType) {
                "info" -> {
                    ivMessageType.visible()
                    ivMessageType.setImageResource(R.drawable.ic_info)
                }
                "warning" -> {
                    ivMessageType.visible()
                    ivMessageType.setImageResource(R.drawable.ic_warning)
                }
                else -> {
                    ivMessageType.invisible()
                }
            }
            btnClose.setOnClickListener {
                dialog.dismiss()
                onCloseClicked()
            }
        }

        dialog.setCancelable(isCancelable)
        dialog.setupDialogFlags()
        return dialog
    }

    fun Activity.showCustomDialogForAreYouSure(message: String,isCancelable: Boolean = false, onYesClicked: () -> Unit, onNoClicked: () -> Unit) {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val binding = CustomDialogAreYouSureBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            tvMessage.text = message
            btnYes.setOnClickListener {
                dialog.dismiss()
                onYesClicked()
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
                onNoClicked()
            }
        }
        dialog.setCancelable(isCancelable)
        dialog.setupDialogFlags()
        dialog.show()
        clearDialogFlags(dialog)
    }

    fun Context.showChargingSummaryDialog(
        isGun1: Boolean,
        tbGunsLastChargingSummary: TbGunsLastChargingSummary,
        isDarkTheme: Boolean,
        onCloseClicked: () -> Unit
    ) {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
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
            incChargingDuration.root.setBackgroundColor(resources.getColor(if (isDarkTheme) R.color.black else R.color.white))

            incChargingStartDateTime.tvSummaryLabel.text =
                getString(R.string.lbl_charging_start_date_time)
            incChargingStartDateTime.tvSummaryUnit.invisible()
            incChargingStartDateTime.tvSummaryValue.text = getString(R.string.hint_date_time)
            incChargingStartDateTime.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incChargingEndDateTime.tvSummaryLabel.text =
                getString(R.string.lbl_charging_end_date_time)
            incChargingEndDateTime.tvSummaryUnit.invisible()
            incChargingEndDateTime.tvSummaryValue.text = getString(R.string.hint_date_time)
            incChargingEndDateTime.root.setBackgroundColor(resources.getColor(if (isDarkTheme) R.color.black else R.color.white))

            incStartSOC.tvSummaryLabel.text = getString(R.string.lbl_start_soc)
            incStartSOC.tvSummaryUnit.visible()
            incStartSOC.tvSummaryUnit.text = getString(R.string.lbl_percentage)
            incStartSOC.tvSummaryValue.text = getString(R.string.hint_0)
            incStartSOC.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incEndSOC.tvSummaryLabel.text = getString(R.string.lbl_end_soc)
            incEndSOC.tvSummaryUnit.visible()
            incEndSOC.tvSummaryUnit.text = getString(R.string.lbl_percentage)
            incEndSOC.tvSummaryValue.text = getString(R.string.hint_0)
            incEndSOC.root.setBackgroundColor(resources.getColor(if (isDarkTheme) R.color.black else R.color.white))

            incEnergyConsumption.tvSummaryLabel.text = getString(R.string.lbl_energy_consumption)
            incEnergyConsumption.tvSummaryUnit.visible()
            incEnergyConsumption.tvSummaryUnit.text = getString(R.string.lbl_kwh)
            incEnergyConsumption.tvSummaryValue.text = getString(R.string.hint_float)
            incEnergyConsumption.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            incSessionEndReason.tvSummaryLabel.text = getString(R.string.lbl_session_end_reason)
            incSessionEndReason.tvSummaryUnit.invisible()
            incSessionEndReason.tvSummaryValue.text = ""
            incSessionEndReason.root.setBackgroundColor(resources.getColor(if (isDarkTheme) R.color.black else R.color.white))

            incSessionTotalCost.tvSummaryLabel.text = getString(R.string.lbl_total_cost)
            incSessionTotalCost.tvSummaryUnit.invisible()
            incSessionTotalCost.tvSummaryValue.text = ""
            incSessionTotalCost.root.setBackgroundColor(resources.getColor(R.color.light_trans_sky_blue))

            tvGunsHeader.text =
                if (isGun1) getString(R.string.lbl_gun_1_charging_summary) else getString(R.string.lbl_gun_2_charging_summary)
            tbGunsLastChargingSummary.apply {
                incEVMacAddress.tvSummaryValue.text = evMacAddress
                incChargingDuration.tvSummaryValue.text = chargingDuration
                incChargingStartDateTime.tvSummaryValue.text = chargingStartDateTime
                incChargingEndDateTime.tvSummaryValue.text = chargingEndDateTime
                incStartSOC.tvSummaryValue.text = startSoc
                incEndSOC.tvSummaryValue.text = endSoc
                incEnergyConsumption.tvSummaryValue.text = energyConsumption
                incSessionEndReason.tvSummaryValue.text = sessionEndReason
                incSessionTotalCost.tvSummaryValue.text = getString(R.string.lbl_gun_total_cost_in_rs, if(totalCost.isEmpty()) 0.0F else totalCost.toFloat())
            }
            btnClose.setOnClickListener {
                dialog.dismiss()
                onCloseClicked()
            }
        }

        dialog.setCancelable(true)
        dialog.setupDialogFlags()
        dialog.show()
        clearDialogFlags(dialog)
    }

    fun Activity.showPasswordPromptDialog(
        popupTitle: String = "Authorize",
        isCancelable: Boolean = true,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val binding = DialogPasswordPromptBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            tvPopupTitle.text = popupTitle
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

        dialog.setCancelable(isCancelable)
        dialog.setupDialogFlags()
        dialog.show()
        clearDialogFlags(dialog)
    }

    fun Fragment.showPinAuthorizationDialog(
        onSuccess: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val dialog = Dialog(requireActivity(), R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val binding = DialogPinAuthorizationBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(false)

        binding.apply {
            btnSubmit.setOnClickListener {
                dialog.dismiss()
                val edtPIN = edtPIN.text.toString()
                if (edtPIN.isNotEmpty() && edtPIN.length % 2 == 0) {
                    onSuccess(edtPIN)
                } else {
                    onFailed()
                }
            }
            btnClose.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.setCancelable(true)
        dialog.setupDialogFlags()
        dialog.show()
        requireActivity().clearDialogFlags(dialog)
    }

    private fun Dialog.setupDialogFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.window?.let { window ->
                val controller = window.decorView.windowInsetsController
                controller?.hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
                controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            val uiFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            this.window?.decorView?.systemUiVisibility = uiFlags
        }
        this.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    fun Context.clearDialogFlags(dialog: Dialog) {
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.updateViewLayout(dialog.window?.decorView, dialog.window?.attributes)
    }

    private fun Dialog.setupWithoutTitle(){
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

}