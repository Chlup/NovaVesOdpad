package com.mugeaters.popelnice.nvpp.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.mugeaters.popelnice.nvpp.model.TrashDay
import com.mugeaters.popelnice.nvpp.notification.NotificationReceiver
import com.mugeaters.popelnice.nvpp.util.Logger
import kotlinx.coroutines.yield
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * Input data for building notifications
 */
data class NotificationBuilderInput(
    val days: List<TrashDay>,
    val notificationEnabled: Boolean,
    val notificationDaysOffset: Int,
    val selectedNotificationHour: Int
)

/**
 * Interface for notification builder
 */
interface NotificationsBuilder {
    suspend fun build(input: NotificationBuilderInput)
    suspend fun cancelAll()
    fun canScheduleExactAlarms(): Boolean
    fun openExactAlarmSettings()
}

/**
 * Implementation of notification builder that schedules local notifications for trash days
 */
class NotificationsBuilderImpl(
    private val context: Context,
    private val logger: Logger
) : NotificationsBuilder {
    
    override suspend fun build(input: NotificationBuilderInput) {
        logger.debug("🔔 NotificationsBuilder.build() called with ${input.days.size} days, enabled: ${input.notificationEnabled}, offset: ${input.notificationDaysOffset}, hour: ${input.selectedNotificationHour}")
        
        // Create notification channel if needed (for Android 8.0+)
        createNotificationChannel()
        
        // Cancel all existing notifications
        cancelAllNotifications()
        
        // If notifications are disabled, just return after canceling
        if (!input.notificationEnabled) {
            logger.debug("Notifications disabled, not scheduling any")
            return
        }
        
        // Check exact alarm permission and warn if needed
        checkExactAlarmPermission()
        
        // Schedule new notifications for all available days
        var scheduledCount = 0
        var skippedCount = 0
        
        for ((index, day) in input.days.withIndex()) {
            // Yield every 10 iterations to prevent blocking
            if (index % 10 == 0) {
                yield()
            }
            
            logger.debug("📅 Processing day: ${day.date} with bins: ${day.bins.map { it.title }}")
            val request = scheduleNotificationForDay(day, input)
            if (request != null) {
                logger.debug("📝 Created notification request for ${day.date}")
                scheduleNotification(request)
                scheduledCount++
            } else {
                skippedCount++
            }
        }
        
        logger.debug("✅ NotificationsBuilder.build() completed: scheduled $scheduledCount notifications, skipped $skippedCount")
    }
    
    override suspend fun cancelAll() {
        cancelAllNotifications()
    }
    
    private fun createNotificationChannel() {
        val name = "Odvoz odpadu"
        val descriptionText = "Upozornění na odvoz odpadu"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun cancelAllNotifications() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancel all pending alarms
        for (requestCode in 0..1000) { // Use a reasonable range for your expected notifications
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, NotificationReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        
        // Remove all delivered notifications
        notificationManager.cancelAll()
    }
    
    private fun scheduleNotificationForDay(
        day: TrashDay, 
        input: NotificationBuilderInput
    ): NotificationRequest? {
        val title: String
        val body: String
        
        when (input.notificationDaysOffset) {
            0 -> {
                title = "Dnes se vyváží odpad"
                body = "Dnes se budou vyvážet tyto popelnice:"
            }
            1 -> {
                title = "Odvoz odpadu je již skoro tady"
                body = "Zítra se budou vyvážet tyto popelnice:"
            }
            2 -> {
                title = "Odvoz odpadu se blíží"
                body = "Za dva dny se budou vyvážet tyto popelnice:"
            }
            else -> {
                title = "Odvoz odpadu se blíží"
                body = "Za tři dny se budou vyvážet tyto popelnice:"
            }
        }
        
        return createNotificationRequest(
            day = day,
            offsetDays = -input.notificationDaysOffset,
            hour = input.selectedNotificationHour,
            title = title,
            body = body
        )
    }
    
    private fun createNotificationRequest(
        day: TrashDay,
        offsetDays: Int,
        hour: Int,
        title: String,
        body: String
    ): NotificationRequest? {
        val date = day.date
        val notificationDate = date.plusDays(offsetDays.toLong())
            .withHour(hour)
            .withMinute(25)
            .withSecond(0)
        
        val now = LocalDateTime.now()
        
        if (notificationDate.isBefore(now)) {
            logger.debug("⚠️ Skipping notification for $notificationDate as it's in the past")
            return null
        }
        
        logger.debug("✅ Scheduling notification for day $day offset $offsetDays hour $hour $notificationDate")
        
        val binsList = day.bins.joinToString("\n") { it.title }
        val finalBody = "$body\n$binsList"
        
        return NotificationRequest(
            notificationId = (day.date.toString() + offsetDays.toString()).hashCode(),
            title = title,
            content = finalBody,
            triggerTime = notificationDate
        )
    }
    
    private fun scheduleNotification(request: NotificationRequest) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.NOTIFICATION_ID, request.notificationId)
            putExtra(NotificationReceiver.NOTIFICATION_TITLE, request.title)
            putExtra(NotificationReceiver.NOTIFICATION_CONTENT, request.content)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            request.notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTimeMillis = request.triggerTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                logger.debug("✅ Using exact alarm scheduling")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                logger.debug("⚠️ Cannot schedule exact alarms, using setExactAndAllowWhileIdle with inexact fallback")
                // Try setExactAndAllowWhileIdle first, then fall back to inexact if needed
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    logger.debug("⚠️ Falling back to inexact alarm due to security exception")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            }
        } else {
            logger.debug("✅ Using exact alarm scheduling (Android < 12)")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
        
        logger.debug("Scheduled notification with ID: ${request.notificationId} for ${Date(triggerTimeMillis)}")
    }
    
    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Always allowed on Android < 12
        }
    }
    
    override fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                logger.debug("Failed to open exact alarm settings: ${e.message}")
            }
        }
    }
    
    private fun checkExactAlarmPermission() {
        if (!canScheduleExactAlarms()) {
            logger.debug("⚠️ Exact alarm permission not granted - notifications may be delayed")
        }
    }
    
    private data class NotificationRequest(
        val notificationId: Int,
        val title: String,
        val content: String,
        val triggerTime: LocalDateTime
    )
    
    companion object {
        const val CHANNEL_ID = "trash_collection_channel"
    }
}