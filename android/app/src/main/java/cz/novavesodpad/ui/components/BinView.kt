package cz.novavesodpad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cz.novavesodpad.model.TrashDay

/**
 * Component that displays a bin type with circular background and appropriate icon
 * Matches iOS BinIconView design
 */
@Composable
fun BinView(
    bin: TrashDay.Bin,
    size: Dp = 24.dp,
    showTitle: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bin.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getBinIcon(bin),
                contentDescription = bin.title,
                tint = bin.iconColor,
                modifier = Modifier.size(size / 2)
            )
        }
        
        if (showTitle) {
            Text(
                text = bin.title,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Returns the appropriate Material icon for each bin type
 * Uses available Material Icons similar to iOS design intent
 */
private fun getBinIcon(bin: TrashDay.Bin): ImageVector {
    return when (bin) {
        TrashDay.Bin.mix -> Icons.Default.Delete    // trash icon
        TrashDay.Bin.plastic -> Icons.Default.Refresh // recycling/refresh icon
        TrashDay.Bin.paper -> Icons.Default.Info      // document/info icon
        TrashDay.Bin.bio -> Icons.Default.Star        // bio/star icon (temporary)
    }
}