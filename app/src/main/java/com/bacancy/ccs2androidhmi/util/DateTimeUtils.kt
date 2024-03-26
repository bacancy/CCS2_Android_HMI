package com.bacancy.ccs2androidhmi.util

import android.icu.text.SimpleDateFormat
import java.util.Locale

object DateTimeUtils {

    private const val DATE_TIME_FORMAT = "dd-MM-yyyy'T'HH:mm:ss"

    fun getCurrentDateTime(dateTimeFormat: String = DATE_TIME_FORMAT): String? {
        val now = System.currentTimeMillis()
        val formatter = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
        return formatter.format(now)
    }

}