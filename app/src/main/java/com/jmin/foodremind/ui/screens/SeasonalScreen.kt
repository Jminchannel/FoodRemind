package com.jmin.foodremind.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jmin.foodremind.R
import com.jmin.foodremind.ui.components.FoodCard
import com.jmin.foodremind.ui.components.SeasonalFoodItem
import com.jmin.foodremind.ui.theme.SubtleText
import com.jmin.foodremind.ui.viewmodel.FoodViewModel

@Composable
fun SeasonalScreen(
    viewModel: FoodViewModel
) {
    val scrollState = rememberScrollState()
    val solarTerm = viewModel.currentSolarTerm

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Current Solar Term
        Text(
            text = stringResource(id = R.string.current_solar_term, stringResource(id = solarTerm.nameResId)),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(id = solarTerm.descriptionResId),
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recommended Foods
        FoodCard(title = stringResource(id = R.string.recommended_foods)) {
            solarTerm.recommendedFoods.forEach { food ->
                SeasonalFoodItem(
                    foodName = stringResource(id = food.nameResId),
                    description = stringResource(id = food.descriptionResId),
                    isRecommended = food.isRecommended
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Foods to Avoid
        FoodCard(title = stringResource(id = R.string.avoid_foods)) {
            solarTerm.avoidFoods.forEach { food ->
                SeasonalFoodItem(
                    foodName = stringResource(id = food.nameResId),
                    description = stringResource(id = food.descriptionResId),
                    isRecommended = food.isRecommended
                )
            }
        }
    }
} 