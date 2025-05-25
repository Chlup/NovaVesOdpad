package com.mugeaters.popelnice.nvpp.ui.trashinfo

import androidx.lifecycle.ViewModel
import com.mugeaters.popelnice.nvpp.model.TrashInfoSection
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