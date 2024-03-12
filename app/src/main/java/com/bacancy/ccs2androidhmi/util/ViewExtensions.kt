package com.bacancy.ccs2androidhmi.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun TextView.setValue(value: Float) {
    this.text = value.formatFloatToString()
}

fun Context.showToast(message: String) {
    Toast.makeText(
        this,
        message, Toast.LENGTH_SHORT
    ).show()
}

fun View.hideKeyboard(context: Context){
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.setBackgroundColorBasedOnTheme() {
    this.setBackgroundColor(resources.getColor(R.color.inverse_color, null))
}