package com.mugeaters.popelnice.nvpp.model

import java.time.LocalDateTime

/**
 * Model representing a month with its trash collection days
 */
data class TrashMonth(
    val date: LocalDateTime,
    val days: List<TrashDay>
) {
    val id: String
        get() = date.toString()
}