package com.jmin.foodremind.data.repository

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun getNickname(): String {
        return prefs.getString(KEY_NICKNAME, DEFAULT_NICKNAME) ?: DEFAULT_NICKNAME
    }

    fun saveNickname(name: String) {
        prefs.edit().putString(KEY_NICKNAME, name).apply()
    }

    companion object {
        private const val PREFS_NAME = "FoodRemindPrefs"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_NICKNAME = "key_nickname"
        private const val DEFAULT_NICKNAME = "User"
    }
} 