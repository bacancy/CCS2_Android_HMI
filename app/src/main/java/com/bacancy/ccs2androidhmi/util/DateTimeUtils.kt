package com.bacancy.ccs2androidhmi.util

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {

    private const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    fun getCurrentDateTime(dateTimeFormat: String = DATE_TIME_FORMAT): String? {
        val now = System.currentTimeMillis()
        val formatter = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
        return formatter.format(now)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.convertDateFormat(): String {
        val originalFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val targetFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)

        // Parse the original date string
        val date = LocalDateTime.parse(this, originalFormatter)

        // Format the parsed date in the desired format
        return targetFormatter.format(date)
    }
}