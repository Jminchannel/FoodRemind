package com.jmin.foodremind.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jmin.foodremind.R
import com.jmin.foodremind.data.model.MealRecord
import com.jmin.foodremind.data.model.ReminderSetting
import com.jmin.foodremind.data.model.SeasonalFood
import com.jmin.foodremind.data.model.SolarTerm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID

/**
 * Food data repository
 * In a real app, this data should be fetched from a database or network
 * For simplicity, we use in-memory data here
 */
class FoodRepository {
    // Food options list
    private val _foodOptions = mutableStateListOf(
        "Burger", "Pizza", "Sushi", 
        "Pasta", "Salad", "Sandwich",
        "Taco", "Ramen"
    )
    val foodOptions: SnapshotStateList<String> = _foodOptions
    
    // Meal history list
    private val _mealHistory = mutableStateListOf(
        MealRecord(
            id = UUID.randomUUID().toString(),
            name = "Chicken Rice",
            cost = 12.0,
            taste = "Medium",
            date = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000) // Yesterday
        ),
        MealRecord(
            id = UUID.randomUUID().toString(),
            name = "Sandwich",
            cost = 8.0,
            taste = "Salty",
            date = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000) // 3 days ago
        ),
        MealRecord(
            id = UUID.randomUUID().toString(),
            name = "Ramen",
            cost = 15.0,
            taste = "Light",
            date = Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000) // 30 days ago
        )
    )
    val mealHistory: SnapshotStateList<MealRecord> = _mealHistory
    
    // Reminder settings list
    private val _reminderSettings = MutableStateFlow(listOf(
        ReminderSetting(
            id = "breakfast",
            nameResId = R.string.breakfast,
            icon = "fas fa-sun",
            time = "08:00",
            leadTime = 15,
            enabled = true
        ),
        ReminderSetting(
            id = "lunch",
            nameResId = R.string.lunch,
            icon = "fas fa-cloud-sun",
            time = "12:30",
            leadTime = 15,
            enabled = true
        ),
        ReminderSetting(
            id = "dinner",
            nameResId = R.string.dinner,
            icon = "fas fa-moon",
            time = "19:00",
            leadTime = 30,
            enabled = false
        )
    ))
    val reminderSettings: StateFlow<List<ReminderSetting>> = _reminderSettings.asStateFlow()
    
    // Current solar term information
    private val _currentSolarTerm = SolarTerm(
        nameResId = R.string.solarterm_whitedew_name,
        descriptionResId = R.string.solarterm_whitedew_desc,
        recommendedFoods = listOf(
            SeasonalFood(
                nameResId = R.string.food_pear_name,
                descriptionResId = R.string.food_pear_desc,
                icon = "fas fa-apple-alt",
                isRecommended = true
            ),
            SeasonalFood(
                nameResId = R.string.food_whitefungus_name,
                descriptionResId = R.string.food_whitefungus_desc,
                icon = "fa-solid fa-seedling",
                isRecommended = true
            ),
            SeasonalFood(
                nameResId = R.string.food_glutinousrice_name,
                descriptionResId = R.string.food_glutinousrice_desc,
                icon = "fa-solid fa-bowl-rice",
                isRecommended = true
            )
        ),
        avoidFoods = listOf(
            SeasonalFood(
                nameResId = R.string.food_coldseafood_name,
                descriptionResId = R.string.food_coldseafood_desc,
                icon = "fas fa-shrimp",
                isRecommended = false
            ),
            SeasonalFood(
                nameResId = R.string.food_spicy_name,
                descriptionResId = R.string.food_spicy_desc,
                icon = "fa-solid fa-pepper-hot",
                isRecommended = false
            )
        )
    )
    val currentSolarTerm: SolarTerm = _currentSolarTerm
    
    // Add food option
    fun addFoodOption(option: String) {
        if (option.isNotBlank() && !_foodOptions.contains(option)) {
            _foodOptions.add(option)
        }
    }
    
    // Remove food option
    fun removeFoodOption(option: String) {
        _foodOptions.remove(option)
    }
    
    // Update food options in batch
    fun updateFoodOptions(options: List<String>) {
        _foodOptions.clear()
        _foodOptions.addAll(options)
    }

    // Add meal record
    fun addMealRecord(name: String, cost: Double, taste: String) {
        val record = MealRecord(
            id = UUID.randomUUID().toString(),
            name = name,
            cost = cost,
            taste = taste,
            date = Date()
        )
        _mealHistory.add(record)
    }
    
    // Update reminder setting
    fun updateReminderSetting(id: String, time: String, leadTime: Int, enabled: Boolean) {
        _reminderSettings.value = _reminderSettings.value.map {
            if (it.id == id) {
                it.copy(time = time, leadTime = leadTime, enabled = enabled)
            } else {
                it
            }
        }
    }
    
    // Update reminder enabled status
    fun updateReminderEnabled(id: String, enabled: Boolean) {
        _reminderSettings.value = _reminderSettings.value.map {
            if (it.id == id) {
                it.copy(enabled = enabled)
            } else {
                it
            }
        }
    }
} 