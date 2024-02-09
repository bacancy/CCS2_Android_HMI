package com.bacancy.ccs2androidhmi.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.CustomToastViewBinding

object ToastUtils {

    fun Context.showCustomToast(message: String, isSuccess: Boolean){
        val binding = CustomToastViewBinding.inflate(LayoutInflater.from(this))
        binding.tvToastMessage.text = message
        binding.tvToastMessage.setBackgroundResource(
            if (isSuccess) {
                R.drawable.bg_green_more_rounded_rect_with_white_border
            } else {
                R.drawable.bg_red_more_rounded_rect_with_white_border
            }
        )
        val toast = Toast(this)
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM, 0, 0)
        toast.view = binding.root
        toast.show()
    }

}