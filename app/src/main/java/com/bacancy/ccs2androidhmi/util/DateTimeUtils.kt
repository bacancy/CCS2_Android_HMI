package com.bacancy.ccs2androidhmi.util

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Build
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    const val DATE_TIME_FORMAT_FOR_UI = "dd-MM-yyyy HH:mm:ss"
    const val DATE_TIME_FORMAT_FROM_CHARGER = "dd/MM/yyyy HH:mm:ss"

    fun calculateDifferenceInMinutes(firstDateTime: String, secondDateTime: String): String {
        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT_FROM_CHARGER, Locale.getDefault())

        val firstDate: Date = dateFormat.parse(firstDateTime)
        val secondDate: Date = dateFormat.parse(secondDateTime)

        val differenceInMillis = secondDate.time - firstDate.time

        return TimeUnit.MILLISECONDS.toMinutes(differenceInMillis).toString()
    }

    fun getCurrentDateTime(dateTimeFormat: String = DATE_TIME_FORMAT): String {
        val now = System.currentTimeMillis()
        val formatter = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
        return formatter.format(now)
    }

    fun String.convertDateFormatToDesiredFormat(currentFormat: String, desiredFormat: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val originalFormatter = SimpleDateFormat(currentFormat, Locale.getDefault())
            val targetFormatter = SimpleDateFormat(desiredFormat, Locale.getDefault())

            // Parse the original date string
            val date = originalFormatter.parse(this)

            // Format the parsed date in the desired format
            targetFormatter.format(date)
        } else {
            this
        }
    }

    fun String.convertToUtc(): String? {
        try {
            // Define the format of the input date-time string
            val inputFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
            inputFormat.timeZone = TimeZone.getDefault() // Set the time zone of the input date-time string

            // Parse the input date-time string into a Date object
            val dateTime = inputFormat.parse(this)

            // Set the time zone to UTC
            val utcFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")

            // Format the date-time in UTC format
            return utcFormat.format(dateTime)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}