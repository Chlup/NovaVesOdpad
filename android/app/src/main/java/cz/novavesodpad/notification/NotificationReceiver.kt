package cz.novavesodpad.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import cz.novavesodpad.MainActivity
import cz.novavesodpad.R
import cz.novavesodpad.service.NotificationsBuilderImpl
import cz.novavesodpad.util.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Broadcast receiver that handles showing notifications when they are triggered
 */
class NotificationReceiver : BroadcastReceiver(), KoinComponent {
    
    private val logger: Logger by inject()
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(NOTIFICATION_TITLE) ?: "Odvoz odpadu"
        val content = intent.getStringExtra(NOTIFICATION_CONTENT) ?: ""
        
        logger.debug("Received notification trigger: $notificationId, $title")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent for when user taps the notification
        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, NotificationsBuilderImpl.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_trash)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show the notification
        notificationManager.notify(notificationId, notification)
    }
    
    companion object {
        const val NOTIFICATION_ID = "notification_id"
        const val NOTIFICATION_TITLE = "notification_title"
        const val NOTIFICATION_CONTENT = "notification_content"
    }
}