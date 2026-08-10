package com.example.uos_lms.feature.hod.domain.repository

import com.example.uos_lms.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface HodRepository {
    fun observeTeachersInDepartment(departmentId: String): Flow<List<User>>
    fun observeUsersInDepartment(departmentId: String): Flow<List<User>>
}
