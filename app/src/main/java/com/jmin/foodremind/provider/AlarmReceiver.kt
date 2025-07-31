package com.jmin.foodremind.provider

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jmin.foodremind.R
import com.jmin.foodremind.data.repository.FoodRepository
import com.jmin.foodremind.utils.LocaleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import com.jmin.foodremind.ReminderActivity

class AlarmReceiver : BroadcastReceiver() {
    // 使用 SupervisorJob 确保即使子协程失败，其他协程仍能继续执行
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        // 使用 LocaleManager 获取正确的 Context
        val localizedContext = LocaleManager.updateResources(context)

        // 获取 WakeLock 以确保设备不会在处理过程中休眠
        val wakeLock = WakeLockHelper.acquireWakeLock(localizedContext)

        scope.launch {
            try {
                if (intent.action == "android.intent.action.BOOT_COMPLETED") {
                    // 重新安排闹钟，使用超时限制避免长时间运行
                    rescheduleAlarms(localizedContext)
                } else {
                    triggerReminderActivity(localizedContext, intent)
                }
            } finally {
                // 确保释放 WakeLock
                WakeLockHelper.releaseWakeLock(wakeLock)
                pendingResult.finish()
            }
        }
    }

    private fun rescheduleAlarms(context: Context) {
        val repository = FoodRepository()
        val scheduler = AlarmSchedulerImpl(context)
        repository.reminderSettings.value
            .filter { it.enabled }
            .forEach { scheduler.schedule(it) }
    }

    private fun triggerReminderActivity(context: Context, intent: Intent) {
        val reminderActivityIntent = Intent(context, ReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("EXTRA_REMINDER_ID", intent.getStringExtra("EXTRA_REMINDER_ID"))
            putExtra("EXTRA_REMINDER_NAME_RES_ID", intent.getIntExtra("EXTRA_REMINDER_NAME_RES_ID", 0))
        }
        context.startActivity(reminderActivityIntent)
    }
}

/**
 * 帮助类，用于管理 WakeLock
 */
object WakeLockHelper {
    private const val WAKE_LOCK_TAG = "FoodRemind:AlarmWakeLock"
    
    fun acquireWakeLock(context: Context): android.os.PowerManager.WakeLock? {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val wakeLock = powerManager.newWakeLock(
                android.os.PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            )
            wakeLock.acquire(60000) // 最多持有60秒
            wakeLock
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun releaseWakeLock(wakeLock: android.os.PowerManager.WakeLock?) {
        try {
            if (wakeLock != null && wakeLock.isHeld) {
                wakeLock.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 