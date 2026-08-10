package com.example.uos_lms.feature.student.presentation.material

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.domain.model.StudyMaterial
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.material.domain.repository.MaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class StudentSubjectMaterialsUiState(
    val materials: List<StudyMaterial> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class StudentSubjectMaterialsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val materialRepository: MaterialRepository,
) : ViewModel() {

    val route: Routes.StudentSubjectMaterials = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(StudentSubjectMaterialsUiState())
    val uiState: StateFlow<StudentSubjectMaterialsUiState> = _uiState.asStateFlow()

    init {
        materialRepository.observeMaterialsForSubject(route.subjectId)
            .onEach { materials ->
                _uiState.value = _uiState.value.copy(
                    materials = materials.sortedByDescending { it.uploadedAt },
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }
}
