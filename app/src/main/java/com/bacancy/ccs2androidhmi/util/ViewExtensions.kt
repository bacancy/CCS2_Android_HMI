package com.bacancy.ccs2androidhmi.util

import android.app.Activity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

fun Activity.showToast(message: String) {
    Toast.makeText(
        this,
        message, Toast.LENGTH_SHORT
    ).show()
}

fun Fragment.showToast(message: String) {
    Toast.makeText(
        requireActivity(),
        message, Toast.LENGTH_SHORT
    ).show()
}