package com.mugeaters.popelnice.nvpp.model

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
            return (5..23).map { NotificationHour(it) }
        }
    }
}

/**
 * Model representing notification day offset options
 */
data class NotificationDayOffset(
    val daysOffset: Int,
    val title: String
) {
    val id: String = daysOffset.toString()
    
    companion object {
        fun createDefaultOptions(): List<NotificationDayOffset> {
            return listOf(
                NotificationDayOffset(3, "Tři dny předem"),
                NotificationDayOffset(2, "Dva dny předem"),
                NotificationDayOffset(1, "Den předem"),
                NotificationDayOffset(0, "V den vývozu")
            )
        }
    }
}