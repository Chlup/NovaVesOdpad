package com.mugeaters.popelnice.nvpp.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Model representing a trash collection day with associated bin types
 */
@Serializable
data class TrashDay(
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,
    val bins: List<Bin>
) {
    val id: String
        get() = date.toString()
    
    val daysDifferenceToToday: Int
        get() = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), date.toLocalDate()).toInt()

    /**
     * Types of bins that can be collected
     */
    @Serializable
    enum class Bin {
        mix, plastic, paper, bio, heavyLoad;
        
        val title: String
            get() = when (this) {
                mix -> "Směs"
                plastic -> "Plast, kov, kartony"
                paper -> "Papír"
                bio -> "Bio"
                heavyLoad -> "Velkoobjemový kontejner"
            }
        
        val color: Color
            get() = when (this) {
                mix -> Color.Black
                plastic -> Color.Yellow
                paper -> Color.Blue
                bio -> Color(0xFF964B00) // Brown
                heavyLoad -> Color(0xFF6BB361) // Green (matches iOS)
            }
        
        val backgroundColor: Color
            get() = when (this) {
                mix -> Color(0xFF000000)      // Black
                plastic -> Color(0xFFFFD60A)  // Yellow
                paper -> Color(0xFF2577E7)    // Blue  
                bio -> Color(0xFFC77234)      // Brown
                heavyLoad -> Color(0xFF6BB361) // Green (matches iOS)
            }
        
        val iconColor: Color
            get() = when (this) {
                plastic -> Color.Black
                paper, bio, mix, heavyLoad -> Color.White
            }
    }
}

/**
 * Custom serializer for LocalDateTime
 */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
    
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(formatter.format(value))
    }
    
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}