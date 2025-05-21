package cz.novavesodpad.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import cz.novavesodpad.MainActivity
import cz.novavesodpad.R
import cz.novavesodpad.model.TrashDay
import cz.novavesodpad.notification.NotificationReceiver
import cz.novavesodpad.util.Logger
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * Input data for building notifications
 */
data class NotificationBuilderInput(
    val days: List<TrashDay>,
    val notificationEnabledThreeDaysBefore: Boolean,
    val selectedNotificationHourThreeDaysBefore: Int,
    val notificationEnabledTwoDaysBefore: Boolean,
    val selectedNotificationHourTwoDaysBefore: Int,
    val notificationEnabledOneDayBefore: Boolean,
    val selectedNotificationHourOneDayBefore: Int,
    val notificationEnabledOnDay: Boolean,
    val selectedNotificationHourOnDay: Int
)

/**
 * Interface for notification builder
 */
interface NotificationsBuilder {
    suspend fun build(input: NotificationBuilderInput)
}

/**
 * Implementation of notification builder that schedules local notifications for trash days
 */
class NotificationsBuilderImpl(
    private val context: Context,
    private val logger: Logger
) : NotificationsBuilder {
    
    override suspend fun build(input: NotificationBuilderInput) {
        // Create notification channel if needed (for Android 8.0+)
        createNotificationChannel()
        
        // Cancel all existing notifications
        cancelAllNotifications()
        
        // Schedule new notifications
        for (day in input.days) {
            val requests = scheduleNotificationsForDay(day, input)
            for (request in requests) {
                scheduleNotification(request)
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Odvoz odpadu"
            val descriptionText = "Upozornění na odvoz odpadu"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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
    
    private fun scheduleNotificationsForDay(
        day: TrashDay, 
        input: NotificationBuilderInput
    ): List<NotificationRequest> {
        val requests = mutableListOf<NotificationRequest>()
        
        // Three days before
        if (input.notificationEnabledThreeDaysBefore) {
            createNotificationRequest(
                day = day,
                offsetDays = -3,
                hour = input.selectedNotificationHourThreeDaysBefore,
                title = "Odvoz odpadu se blíží",
                body = "Za tři dny se budou vyvážet tyto popelnice:"
            )?.let { requests.add(it) }
        }
        
        // Two days before
        if (input.notificationEnabledTwoDaysBefore) {
            createNotificationRequest(
                day = day,
                offsetDays = -2,
                hour = input.selectedNotificationHourTwoDaysBefore,
                title = "Odvoz odpadu se blíží",
                body = "Za dva dny se budou vyvážet tyto popelnice:"
            )?.let { requests.add(it) }
        }
        
        // One day before
        if (input.notificationEnabledOneDayBefore) {
            createNotificationRequest(
                day = day,
                offsetDays = -1,
                hour = input.selectedNotificationHourOneDayBefore,
                title = "Odvoz odpadu je již skoro tady",
                body = "Zítra se budou vyvážet tyto popelnice:"
            )?.let { requests.add(it) }
        }
        
        // On the day
        if (input.notificationEnabledOnDay) {
            createNotificationRequest(
                day = day,
                offsetDays = 0,
                hour = input.selectedNotificationHourOnDay,
                title = "Dnes se vyváží odpad",
                body = "Dnes se budou vyvážet tyto popelnice:"
            )?.let { requests.add(it) }
        }
        
        return requests
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
            .withMinute(40)
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
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                logger.debug("Cannot schedule exact alarms, using inexact timing")
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
        
        logger.debug("Scheduled notification with ID: ${request.notificationId} for ${Date(triggerTimeMillis)}")
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