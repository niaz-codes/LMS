package com.example.uos_lms.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.CalendarEvent
import com.example.uos_lms.core.domain.model.CalendarEventType
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.calendar.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AcademicCalendarUiState(
    val events: List<CalendarEvent> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class AcademicCalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademicCalendarUiState())
    val uiState: StateFlow<AcademicCalendarUiState> = _uiState.asStateFlow()

    private var currentUid: String? = null

    init {
        viewModelScope.launch {
            val session = sessionManager.cachedSession.filterNotNull().first()
            currentUid = session.uid
            _uiState.value = _uiState.value.copy(isAdmin = session.role == UserRole.ADMIN)
        }

        calendarRepository.observeEvents()
            .onEach { events -> _uiState.value = _uiState.value.copy(events = events, isLoading = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun addEvent(title: String, description: String, type: CalendarEventType, dateMillis: Long) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            when (val result = calendarRepository.createEvent(title, description, type, dateMillis, uid)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            when (val result = calendarRepository.deleteEvent(id)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
