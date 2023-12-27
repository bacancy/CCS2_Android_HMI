package com.bacancy.ccs2androidhmi.util

import android.view.View
import android.widget.TextView
import com.bacancy.ccs2androidhmi.util.ModbusTypeConverter.formatFloatToString

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun View.invisible(){
    this.visibility = View.INVISIBLE
}

fun View.gone(){
    this.visibility = View.GONE
}

fun TextView.setValue(value: Float) {
    this.text = value.formatFloatToString()
}