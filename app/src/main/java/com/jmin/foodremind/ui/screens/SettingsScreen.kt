package com.jmin.foodremind.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jmin.foodremind.R
import com.jmin.foodremind.ui.components.NicknameDialog
import com.jmin.foodremind.ui.components.ReminderSettingDialog
import com.jmin.foodremind.ui.components.ReminderSettingItem
import com.jmin.foodremind.ui.components.SettingItem
import com.jmin.foodremind.ui.theme.SubtleText
import com.jmin.foodremind.ui.viewmodel.FoodViewModel
import com.jmin.foodremind.utils.LocaleManager
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    viewModel: FoodViewModel,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showReminderDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val showNicknameDialog by viewModel.showNicknameDialog.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val reminderSettings by viewModel.reminderSettings.collectAsState()

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
            .verticalScroll(scrollState)
    ) {
        // Account Settings
        Text(
            text = stringResource(id = R.string.account_settings),
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleText,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        SettingItem(
            title = stringResource(id = R.string.edit_nickname),
            icon = Icons.Filled.Person,
            onClick = { viewModel.showNicknameEditor() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Meal Reminders
        Text(
            text = stringResource(id = R.string.meal_reminders),
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleText,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        reminderSettings.forEach { setting ->
            ReminderSettingItem(
                title = stringResource(id = setting.nameResId),
                icon = getSettingIcon(setting.id),
                time = setting.time,
                isEnabled = setting.enabled,
                onToggleChange = { enabled ->
                    viewModel.updateReminderEnabled(setting.id, enabled)
                },
                onClick = {
                    viewModel.currentEditingReminderId = setting.id
                    showReminderDialog = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About section
        Text(
            text = stringResource(id = R.string.about),
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleText,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        SettingItem(
            title = stringResource(id = R.string.about_us),
            icon = Icons.Filled.Info,
            onClick = { showAboutDialog = true }
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingItem(
            title = stringResource(id = R.string.rate_us),
            icon = Icons.Filled.Star,
            onClick = { 
                // 打开应用商店评分页面
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // 如果没有应用商店，则打开浏览器访问Google Play
                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                }
            }
        )
    }

    // Reminder settings dialog
    if (showReminderDialog && viewModel.currentEditingReminderId != null) {
        val setting = reminderSettings.find { it.id == viewModel.currentEditingReminderId }
        if (setting != null) {
            ReminderSettingDialog(
                title = stringResource(
                    id = R.string.set_reminder_title,
                    stringResource(id = setting.nameResId)
                ),
                initialTime = setting.time,
                initialLeadTime = setting.leadTime,
                onDismiss = {
                    showReminderDialog = false
                    viewModel.currentEditingReminderId = null
                },
                onConfirm = { time, leadTime ->
                    viewModel.updateReminderSetting(
                        setting.id,
                        time,
                        leadTime,
                        setting.enabled
                    )
                    showReminderDialog = false
                    viewModel.currentEditingReminderId = null
                }
            )
        }
    }
    
    // About Us dialog
    if (showAboutDialog) {
        AboutUsDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun AboutUsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.about_us)) },
        text = {
            Column {
                Text("FoodRemind - Version 1.0.0")
                Spacer(modifier = Modifier.height(8.dp))
                Text("An app that helps you record and plan your diet.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("© 2023-2025 JMin. reserves all rights.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.btn_confirm))
            }
        }
    )
}

@Composable
private fun getCurrentLanguageName(languageCode: String): String {
    return when (languageCode) {
        "en" -> "English"
        "zh-CN" -> "简体中文"
        "zh-TW" -> "繁體中文"
        "ja" -> "日本語"
        else -> "English"
    }
}

private fun getSettingIcon(id: String): ImageVector {
    return when (id) {
        "breakfast" -> Icons.Filled.Favorite
        "lunch" -> Icons.Filled.Home
        "dinner" -> Icons.Filled.Warning
        else -> Icons.Filled.Info
    }
} 