package cz.novavesodpad.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    /**
     * Types of bins that can be collected
     */
    @Serializable
    enum class Bin {
        mix, plastic, paper, bio;
        
        val title: String
            get() = when (this) {
                mix -> "Směsný komunální odpad"
                plastic -> "Plasty"
                paper -> "Papír"
                bio -> "Bio odpad"
            }
        
        val color: Color
            get() = when (this) {
                mix -> Color.Black
                plastic -> Color.Yellow
                paper -> Color.Blue
                bio -> Color(0xFF964B00) // Brown
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