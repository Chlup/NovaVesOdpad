package com.mugeaters.popelnice.nvpp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mugeaters.popelnice.nvpp.service.NotificationBuilderInput
import com.mugeaters.popelnice.nvpp.service.NotificationsBuilder
import com.mugeaters.popelnice.nvpp.service.NotificationsBuilderImpl
import com.mugeaters.popelnice.nvpp.ui.settings.PreferencesManager
import com.mugeaters.popelnice.nvpp.ui.settings.SharedPreferencesManager
import com.mugeaters.popelnice.nvpp.util.LogcatLogger
import com.mugeaters.popelnice.nvpp.util.Logger
import com.mugeaters.popelnice.nvpp.util.TrashDayGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Broadcast receiver that handles device boot completion and reschedules notifications
 */
class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {
    
    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "📱 BootCompletedReceiver.onReceive() called with action: ${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "🔄 Device rebooted, attempting to reschedule notifications")
            
            // Use a coroutine scope to handle async operations
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleNotifications(context)
                    Log.d(TAG, "✅ Notifications rescheduled successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to reschedule notifications", e)
                } finally {
                    pendingResult.finish()
                }
            }
        } else {
            Log.d(TAG, "📱 Ignoring action: ${intent.action}")
        }
    }
    
    private suspend fun rescheduleNotifications(context: Context) {
        Log.d(TAG, "🔧 Starting notification rescheduling process")
        
        try {
            // Try to use Koin DI first, but fallback to manual instantiation if needed
            val logger = try {
                val injectedLogger: Logger by inject()
                injectedLogger
            } catch (e: Exception) {
                Log.w(TAG, "Koin DI not available, using fallback logger")
                LogcatLogger()
            }
            
            val preferencesManager = try {
                val injectedPrefs: PreferencesManager by inject()
                injectedPrefs
            } catch (e: Exception) {
                Log.w(TAG, "Koin DI not available, creating fallback PreferencesManager")
                SharedPreferencesManager(context)
            }
            
            val trashDayGenerator = try {
                val injectedGenerator: TrashDayGenerator by inject()
                injectedGenerator
            } catch (e: Exception) {
                Log.w(TAG, "Koin DI not available, creating fallback TrashDayGenerator")
                TrashDayGenerator()
            }
            
            val notificationsBuilder = try {
                val injectedBuilder: NotificationsBuilder by inject()
                injectedBuilder
            } catch (e: Exception) {
                Log.w(TAG, "Koin DI not available, creating fallback NotificationsBuilder")
                NotificationsBuilderImpl(context, logger)
            }
            
            logger.debug("🔧 Retrieved dependencies, getting notification settings")
            
            // Get current notification settings
            val notificationSettings = preferencesManager.getNotificationSettings()
            logger.debug("🔧 Settings: enabled=${notificationSettings.notificationEnabled}, offset=${notificationSettings.notificationDaysOffset}, hour=${notificationSettings.selectedNotificationHour}")
            
            // Only proceed if notifications are enabled
            if (!notificationSettings.notificationEnabled) {
                logger.debug("🔧 Notifications are disabled, skipping reschedule")
                return
            }
            
            // Generate trash days using the shared generator
            val trashDays = trashDayGenerator.generateTrashDays()
            logger.debug("🔧 Generated ${trashDays.size} trash days")
            
            // Build notification input
            val input = NotificationBuilderInput(
                days = trashDays,
                notificationEnabled = notificationSettings.notificationEnabled,
                notificationDaysOffset = notificationSettings.notificationDaysOffset,
                selectedNotificationHour = notificationSettings.selectedNotificationHour
            )
            
            // Check exact alarm permission before scheduling
            val canScheduleExact = notificationsBuilder.canScheduleExactAlarms()
            logger.debug("🔧 Can schedule exact alarms: $canScheduleExact")
            
            if (!canScheduleExact) {
                logger.debug("⚠️ Exact alarm permission not granted - notifications may be delayed after reboot")
            }
            
            // Reschedule all notifications
            logger.debug("🔧 Calling notificationsBuilder.build()")
            notificationsBuilder.build(input)
            logger.debug("🔧 NotificationsBuilder.build() completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception during notification rescheduling", e)
            throw e
        }
    }
}