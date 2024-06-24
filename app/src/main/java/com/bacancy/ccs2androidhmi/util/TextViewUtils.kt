package com.bacancy.ccs2androidhmi.util

import android.content.Context
import android.graphics.Typeface
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.bacancy.ccs2androidhmi.R

object TextViewUtils {

    fun TextView.startBlinking(context: Context) {
        val blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink)
        this.startAnimation(blinkAnimation)
        this.setTextColor(resources.getColor(R.color.red, null))
    }

    fun TextView.removeBlinking(){
        this.clearAnimation()
        this.setTextColor(resources.getColor(R.color.yellow, null))
    }

    fun TextView.setBold(isBold: Boolean) {
        val typeface = typeface ?: Typeface.DEFAULT
        val newTypeface = if (isBold) {
            val style = typeface.style
            if (style and Typeface.BOLD != 0) {
                typeface
            } else {
                Typeface.create(typeface, Typeface.BOLD)
            }
        } else {
            val style = typeface.style
            if (style and Typeface.BOLD == 0) {
                typeface
            } else {
                Typeface.create(typeface, Typeface.NORMAL)
            }
        }
        setTypeface(newTypeface)
    }
}