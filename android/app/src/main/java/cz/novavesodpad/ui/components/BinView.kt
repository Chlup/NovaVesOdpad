package cz.novavesodpad.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.novavesodpad.model.TrashDay

/**
 * Component that displays a bin type with its icon and title
 */
@Composable
fun BinView(bin: TrashDay.Bin) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = bin.title,
            tint = bin.color
        )
        
        Text(
            text = bin.title,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}