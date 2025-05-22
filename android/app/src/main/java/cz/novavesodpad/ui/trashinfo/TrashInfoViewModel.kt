package cz.novavesodpad.ui.trashinfo

import androidx.lifecycle.ViewModel
import cz.novavesodpad.model.TrashInfoSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel state for the TrashInfo screen
 */
data class TrashInfoState(
    val sections: List<TrashInfoSection> = emptyList()
)

/**
 * ViewModel for the TrashInfo screen
 */
class TrashInfoViewModel(
    sections: List<TrashInfoSection>
) : ViewModel() {
    
    private val _state = MutableStateFlow(TrashInfoState(sections = sections))
    val state: StateFlow<TrashInfoState> = _state.asStateFlow()
}