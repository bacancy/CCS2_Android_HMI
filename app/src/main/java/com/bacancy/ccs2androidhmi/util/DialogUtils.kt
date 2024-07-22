package com.bacancy.ccs2androidhmi.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.CustomDialogAreYouSureBinding
import com.bacancy.ccs2androidhmi.databinding.CustomDialogBinding
import com.bacancy.ccs2androidhmi.databinding.CustomLoadingDialogBinding
import com.bacancy.ccs2androidhmi.databinding.DialogGunsChargingSummaryBinding
import com.bacancy.ccs2androidhmi.databinding.DialogPasswordPromptBinding
import com.bacancy.ccs2androidhmi.databinding.DialogPinAuthorizationBinding
import com.bacancy.ccs2androidhmi.databinding.DialogSelectAppLanguageBinding
import com.bacancy.ccs2androidhmi.databinding.DialogSessionModeSelectionBinding
import com.bacancy.ccs2androidhmi.databinding.DialogStartStopChargingSelectionBinding
import com.bacancy.ccs2androidhmi.db.entity.TbGunsLastChargingSummary
import com.bacancy.ccs2androidhmi.models.Language
import com.bacancy.ccs2androidhmi.util.CommonUtils.LOCAL_START_STOP_PIN
import com.bacancy.ccs2androidhmi.util.LanguageConfig.getAppLanguage
import com.bacancy.ccs2androidhmi.util.LanguageConfig.getLanguagesList
import com.bacancy.ccs2androidhmi.views.adapters.LanguageListAdapter

object DialogUtils {

    fun Activity.showCustomLoadingDialog(
        message: String,
        isCancelable: Boolean = false,
    ): Dialog {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        val binding = CustomLoadingDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            tvMessage.text = message
        }

