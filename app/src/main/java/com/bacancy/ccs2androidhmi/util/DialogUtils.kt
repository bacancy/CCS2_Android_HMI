package com.bacancy.ccs2androidhmi.util

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.databinding.CustomDialogBinding


object DialogUtils {

    @RequiresApi(Build.VERSION_CODES.R)
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

    fun Fragment.showCustomDialog(message: String, onCloseClicked: () -> Unit) {
        // Show custom dialog without creating a new class
        val dialog = Dialog(requireActivity(), R.style.CustomAlertDialog)
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

}