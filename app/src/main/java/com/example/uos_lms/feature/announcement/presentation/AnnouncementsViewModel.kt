package com.example.uos_lms.feature.announcement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Announcement
import com.example.uos_lms.core.domain.model.AnnouncementScope
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.feature.announcement.domain.repository.AnnouncementRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
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

data class AnnouncementsUiState(
    val announcements: List<Announcement> = emptyList(),
    val role: UserRole? = null,
    val departments: List<Department> = emptyList(),
    val mySubjects: List<Subject> = emptyList(),
    val myDepartmentId: String? = null,
    val myDepartmentName: String? = null,
    val isLoading: Boolean = true,
    val isPosting: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementsUiState())
    val uiState: StateFlow<AnnouncementsUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val user = result.data
                    currentUser = user
                    _uiState.value = _uiState.value.copy(role = user?.role)
                    if (user != null) {
                        when (user.role) {
                            UserRole.ADMIN -> universityRepository.observeDepartments()
                                .onEach { departments -> _uiState.value = _uiState.value.copy(departments = departments) }
                                .launchIn(viewModelScope)
                            UserRole.HOD -> {
                                val deptId = user.department
                                if (deptId != null) {
                                    universityRepository.observeDepartments()
                                        .onEach { departments ->
                                            val dept = departments.find { it.id == deptId }
                                            _uiState.value = _uiState.value.copy(
                                                myDepartmentId = deptId,
                                                myDepartmentName = dept?.name,
                                            )
                                        }
                                        .launchIn(viewModelScope)
                                }
                            }
                            UserRole.TEACHER -> universityRepository.observeSubjectsForTeacher(user.uid)
                                .onEach { subjects -> _uiState.value = _uiState.value.copy(mySubjects = subjects) }
                                .launchIn(viewModelScope)
                            UserRole.STUDENT -> {}
                        }
                    }
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }

        announcementRepository.observeAnnouncements()
            .onEach { announcements -> _uiState.value = _uiState.value.copy(announcements = announcements, isLoading = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun postAll(title: String, body: String) =
        postScoped(title, body, AnnouncementScope.ALL, null, null, null, null)

    fun postToDepartment(title: String, body: String, department: Department) =
        postScoped(title, body, AnnouncementScope.DEPARTMENT, department.id, department.name, null, null)

    fun postToMyDepartment(title: String, body: String) {
        val deptId = _uiState.value.myDepartmentId ?: return
        postScoped(title, body, AnnouncementScope.DEPARTMENT, deptId, _uiState.value.myDepartmentName, null, null)
    }

    fun postToSubject(title: String, body: String, subject: Subject) =
        postScoped(title, body, AnnouncementScope.SUBJECT, subject.departmentId, null, subject.id, subject.title)

    private fun postScoped(
        title: String,
        body: String,
        scope: AnnouncementScope,
        departmentId: String?,
        departmentName: String?,
        subjectId: String?,
        subjectName: String?,
    ) {
        val user = currentUser ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPosting = true, errorMessage = null)
            when (
                val result = announcementRepository.postAnnouncement(
                    title = title,
                    body = body,
                    authorUid = user.uid,
                    authorName = user.fullName,
                    scope = scope,
                    departmentId = departmentId,
                    departmentName = departmentName,
                    subjectId = subjectId,
                    subjectName = subjectName,
                )
            ) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isPosting = false)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(isPosting = false, errorMessage = result.message)
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            when (val result = announcementRepository.deleteAnnouncement(id)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