        dialog.setCancelable(isCancelable)
        dialog.setupDialogFlags()
        return dialog
    }

    fun Activity.showCustomDialog(
        message: String,
        messageType: String = "info",
        buttonLabel: String = getString(R.string.lbl_close),
        isCancelable: Boolean = true,
        onCloseClicked: () -> Unit
    ): Dialog {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        val binding = CustomDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            tvMessage.text = message
            btnClose.text = buttonLabel
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
    ): Dialog {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
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
        dialog.setupDialogFlags(true)
        return dialog
    }

    fun Activity.showPasswordPromptDialog(
        popupTitle: String,
        isCancelable: Boolean = true,
        onSuccess: () -> Unit,
        onFailed: () -> Unit,
        password: String = LOCAL_START_STOP_PIN
    ) {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        val binding = DialogPasswordPromptBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            tvPopupTitle.text = popupTitle
            btnSubmit.setOnClickListener {
                dialog.dismiss()
                val enteredPassword = edtPassword.text.toString()
                if (enteredPassword.length == password.length && enteredPassword == password) {
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

    private fun Dialog.setupDialogFlags(isMatchParent: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.window?.let { window ->
                val controller = window.decorView.windowInsetsController
                controller?.hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
                controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                window.setLayout(
                    if(isMatchParent) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
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

    fun Activity.showSessionModeDialog(
        onSuccess: (Int, String) -> Unit,
    ): Dialog {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        val binding = DialogSessionModeSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(false)

        binding.apply {
            edtSessionModeValue.text.clear()
            edtSessionModeValue.gone()
            btnSubmit.setOnClickListener {
                if (!radioByAuto.isChecked && edtSessionModeValue.text.isEmpty()) {
                    edtSessionModeValue.error = getString(R.string.msg_please_enter_value)
                    return@setOnClickListener
                } else if(!validateSelectedValueRange()){
                    edtSessionModeValue.error = getString(R.string.msg_value_must_be_in_range)
                    return@setOnClickListener
                }

                val sessionModeValue =
                    if (radioByAuto.isChecked) "100" else edtSessionModeValue.text.toString()

                val radioButtonMap = mapOf(
                    radioByAuto to 0,
                    radioByTime to 1,
                    radioByEnergy to 3,
                    radioBySoc to 2
                )

                val selectedRadioButton = radioButtonMap.entries.find { it.key.isChecked }?.value ?: 0
                onSuccess(selectedRadioButton, sessionModeValue)
                dialog.dismiss()
            }
            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            radioByAuto.isChecked = true

            radioByAuto.setOnCheckedChangeListener { _, isClicked ->
                if (isClicked) {
                    lnrAuto.setBackgroundResource(R.drawable.radio_auto_rounded_rect_selected)
                    handleRadioButtonSelection(this@showSessionModeDialog,radioByAuto)
                }else{
                    lnrAuto.setBackgroundResource(R.drawable.radio_auto_rounded_rect)
                }
            }

            radioByTime.setOnCheckedChangeListener { _, isClicked ->
                if (isClicked) {
                    lnrTime.setBackgroundResource(R.drawable.radio_time_rounded_rect_selected)
                    handleRadioButtonSelection(this@showSessionModeDialog,radioByTime)
                }else{
                    lnrTime.setBackgroundResource(R.drawable.radio_time_rounded_rect)
                }
            }

            radioByEnergy.setOnCheckedChangeListener { _, isClicked ->
                if (isClicked) {
                    lnrEnergy.setBackgroundResource(R.drawable.radio_energy_rounded_rect_selected)
                    handleRadioButtonSelection(this@showSessionModeDialog,radioByEnergy)
                }else{
                    lnrEnergy.setBackgroundResource(R.drawable.radio_energy_rounded_rect)
                }
            }

            radioBySoc.setOnCheckedChangeListener { _, isClicked ->
                if (isClicked) {
                    lnrSoC.setBackgroundResource(R.drawable.radio_soc_rounded_rect_selected)
                    handleRadioButtonSelection(this@showSessionModeDialog,radioBySoc)
                }else{
                    lnrSoC.setBackgroundResource(R.drawable.radio_soc_rounded_rect)
                }
            }
        }

        dialog.setupDialogFlags()

        return dialog
    }

    fun Activity.showStartStopChargingDialog(
        isStartCharging: Boolean,
        onOTPClick: () -> Unit,
        onRFIDClick: () -> Unit
    ): Dialog {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.setupWithoutTitle()
        val binding = DialogStartStopChargingSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(false)

        binding.apply {
            tvStartStopCharging.text = if (isStartCharging) getString(R.string.lbl_start_charging) else getString(R.string.lbl_stop_charging)

            ivEnterOTP.setOnClickListener {
                dialog.dismiss()
                onOTPClick()
            }
            ivTapRFID.setOnClickListener {
                dialog.dismiss()
                onRFIDClick()
            }
        }

        dialog.setupDialogFlags()

        return dialog
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

    private fun DialogSessionModeSelectionBinding.handleRadioButtonSelection(context: Context,selectedRadioButton: RadioButton) {
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
                    context.getString(R.string.msg_enter_time_in_minutes_1_999),
                    InputType.TYPE_CLASS_NUMBER,
                    InputFilter.LengthFilter(3)
                ) { text ->
                    validateTextValue(context,text, 1, 999)
                }
            }

            radioByEnergy -> {
                handleEditTextVisibility(
                    true,
                    context.getString(R.string.msg_enter_energy_in_kwh_0_01_999_99),
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                    InputFilter.LengthFilter(6)
                ) { text ->
                    validateTextValue(context,text, 0.01, 999.99)
                }
            }

            radioBySoc -> {
                handleEditTextVisibility(
                    true,
                    context.getString(R.string.msg_enter_soc_in_1_100),
                    InputType.TYPE_CLASS_NUMBER,
                    InputFilter.LengthFilter(3)
                ) { text ->
                    validateTextValue(context,text, 1, 100)
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
        context: Context,
        text: CharSequence?,
        minValue: Number,
        maxValue: Number
    ) {
        val value = text?.toString()?.toDoubleOrNull()
        if (value != null && (value < minValue.toDouble() || value > maxValue.toDouble())) {
            edtSessionModeValue.error =
                context.getString(R.string.msg_value_must_be_between_and, minValue, maxValue)
        } else {
            edtSessionModeValue.error = null
        }
    }

    fun Activity.showSelectAppLanguageDialog(
        prefHelper: PrefHelper,
        onLanguageSelected: (Language) -> Unit,
    ) {
        val dialog = Dialog(this, R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogSelectAppLanguageBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            languageRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@showSelectAppLanguageDialog)
                getLanguagesList().forEach { language ->
                    language.isSelected = language.code == getAppLanguage(prefHelper)
                }
                adapter = LanguageListAdapter(getLanguagesList()) { selectedLanguage ->
                    onLanguageSelected(selectedLanguage)
                    dialog.dismiss()
                }
            }
        }

        dialog.setupDialogFlags()
        dialog.show()
        clearDialogFlags(dialog)
    }
}