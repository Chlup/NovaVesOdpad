package cz.novavesodpad.ui.settings

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.novavesodpad.model.NotificationHour
import cz.novavesodpad.model.TrashDay
import cz.novavesodpad.service.NotificationBuilderInput
import cz.novavesodpad.service.NotificationsBuilder
import cz.novavesodpad.util.Logger
import cz.novavesodpad.util.TasksManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewState for the Settings screen
 */
data class SettingsState(
    val notificationsAuthorized: Boolean = false,
    val notificationHours: List<NotificationHour> = NotificationHour.createDefaultHours(),
    
    val notificationEnabledThreeDaysBefore: Boolean = false,
    val selectedNotificationHourThreeDaysBefore: Int = 8,
    
    val notificationEnabledTwoDaysBefore: Boolean = false,
    val selectedNotificationHourTwoDaysBefore: Int = 8,
    
    val notificationEnabledOneDayBefore: Boolean = false,
    val selectedNotificationHourOneDayBefore: Int = 8,
    
    val notificationEnabledOnDay: Boolean = false,
    val selectedNotificationHourOnDay: Int = 8,
    
    val days: List<TrashDay> = emptyList()
) {
    val notificationsEnabledForAnyDay: Boolean
        get() = notificationEnabledThreeDaysBefore || 
                notificationEnabledTwoDaysBefore || 
                notificationEnabledOneDayBefore || 
                notificationEnabledOnDay
}

/**
 * ViewModel for the Settings screen
 */
