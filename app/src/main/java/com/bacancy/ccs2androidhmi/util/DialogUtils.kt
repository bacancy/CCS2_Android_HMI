package com.bacancy.ccs2androidhmi.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.CustomDialogAreYouSureBinding
import com.bacancy.ccs2androidhmi.databinding.CustomDialogBinding
import com.bacancy.ccs2androidhmi.databinding.DialogGunsChargingSummaryBinding
import com.bacancy.ccs2androidhmi.databinding.DialogPasswordPromptBinding
import com.bacancy.ccs2androidhmi.databinding.DialogPinAuthorizationBinding
import com.bacancy.ccs2androidhmi.databinding.DialogSessionModeSelectionBinding
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

        alertDialog.configureWindow()

        alertDialog.show()
    }

    fun Activity.showCustomDialog(
        message: String,
        messageType: String = "info",
        onCloseClicked: () -> Unit
    ): Dialog {
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

        dialog.configureWindow(true)

        return dialog
    }

    fun Activity.showCustomDialogForAreYouSure(
        message: String,
        onYesClicked: () -> Unit,
        onNoClicked: () -> Unit
    ) {
        // Show custom dialog without creating a new class
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val binding = CustomDialogAreYouSureBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        // Initialize your custom views and handle interactions here
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

        dialog.configureWindow(true)

        // Show the dialog
        dialog.show()
    }

    fun Context.showChargingSummaryDialog(
        isGun1: Boolean,
        tbGunsLastChargingSummary: TbGunsLastChargingSummary,
        isDarkTheme: Boolean,
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

        dialog.configureWindow(true)

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

        dialog.configureWindow()

        dialog.show()
    }

    fun Fragment.showPinAuthorizationDialog(
        onSuccess: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val dialog = Dialog(requireActivity(), R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
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

        dialog.configureWindow()

        dialog.show()
    }

    fun Activity.showSessionModeDialog(
        onSuccess: (String, String) -> Unit,
    ) {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogSessionModeSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(false)

        binding.apply {
            btnSubmit.setOnClickListener {
                if (!radioByAuto.isChecked && edtSessionModeValue.text.isEmpty()) {
                    edtSessionModeValue.error = "Please enter value"
                    return@setOnClickListener
                } else if(!validateSelectedValueRange()){
                    edtSessionModeValue.error = "Value must be in range"
                    return@setOnClickListener
                }

                val sessionModeValue =
                    if (radioByAuto.isChecked) "100" else edtSessionModeValue.text.toString()

                val radioButtonMap = mapOf(
                    radioByAuto to "Auto",
                    radioByTime to "Time",
                    radioByEnergy to "Energy",
                    radioBySoc to "SOC"
                )

                val selectedRadioButton =
                    radioButtonMap.entries.find { it.key.isChecked }?.value ?: ""
                onSuccess(selectedRadioButton, sessionModeValue)
                dialog.dismiss()
            }
            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            radioByAuto.isChecked = true

            radioByAuto.setOnCheckedChangeListener { _, isClicked ->
                if (isClicked) {
                    handleRadioButtonSelection(radioByAuto)
                }
            }

            radioByTime.setOnCheckedChangeListener { _, isClicked ->
                if (isClicked) {
                    handleRadioButtonSelection(radioByTime)
                }
            }

            radioByEnergy.setOnCheckedChangeListener { _, isClicked ->
                if (isClicked) {
                    handleRadioButtonSelection(radioByEnergy)
                }
            }

            radioBySoc.setOnCheckedChangeListener { _, isClicked ->
                if (isClicked) {
                    handleRadioButtonSelection(radioBySoc)
                }
            }
        }

        dialog.configureWindow()

        dialog.show()
    }

    private fun DialogSessionModeSelectionBinding.checkValueInRange(minValue: Number, maxValue: Number): Boolean {
        val value = edtSessionModeValue.text.toString().toDoubleOrNull()
        return value != null && (value >= minValue.toDouble() && value <= maxValue.toDouble())
    }

    private fun DialogSessionModeSelectionBinding.validateSelectedValueRange(): Boolean {
        return when {
            radioByTime.isChecked -> checkValueInRange(1, 999)
            radioByEnergy.isChecked -> checkValueInRange(0.01, 999.99)
            radioBySoc.isChecked -> checkValueInRange(1, 100)
            else -> true
        }
    }

    private var timeTextWatcher: TextWatcher? = null
    private var energyTextWatcher: TextWatcher? = null
    private var socTextWatcher: TextWatcher? = null

    private fun DialogSessionModeSelectionBinding.handleRadioButtonSelection(selectedRadioButton: RadioButton) {
        // List of all radio buttons
        val radioButtons = listOf(radioByAuto, radioByTime, radioByEnergy, radioBySoc)

        // Uncheck all radio buttons except the selected one
        radioButtons.forEach { radioButton ->
            radioButton.isChecked = (radioButton == selectedRadioButton)
        }

        // Remove any existing TextWatchers
        clearTextWatchers()

        // Based on the selected radio button, perform the necessary actions
        when (selectedRadioButton) {
            radioByAuto -> {
                handleEditTextVisibility(false)
            }

            radioByTime -> {
                handleEditTextVisibility(
                    true,
                    "Enter Time in Minutes (1-999)",
                    InputType.TYPE_CLASS_NUMBER,
                    InputFilter.LengthFilter(3)
                ) { text ->
                    validateTextValue(text, 1, 999)
                }
            }

            radioByEnergy -> {
                handleEditTextVisibility(
                    true,
                    "Enter Energy in kWh (0.01-999.99)",
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                    InputFilter.LengthFilter(6)
                ) { text ->
                    validateTextValue(text, 0.01, 999.99)
                }
            }

            radioBySoc -> {
                handleEditTextVisibility(
                    true,
                    "Enter SOC in % (1-100)",
                    InputType.TYPE_CLASS_NUMBER,
                    InputFilter.LengthFilter(3)
                ) { text ->
                    validateTextValue(text, 1, 100)
                }
            }
        }
    }

    private fun DialogSessionModeSelectionBinding.clearTextWatchers() {
        edtSessionModeValue.apply {
            removeTextChangedListener(timeTextWatcher)
            removeTextChangedListener(energyTextWatcher)
            removeTextChangedListener(socTextWatcher)
            text.clear()
            error = null
        }
    }

    private fun DialogSessionModeSelectionBinding.handleEditTextVisibility(
        isVisible: Boolean,
        hint: String? = null,
        inputType: Int? = null,
        vararg filters: InputFilter,
        textWatcher: (CharSequence?) -> Unit = {}
    ) {
        edtSessionModeValue.apply {
            if (isVisible) {
                visible()
                this.hint = hint
                this.inputType = inputType ?: InputType.TYPE_CLASS_TEXT
                this.filters = filters
                timeTextWatcher = createTextWatcher(textWatcher)
                addTextChangedListener(timeTextWatcher)
            } else {
                gone()
            }
        }
    }

    private fun createTextWatcher(textWatcher: (CharSequence?) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textWatcher(s)
            }
        }
    }

    private fun DialogSessionModeSelectionBinding.validateTextValue(
        text: CharSequence?,
        minValue: Number,
        maxValue: Number
    ) {
        val value = text?.toString()?.toDoubleOrNull()
        if (value != null && (value < minValue.toDouble() || value > maxValue.toDouble())) {
            edtSessionModeValue.error = "Value must be between $minValue and $maxValue"
        } else {
            edtSessionModeValue.error = null
        }
    }

    private fun Dialog.configureWindow(shouldAvoidUserInteractions: Boolean = false) {
        this.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }

            val layoutParams = window.attributes
            window.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            if (shouldAvoidUserInteractions) {
                layoutParams.flags = layoutParams.flags or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }

            window.attributes = layoutParams
        }
    }
}