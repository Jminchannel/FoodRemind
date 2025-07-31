package com.jmin.foodremind.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManager {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "language"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLocale(context: Context, language: String) {
        persistLanguage(context, language)
        updateAppLocale(language)
        
        // 强制更新资源配置
        updateResources(context)
    }

    fun getLanguage(context: Context): String {
        return getPreferences(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }
    
    fun applyLocale(context: Context) {
        val language = getLanguage(context)
        updateAppLocale(language)
        updateResources(context)
    }

    private fun persistLanguage(context: Context, language: String) {
        getPreferences(context).edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    private fun updateAppLocale(language: String) {
        val localeTag = when(language) {
            "zh-CN" -> "zh-rCN"
            "zh-TW" -> "zh-rTW"
            "ja" -> "ja-rJP"
            else -> language
        }
        
        val locale = if (localeTag.isNotEmpty()) {
            Locale.forLanguageTag(localeTag)
        } else {
            Locale.getDefault()
        }
        val appLocale = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
    
    // 直接更新资源配置
    fun updateResources(context: Context): Context {
        val language = getLanguage(context)
        val locale = when(language) {
            "zh-CN" -> Locale("zh", "CN")
            "zh-TW" -> Locale("zh", "TW")
            "ja" -> Locale("ja", "JP")
            else -> Locale("en")
        }
        
        Locale.setDefault(locale)
        
        val resources = context.resources
        val config = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
            context
        }
    }
}
