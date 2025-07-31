package com.jmin.foodremind.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar
import androidx.compose.ui.res.stringResource
import com.jmin.foodremind.R

@Composable
fun YearMonthPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (2020..currentYear).toList()
    val months = (1..12).toList()

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(id = R.string.select_month), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Picker(
                        items = years.map { it.toString() },
                        initialItem = selectedYear.toString(),
                        onItemSelected = { selectedYear = it.toInt() }
                    )
                    Picker(
                        items = months.map { String.format("%02d", it) },
                        initialItem = String.format("%02d", selectedMonth),
                        onItemSelected = { selectedMonth = it.toInt() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                        Text(stringResource(id = R.string.confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun Picker(
    items: List<String>,
    initialItem: String,
    onItemSelected: (String) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = items.indexOf(initialItem)
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.height(150.dp)
    ) {
        items(items.size) { index ->
            Text(
                text = items[index],
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        onItemSelected(items[index])
                    },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 