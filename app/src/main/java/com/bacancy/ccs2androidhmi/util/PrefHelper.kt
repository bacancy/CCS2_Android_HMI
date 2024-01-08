package com.bacancy.ccs2androidhmi.util

import android.content.Context
import android.content.SharedPreferences

class PrefHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "HMIPrefs"
    }

    fun setSelectedGunNumber(key: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getSelectedGunNumber(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

}