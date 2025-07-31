package com.jmin.foodremind.data.model

import androidx.annotation.StringRes
import java.util.Date

/**
 * 餐食记录数据模型
 */
data class MealRecord(
    val id: String = "",
    val name: String,
    val cost: Double,
    val taste: String,
    val date: Date
)

/**
 * 提醒设置数据模型
 */
data class ReminderSetting(
    val id: String,
    @StringRes val nameResId: Int,
    val icon: String,
    val time: String,
    val leadTime: Int,
    val enabled: Boolean
)

/**
 * 时令食物数据模型
 */
data class SeasonalFood(
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int,
    val icon: String,
    val isRecommended: Boolean
)

/**
 * 节气信息数据模型
 */
data class SolarTerm(
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int,
    val recommendedFoods: List<SeasonalFood>,
    val avoidFoods: List<SeasonalFood>
) 