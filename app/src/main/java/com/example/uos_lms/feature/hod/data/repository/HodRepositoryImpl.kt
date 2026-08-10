package com.example.uos_lms.feature.hod.data.repository

import com.example.uos_lms.core.data.remote.FirestoreUserDataSource
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.hod.domain.repository.HodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HodRepositoryImpl @Inject constructor(
    private val userDataSource: FirestoreUserDataSource,
) : HodRepository {

    override fun observeTeachersInDepartment(departmentId: String): Flow<List<User>> =
        userDataSource.observeTeachersInDepartment(departmentId)

    override fun observeUsersInDepartment(departmentId: String): Flow<List<User>> =
        userDataSource.observeUsersInDepartment(departmentId)
}
