package com.bacancy.ccs2androidhmi.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.bacancy.ccs2androidhmi.databinding.CustomToastViewBinding

object ToastUtils {

    fun Context.showCustomToast(message: String){
        val binding = CustomToastViewBinding.inflate(LayoutInflater.from(this))
        binding.tvToastMessage.text = message

        val toast = Toast(this)
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM, 0, 0)
        toast.view = binding.root
        toast.show()
    }

}