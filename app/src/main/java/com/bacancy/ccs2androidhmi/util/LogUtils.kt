package com.bacancy.ccs2androidhmi.util

import android.util.Log

object LogUtils {

    fun debugLog(message: String){
        Log.d("DEBUG_TAG", message)
    }

    fun errorLog(message: String){
        Log.e("ERROR_TAG", message)
    }

}