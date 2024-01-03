package com.bacancy.ccs2androidhmi.util

import android.content.Context
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog

object DialogUtils {

    fun Context.showAlertDialog(
        title: String,
        message: String,
        ok: Pair<String,()->Unit>,
        cancel: Pair<String,()->Unit>? = null){

        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(ok.first) { _,_ -> ok.second() }

        cancel?.let{
            builder.setNegativeButton(it.first) { _, _ -> it.second() }
        }

        val alertDialog = builder.create()

        // Set flags for full-screen mode
        alertDialog.window?.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        alertDialog.show()
    }

}