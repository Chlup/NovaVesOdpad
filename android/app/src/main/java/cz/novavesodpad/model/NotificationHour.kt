package cz.novavesodpad.model

/**
 * Model representing a notification time configuration
 */
data class NotificationHour(
    val hour: Int
) {
    val id: String = hour.toString()
    
    val title: String
        get() = if (hour < 10) "0$hour:00" else "$hour:00"
    
    companion object {
        fun createDefaultHours(): List<NotificationHour> {
            return (7..20).map { NotificationHour(it) }
        }
    }
}