package com.example.uos_lms.core.navigation

import com.example.uos_lms.core.domain.model.UserRole
import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable data object Splash : Routes
    @Serializable data object Login : Routes
    @Serializable data object Register : Routes
    @Serializable data object PendingApproval : Routes
    @Serializable data object Rejected : Routes
    @Serializable data object Suspended : Routes
    @Serializable data object AdminHome : Routes
    @Serializable data object AdminDashboard : Routes
    @Serializable data class AdminUserDetail(val uid: String) : Routes
    @Serializable data object AdminReports : Routes
    @Serializable data object AdminAttendanceReports : Routes
    @Serializable data object AdminAssignmentMonitor : Routes
    @Serializable data object AdminQuizMonitor : Routes
    @Serializable data object AdminExamResultMonitor : Routes
    @Serializable data object AcademicCalendar : Routes
    @Serializable data object Announcements : Routes
    @Serializable data object DepartmentList : Routes
    @Serializable data class DepartmentDetail(val departmentId: String) : Routes
    @Serializable data class DepartmentSessionSemesters(val departmentId: String, val sessionId: String) : Routes
    @Serializable data class SemesterSubjects(val departmentId: String, val semesterId: String, val sessionId: String) : Routes
    @Serializable data class AdminStudentSemesterList(val departmentId: String, val sessionId: String) : Routes
    @Serializable data object HodDashboard : Routes
    @Serializable data class HodSemesterSubjects(val departmentId: String, val semesterId: String) : Routes
    @Serializable data object HodTeachers : Routes
    @Serializable data object HodStudents : Routes
    @Serializable data object HodAttendanceReports : Routes
    @Serializable data object HodAssignmentMonitor : Routes
    @Serializable data object HodQuizMonitor : Routes
    @Serializable data object HodExamResultApprovals : Routes
    @Serializable data object HodReports : Routes
    @Serializable data object TeacherDashboard : Routes
    @Serializable data object TeacherStudents : Routes
    @Serializable data object TeacherAttendanceReports : Routes
    @Serializable data object TeacherReports : Routes
    @Serializable data class TeacherSubjectMaterials(
        val subjectId: String,
        val departmentId: String,
        val semesterId: String,
    ) : Routes
    @Serializable data class TeacherSubjectAttendance(
        val subjectId: String,
        val departmentId: String,
        val semesterId: String,
    ) : Routes
    @Serializable data class MarkAttendance(
        val subjectId: String,
        val departmentId: String,
        val semesterId: String,
        val dateKey: String,
    ) : Routes
    @Serializable data class TeacherSubjectAssignments(
        val subjectId: String,
        val departmentId: String,
        val semesterId: String,
    ) : Routes
    @Serializable data class CreateAssignment(
        val subjectId: String,
        val departmentId: String,
        val semesterId: String,
    ) : Routes
    @Serializable data class AssignmentSubmissions(
        val assignmentId: String,
        val title: String,
        val maxMarks: Int,
    ) : Routes
    @Serializable data class TeacherSubjectQuizzes(
        val subjectId: String,
        val departmentId: String,
        val semesterId: String,
    ) : Routes
    @Serializable data class CreateQuiz(
        val subjectId: String,
        val departmentId: String,
        val semesterId: String,
    ) : Routes
    @Serializable data class QuizAttempts(
        val quizId: String,
        val title: String,
        val totalMarks: Int,
    ) : Routes
    @Serializable data class TeacherSubjectExamResults(
        val subjectId: String,
        val subjectCode: String,
        val subjectTitle: String,
        val creditHours: Int,
        val departmentId: String,
        val semesterId: String,
    ) : Routes
    @Serializable data object StudentDashboard : Routes
    @Serializable data object StudentAttendance : Routes
    @Serializable data object StudentSubmissions : Routes
    @Serializable data class StudentSubjectAttendance(val subjectId: String) : Routes
    @Serializable data class StudentSubjectAssignments(val subjectId: String) : Routes
    @Serializable data class StudentSubjectMaterials(val subjectId: String) : Routes
    @Serializable data class SubmitAssignment(
        val assignmentId: String,
        val subjectId: String,
        val maxMarks: Int,
    ) : Routes
    @Serializable data class StudentSubjectQuizzes(val subjectId: String) : Routes
    @Serializable data class TakeQuiz(val quizId: String, val subjectId: String) : Routes
    @Serializable data object StudentExamResults : Routes
    @Serializable data class Profile(val role: UserRole) : Routes
}

fun UserRole.toDashboardRoute(): Routes = when (this) {
    UserRole.ADMIN -> Routes.AdminHome
    UserRole.HOD -> Routes.HodDashboard
    UserRole.TEACHER -> Routes.TeacherDashboard
    UserRole.STUDENT -> Routes.StudentDashboard
}
