package com.mugeaters.popelnice.nvpp.model

/**
 * Model representing a section in the trash information screen
 * Matches iOS TrashInfoSection structure
 */
data class TrashInfoSection(
    val title: String,
    val text: String? = null,
    val pdfFileName: String? = null
) {
    val id: String
        get() = title
}