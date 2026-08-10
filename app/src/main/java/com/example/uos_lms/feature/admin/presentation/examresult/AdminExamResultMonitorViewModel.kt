package com.example.uos_lms.feature.admin.presentation.examresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.domain.model.ExamResult
import com.example.uos_lms.core.domain.model.ResultStatus
import com.example.uos_lms.feature.examresult.domain.repository.ExamResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminExamResultUiState(
    val results: List<ExamResult> = emptyList(),
    val statusFilter: ResultStatus? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val visibleResults: List<ExamResult>
        get() = results
            .filter { statusFilter == null || it.status == statusFilter }
            .sortedByDescending { it.updatedAt }

    val counts: Map<ResultStatus, Int> get() = ResultStatus.entries.associateWith { status -> results.count { it.status == status } }
}

@HiltViewModel
class AdminExamResultMonitorViewModel @Inject constructor(
    examResultRepository: ExamResultRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminExamResultUiState())
    val uiState: StateFlow<AdminExamResultUiState> = _uiState.asStateFlow()

    init {
        examResultRepository.observeAllResults()
            .onEach { results -> _uiState.value = _uiState.value.copy(results = results, isLoading = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun setStatusFilter(status: ResultStatus?) {
        _uiState.value = _uiState.value.copy(statusFilter = status)
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