class SettingsViewModel(
    private val context: Context,
    private val notificationsBuilder: NotificationsBuilder,
    private val preferencesManager: PreferencesManager,
    private val tasksManager: TasksManager,
    private val logger: Logger
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    private val scheduleNotificationsTaskId = "schedule_notifications_task"
    
    init {
        loadSettings()
        checkNotificationPermission()
    }
    
    /**
     * Loads saved notification settings from preferences
     */
    fun loadSettings() {
        val settings = preferencesManager.getNotificationSettings()
        _state.update { 
            it.copy(
                notificationEnabledThreeDaysBefore = settings.notificationEnabledThreeDaysBefore,
                selectedNotificationHourThreeDaysBefore = settings.selectedNotificationHourThreeDaysBefore,
                
                notificationEnabledTwoDaysBefore = settings.notificationEnabledTwoDaysBefore,
                selectedNotificationHourTwoDaysBefore = settings.selectedNotificationHourTwoDaysBefore,
                
                notificationEnabledOneDayBefore = settings.notificationEnabledOneDayBefore,
                selectedNotificationHourOneDayBefore = settings.selectedNotificationHourOneDayBefore,
                
                notificationEnabledOnDay = settings.notificationEnabledOnDay,
                selectedNotificationHourOnDay = settings.selectedNotificationHourOnDay
            )
        }
    }
    
    /**
     * Sets the list of trash days (usually passed from the Home screen)
     */
    fun setDays(days: List<TrashDay>) {
        _state.update { it.copy(days = days) }
    }
    
    /**
     * Checks if notification permission is granted
     */
    fun checkNotificationPermission() {
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Before Android 13, notification permission was granted by default
        }
        
        _state.update { it.copy(notificationsAuthorized = permissionGranted) }
    }
    
    /**
     * Called when notification settings are changed
     */
    fun notificationSettingsChanged() {
        val currentState = state.value
        
        // Save settings to preferences
        preferencesManager.saveNotificationSettings(
            NotificationSettings(
                notificationEnabledThreeDaysBefore = currentState.notificationEnabledThreeDaysBefore,
                selectedNotificationHourThreeDaysBefore = currentState.selectedNotificationHourThreeDaysBefore,
                
                notificationEnabledTwoDaysBefore = currentState.notificationEnabledTwoDaysBefore,
                selectedNotificationHourTwoDaysBefore = currentState.selectedNotificationHourTwoDaysBefore,
                
                notificationEnabledOneDayBefore = currentState.notificationEnabledOneDayBefore,
                selectedNotificationHourOneDayBefore = currentState.selectedNotificationHourOneDayBefore,
                
                notificationEnabledOnDay = currentState.notificationEnabledOnDay,
                selectedNotificationHourOnDay = currentState.selectedNotificationHourOnDay
            )
        )
        
        // Schedule notifications
        scheduleNotifications()
    }
    
    /**
     * Sets the notification enabled state for three days before
     */
    fun setNotificationEnabledThreeDaysBefore(enabled: Boolean) {
        _state.update { it.copy(notificationEnabledThreeDaysBefore = enabled) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification hour for three days before
     */
    fun setSelectedNotificationHourThreeDaysBefore(hour: Int) {
        _state.update { it.copy(selectedNotificationHourThreeDaysBefore = hour) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification enabled state for two days before
     */
    fun setNotificationEnabledTwoDaysBefore(enabled: Boolean) {
        _state.update { it.copy(notificationEnabledTwoDaysBefore = enabled) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification hour for two days before
     */
    fun setSelectedNotificationHourTwoDaysBefore(hour: Int) {
        _state.update { it.copy(selectedNotificationHourTwoDaysBefore = hour) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification enabled state for one day before
     */
    fun setNotificationEnabledOneDayBefore(enabled: Boolean) {
        _state.update { it.copy(notificationEnabledOneDayBefore = enabled) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification hour for one day before
     */
    fun setSelectedNotificationHourOneDayBefore(hour: Int) {
        _state.update { it.copy(selectedNotificationHourOneDayBefore = hour) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification enabled state for the day of collection
     */
    fun setNotificationEnabledOnDay(enabled: Boolean) {
        _state.update { it.copy(notificationEnabledOnDay = enabled) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification hour for the day of collection
     */
    fun setSelectedNotificationHourOnDay(hour: Int) {
        _state.update { it.copy(selectedNotificationHourOnDay = hour) }
        notificationSettingsChanged()
    }
    
    /**
     * Schedules notifications based on current settings
     */
    private fun scheduleNotifications() {
        val currentState = state.value
        
        // Only schedule if permission is granted and there are days
        if (!currentState.notificationsAuthorized || currentState.days.isEmpty()) {
            return
        }
        
        tasksManager.addTask(scheduleNotificationsTaskId) {
            val input = NotificationBuilderInput(
                days = currentState.days,
                notificationEnabledThreeDaysBefore = currentState.notificationEnabledThreeDaysBefore,
                selectedNotificationHourThreeDaysBefore = currentState.selectedNotificationHourThreeDaysBefore,
                notificationEnabledTwoDaysBefore = currentState.notificationEnabledTwoDaysBefore,
                selectedNotificationHourTwoDaysBefore = currentState.selectedNotificationHourTwoDaysBefore,
                notificationEnabledOneDayBefore = currentState.notificationEnabledOneDayBefore,
                selectedNotificationHourOneDayBefore = currentState.selectedNotificationHourOneDayBefore,
                notificationEnabledOnDay = currentState.notificationEnabledOnDay,
                selectedNotificationHourOnDay = currentState.selectedNotificationHourOnDay
            )
            
            notificationsBuilder.build(input)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        tasksManager.cancelTask(scheduleNotificationsTaskId)
    }
}

/**
 * Data class for storing notification settings
 */
data class NotificationSettings(
    val notificationEnabledThreeDaysBefore: Boolean = false,
    val selectedNotificationHourThreeDaysBefore: Int = 8,
    
    val notificationEnabledTwoDaysBefore: Boolean = false,
    val selectedNotificationHourTwoDaysBefore: Int = 8,
    
    val notificationEnabledOneDayBefore: Boolean = false,
    val selectedNotificationHourOneDayBefore: Int = 8,
    
    val notificationEnabledOnDay: Boolean = false,
    val selectedNotificationHourOnDay: Int = 8
)

/**
 * Interface for managing preferences
 */
interface PreferencesManager {
    fun getNotificationSettings(): NotificationSettings
    fun saveNotificationSettings(settings: NotificationSettings)
}

/**
 * Implementation of preferences manager that uses SharedPreferences
 */
class SharedPreferencesManager(context: Context) : PreferencesManager {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    
    override fun getNotificationSettings(): NotificationSettings {
        return NotificationSettings(
            notificationEnabledThreeDaysBefore = preferences.getBoolean(KEY_NOTIF_ENABLED_3_DAYS, false),
            selectedNotificationHourThreeDaysBefore = preferences.getInt(KEY_NOTIF_HOUR_3_DAYS, 8),
            
            notificationEnabledTwoDaysBefore = preferences.getBoolean(KEY_NOTIF_ENABLED_2_DAYS, false),
            selectedNotificationHourTwoDaysBefore = preferences.getInt(KEY_NOTIF_HOUR_2_DAYS, 8),
            
            notificationEnabledOneDayBefore = preferences.getBoolean(KEY_NOTIF_ENABLED_1_DAY, false),
            selectedNotificationHourOneDayBefore = preferences.getInt(KEY_NOTIF_HOUR_1_DAY, 8),
            
            notificationEnabledOnDay = preferences.getBoolean(KEY_NOTIF_ENABLED_0_DAYS, false),
            selectedNotificationHourOnDay = preferences.getInt(KEY_NOTIF_HOUR_0_DAYS, 8)
        )
    }
    
    override fun saveNotificationSettings(settings: NotificationSettings) {
        preferences.edit().apply {
            putBoolean(KEY_NOTIF_ENABLED_3_DAYS, settings.notificationEnabledThreeDaysBefore)
            putInt(KEY_NOTIF_HOUR_3_DAYS, settings.selectedNotificationHourThreeDaysBefore)
            
            putBoolean(KEY_NOTIF_ENABLED_2_DAYS, settings.notificationEnabledTwoDaysBefore)
            putInt(KEY_NOTIF_HOUR_2_DAYS, settings.selectedNotificationHourTwoDaysBefore)
            
            putBoolean(KEY_NOTIF_ENABLED_1_DAY, settings.notificationEnabledOneDayBefore)
            putInt(KEY_NOTIF_HOUR_1_DAY, settings.selectedNotificationHourOneDayBefore)
            
            putBoolean(KEY_NOTIF_ENABLED_0_DAYS, settings.notificationEnabledOnDay)
            putInt(KEY_NOTIF_HOUR_0_DAYS, settings.selectedNotificationHourOnDay)
        }.apply()
    }
    
    companion object {
        private const val PREFERENCES_NAME = "nova_ves_odpad_prefs"
        
        private const val KEY_NOTIF_ENABLED_3_DAYS = "notification_enabled_3_days"
        private const val KEY_NOTIF_HOUR_3_DAYS = "notification_hour_3_days"
        
        private const val KEY_NOTIF_ENABLED_2_DAYS = "notification_enabled_2_days"
        private const val KEY_NOTIF_HOUR_2_DAYS = "notification_hour_2_days"
        
        private const val KEY_NOTIF_ENABLED_1_DAY = "notification_enabled_1_day"
        private const val KEY_NOTIF_HOUR_1_DAY = "notification_hour_1_day"
        
        private const val KEY_NOTIF_ENABLED_0_DAYS = "notification_enabled_0_days"
        private const val KEY_NOTIF_HOUR_0_DAYS = "notification_hour_0_days"
    }
}

/**
 * Composable function that returns a permission request launcher for notification permission
 */
@Composable
fun rememberNotificationPermissionLauncher(
    onPermissionResult: (Boolean) -> Unit
): ManagedActivityResultLauncher<String, Boolean> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult
    )
}