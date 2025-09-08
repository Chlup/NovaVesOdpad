package com.mugeaters.popelnice.nvpp.util

import com.mugeaters.popelnice.nvpp.model.TrashDay
import java.time.LocalDateTime
import java.time.temporal.WeekFields

/**
 * Utility class for generating trash collection days programmatically
 * Algorithm: every Wednesday, alternating bin types based on week number
 */
class TrashDayGenerator {
    
    /**
     * Generates trash collection days programmatically
     * Algorithm matches iOS implementation - every Wednesday, alternating bin types based on week number
     */
    fun generateTrashDays(): List<TrashDay> {
        val now = LocalDateTime.now()
        val nextWednesday = getNextWednesday(now)
        
        val days = mutableListOf<TrashDay>()
        
        // Generate 53 weeks of trash days (1 year + 1 week buffer)
        for (i in 0..52) {
            val dayDate = nextWednesday.plusWeeks(i.toLong())
            val weekOfYear = dayDate.get(WeekFields.ISO.weekOfWeekBasedYear())
            
            val bins = if (weekOfYear % 2 == 0) {
                // Even weeks: paper, bio, mix
                listOf(TrashDay.Bin.paper, TrashDay.Bin.bio, TrashDay.Bin.mix)
            } else {
                // Odd weeks: plastic, mix
                listOf(TrashDay.Bin.plastic, TrashDay.Bin.mix)
            }
            
            val day = TrashDay(date = dayDate, bins = bins)
            days.add(day)
        }
        
        // Add hardcoded heavy load days
        addHeavyLoadDay(days, 2025, 9, 6)
        addHeavyLoadDay(days, 2025, 10, 4)
        addHeavyLoadDay(days, 2025, 11, 8)
        
        return days.sortedBy { it.date }
    }
    
    /**
     * Adds a heavy load day to the list
     */
    private fun addHeavyLoadDay(days: MutableList<TrashDay>, year: Int, month: Int, day: Int) {
        val date = LocalDateTime.of(year, month, day, 23, 59)
        val now = LocalDateTime.now()
        
        // Ignore dates that are in the past
        if (date.isBefore(now)) {
            return
        }
        
        val heavyLoadDay = TrashDay(date = date, bins = listOf(TrashDay.Bin.heavyLoad))
        days.add(heavyLoadDay)
    }
    
    /**
     * Finds the next Wednesday from the given date
     * If today is Wednesday, returns today
     */
    private fun getNextWednesday(from: LocalDateTime): LocalDateTime {
        val dayOfWeek = from.dayOfWeek.value // Monday=1, Tuesday=2, ..., Sunday=7
        val wednesdayValue = 3 // Wednesday
        
        return if (dayOfWeek == wednesdayValue) {
            // If today is Wednesday, return today
            from.toLocalDate().atStartOfDay()
        } else {
            // Calculate days until next Wednesday
            val daysUntilWednesday = (wednesdayValue - dayOfWeek + 7) % 7
            from.toLocalDate().plusDays(daysUntilWednesday.toLong()).atStartOfDay()
        }
    }
}