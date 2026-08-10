package com.example.uos_lms.core.di

import com.example.uos_lms.feature.admin.data.repository.AdminRepositoryImpl
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import com.example.uos_lms.feature.announcement.data.repository.AnnouncementRepositoryImpl
import com.example.uos_lms.feature.announcement.domain.repository.AnnouncementRepository
import com.example.uos_lms.feature.assignment.data.repository.AssignmentRepositoryImpl
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import com.example.uos_lms.feature.attendance.data.repository.AttendanceRepositoryImpl
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.auth.data.repository.AuthRepositoryImpl
import com.example.uos_lms.feature.auth.domain.repository.AuthRepository
import com.example.uos_lms.feature.calendar.data.repository.CalendarRepositoryImpl
import com.example.uos_lms.feature.calendar.domain.repository.CalendarRepository
import com.example.uos_lms.feature.examresult.data.repository.ExamResultRepositoryImpl
import com.example.uos_lms.feature.examresult.domain.repository.ExamResultRepository
import com.example.uos_lms.feature.hod.data.repository.HodRepositoryImpl
import com.example.uos_lms.feature.hod.domain.repository.HodRepository
import com.example.uos_lms.feature.material.data.repository.MaterialRepositoryImpl
import com.example.uos_lms.feature.material.domain.repository.MaterialRepository
import com.example.uos_lms.feature.quiz.data.repository.QuizRepositoryImpl
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
import com.example.uos_lms.feature.university.data.repository.UniversityRepositoryImpl
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository

    @Binds
    @Singleton
    abstract fun bindUniversityRepository(impl: UniversityRepositoryImpl): UniversityRepository

    @Binds
    @Singleton
    abstract fun bindHodRepository(impl: HodRepositoryImpl): HodRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindAssignmentRepository(impl: AssignmentRepositoryImpl): AssignmentRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository

    @Binds
    @Singleton
    abstract fun bindAnnouncementRepository(impl: AnnouncementRepositoryImpl): AnnouncementRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindMaterialRepository(impl: MaterialRepositoryImpl): MaterialRepository

    @Binds
    @Singleton
    abstract fun bindExamResultRepository(impl: ExamResultRepositoryImpl): ExamResultRepository
}
