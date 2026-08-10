package com.example.uos_lms.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.uos_lms.feature.admin.presentation.assignments.AdminAssignmentMonitorScreen
import com.example.uos_lms.feature.admin.presentation.attendance.AdminAttendanceReportsScreen
import com.example.uos_lms.feature.admin.presentation.components.AdminBottomNavBar
import com.example.uos_lms.feature.admin.presentation.components.AdminTab
import com.example.uos_lms.feature.admin.presentation.dashboard.AdminDashboardScreen
import com.example.uos_lms.feature.admin.presentation.examresult.AdminExamResultMonitorScreen
import com.example.uos_lms.feature.admin.presentation.quizzes.AdminQuizMonitorScreen
import com.example.uos_lms.feature.admin.presentation.reports.ReportsScreen
import com.example.uos_lms.feature.admin.presentation.userdetail.AdminUserDetailScreen
import com.example.uos_lms.feature.admin.presentation.usermanagement.student.AdminStudentSemesterListScreen
import com.example.uos_lms.feature.admin.university.presentation.department.DepartmentDetailScreen
import com.example.uos_lms.feature.admin.university.presentation.department.DepartmentListScreen
import com.example.uos_lms.feature.admin.university.presentation.home.AdminHomeScreen
import com.example.uos_lms.feature.admin.university.presentation.semester.SemesterSubjectsScreen
import com.example.uos_lms.feature.admin.university.presentation.session.SessionSemesterListScreen
import com.example.uos_lms.feature.announcement.presentation.AnnouncementsScreen
import com.example.uos_lms.feature.auth.presentation.login.LoginScreen
import com.example.uos_lms.feature.auth.presentation.register.RegisterScreen
import com.example.uos_lms.feature.auth.presentation.splash.SplashScreen
import com.example.uos_lms.feature.auth.presentation.status.PendingApprovalScreen
import com.example.uos_lms.feature.auth.presentation.status.RejectedScreen
import com.example.uos_lms.feature.auth.presentation.status.SuspendedScreen
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.feature.calendar.presentation.AcademicCalendarScreen
import com.example.uos_lms.feature.hod.presentation.assignments.HodAssignmentMonitorScreen
import com.example.uos_lms.feature.hod.presentation.attendance.HodAttendanceReportsScreen
import com.example.uos_lms.feature.hod.presentation.components.HodBottomNavBar
import com.example.uos_lms.feature.hod.presentation.components.HodTab
import com.example.uos_lms.feature.hod.presentation.dashboard.HodDashboardScreen
import com.example.uos_lms.feature.hod.presentation.examresult.HodExamResultApprovalScreen
import com.example.uos_lms.feature.hod.presentation.quizzes.HodQuizMonitorScreen
import com.example.uos_lms.feature.hod.presentation.reports.HodReportsScreen
import com.example.uos_lms.feature.hod.presentation.semester.HodSemesterSubjectsScreen
import com.example.uos_lms.feature.hod.presentation.students.HodStudentsScreen
import com.example.uos_lms.feature.hod.presentation.teachers.HodTeachersScreen
import com.example.uos_lms.feature.profile.presentation.ProfileScreen
import com.example.uos_lms.feature.student.presentation.assignment.StudentSubjectAssignmentsScreen
import com.example.uos_lms.feature.student.presentation.assignment.SubmitAssignmentScreen
import com.example.uos_lms.feature.student.presentation.attendance.StudentAttendanceScreen
import com.example.uos_lms.feature.student.presentation.attendance.StudentSubjectAttendanceScreen
import com.example.uos_lms.feature.student.presentation.components.StudentBottomNavBar
import com.example.uos_lms.feature.student.presentation.components.StudentTab
import com.example.uos_lms.feature.student.presentation.dashboard.StudentDashboardScreen
import com.example.uos_lms.feature.student.presentation.examresult.StudentExamResultScreen
import com.example.uos_lms.feature.student.presentation.material.StudentSubjectMaterialsScreen
import com.example.uos_lms.feature.student.presentation.quiz.StudentSubjectQuizzesScreen
import com.example.uos_lms.feature.student.presentation.quiz.TakeQuizScreen
import com.example.uos_lms.feature.student.presentation.submissions.StudentSubmissionsScreen
import com.example.uos_lms.feature.teacher.presentation.assignment.AssignmentSubmissionsScreen
import com.example.uos_lms.feature.teacher.presentation.assignment.CreateAssignmentScreen
import com.example.uos_lms.feature.teacher.presentation.assignment.TeacherSubjectAssignmentsScreen
import com.example.uos_lms.feature.teacher.presentation.attendance.MarkAttendanceScreen
import com.example.uos_lms.feature.teacher.presentation.attendance.TeacherAttendanceReportsScreen
import com.example.uos_lms.feature.teacher.presentation.attendance.TeacherSubjectAttendanceScreen
import com.example.uos_lms.feature.teacher.presentation.components.TeacherBottomNavBar
import com.example.uos_lms.feature.teacher.presentation.components.TeacherTab
import com.example.uos_lms.feature.teacher.presentation.dashboard.TeacherDashboardScreen
import com.example.uos_lms.feature.teacher.presentation.examresult.TeacherExamResultScreen
import com.example.uos_lms.feature.teacher.presentation.material.TeacherSubjectMaterialsScreen
import com.example.uos_lms.feature.teacher.presentation.quiz.CreateQuizScreen
import com.example.uos_lms.feature.teacher.presentation.quiz.QuizAttemptsScreen
import com.example.uos_lms.feature.teacher.presentation.quiz.TeacherSubjectQuizzesScreen
import com.example.uos_lms.feature.teacher.presentation.reports.TeacherReportsScreen
import com.example.uos_lms.feature.teacher.presentation.students.TeacherStudentsScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable<Routes.Splash> {
            SplashScreen(
                onResolved = { destination ->
                    navController.navigate(destination) {
                        popUpTo<Routes.Splash> { inclusive = true }
                    }
                },
            )
        }
        composable<Routes.Login> {
            LoginScreen(
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo<Routes.Login> { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Routes.Register) },
            )
        }
        composable<Routes.Register> {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.Login) {
                        popUpTo<Routes.Register> { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() },
            )
        }
        composable<Routes.PendingApproval> {
            PendingApprovalScreen(onBackToLogin = { navController.navigateToLoginClearingStack() })
        }
        composable<Routes.Rejected> {
            RejectedScreen(onBackToLogin = { navController.navigateToLoginClearingStack() })
        }
        composable<Routes.Suspended> {
            SuspendedScreen(onBackToLogin = { navController.navigateToLoginClearingStack() })
        }
        composable<Routes.AdminHome> {
            AdminHomeScreen(
                onLogout = { navController.navigateToLoginClearingStack() },
                onUsersClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminDashboard) },
                onDepartmentsClick = { navController.navigateTab(Routes.AdminHome, Routes.DepartmentList) },
                onReportsClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminReports) },
                onProfileClick = { navController.navigateTab(Routes.AdminHome, Routes.Profile(UserRole.ADMIN)) },
                onAttendanceReportsClick = { navController.navigate(Routes.AdminAttendanceReports) },
                onAssignmentMonitorClick = { navController.navigate(Routes.AdminAssignmentMonitor) },
                onQuizMonitorClick = { navController.navigate(Routes.AdminQuizMonitor) },
                onExamResultMonitorClick = { navController.navigate(Routes.AdminExamResultMonitor) },
                onAcademicCalendarClick = { navController.navigate(Routes.AcademicCalendar) },
                onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
            )
        }
        composable<Routes.AdminDashboard> {
            AdminDashboardScreen(
                onUserClick = { uid -> navController.navigate(Routes.AdminUserDetail(uid)) },
                onOpenSession = { departmentId, sessionId ->
                    navController.navigate(Routes.AdminStudentSemesterList(departmentId, sessionId))
                },
                onHomeClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminHome) },
                onDepartmentsClick = { navController.navigateTab(Routes.AdminHome, Routes.DepartmentList) },
                onReportsClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminReports) },
                onProfileClick = { navController.navigateTab(Routes.AdminHome, Routes.Profile(UserRole.ADMIN)) },
            )
        }
        composable<Routes.AdminUserDetail> {
            AdminUserDetailScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.AdminStudentSemesterList> {
            AdminStudentSemesterListScreen(
                onBack = { navController.popBackStack() },
                onOpenUser = { uid -> navController.navigate(Routes.AdminUserDetail(uid)) },
            )
        }
        composable<Routes.AdminReports> {
            ReportsScreen(
                onHomeClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminHome) },
                onUsersClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminDashboard) },
                onDepartmentsClick = { navController.navigateTab(Routes.AdminHome, Routes.DepartmentList) },
                onProfileClick = { navController.navigateTab(Routes.AdminHome, Routes.Profile(UserRole.ADMIN)) },
            )
        }
        composable<Routes.DepartmentList> {
            DepartmentListScreen(
                onDepartmentClick = { departmentId ->
                    navController.navigate(Routes.DepartmentDetail(departmentId))
                },
                onHomeClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminHome) },
                onUsersClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminDashboard) },
                onReportsClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminReports) },
                onProfileClick = { navController.navigateTab(Routes.AdminHome, Routes.Profile(UserRole.ADMIN)) },
            )
        }
        composable<Routes.AdminAttendanceReports> {
            AdminAttendanceReportsScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.AdminAssignmentMonitor> {
            AdminAssignmentMonitorScreen(
                onBack = { navController.popBackStack() },
                onOpenAssignment = { assignmentId, title, maxMarks ->
                    navController.navigate(Routes.AssignmentSubmissions(assignmentId, title, maxMarks))
                },
            )
        }
        composable<Routes.AdminQuizMonitor> {
            AdminQuizMonitorScreen(
                onBack = { navController.popBackStack() },
                onOpenQuiz = { quizId, title, totalMarks ->
                    navController.navigate(Routes.QuizAttempts(quizId, title, totalMarks))
                },
            )
        }
        composable<Routes.AdminExamResultMonitor> {
            AdminExamResultMonitorScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.AcademicCalendar> {
            AcademicCalendarScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.Announcements> {
            AnnouncementsScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.DepartmentDetail> {
            DepartmentDetailScreen(
                onBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    val route: Routes.DepartmentDetail = it.toRoute()
                    navController.navigate(Routes.DepartmentSessionSemesters(route.departmentId, sessionId))
                },
            )
        }
        composable<Routes.DepartmentSessionSemesters> {
            SessionSemesterListScreen(
                onBack = { navController.popBackStack() },
                onSemesterClick = { semesterId ->
                    val route: Routes.DepartmentSessionSemesters = it.toRoute()
                    navController.navigate(Routes.SemesterSubjects(route.departmentId, semesterId, route.sessionId))
                },
            )
        }
        composable<Routes.SemesterSubjects> {
            SemesterSubjectsScreen(
                onBack = { navController.popBackStack() },
                onOpenUser = { uid -> navController.navigate(Routes.AdminUserDetail(uid)) },
            )
        }
        composable<Routes.HodDashboard> {
            HodDashboardScreen(
                onLogout = { navController.navigateToLoginClearingStack() },
                onSemesterClick = { departmentId, semesterId ->
                    navController.navigate(Routes.HodSemesterSubjects(departmentId, semesterId))
                },
                onTeachersClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodTeachers) },
                onStudentsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodStudents) },
                onAttendanceClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodAttendanceReports) },
                onReportsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodReports) },
                onAssignmentMonitorClick = { navController.navigate(Routes.HodAssignmentMonitor) },
                onQuizMonitorClick = { navController.navigate(Routes.HodQuizMonitor) },
                onExamResultApprovalsClick = { navController.navigate(Routes.HodExamResultApprovals) },
                onProfileClick = {
                    navController.navigateTab(Routes.HodDashboard, Routes.Profile(UserRole.HOD))
                },
            )
        }
        composable<Routes.HodSemesterSubjects> {
            HodSemesterSubjectsScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.HodTeachers> {
            HodTeachersScreen(
                onHomeClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodDashboard) },
                onStudentsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodStudents) },
                onAttendanceClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodAttendanceReports) },
                onReportsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodReports) },
                onProfileClick = { navController.navigateTab(Routes.HodDashboard, Routes.Profile(UserRole.HOD)) },
            )
        }
        composable<Routes.HodStudents> {
            HodStudentsScreen(
                onHomeClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodDashboard) },
                onTeachersClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodTeachers) },
                onAttendanceClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodAttendanceReports) },
                onReportsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodReports) },
                onProfileClick = { navController.navigateTab(Routes.HodDashboard, Routes.Profile(UserRole.HOD)) },
            )
        }
        composable<Routes.HodAttendanceReports> {
            HodAttendanceReportsScreen(
                onHomeClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodDashboard) },
                onTeachersClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodTeachers) },
                onStudentsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodStudents) },
                onReportsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodReports) },
                onProfileClick = { navController.navigateTab(Routes.HodDashboard, Routes.Profile(UserRole.HOD)) },
            )
        }
        composable<Routes.HodReports> {
            HodReportsScreen(
                onHomeClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodDashboard) },
                onTeachersClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodTeachers) },
                onStudentsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodStudents) },
                onAttendanceClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodAttendanceReports) },
                onProfileClick = { navController.navigateTab(Routes.HodDashboard, Routes.Profile(UserRole.HOD)) },
            )
        }
        composable<Routes.HodAssignmentMonitor> {
            HodAssignmentMonitorScreen(
                onBack = { navController.popBackStack() },
                onOpenAssignment = { assignmentId, title, maxMarks ->
                    navController.navigate(Routes.AssignmentSubmissions(assignmentId, title, maxMarks))
                },
            )
        }
        composable<Routes.HodQuizMonitor> {
            HodQuizMonitorScreen(
                onBack = { navController.popBackStack() },
                onOpenQuiz = { quizId, title, totalMarks ->
                    navController.navigate(Routes.QuizAttempts(quizId, title, totalMarks))
                },
            )
        }
        composable<Routes.HodExamResultApprovals> {
            HodExamResultApprovalScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.TeacherDashboard> {
            TeacherDashboardScreen(
                onLogout = { navController.navigateToLoginClearingStack() },
                onTakeAttendance = { subjectId, departmentId, semesterId ->
                    navController.navigate(Routes.TeacherSubjectAttendance(subjectId, departmentId, semesterId))
                },
                onAssignments = { subjectId, departmentId, semesterId ->
                    navController.navigate(Routes.TeacherSubjectAssignments(subjectId, departmentId, semesterId))
                },
                onQuizzes = { subjectId, departmentId, semesterId ->
                    navController.navigate(Routes.TeacherSubjectQuizzes(subjectId, departmentId, semesterId))
                },
                onMaterials = { subjectId, departmentId, semesterId ->
                    navController.navigate(Routes.TeacherSubjectMaterials(subjectId, departmentId, semesterId))
                },
                onExamResults = { subjectId, subjectCode, subjectTitle, creditHours, departmentId, semesterId ->
                    navController.navigate(
                        Routes.TeacherSubjectExamResults(subjectId, subjectCode, subjectTitle, creditHours, departmentId, semesterId),
                    )
                },
                onStudentsClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherStudents) },
                onAttendanceReportsClick = {
                    navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherAttendanceReports)
                },
                onReportsClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherReports) },
                onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                onProfileClick = {
                    navController.navigateTab(Routes.TeacherDashboard, Routes.Profile(UserRole.TEACHER))
                },
            )
        }
        composable<Routes.TeacherStudents> {
            TeacherStudentsScreen(
                onHomeClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherDashboard) },
                onAttendanceClick = {
                    navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherAttendanceReports)
                },
                onReportsClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherReports) },
                onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                onProfileClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.Profile(UserRole.TEACHER)) },
            )
        }
        composable<Routes.TeacherAttendanceReports> {
            TeacherAttendanceReportsScreen(
                onHomeClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherDashboard) },
                onStudentsClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherStudents) },
                onReportsClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherReports) },
                onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                onProfileClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.Profile(UserRole.TEACHER)) },
            )
        }
        composable<Routes.TeacherReports> {
            TeacherReportsScreen(
                onHomeClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherDashboard) },
                onStudentsClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherStudents) },
                onAttendanceClick = {
                    navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherAttendanceReports)
                },
                onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                onProfileClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.Profile(UserRole.TEACHER)) },
            )
        }
        composable<Routes.TeacherSubjectMaterials> {
            TeacherSubjectMaterialsScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.TeacherSubjectAttendance> {
            TeacherSubjectAttendanceScreen(
                onBack = { navController.popBackStack() },
                onOpenDate = { dateKey ->
                    val route: Routes.TeacherSubjectAttendance = it.toRoute()
                    navController.navigate(
                        Routes.MarkAttendance(route.subjectId, route.departmentId, route.semesterId, dateKey),
                    )
                },
            )
        }
        composable<Routes.MarkAttendance> {
            MarkAttendanceScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.TeacherSubjectAssignments> {
            TeacherSubjectAssignmentsScreen(
                onBack = { navController.popBackStack() },
                onCreateAssignment = {
                    val route: Routes.TeacherSubjectAssignments = it.toRoute()
                    navController.navigate(
                        Routes.CreateAssignment(route.subjectId, route.departmentId, route.semesterId),
                    )
                },
                onOpenAssignment = { assignmentId, title, maxMarks ->
                    navController.navigate(Routes.AssignmentSubmissions(assignmentId, title, maxMarks))
                },
            )
        }
        composable<Routes.CreateAssignment> {
            CreateAssignmentScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.AssignmentSubmissions> {
            AssignmentSubmissionsScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.TeacherSubjectQuizzes> {
            TeacherSubjectQuizzesScreen(
                onBack = { navController.popBackStack() },
                onCreateQuiz = {
                    val route: Routes.TeacherSubjectQuizzes = it.toRoute()
                    navController.navigate(Routes.CreateQuiz(route.subjectId, route.departmentId, route.semesterId))
                },
                onOpenQuiz = { quizId, title, totalMarks ->
                    navController.navigate(Routes.QuizAttempts(quizId, title, totalMarks))
                },
            )
        }
        composable<Routes.CreateQuiz> {
            CreateQuizScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.QuizAttempts> {
            QuizAttemptsScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.TeacherSubjectExamResults> {
            TeacherExamResultScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.StudentDashboard> {
            StudentDashboardScreen(
                onLogout = { navController.navigateToLoginClearingStack() },
                onViewAttendance = { subjectId ->
                    navController.navigate(Routes.StudentSubjectAttendance(subjectId))
                },
                onAssignments = { subjectId ->
                    navController.navigate(Routes.StudentSubjectAssignments(subjectId))
                },
                onQuizzes = { subjectId ->
                    navController.navigate(Routes.StudentSubjectQuizzes(subjectId))
                },
                onMaterials = { subjectId ->
                    navController.navigate(Routes.StudentSubjectMaterials(subjectId))
                },
                onResultsClick = { navController.navigate(Routes.StudentExamResults) },
                onAttendanceClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentAttendance) },
                onSubmissionsClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentSubmissions) },
                onCalendarClick = { navController.navigate(Routes.AcademicCalendar) },
                onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                onProfileClick = {
                    navController.navigateTab(Routes.StudentDashboard, Routes.Profile(UserRole.STUDENT))
                },
            )
        }
        composable<Routes.StudentAttendance> {
            StudentAttendanceScreen(
                onHomeClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentDashboard) },
                onSubmissionsClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentSubmissions) },
                onCalendarClick = { navController.navigate(Routes.AcademicCalendar) },
                onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                onProfileClick = { navController.navigateTab(Routes.StudentDashboard, Routes.Profile(UserRole.STUDENT)) },
            )
        }
        composable<Routes.StudentSubmissions> {
            StudentSubmissionsScreen(
                onHomeClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentDashboard) },
                onAttendanceClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentAttendance) },
                onCalendarClick = { navController.navigate(Routes.AcademicCalendar) },
                onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                onProfileClick = { navController.navigateTab(Routes.StudentDashboard, Routes.Profile(UserRole.STUDENT)) },
                onOpenAssignment = { assignmentId, subjectId, maxMarks ->
                    navController.navigate(Routes.SubmitAssignment(assignmentId, subjectId, maxMarks))
                },
            )
        }
        composable<Routes.StudentSubjectMaterials> {
            StudentSubjectMaterialsScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.StudentSubjectAttendance> {
            StudentSubjectAttendanceScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.StudentSubjectAssignments> {
            StudentSubjectAssignmentsScreen(
                onBack = { navController.popBackStack() },
                onOpenAssignment = { assignmentId, subjectId, maxMarks ->
                    navController.navigate(Routes.SubmitAssignment(assignmentId, subjectId, maxMarks))
                },
            )
        }
        composable<Routes.SubmitAssignment> {
            SubmitAssignmentScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.StudentSubjectQuizzes> {
            StudentSubjectQuizzesScreen(
                onBack = { navController.popBackStack() },
                onOpenQuiz = { quizId, subjectId ->
                    navController.navigate(Routes.TakeQuiz(quizId, subjectId))
                },
            )
        }
        composable<Routes.TakeQuiz> {
            TakeQuizScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.StudentExamResults> {
            StudentExamResultScreen(onBack = { navController.popBackStack() })
        }
        composable<Routes.Profile> {
            val route: Routes.Profile = it.toRoute()
            ProfileScreen(
                onLogout = { navController.navigateToLoginClearingStack() },
                bottomBar = {
                    when (route.role) {
                        UserRole.ADMIN -> AdminBottomNavBar(
                            selected = AdminTab.PROFILE,
                            onHomeClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminHome) },
                            onUsersClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminDashboard) },
                            onDepartmentsClick = { navController.navigateTab(Routes.AdminHome, Routes.DepartmentList) },
                            onReportsClick = { navController.navigateTab(Routes.AdminHome, Routes.AdminReports) },
                            onProfileClick = {},
                        )
                        UserRole.HOD -> HodBottomNavBar(
                            selected = HodTab.PROFILE,
                            onHomeClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodDashboard) },
                            onTeachersClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodTeachers) },
                            onStudentsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodStudents) },
                            onAttendanceClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodAttendanceReports) },
                            onReportsClick = { navController.navigateTab(Routes.HodDashboard, Routes.HodReports) },
                            onProfileClick = {},
                        )
                        UserRole.TEACHER -> TeacherBottomNavBar(
                            selected = TeacherTab.PROFILE,
                            onHomeClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherDashboard) },
                            onStudentsClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherStudents) },
                            onAttendanceClick = {
                                navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherAttendanceReports)
                            },
                            onReportsClick = { navController.navigateTab(Routes.TeacherDashboard, Routes.TeacherReports) },
                            onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                            onProfileClick = {},
                        )
                        UserRole.STUDENT -> StudentBottomNavBar(
                            selected = StudentTab.PROFILE,
                            onHomeClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentDashboard) },
                            onAttendanceClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentAttendance) },
                            onSubmissionsClick = { navController.navigateTab(Routes.StudentDashboard, Routes.StudentSubmissions) },
                            onCalendarClick = { navController.navigate(Routes.AcademicCalendar) },
                            onAnnouncementsClick = { navController.navigate(Routes.Announcements) },
                            onProfileClick = {},
                        )
                    }
                },
            )
        }
    }
}

private fun NavHostController.navigateToLoginClearingStack() {
    navigate(Routes.Login) {
        popUpTo(0) { inclusive = true }
    }
}

// Each role's top-level screens (Admin: Home/Users/Departments; HOD/Teacher/
// Student: Home/Profile) behave like bottom-nav tabs: switching between them
// preserves each tab's scroll/filter state instead of stacking up back
// entries, matching the standard Compose Navigation bottom-bar pattern.
// `homeRoute` is the tab-set's anchor to pop up to; `target` is where to land.
private fun NavHostController.navigateTab(homeRoute: Routes, target: Routes) {
    navigate(target) {
        popUpTo(homeRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
