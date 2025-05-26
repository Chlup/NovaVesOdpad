package com.mugeaters.popelnice.nvpp.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.mugeaters.popelnice.nvpp.MainActivity
import com.mugeaters.popelnice.nvpp.R
import com.mugeaters.popelnice.nvpp.service.NotificationsBuilderImpl
import com.mugeaters.popelnice.nvpp.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Broadcast receiver that handles showing notifications when they are triggered
 */
class NotificationReceiver : BroadcastReceiver(), KoinComponent {
    
    private val logger: Logger by inject()
    
    override fun onReceive(context: Context, intent: Intent) {
        logger.debug("🔔 NotificationReceiver.onReceive() called!")
        
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(NOTIFICATION_TITLE) ?: "Odvoz odpadu"
        val content = intent.getStringExtra(NOTIFICATION_CONTENT) ?: ""
        
        logger.debug("📱 Received notification trigger: ID=$notificationId, title=$title")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent for when user taps the notification
        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create large icon from drawable logo (works better than adaptive icon for notifications)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, NotificationsBuilderImpl.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_trash)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show the notification
        notificationManager.notify(notificationId, notification)
        logger.debug("✅ Notification shown with ID: $notificationId")
    }
    
    companion object {
        const val NOTIFICATION_ID = "notification_id"
        const val NOTIFICATION_TITLE = "notification_title"
        const val NOTIFICATION_CONTENT = "notification_content"
    }
}