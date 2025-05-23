package cz.novavesodpad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import cz.novavesodpad.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                painter = painterResource(id = getBinIcon(bin)),
                contentDescription = bin.title,
                tint = bin.iconColor,
                modifier = Modifier.size(size / 1.75f)
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
 * Returns the appropriate drawable resource for each bin type
 * Uses custom icons that match iOS design
 */
private fun getBinIcon(bin: TrashDay.Bin): Int {
    return when (bin) {
        TrashDay.Bin.mix -> R.drawable.ic_bin
        TrashDay.Bin.plastic -> R.drawable.ic_recycle
        TrashDay.Bin.paper -> R.drawable.ic_paper
        TrashDay.Bin.bio -> R.drawable.ic_bio
    }
}