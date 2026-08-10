package com.example.uos_lms.feature.teacher.presentation.material

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.MaterialType
import com.example.uos_lms.core.domain.model.StudyMaterial
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.material.domain.repository.MaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherSubjectMaterialsUiState(
    val materials: List<StudyMaterial> = emptyList(),
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val uploadProgress: Int? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class TeacherSubjectMaterialsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val materialRepository: MaterialRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    val route: Routes.TeacherSubjectMaterials = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(TeacherSubjectMaterialsUiState())
    val uiState: StateFlow<TeacherSubjectMaterialsUiState> = _uiState.asStateFlow()

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

    fun upload(title: String, materialType: MaterialType, fileUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, uploadProgress = null, errorMessage = null)
            when (val user = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val teacher = user.data
                    if (teacher == null) {
                        _uiState.value = _uiState.value.copy(isUploading = false, errorMessage = "Session expired.")
                        return@launch
                    }
                    val result = materialRepository.uploadMaterial(
                        subjectId = route.subjectId,
                        departmentId = route.departmentId,
                        semesterId = route.semesterId,
                        title = title,
                        materialType = materialType,
                        fileUri = fileUri,
                        uploadedBy = teacher.uid,
                        uploadedByName = teacher.fullName,
                        onProgress = { progress ->
                            _uiState.value = _uiState.value.copy(uploadProgress = progress)
                        },
                    )
                    when (result) {
                        is AppResult.Success ->
                            _uiState.value = _uiState.value.copy(isUploading = false, uploadProgress = null)
                        is AppResult.Error ->
                            _uiState.value = _uiState.value.copy(isUploading = false, uploadProgress = null, errorMessage = result.message)
                    }
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(isUploading = false, errorMessage = user.message)
            }
        }
    }

    fun delete(materialId: String) {
        viewModelScope.launch {
            materialRepository.deleteMaterial(materialId)
        }
    }
}
