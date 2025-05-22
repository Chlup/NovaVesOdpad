package cz.novavesodpad.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.novavesodpad.model.NotificationHour
import cz.novavesodpad.ui.theme.LocalAppColors

/**
 * Reusable component for notification setup items - redesigned to match iOS version
 */
@Composable
fun NotificationSetupItem(
    title: String,
    isEnabled: Boolean,
    selectedHour: Int,
    availableHours: List<NotificationHour>,
    onEnabledChanged: (Boolean) -> Unit,
    onHourSelected: (Int) -> Unit
) {
    val appColors = LocalAppColors.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.sectionBackground)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            // Enable/disable toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.regularText,
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChanged
                )
            }
            
            // Hour selector - only shown if notifications are enabled
            if (isEnabled) {
                NotificationTimeSelector(
                    selectedHour = selectedHour,
                    availableHours = availableHours,
                    onHourSelected = onHourSelected
                )
            }
        }
    }
}

/**
 * Time selector dropdown for notification hour
 */
@Composable
fun NotificationTimeSelector(
    selectedHour: Int,
    availableHours: List<NotificationHour>,
    onHourSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedHourLabel = if (selectedHour < 10) "0$selectedHour:00" else "$selectedHour:00"
    val appColors = LocalAppColors.current
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Čas notifikace",
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.regularText,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                text = selectedHourLabel,
                color = appColors.regularText
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableHours.forEach { hour ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = hour.title,
                            color = appColors.regularText
                        ) 
                    },
                    onClick = {
                        onHourSelected(hour.hour)
                        expanded = false
                    }
                )
            }
        }
    }
}