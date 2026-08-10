package com.example.uos_lms.feature.admin.university.presentation.department

import com.example.uos_lms.core.domain.model.Department

data class DepartmentListUiState(
    val departments: List<Department> = emptyList(),
    val isLoading: Boolean = true,
    val listErrorMessage: String? = null,
)
