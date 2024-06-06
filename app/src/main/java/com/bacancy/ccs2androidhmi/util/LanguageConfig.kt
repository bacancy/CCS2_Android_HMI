package com.bacancy.ccs2androidhmi.util

import android.content.Context
import android.util.Log
import com.bacancy.ccs2androidhmi.models.Language
import java.util.Locale

object LanguageConfig {

    private const val SELECTED_APP_LANGUAGE = "selected_app_language"
    private const val DEFAULT_APP_LANGUAGE = "en"

    val languageList = listOf(
        Language("en", "English"),
        Language("es", "Spanish"),
        Language("fr", "French"),
        Language("pt", "Portuguese"),
        Language("nl", "Dutch"),
        Language("tr", "Turkish"),
    )

    fun Context.setAppLanguage(languageCode: String, prefHelper: PrefHelper) {
        try {
            prefHelper.setStringValue(SELECTED_APP_LANGUAGE, languageCode)
            Locale.setDefault(Locale(languageCode))
            val configuration = resources.configuration
            configuration.setLocale(Locale(languageCode))
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun getAppLanguage(prefHelper: PrefHelper): String {
        return prefHelper.getStringValue(SELECTED_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)
    }

}