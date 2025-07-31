package com.jmin.foodremind.ui.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.foodremind.R
import com.jmin.foodremind.data.model.MealRecord
import com.jmin.foodremind.data.repository.FoodRepository
import com.jmin.foodremind.data.repository.UserPreferencesRepository
import com.jmin.foodremind.provider.AlarmScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class FoodViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    // Data repository
    private val repository = FoodRepository()

    private val _nicknameUpdateSuccess = MutableSharedFlow<Unit>()
    val nicknameUpdateSuccess = _nicknameUpdateSuccess.asSharedFlow()

    // Food options
    val foodOptions = repository.foodOptions

    // Meal history
    val mealHistory = repository.mealHistory

    // Reminder settings
    val reminderSettings = repository.reminderSettings

    // Current solar term information
    val currentSolarTerm = repository.currentSolarTerm

    // Filtered meal history
    var filteredMealHistory by mutableStateOf<List<MealRecord>>(mealHistory)
        private set

    // Currently selected food
    var selectedFood by mutableStateOf<String?>(null)
        private set

    // Is spinning
    var isSpinning by mutableStateOf(false)
        private set

    // Pick count
    var pickCount by mutableStateOf(0)
        private set

    private val _nextMealCountdown = MutableStateFlow("00:00:00")
    val nextMealCountdown = _nextMealCountdown.asStateFlow()

    // Current editing reminder ID
    var currentEditingReminderId by mutableStateOf<String?>(null)

    // Selected month
    var selectedMonth by mutableStateOf<String?>(null)
        private set

    // Selected taste
    var selectedTaste by mutableStateOf<String?>(null)
        private set

    // Nickname
    private val _nickname = MutableStateFlow(userPreferencesRepository.getNickname())
    val nickname = _nickname.asStateFlow()

    // Show nickname dialog
    private val _showNicknameDialog = MutableStateFlow(userPreferencesRepository.isFirstLaunch())
    val showNicknameDialog = _showNicknameDialog.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _nextMealCountdown.value = getNextMealCountdown()
                delay(1000)
            }
        }
    }
    
    fun saveNickname(name: String) {
        userPreferencesRepository.saveNickname(name)
        _nickname.value = name
        if (userPreferencesRepository.isFirstLaunch()) {
            userPreferencesRepository.setFirstLaunchCompleted()
        }
        _showNicknameDialog.value = false
        viewModelScope.launch {
            _nicknameUpdateSuccess.emit(Unit)
        }
    }

    fun onNicknameDialogDismiss() {
        if (userPreferencesRepository.isFirstLaunch()) {
            // If it's the first launch and the user dismisses the dialog,
            // we should still save a default name and mark the first launch as completed.
            saveNickname(userPreferencesRepository.getNickname())
        }
        _showNicknameDialog.value = false
    }

    fun showNicknameEditor() {
        _showNicknameDialog.value = true
    }

    // Get current greeting resource ID
    @StringRes
    fun getCurrentGreetingResId(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 6 -> R.string.greeting_night
            hour < 11 -> R.string.greeting_morning
            hour < 13 -> R.string.greeting_noon
            hour < 18 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
    }

    // Get next meal name resource ID
    @StringRes
    fun getNextMealNameResId(): Int {
        return getNextMealTimeInfo().first
    }

    // Get next meal countdown
    private fun getNextMealCountdown(): String {
        val now = Calendar.getInstance()
        val targetCalendar = getNextMealTimeInfo().second

        val diff = targetCalendar.timeInMillis - now.timeInMillis
        val diffHours = diff / (1000 * 60 * 60)
        val diffMinutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
        val diffSeconds = (diff % (1000 * 60)) / 1000

        return String.format("%02d:%02d:%02d", diffHours, diffMinutes, diffSeconds)
    }

    private fun getNextMealTimeInfo(): Pair<Int, Calendar> {
        val now = Calendar.getInstance()

        val breakfastSetting = reminderSettings.value.find { it.id == "breakfast" }
        val lunchSetting = reminderSettings.value.find { it.id == "lunch" }
        val dinnerSetting = reminderSettings.value.find { it.id == "dinner" }

        val breakfastTimeParts = breakfastSetting?.time?.split(":")?.map { it.toInt() } ?: listOf(8, 0)
        val lunchTimeParts = lunchSetting?.time?.split(":")?.map { it.toInt() } ?: listOf(12, 0)
        val dinnerTimeParts = dinnerSetting?.time?.split(":")?.map { it.toInt() } ?: listOf(19, 0)

        val breakfastCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, breakfastTimeParts[0])
            set(Calendar.MINUTE, breakfastTimeParts[1])
            set(Calendar.SECOND, 0)
        }
        val lunchCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, lunchTimeParts[0])
            set(Calendar.MINUTE, lunchTimeParts[1])
            set(Calendar.SECOND, 0)
        }
        val dinnerCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, dinnerTimeParts[0])
            set(Calendar.MINUTE, dinnerTimeParts[1])
            set(Calendar.SECOND, 0)
        }

        return when {
            now.before(breakfastCalendar) -> Pair(R.string.breakfast, breakfastCalendar)
            now.before(lunchCalendar) -> Pair(R.string.lunch, lunchCalendar)
            now.before(dinnerCalendar) -> Pair(R.string.dinner, dinnerCalendar)
            else -> {
                // Next day's breakfast
                breakfastCalendar.add(Calendar.DAY_OF_YEAR, 1)
                Pair(R.string.breakfast, breakfastCalendar)
            }
        }
    }
    
    fun setFinalSelectedFood(food: String) {
        selectedFood = food
        pickCount++
    }

    // Add food option
    fun addFoodOption(option: String) {
        repository.addFoodOption(option)
    }

    // Remove food option
    fun removeFoodOption(option: String) {
        repository.removeFoodOption(option)
    }

    // Update food options in batch
    fun updateFoodOptions(options: List<String>) {
        repository.updateFoodOptions(options)
    }

    // Add meal record
    fun addMealRecord(name: String, cost: Double, taste: String) {
        repository.addMealRecord(name, cost, taste)
        // Update filtered records
        applyFilters()
    }

    // Update reminder setting
    fun updateReminderSetting(id: String, time: String, leadTime: Int, enabled: Boolean) {
        repository.updateReminderSetting(id, time, leadTime, enabled)
        val reminder = reminderSettings.value.find { it.id == id }
        if (reminder != null) {
            if (enabled) {
                alarmScheduler.schedule(reminder)
            } else {
                alarmScheduler.cancel(reminder)
            }
        }
    }

    // Update reminder enabled status
    fun updateReminderEnabled(id: String, enabled: Boolean) {
        repository.updateReminderEnabled(id, enabled)
        val reminder = reminderSettings.value.find { it.id == id }
        if (reminder != null) {
            if (enabled) {
                alarmScheduler.schedule(reminder)
            } else {
                alarmScheduler.cancel(reminder)
            }
        }
    }

    // Set filters
    fun setFilters(month: String?, taste: String?) {
        selectedMonth = month
        selectedTaste = taste
        applyFilters()
    }

    // Reset filters
    fun resetFilters() {
        selectedMonth = null
        selectedTaste = null
        filteredMealHistory = mealHistory
    }

    // Apply filters
    private fun applyFilters() {
        var filtered = mealHistory.toList()

        // Filter by month
        if (!selectedMonth.isNullOrEmpty()) {
            val (year, month) = selectedMonth!!.split("-").map { it.toInt() }
            filtered = filtered.filter { record ->
                val calendar = Calendar.getInstance()
                calendar.time = record.date
                calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) + 1 == month
            }
        }

        // Filter by taste
        if (!selectedTaste.isNullOrEmpty() && selectedTaste != "all") {
            filtered = filtered.filter { it.taste == selectedTaste }
        }

        filteredMealHistory = filtered
    }
} 