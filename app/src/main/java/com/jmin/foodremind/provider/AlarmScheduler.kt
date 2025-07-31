package com.jmin.foodremind.provider

import com.jmin.foodremind.data.model.ReminderSetting

interface AlarmScheduler {
    fun schedule(reminder: ReminderSetting)
    fun cancel(reminder: ReminderSetting)
} 