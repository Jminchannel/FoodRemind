package com.jmin.foodremind

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.jmin.foodremind.data.repository.UserPreferencesRepository
import com.jmin.foodremind.provider.AlarmSchedulerImpl
import com.jmin.foodremind.ui.navigation.MainNavigation
import com.jmin.foodremind.ui.theme.FoodRemindTheme
import com.jmin.foodremind.ui.viewmodel.FoodViewModel
import com.jmin.foodremind.ui.viewmodel.ViewModelFactory
import com.jmin.foodremind.utils.LocaleManager
import android.content.ComponentName
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.res.stringResource

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.updateResources(newBase))
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "需要通知权限才能提醒您用餐时间", Toast.LENGTH_LONG).show()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkAlarmPermission() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "需要精确闹钟权限才能准时提醒您用餐", Toast.LENGTH_LONG).show()
                try {
                    // 跳转到精确闹钟权限设置页面
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    // 如果无法直接跳转，则引导用户到应用设置页面
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager.applyLocale(this)
        askNotificationPermission()
        checkAlarmPermission()

        val userPreferencesRepository = UserPreferencesRepository(this)
        val alarmScheduler = AlarmSchedulerImpl(this)
        val viewModel: FoodViewModel by viewModels {
            ViewModelFactory(userPreferencesRepository, alarmScheduler)
        }

        setContent {
            var showAutostartDialog by remember { mutableStateOf(false) }
            var showFullscreenDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val autostartPermissionGiven = prefs.getBoolean("autostart_permission_given", false)
                if (!autostartPermissionGiven) {
                    showAutostartDialog = true
                }

                checkAndShowFullscreenPermissionDialog {
                    showFullscreenDialog = true
                }
            }

            FoodRemindTheme {
                MainNavigation(viewModel = viewModel)

                if (showAutostartDialog) {
                    AlertDialog(
                        onDismissRequest = { /* Prevent dismissing by clicking outside */ },
                        title = { Text("重要：开启后台权限") },
                        text = { Text("为了确保用餐提醒能够准时送达，请在本应用的设置中，允许“自启动”或“后台运行”。这是应用核心功能正常工作的关键。") },
                        confirmButton = {
                            TextButton(onClick = {
                                openAutostartSettings()
                                prefs.edit().putBoolean("autostart_permission_given", true).apply()
                                showAutostartDialog = false
                            }) {
                                Text("去设置")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAutostartDialog = false }) {
                                Text("稍后提醒")
                            }
                        }
                    )
                }

                if (showFullscreenDialog) {
                    AlertDialog(
                        onDismissRequest = { showFullscreenDialog = false },
                        title = { Text(stringResource(id = R.string.fullscreen_permission_title)) },
                        text = { Text(stringResource(id = R.string.fullscreen_permission_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                                showFullscreenDialog = false
                            }) {
                                Text("去设置")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showFullscreenDialog = false }) {
                                Text("稍后")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkAndShowFullscreenPermissionDialog(showDialog: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val notificationManager = NotificationManagerCompat.from(this)
            if (!notificationManager.areNotificationsEnabled()) {
                // 如果通知被禁用，首先请求通知权限
                askNotificationPermission()
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
                if (!notificationManager.canUseFullScreenIntent()) {
                    showDialog()
                }
            } else if (!Settings.canDrawOverlays(this)) {
                // 对于旧版本，检查悬浮窗权限
                showDialog()
            }
        }
    }

    private fun openAutostartSettings() {
        try {
            val intent = Intent()
            val manufacturer = Build.MANUFACTURER.toLowerCase()

            when {
                manufacturer.contains("xiaomi") -> {
                    intent.component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                manufacturer.contains("oppo") -> {
                    intent.component = ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                }
                manufacturer.contains("vivo") -> {
                    intent.component = ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                }
                manufacturer.contains("huawei") -> {
                    intent.component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                    )
                }
                manufacturer.contains("samsung") -> {
                    intent.component = ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                    )
                }
                else -> {
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.parse("package:$packageName")
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            e.printStackTrace()
        }
    }
}