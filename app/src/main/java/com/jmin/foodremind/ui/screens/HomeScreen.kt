package com.jmin.foodremind.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jmin.foodremind.R
import com.jmin.foodremind.ui.components.FoodCard
import com.jmin.foodremind.ui.components.NicknameDialog
import com.jmin.foodremind.ui.components.PrimaryButton
import com.jmin.foodremind.ui.components.RecordMealDialog
import com.jmin.foodremind.ui.components.SecondaryButton
import com.jmin.foodremind.ui.viewmodel.FoodViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    viewModel: FoodViewModel,
    onFoodPickerClick: () -> Unit,
    onManualRecordClick: () -> Unit,
    showRecordDialog: Boolean = false
) {
    val nickname by viewModel.nickname.collectAsState()
    val showNicknameDialog by viewModel.showNicknameDialog.collectAsState()
    val countdown by viewModel.nextMealCountdown.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.nicknameUpdateSuccess.collectLatest {
            Toast.makeText(context, R.string.nickname_saved_success, Toast.LENGTH_SHORT).show()
        }
    }

    if (showNicknameDialog) {
        NicknameDialog(
            currentNickname = nickname,
            onDismiss = { viewModel.onNicknameDialogDismiss() },
            onConfirm = { name -> viewModel.saveNickname(name) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Greeting
        Text(
            text = stringResource(id = viewModel.getCurrentGreetingResId(), nickname),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Next Meal Countdown
        val nextMealName = stringResource(id = viewModel.getNextMealNameResId())
        FoodCard(title = stringResource(id = R.string.meal_countdown, nextMealName)) {
            Text(
                text = countdown,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimaryButton(
                text = stringResource(id = R.string.food_picker_btn),
                icon = Icons.Filled.Refresh,
                onClick = onFoodPickerClick
            )
            SecondaryButton(
                text = stringResource(id = R.string.manual_record_btn),
                icon = Icons.Filled.Add,
                onClick = onManualRecordClick
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Seasonal Tips
        val solarTerm = viewModel.currentSolarTerm
        FoodCard(
            title = stringResource(
                id = R.string.seasonal_tips_title,
                stringResource(id = solarTerm.nameResId)
            )
        ) {
            Text(
                text = stringResource(id = solarTerm.descriptionResId),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showRecordDialog) {
        RecordMealDialog(
            initialMealName = "",
            onDismiss = { onManualRecordClick() },
            onConfirm = { name, cost, taste ->
                viewModel.addMealRecord(name, cost, taste)
                onManualRecordClick()
            }
        )
    }
}
