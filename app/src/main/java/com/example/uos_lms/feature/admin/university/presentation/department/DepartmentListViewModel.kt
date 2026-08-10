package com.example.uos_lms.feature.admin.university.presentation.department

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepartmentListViewModel @Inject constructor(
    private val repository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DepartmentListUiState())
    val uiState: StateFlow<DepartmentListUiState> = _uiState.asStateFlow()

    init {
        repository.observeDepartments()
            .onEach { departments -> _uiState.value = _uiState.value.copy(departments = departments, isLoading = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, listErrorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    suspend fun createDepartment(name: String, code: String, description: String): AppResult<Unit> =
        repository.createDepartment(name, code, description)

    suspend fun updateDepartment(id: String, name: String, code: String, description: String): AppResult<Unit> =
        repository.updateDepartment(id, name, code, description)

    fun deleteDepartment(id: String) {
        viewModelScope.launch {
            when (val result = repository.deleteDepartment(id)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(listErrorMessage = result.message)
            }
        }
    }

    fun clearListError() {
        _uiState.value = _uiState.value.copy(listErrorMessage = null)
    }
}
