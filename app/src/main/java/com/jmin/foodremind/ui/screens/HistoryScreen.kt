package com.jmin.foodremind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jmin.foodremind.R
import com.jmin.foodremind.ui.components.HistoryItem
import com.jmin.foodremind.ui.components.YearMonthPickerDialog
import com.jmin.foodremind.ui.theme.SubtleText
import com.jmin.foodremind.ui.viewmodel.FoodViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: FoodViewModel
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        YearMonthPickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { year, month ->
                viewModel.setFilters("$year-$month", null)
                showDatePicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { showDatePicker = true }) {
                Text(text = viewModel.selectedMonth ?: stringResource(id = R.string.btn_filter))
            }
            Button(onClick = { viewModel.resetFilters() }) {
                Text(text = stringResource(id = R.string.btn_reset))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History List
        if (viewModel.filteredMealHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SubtleText
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.filteredMealHistory) { record ->
                    HistoryItem(
                        name = record.name,
                        time = formatDate(date = record.date),
                        cost = record.cost,
                        taste = record.taste
                    )
                }
            }
        }
    }
}

@Composable
fun formatDate(date: Date): String {
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance()
    calendar.time = date

    val timeStr = "${calendar.get(Calendar.HOUR_OF_DAY)}:${String.format("%02d", calendar.get(Calendar.MINUTE))}"

    return when {
        now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) -> {
            stringResource(id = R.string.today, timeStr)
        }
        now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) + 1 -> {
            stringResource(id = R.string.yesterday, timeStr)
        }
        else -> {
            stringResource(id = R.string.date_format, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)) + " $timeStr"
        }
    }
} 