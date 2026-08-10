package com.example.uos_lms.feature.hod.presentation.examresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.ExamResult
import com.example.uos_lms.core.domain.model.GradeScale
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.examresult.domain.repository.ExamResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ResultReviewTab { PENDING, ALL }

data class HodExamResultUiState(
    val tab: ResultReviewTab = ResultReviewTab.PENDING,
    val pending: List<ExamResult> = emptyList(),
    val all: List<ExamResult> = emptyList(),
    val isLoading: Boolean = true,
    val processingResultId: String? = null,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
) {
    val visibleResults: List<ExamResult>
        get() = if (tab == ResultReviewTab.PENDING) pending else all.sortedByDescending { it.updatedAt }
}

@HiltViewModel
class HodExamResultApprovalViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val examResultRepository: ExamResultRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HodExamResultUiState())
    val uiState: StateFlow<HodExamResultUiState> = _uiState.asStateFlow()

    private var reviewerUid: String? = null

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val user = result.data
                    reviewerUid = user?.uid
                    val departmentId = user?.department
                    if (departmentId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    combine(
                        examResultRepository.observePendingApprovalsForDepartment(departmentId),
                        examResultRepository.observeAllResultsForDepartment(departmentId),
                    ) { pending, all -> pending.sortedBy { it.studentName } to all }
                        .onEach { (pending, all) ->
                            _uiState.value = _uiState.value.copy(pending = pending, all = all, isLoading = false)
                        }
                        .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun selectTab(tab: ResultReviewTab) {
        _uiState.value = _uiState.value.copy(tab = tab)
    }

    fun approve(result: ExamResult) {
        val reviewer = reviewerUid ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please log in again.")
            return
        }
        val grade = GradeScale.letterFor(result.percentage)
        val gpaPoint = GradeScale.gpaPointFor(result.percentage)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingResultId = result.id, errorMessage = null)
            when (val res = examResultRepository.approve(result.id, grade, gpaPoint, reviewer)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    processingResultId = null,
                    actionMessage = "Approved ${result.studentName}'s result ($grade, ${"%.2f".format(gpaPoint)} GPA).",
                )
                is AppResult.Error -> _uiState.value = _uiState.value.copy(processingResultId = null, errorMessage = res.message)
            }
        }
    }

    fun reject(result: ExamResult, reason: String) {
        val reviewer = reviewerUid ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please log in again.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingResultId = result.id, errorMessage = null)
            when (val res = examResultRepository.reject(result.id, reason, reviewer)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    processingResultId = null,
                    actionMessage = "Rejected ${result.studentName}'s result.",
                )
                is AppResult.Error -> _uiState.value = _uiState.value.copy(processingResultId = null, errorMessage = res.message)
            }
        }
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, actionMessage = null)
    }
}
