package cz.novavesodpad.ui.dayslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.novavesodpad.model.TrashDay
import cz.novavesodpad.model.TrashMonth
import cz.novavesodpad.util.Logger
import cz.novavesodpad.util.TasksManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ViewModel state for the Days List screen
 */
data class DaysListState(
    val allDays: List<TrashDay> = emptyList(),
    val months: List<TrashMonth> = emptyList()
)

/**
 * ViewModel for the Days List screen - shows calendar of all trash collection days
 */
class DaysListViewModel(
    private val initialDays: List<TrashDay>,
    private val tasksManager: TasksManager,
    private val logger: Logger
) : ViewModel() {
    
    private val _state = MutableStateFlow(DaysListState(allDays = initialDays))
    val state: StateFlow<DaysListState> = _state.asStateFlow()
    
    private val dayTitleFormatter = DateTimeFormatter.ofPattern("dd. MM. yyyy", Locale.getDefault())
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    private val groupDaysTaskId = "group_days_task"
    
    init {
        groupDays()
    }
    
    /**
     * Groups trash days by month for organized display
     */
    private fun groupDays() {
        tasksManager.addTask(groupDaysTaskId) {
            try {
                val allDays = _state.value.allDays
                if (allDays.isEmpty()) return@addTask
                
                val months = mutableListOf<TrashMonth>()
                var daysForMonth = mutableListOf<TrashDay>()
                
                for (day in allDays) {
                    if (daysForMonth.isEmpty()) {
                        daysForMonth.add(day)
                        continue
                    }
                    
                    val previousDay = daysForMonth.lastOrNull()
                    val firstDay = daysForMonth.firstOrNull()
                    
                    if (previousDay == null || firstDay == null) break
                    
                    val previousDayMonth = previousDay.date.monthValue
                    val currentDayMonth = day.date.monthValue
                    
                    if (previousDayMonth != currentDayMonth) {
                        // Month changed, create TrashMonth and start new list
                        val month = TrashMonth(date = firstDay.date, days = daysForMonth.toList())
                        months.add(month)
                        daysForMonth = mutableListOf(day)
                    } else {
                        // Same month, add to current list
                        daysForMonth.add(day)
                    }
                }
                
                // Add the last month if there are remaining days
                if (daysForMonth.isNotEmpty()) {
                    val firstDay = daysForMonth.firstOrNull()
                    if (firstDay != null) {
                        val month = TrashMonth(date = firstDay.date, days = daysForMonth.toList())
                        months.add(month)
                    }
                }
                
                _state.update { it.copy(months = months) }
            } catch (e: Exception) {
                logger.error("Failed to group days by month", e)
            }
        }
    }
    
    /**
     * Formats a date as a day title string
     */
    fun titleForDay(date: LocalDateTime): String {
        val formatted = dayTitleFormatter.format(date)
        return formatted.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Formats a date as a month title string
     */
    fun titleForMonth(date: LocalDateTime): String {
        val formatted = monthTitleFormatter.format(date)
        return formatted.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Called when screen appears
     */
    fun onAppear() {
        groupDays()
    }
    
    override fun onCleared() {
        super.onCleared()
        tasksManager.cancelTask(groupDaysTaskId)
    }
}