package com.bacancy.ccs2androidhmi.util

import android.content.Context
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.models.Language
import com.bacancy.ccs2androidhmi.views.fragment.AppSettingsFragment
import java.util.Locale

object LanguageConfig {

    private const val SELECTED_APP_LANGUAGE = "selected_app_language"
    private const val DEFAULT_APP_LANGUAGE = "en"

    private var languagesList: List<Language> = listOf()

    private const val ENGLISH_LANGUAGE_CODE = "en"
    private const val HINDI_LANGUAGE_CODE = "hi"
    private const val ARABIC_LANGUAGE_CODE = "ar"
    private const val SPANISH_LANGUAGE_CODE = "es"
    private const val FRENCH_LANGUAGE_CODE = "fr"
    private const val PORTUGESE_LANGUAGE_CODE = "pt"
    private const val DUTCH_LANGUAGE_CODE = "nl"
    private const val TURKISH_LANGUAGE_CODE = "tr"

    fun Context.setupLanguagesList() {
        languagesList = listOf(
            Language(ENGLISH_LANGUAGE_CODE, resources.getString(R.string.lbl_english),resources.getString(R.string.lbl_english_default)),
            Language(HINDI_LANGUAGE_CODE,resources.getString(R.string.lbl_hindi),resources.getString(R.string.lbl_hindi_default)),
            Language(ARABIC_LANGUAGE_CODE,resources.getString(R.string.lbl_arabic),resources.getString(R.string.lbl_arabic_default)),
            Language(SPANISH_LANGUAGE_CODE, resources.getString(R.string.lbl_spanish),resources.getString(R.string.lbl_spanish_default)),
            Language(FRENCH_LANGUAGE_CODE, resources.getString(R.string.lbl_french),resources.getString(R.string.lbl_french_default)),
            Language(PORTUGESE_LANGUAGE_CODE, resources.getString(R.string.lbl_portugese),resources.getString(R.string.lbl_portugese_default)),
            Language(DUTCH_LANGUAGE_CODE, resources.getString(R.string.lbl_dutch),resources.getString(R.string.lbl_dutch_default)),
            Language(TURKISH_LANGUAGE_CODE, resources.getString(R.string.lbl_turkish),resources.getString(R.string.lbl_turkish_default)),
        )
    }

    fun getLanguagesList(): List<Language> {
        return languagesList
    }

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

    fun Context.getLanguageName(prefHelper: PrefHelper): String {
        val languageCode = getAppLanguage(prefHelper)
        return languagesList.find { it.code == languageCode }?.name ?: AppSettingsFragment.ENGLISH
    }

}