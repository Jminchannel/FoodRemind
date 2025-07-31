package com.jmin.foodremind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.jmin.foodremind.R
import com.jmin.foodremind.ui.theme.PrimaryColor
import com.jmin.foodremind.ui.theme.SuccessGreen
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

/**
 * 记录餐食对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordMealDialog(
    initialMealName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, cost: Double, taste: String) -> Unit
) {
    var mealName by remember { mutableStateOf(initialMealName) }
    var mealCost by remember { mutableStateOf("") }
    
    // 使用 remember 和 mutableStateOf 来存储初始值
    var mealTaste by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    // 在 Composable 函数的执行上下文中获取本地化字符串
    val tasteMedium = stringResource(id = R.string.taste_medium)
    val tasteLight = stringResource(id = R.string.taste_light)
    val tasteSpicy = stringResource(id = R.string.taste_spicy)
    val tasteGreasy = stringResource(id = R.string.taste_greasy)
    val tasteSweet = stringResource(id = R.string.taste_sweet)
    val tasteSalty = stringResource(id = R.string.taste_salty)
    
    // 使用 LaunchedEffect 来设置初始值
    LaunchedEffect(tasteMedium) {
        if (mealTaste.isEmpty()) {
            mealTaste = tasteMedium
        }
    }
    
    val tasteOptions = listOf(
        tasteLight,
        tasteMedium,
        tasteSpicy,
        tasteGreasy,
        tasteSweet,
        tasteSalty
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.record_meal_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Column {
                    Text(
                        text = stringResource(id = R.string.meal_name_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(id = R.string.meal_name_hint)) },
                        singleLine = true
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(id = R.string.meal_cost_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = mealCost,
                        onValueChange = { mealCost = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(id = R.string.meal_cost_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(id = R.string.meal_taste_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = mealTaste,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            tasteOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        mealTaste = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Button(
                        onClick = {
                            val cost = mealCost.toDoubleOrNull() ?: 0.0
                            if (mealName.isNotBlank()) {
                                onConfirm(mealName, cost, mealTaste)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                }
            }
        }
    }
}

/**
 * 自定义菜单对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeMenuDialog(
    foodOptions: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var options by remember { mutableStateOf(foodOptions.toMutableList()) }
    var newOption by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.customize_menu_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (options.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_options),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEachIndexed { index, option ->
                            FoodOptionItem(
                                name = option,
                                onDelete = {
                                    if (options.size > 2) {
                                        val currentList = options.toMutableList()
                                        currentList.removeAt(index)
                                        options = currentList
                                    } else {
                                        Toast.makeText(
                                            context,
                                            R.string.min_options_warning,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newOption,
                        onValueChange = { newOption = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(id = R.string.add_new_option)) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newOption.isNotBlank() && !options.contains(newOption)) {
                                val currentList = options.toMutableList()
                                currentList.add(newOption)
                                options = currentList
                                newOption = ""
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryColor)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.add),
                            tint = Color.White
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Button(
                        onClick = { 
                            if (options.size >= 2) {
                                onConfirm(options) 
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                }
            }
        }
    }
}

/**
 * 提醒设置对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingDialog(
    title: String,
    initialTime: String,
    initialLeadTime: Int,
    onDismiss: () -> Unit,
    onConfirm: (time: String, leadTime: Int) -> Unit
) {
    var time by remember { mutableStateOf(initialTime) }
    var expanded by remember { mutableStateOf(false) }
    var leadTime by remember { mutableStateOf(initialLeadTime) }
    var showTimePicker by remember { mutableStateOf(false) }

    val timeState = rememberTimePickerState(
        initialHour = initialTime.split(":").getOrNull(0)?.toIntOrNull() ?: 8,
        initialMinute = initialTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
        is24Hour = true
    )

    val leadTimeOptions = mapOf(
        0 to stringResource(R.string.on_time),
        5 to stringResource(R.string.lead_time_5),
        15 to stringResource(R.string.lead_time_15),
        30 to stringResource(R.string.lead_time_30),
        60 to stringResource(R.string.lead_time_60)
    )

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                time = String.format("%02d:%02d", timeState.hour, timeState.minute)
                showTimePicker = false
            },
            timeState = timeState
        )
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Column {
                    Text(
                        text = "用餐时间",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.clickable { showTimePicker = true }) {
                        OutlinedTextField(
                            value = time,
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            readOnly = true,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "提前多久提醒",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = leadTimeOptions[leadTime] ?: "准时提醒",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            leadTimeOptions.forEach { (value, text) ->
                                DropdownMenuItem(
                                    text = { Text(text) },
                                    onClick = {
                                        leadTime = value
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = { onConfirm(time, leadTime) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timeState: androidx.compose.material3.TimePickerState
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "选择时间", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(state = timeState)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis?.let {
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = it
                        calendar
                    }
                    if (selectedDate != null) {
                        onConfirm(
                            selectedDate.get(java.util.Calendar.YEAR),
                            selectedDate.get(java.util.Calendar.MONTH) + 1
                        )
                    }
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        androidx.compose.material3.DatePicker(state = datePickerState)
    }
} 

@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "en" to "English",
        "zh-CN" to "简体中文",
        "zh-TW" to "繁體中文",
        "ja" to "日本語"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.language)) },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    TextButton(
                        onClick = { onLanguageSelected(code) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.btn_cancel))
            }
        }
    )
} 