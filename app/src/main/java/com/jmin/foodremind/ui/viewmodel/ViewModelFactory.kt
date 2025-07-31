package com.jmin.foodremind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jmin.foodremind.data.repository.UserPreferencesRepository
import com.jmin.foodremind.provider.AlarmScheduler
import com.jmin.foodremind.ui.viewmodel.FoodViewModel

class ViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(userPreferencesRepository, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 