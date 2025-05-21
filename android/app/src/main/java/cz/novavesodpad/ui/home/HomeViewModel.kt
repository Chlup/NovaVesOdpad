package cz.novavesodpad.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.novavesodpad.model.TrashDay
import cz.novavesodpad.util.Logger
import cz.novavesodpad.util.TasksManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ViewModel state for the Home screen
 */
data class HomeState(
    val days: List<TrashDay> = emptyList()
)

/**
 * ViewModel for the Home screen
 */
class HomeViewModel(
    private val assetsProvider: AssetsProvider,
    private val tasksManager: TasksManager,
    private val logger: Logger
) : ViewModel() {
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    private val dayTitleFormatter = DateTimeFormatter.ofPattern("EEEE dd. MM. yyyy", Locale.getDefault())
    private val loadDaysTaskId = "load_days_task"
    
    /**
     * Loads trash days data from the calendar.json file
     */
    fun loadData() {
        tasksManager.addTask(loadDaysTaskId) {
            try {
                val calendarJson = assetsProvider.getAssetAsString("calendar.json")
                val json = Json { ignoreUnknownKeys = true }
                
                val days = json.decodeFromString<List<TrashDay>>(calendarJson)
                    .filter { it.date.isAfter(LocalDateTime.now()) }
                
                _state.update { it.copy(days = days) }
            } catch (e: Exception) {
                logger.error("Failed to load calendar data", e)
            }
        }
    }
    
    /**
     * Formats a date as a title string
     */
    fun titleForDay(date: LocalDateTime): String {
        val formatted = dayTitleFormatter.format(date)
        return formatted.replaceFirstChar { it.uppercase() }
    }
    
    override fun onCleared() {
        super.onCleared()
        tasksManager.cancelTask(loadDaysTaskId)
    }
}

/**
 * Interface for accessing assets from the app's resources
 */
interface AssetsProvider {
    fun getAssetAsString(fileName: String): String
    fun getAssetAsStream(fileName: String): InputStream
}

/**
 * Implementation that provides assets from Android's assets folder
 */
class AndroidAssetsProvider(
    private val resourcesProvider: () -> android.content.res.Resources,
    private val packageName: String
) : AssetsProvider {
    
    override fun getAssetAsString(fileName: String): String {
        return getAssetAsStream(fileName).bufferedReader().use { it.readText() }
    }
    
    override fun getAssetAsStream(fileName: String): InputStream {
        return resourcesProvider().assets.open(fileName)
    }
}