package cz.novavesodpad.model

import android.net.Uri

/**
 * Model representing a section in the trash information screen
 */
data class TrashInfoSection(
    val title: String,
    val bin: TrashDay.Bin,
    val text: String? = null,
    val pdfFileUris: List<Uri> = emptyList(),
    val id: String = title
)