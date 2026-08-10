# Chapter content for the UOS LMS thesis. Imported and invoked by build_thesis.py.
# Documentation-only: no application source code is modified by this script.

from docx.shared import Pt


def write_all(*, doc, h1, h2, h3, p, bullet, numbered, center, page_break,
              add_figure, add_table, add_table_caption, DIAG):

    # =======================================================================
    # CHAPTER 1: INTRODUCTION
    # =======================================================================
    h1("Chapter 1: Introduction")

    h2("1.1 Background and Motivation")
    p("Academic institutions rely on a large number of interconnected administrative and academic "
      "processes: enrolling students into departments and semesters, assigning subjects to teachers, "
      "recording attendance, distributing and grading coursework, conducting quizzes and examinations, "
      "and communicating announcements. In many universities, and particularly in smaller or "
      "newly-established institutions such as the University of Shangla, these processes are still "
      "carried out manually or through a patchwork of spreadsheets, paper registers, and informal "
      "messaging groups.")
    p("This manual approach introduces several recurring problems. Attendance registers can be lost, "
      "falsified, or take considerable class time to complete. Assignment submissions handed in on "
      "paper or over chat applications are difficult to track, easy to lose, and cannot be graded "
      "consistently. Announcements posted on physical notice boards fail to reach students who miss a "
      "single visit to campus. Compiling attendance percentages, subject loads, or department-wide "
      "performance reports by hand is slow and error-prone. As student enrolment grows, these problems "
      "compound rather than diminish.")
    p("A dedicated Learning Management System (LMS) addresses these problems by moving academic "
      "workflows onto a single digital platform accessible from any Android device. UOS LMS was "
      "conceived specifically for the University of Shangla to provide a mobile-first, role-based "
      "platform that every stakeholder — administrators, heads of department, teachers, and students — "
      "can use from a single application, each seeing only the functionality and data relevant to "
      "their role.")

    h2("1.2 Problem Statement")
    p("The University of Shangla currently lacks a unified digital system for managing core academic "
      "operations. Attendance is recorded manually, assignments and quizzes are administered without "
      "a central record of submissions or grades, user accounts are not verified before granting "
      "system access, and there is no single source of truth for the university's departmental "
      "structure (departments, semesters, sessions, and subjects). This project addresses the "
      "following core problem: how can a single mobile application provide secure, role-appropriate "
      "access to the full academic lifecycle — from account registration and administrative approval, "
      "through attendance and coursework, to quizzes, reporting, and communication — while remaining "
      "usable, secure, and maintainable for a small institutional development team?")

    h2("1.3 Aims and Objectives")
    p("The aim of this project is to design, implement, and evaluate a native Android Learning "
      "Management System for the University of Shangla. The specific objectives are to:")
    bullet("Design a role-based authentication system supporting four distinct roles — Admin, HOD, "
           "Teacher, and Student — where each account is verified against Firebase Authentication and "
           "matched against the role selected at login, with mismatched roles rejected outright.")
    bullet("Implement an administrative approval workflow so that new HOD, Teacher, and Student "
           "accounts remain inactive (pending) until reviewed and approved by an Administrator.")
    bullet("Provide full CRUD management of the university's academic structure: departments, "
           "semesters, sessions, and subjects, including department-scoped teacher-to-subject "
           "assignment performed by the relevant HOD.")
    bullet("Digitise attendance capture (by Teachers) and attendance review (by Students, HODs, and "
           "Admins), including department- and semester-level aggregate reporting.")
    bullet("Enable assignment creation, file-attached submission, and manual grading with feedback, "
           "using a dedicated cloud media pipeline rather than storing files in the database.")
    bullet("Implement a unified quiz and examination engine with automatic multiple-choice grading and "
           "an optional manual score override for examinations.")
    bullet("Provide study-material distribution, scoped announcements, and an academic calendar "
           "shared across roles.")
    bullet("Design and apply a consistent, modern Material Design 3 visual language across all four "
           "roles of the application.")
    bullet("Evaluate the resulting system through functional and manual testing to verify correctness "
           "of the core workflows described above.")

    h2("1.4 Scope of the Project")
    p("The scope of UOS LMS covers the complete academic-management lifecycle for a single "
      "institution: user registration and role-based approval; hierarchical academic-structure "
      "management; attendance; assignments; quizzes and examinations; study materials; announcements; "
      "an academic calendar; and role-specific reporting dashboards. The system is delivered as a "
      "native Android application targeting phones and tablets running Android 7.0 (API level 24) and "
      "above. The following items are explicitly out of scope for the current version and are "
      "discussed further as future work in Chapter 8: push notifications, a fully interactive drag-"
      "and-drop timetable builder, offline-first data access, and a web or iOS client.")

    h2("1.5 Significance of the Study")
    p("This project demonstrates the practical application of a modern, production-grade mobile "
      "architecture — Jetpack Compose, MVVM, Dagger Hilt, and a Firebase/Cloudinary backend — to a "
      "real institutional problem. Beyond its immediate use at the University of Shangla, the project "
      "serves as a reference implementation of role-based access control, hierarchical data "
      "management, and secure media handling patterns that are broadly applicable to other academic "
      "or enterprise mobile systems.")

    h2("1.6 Tools and Technologies Overview")
    p("UOS LMS is implemented in Kotlin using Jetpack Compose for its entire user interface, following "
      "the MVVM architectural pattern. Dagger Hilt provides dependency injection. Firebase "
      "Authentication verifies user credentials, Cloud Firestore stores all structured application "
      "data, and Cloudinary hosts all binary media (profile photographs, assignment attachments, and "
      "study materials) via a signed upload API. A complete justification of this technology stack is "
      "provided in Section 4.3.")

    h2("1.7 Organization of the Report")
    p("The remainder of this report is organized as follows. Chapter 2 reviews existing Learning "
      "Management System platforms and positions UOS LMS relative to them. Chapter 3 documents the "
      "requirement-gathering process, the system's functional and non-functional requirements, and its "
      "use-case analysis. Chapter 4 presents the system design, including the architecture, database "
      "design, and the full set of UML and structured-analysis diagrams. Chapter 5 describes the "
      "implementation of each module in detail. Chapter 6 documents the testing strategy and results. "
      "Chapter 7 presents the results achieved and discusses them against the stated objectives. "
      "Chapter 8 concludes the report and outlines directions for future work.")
    page_break()

    # =======================================================================
    # CHAPTER 2: LITERATURE REVIEW
    # =======================================================================
    h1("Chapter 2: Literature Review")

    h2("2.1 Overview of Learning Management Systems")
    p("A Learning Management System is software that supports the administration, documentation, "
      "tracking, and delivery of educational courses and training programmes. Modern LMS platforms "
      "typically provide course/content management, assessment tools, communication features, and "
      "analytics, and are consumed either through a web browser, a dedicated desktop client, or a "
      "mobile application. Institutional adoption of LMS platforms has accelerated over the past "
      "decade, driven both by the general shift toward digital administration and, more recently, by "
      "the need for remote and hybrid learning capability.")

    h2("2.2 Review of Existing Systems")
    p("Several well-established LMS platforms were reviewed prior to designing UOS LMS, to understand "
      "common feature sets, architectural patterns, and gaps that a purpose-built, mobile-first system "
      "could address for a smaller institution.")
    bullet("Moodle — a widely deployed, open-source, web-based LMS offering extensive course "
           "management, plugin extensibility, and grading tools, but requiring dedicated server "
           "infrastructure and a fairly heavyweight administrative setup that is not always practical "
           "for smaller institutions.")
    bullet("Google Classroom — a lightweight, mobile-friendly platform tightly integrated with Google "
           "Workspace, strong for assignment distribution and grading, but with limited support for "
           "granular, department-level academic-structure management (departments, semesters, sessions) "
           "and no built-in attendance module.")
    bullet("Edmodo — a social-network-inspired LMS aimed primarily at K-12 education, strong on "
           "communication features but comparatively weak on formal academic administration such as "
           "hierarchical user approval and departmental reporting.")
    bullet("Blackboard Learn — a feature-rich, enterprise-grade LMS common in higher education, but "
           "typically licensed and hosted at significant cost, which is often impractical for a smaller "
           "public-sector university.")
    add_table_caption(
        "Table 2.1: Comparative Analysis of Existing LMS Platforms Against UOS LMS", "tbl_2_1")
    add_table(
        ["Feature", "Moodle", "Google Classroom", "Edmodo", "UOS LMS"],
        [
            ["Native mobile-first UI", "Partial (web wrapper)", "Yes", "Partial", "Yes (native Android)"],
            ["Hierarchical academic structure\n(Dept/Semester/Session)", "Manual configuration", "No", "No", "Yes, built-in"],
            ["Role-based approval workflow", "Plugin-dependent", "No (Workspace-managed)", "Limited", "Yes, built-in"],
            ["Attendance module", "Plugin-dependent", "No", "No", "Yes, built-in"],
            ["Auto-graded MCQ quizzes/exams", "Yes", "Limited (Forms add-on)", "Yes", "Yes, built-in"],
            ["Self-hosting / licensing cost", "Free, self-hosted", "Free (Workspace account)", "Free", "Firebase/Cloudinary\npay-as-you-go"],
        ],
        widths=[1.9, 1.35, 1.35, 1.05, 1.55],
    )

    h2("2.3 Mobile-First and Role-Based LMS Approaches")
    p("A recurring theme in recent educational-technology literature is the shift from web-first LMS "
      "platforms retrofitted with a mobile wrapper, toward genuinely mobile-first applications built "
      "natively for the platform they target. Native applications offer smoother animations, better "
      "offline resilience, and tighter integration with platform capabilities such as notifications and "
      "camera access, at the cost of requiring separate codebases per platform. UOS LMS adopts the "
      "native, mobile-first approach for Android specifically, reflecting the device profile most "
      "commonly available to students and staff at the University of Shangla.")
    p("Role-based access control (RBAC) is a further recurring requirement in institutional systems: "
      "different classes of user (administrators, department heads, teaching staff, and students) must "
      "see different data and different capabilities within the same application. UOS LMS implements "
      "RBAC both at the user-interface level (four entirely separate navigation graphs and dashboards) "
      "and at the data-access level (Firestore security rules scoped by the authenticated user's role "
      "and department), which is discussed in detail in Section 4.9.")

    h2("2.4 Backend-as-a-Service in Educational Applications")
    p("Backend-as-a-Service (BaaS) platforms such as Firebase allow small development teams to build "
      "full-featured applications — authentication, a real-time document database, and security rules "
      "— without provisioning or maintaining dedicated servers. This significantly lowers the barrier "
      "to building institution-specific software, which is particularly relevant for a university "
      "project developed and maintained by a small team. Cloud Firestore's document-oriented model "
      "maps naturally onto the entity types required by an LMS (users, subjects, assignments, "
      "attendance records) while its real-time listener API allows the UI to update instantly when "
      "underlying data changes, without manual polling.")

    h2("2.5 Research Gap and Justification for UOS LMS")
    p("The reviewed platforms are either too heavyweight to self-host for a small institution (Moodle, "
      "Blackboard) or lack the specific hierarchical academic-structure and approval-gated onboarding "
      "features required by a university with formal departments, semesters, and sessions (Google "
      "Classroom, Edmodo). None of the reviewed systems combine a native Android-first interface, "
      "built-in departmental hierarchy, an administrative approval gate for new accounts, and a "
      "unified quiz/exam engine in a single lightweight package suited to a smaller institution's "
      "budget and infrastructure. UOS LMS was designed specifically to close this gap for the "
      "University of Shangla.")
    page_break()

    # =======================================================================
    # CHAPTER 3: SYSTEM ANALYSIS AND REQUIREMENTS
    # =======================================================================
    h1("Chapter 3: System Analysis and Requirements")

    h2("3.1 Requirement Gathering Methodology")
    p("Requirements were derived through an iterative process combining an analysis of the manual "
      "academic workflows currently followed at the University of Shangla, informal consultation with "
      "prospective users in each of the four target roles, and a review of feature sets offered by the "
      "existing LMS platforms discussed in Chapter 2. Requirements were refined incrementally across "
      "the project's development phases, with each phase (university-structure management, "
      "user-management, attendance, assignments, quizzes, reporting, and the final UI redesign) adding "
      "a functionally complete module before the next was started.")

    h2("3.2 Stakeholders / System Actors")
    p("The system defines four actors, each mapped to exactly one value of the UserRole enumeration "
      "(ADMIN, HOD, TEACHER, STUDENT) stored on the user's profile document:")
    bullet("Administrator — has full authority over the university's academic structure, all user "
           "accounts (approval, rejection, suspension), and university-wide reporting and "
           "communication.")
    bullet("Head of Department (HOD) — manages teacher-to-subject assignment, and views students, "
           "teachers, attendance, and reports scoped strictly to their own department.")
    bullet("Teacher — manages attendance, assignments, quizzes/exams, and study materials for the "
           "specific subjects assigned to them, and views the students enrolled in those subjects.")
    bullet("Student — views their own subjects, attendance, submits assignments, attempts quizzes and "
           "exams, and reviews their own grades and feedback.")

    h2("3.3 Functional Requirements")
    add_table_caption("Table 3.1: Functional Requirements Summary by Role", "tbl_3_1")
    add_table(
        ["ID", "Requirement", "Primary Actor(s)"],
        [
            ["FR-01", "Register a new account with role HOD, Teacher, or Student; account remains "
                      "PENDING until reviewed", "HOD / Teacher / Student"],
            ["FR-02", "Log in with role selection; reject login if the account's stored role does "
                      "not match the selected role", "All roles"],
            ["FR-03", "Reset a forgotten password via a secure e-mailed reset link", "All roles"],
            ["FR-04", "Approve, reject, or suspend a pending or existing user account", "Admin"],
            ["FR-05", "Create, edit, and delete Departments, Semesters, Sessions, and Subjects", "Admin"],
            ["FR-06", "Assign or unassign a Teacher to/from a Subject within their department", "HOD"],
            ["FR-07", "Browse users through a hierarchical (Department → Semester → Session) "
                      "expandable directory", "Admin / HOD"],
            ["FR-08", "Mark daily attendance (Present/Absent) for students of an assigned subject", "Teacher"],
            ["FR-09", "View own attendance history and aggregate percentage", "Student"],
            ["FR-10", "Create an assignment with a due date, maximum marks, and optional file "
                      "attachment", "Teacher"],
            ["FR-11", "Submit an assignment with a text answer and/or file attachment", "Student"],
            ["FR-12", "Grade a submitted assignment with marks and written feedback", "Teacher"],
            ["FR-13", "Create a quiz or exam consisting of multiple-choice questions with a time "
                      "limit and due date", "Teacher"],
            ["FR-14", "Attempt a quiz/exam; receive an automatically computed score for MCQ answers", "Student"],
            ["FR-15", "Manually override an examination's auto-computed score and add feedback", "Teacher"],
            ["FR-16", "Upload and browse study materials (PDF/PPT/Video/Notes) per subject", "Teacher / Student"],
            ["FR-17", "Post announcements scoped to the whole university, a department, or a subject", "Admin / HOD / Teacher"],
            ["FR-18", "View and manage academic calendar events (holidays, events, exams)", "Admin / all roles (view)"],
            ["FR-19", "View role-appropriate attendance, assignment, and quiz analytics/reports", "Admin / HOD / Teacher"],
            ["FR-20", "View and edit own profile, including profile photo and theme preference", "All roles"],
        ],
        widths=[0.55, 4.15, 1.3],
    )

    h2("3.4 Non-Functional Requirements")
    add_table_caption("Table 3.2: Non-Functional Requirements", "tbl_3_2")
    add_table(
        ["Category", "Requirement"],
        [
            ["Security", "Every authenticated session must be validated against the account's stored "
                          "role before dashboard access is granted; unapproved accounts must never "
                          "reach an authenticated state."],
            ["Security", "Firestore and Cloudinary storage access must be enforced by server-side "
                          "security rules, not solely by client-side UI gating."],
            ["Usability", "The interface must follow a single, consistent Material Design 3 visual "
                          "language across all four roles, with legible typography and accessible "
                          "contrast."],
            ["Performance", "List and count-based UI elements (e.g. hierarchy badges) must use "
                             "Firestore aggregate queries rather than downloading full document sets."],
            ["Reliability", "Network or validation failures must be surfaced to the user through a "
                             "consistent error-state component rather than silent failure."],
            ["Maintainability", "The codebase must be organised into isolated, per-role feature "
                                 "modules sharing a common core module, to keep unrelated roles from "
                                 "coupling to one another."],
            ["Compatibility", "The application must run on Android 7.0 (API 24) and above, covering "
                               "the large majority of active Android devices."],
            ["Portability", "Domain logic must be UI-framework agnostic (plain Kotlin data classes "
                             "and use cases) so it can be unit-tested independently of Compose."],
        ],
        widths=[1.3, 4.7],
    )

    h2("3.5 Use Case Analysis")
    p("Figures 3.1 and 3.2 present the system's use cases split across its two pairs of actors: "
      "administrative actors (Admin and HOD), who primarily manage structure, accounts, and "
      "department-wide oversight; and academic actors (Teacher and Student), who primarily create, "
      "submit, and consume day-to-day coursework.")
    add_figure("fig_3_1_usecase_admin_hod.png",
               "Figure 3.1: Use Case Diagram – Administrative Actors (Admin & HOD)", "fig_3_1", width=6.3)
    add_figure("fig_3_2_usecase_teacher_student.png",
               "Figure 3.2: Use Case Diagram – Academic Actors (Teacher & Student)", "fig_3_2", width=6.3)

    h3("3.5.1 Sample Use Case Descriptions")
    add_table_caption("Table 3.3: Sample Use Case Descriptions", "tbl_3_3")
    add_table(
        ["Use Case", "Actor", "Description", "Post-condition"],
        [
            ["Login (role-validated)", "All roles",
             "Actor enters e-mail, password, and selects a role from the login spinner. System "
             "authenticates the credentials against Firebase, retrieves the Firestore profile, and "
             "compares the profile's stored role against the selected role.",
             "Session is cached and the actor is routed to their role's dashboard only if credentials, "
             "role, and approval status all match; otherwise the session is signed out and a specific "
             "error is shown."],
            ["Approve/Reject/Suspend User", "Admin",
             "Admin opens a pending or existing account from the hierarchical Users directory and "
             "selects an approval action.", "The account's status field is updated; only an APPROVED "
             "account may subsequently authenticate successfully."],
            ["Mark Attendance", "Teacher",
             "Teacher selects a subject and date, then marks each enrolled student Present or Absent.",
             "One AttendanceRecord document is written per student for that subject and date."],
            ["Submit Assignment", "Student",
             "Student opens an assigned assignment, writes a text answer and/or attaches a file, and "
             "submits.", "An AssignmentSubmission document is created, with any attached file uploaded "
             "to Cloudinary first."],
            ["Attempt Quiz/Exam", "Student",
             "Student answers each multiple-choice question within the configured time limit and "
             "submits the attempt.", "A QuizAttempt document is stored with an automatically computed "
             "score; for exams, the teacher may later add a manualScore."],
        ],
        widths=[1.2, 0.65, 2.55, 1.6],
    )

    h2("3.6 Feasibility Study")
    h3("3.6.1 Technical Feasibility")
    p("All required technologies — Kotlin, Jetpack Compose, Firebase, and Cloudinary — are mature, "
      "well-documented, and freely available for development. The development team had prior exposure "
      "to Android development and Kotlin, making the technical risk of the project low.")
    h3("3.6.2 Operational Feasibility")
    p("The application requires only an internet-connected Android device to operate, which is "
      "consistent with device availability among the target user base. The four-role structure mirrors "
      "the university's existing organisational hierarchy, minimising the change-management burden of "
      "adoption.")
    h3("3.6.3 Economic Feasibility")
    p("Firebase and Cloudinary are both offered on usage-based free tiers that comfortably cover a "
      "single-institution deployment at the University of Shangla's expected scale, making the "
      "recurring operating cost of the system negligible relative to the manual processes it replaces.")
    h3("3.6.4 Schedule Feasibility")
    p("The project was executed in incremental phases, each delivering one functionally complete "
      "module (authentication, university-structure management, user hierarchy, subject assignment, "
      "attendance, assignments, quizzes/exams, reporting, and a final UI polish pass), which kept the "
      "project on a manageable, continuously-shippable schedule.")
    page_break()

    # =======================================================================
    # CHAPTER 4: SYSTEM DESIGN
    # =======================================================================
    h1("Chapter 4: System Design")

    h2("4.1 Design Goals and Principles")
    bullet("Separation of concerns between UI, state management, domain logic, and data access, "
           "following MVVM and a lightweight Clean-Architecture-inspired layering.")
    bullet("Strict, server-enforced role-based access control rather than UI-only gating.")
    bullet("A single shared design system (colours, gradients, shapes, typography, and reusable "
           "components) applied consistently across all four role-specific navigation graphs.")
    bullet("Feature-based modularity: each functional area (attendance, assignment, quiz, etc.) is an "
           "isolated package that can be developed, reasoned about, and tested independently.")

    h2("4.2 System Architecture")
    p("UOS LMS follows a layered architecture, shown in Figure 4.1. The Presentation Layer consists "
      "entirely of Jetpack Compose screens, one set per role, built from a shared library of design-"
      "system components (GradientTopAppBar, GlassCard, AppBottomNavBar, and others). Each screen "
      "observes a ViewModel's StateFlow<UiState> in the Presentation State Layer, which also exposes a "
      "one-shot Kotlin Channel for navigation events and wraps every asynchronous operation's outcome "
      "in an AppResult<T> success/error type. The Domain Layer defines plain-Kotlin use cases, "
      "repository interfaces, and domain models with no dependency on Android or Compose, keeping "
      "business logic independently testable. The Data Layer implements those repository interfaces "
      "against concrete data sources — FirestoreUserDataSource, FirestoreUniversityDataSource, "
      "FirebaseAuthDataSource, and CloudinaryDataSource — which in turn talk to the three external "
      "services: Firebase Authentication, Cloud Firestore, and Cloudinary. Dagger Hilt wires every "
      "layer together at compile time via generated dependency graphs, so a ViewModel never "
      "instantiates its own use cases or repositories directly.")
    add_figure("fig_4_1_system_architecture.png", "Figure 4.1: Layered System Architecture of UOS LMS",
               "fig_4_1", width=6.3)

    h2("4.3 Technology Stack Justification")
    add_table_caption("Table 4.1: Technology Stack", "tbl_4_1")
    add_table(
        ["Layer / Concern", "Technology", "Justification"],
        [
            ["Language", "Kotlin 2.2", "Official first-class language for Android; concise, null-safe, "
                                        "and fully interoperable with the Android SDK."],
            ["UI Toolkit", "Jetpack Compose\n(BOM 2026.02.01)", "Declarative, state-driven UI toolkit "
                                                                 "that eliminates most manual view-"
                                                                 "invalidation bugs and pairs naturally "
                                                                 "with StateFlow-based ViewModels."],
            ["Architecture", "MVVM", "Cleanly separates UI from state and business logic, and is the "
                                      "architecture Google explicitly recommends for Compose apps."],
            ["Dependency Injection", "Dagger Hilt 2.59.2", "Compile-time-verified DI eliminates a whole "
                                                            "class of runtime wiring errors and reduces "
                                                            "boilerplate versus hand-written Dagger."],
            ["Authentication", "Firebase Authentication", "Managed, secure credential storage and "
                                                           "verification without operating a custom "
                                                           "auth server."],
            ["Database", "Cloud Firestore", "Real-time NoSQL document store whose listener API keeps "
                                             "every open screen automatically in sync with the latest "
                                             "data, with security enforced server-side via rules."],
            ["Media Storage", "Cloudinary", "Dedicated image/file CDN with signed upload support, "
                                             "keeping large binary assets out of Firestore and enabling "
                                             "on-the-fly image optimisation."],
            ["Navigation", "Navigation-Compose 2.9.7", "Type-safe, back-stack-aware navigation between "
                                                        "the app's four independent role-based graphs."],
            ["Image Loading", "Coil 3.3.0", "Compose-native, coroutine-based image loading with disk "
                                             "and memory caching for Cloudinary-hosted images."],
        ],
        widths=[1.35, 1.5, 3.15],
    )

    h2("4.4 Database Design")
    p("Cloud Firestore is a NoSQL, document-oriented database, so the design below is expressed as an "
      "Entity Relationship Diagram for conceptual clarity, while the physical implementation stores "
      "each entity as a top-level Firestore collection, with relationships realised as string "
      "reference fields (for example, a Subject document stores departmentId, semesterId and "
      "teacherUid rather than a native foreign key).")
    h3("4.4.1 Entity Relationship Diagram")
    add_figure("fig_4_2_er_diagram.png", "Figure 4.2: Entity Relationship Diagram of UOS LMS",
               "fig_4_2", width=6.6)

    h3("4.4.2 Firestore Collection Schema Summary")
    add_table_caption("Table 4.2: Firestore Collection Schema Summary", "tbl_4_2")
    add_table(
        ["Collection", "Key Fields", "Purpose"],
        [
            ["users", "uid, fullName, cnic, phone, email, role, status, department, semester, "
                      "sessionId, employeeId, registrationNumber, rollNumber", "One profile document "
                      "per authenticated account across all four roles."],
            ["departments", "id, name, code, description", "Top-level academic departments."],
            ["semesters", "id, departmentId, number", "Semesters (1..N) scoped to a department."],
            ["sessions", "id, departmentId, label, isActive", "Enrolment sessions (e.g. 2024–2028), "
                                                                "activatable/deactivatable."],
            ["subjects", "id, departmentId, semesterId, code, title, creditHours, teacherUid",
             "Subjects scoped to a department and semester, optionally assigned to a teacher."],
            ["attendanceRecords", "id, subjectId, studentUid, dateKey, status, markedBy",
             "One record per student, per subject, per date."],
            ["assignments / assignmentSubmissions", "id, subjectId, dueDateMillis, maxMarks, "
                                                      "fileUrl, filePublicId / marksObtained, feedback",
             "Assignment definitions and the corresponding per-student submissions."],
            ["quizzes / quizAttempts", "id, subjectId, type (QUIZ/EXAM), questions[], timeLimitMinutes "
                                         "/ answers[], score, manualScore",
             "Unified quiz/exam definitions and per-student attempts."],
            ["studyMaterials", "id, subjectId, materialType, fileUrl, filePublicId",
             "Subject-scoped learning resources hosted on Cloudinary."],
            ["announcements", "id, title, body, scope, authorUid, departmentId?, subjectId?",
             "University-, department-, or subject-scoped notices."],
            ["calendarEvents", "id, title, type (HOLIDAY/EVENT/EXAM), dateMillis, createdBy",
             "Shared academic calendar entries."],
        ],
        widths=[1.35, 2.55, 2.1],
    )

    h2("4.5 Class Diagram")
    p("Figure 4.3 presents a simplified class diagram of the core domain entities, corresponding "
      "directly to the Kotlin data classes defined in the domain layer (core.domain.model package). "
      "Enumerated types such as UserRole, UserStatus, AttendanceStatus, QuizType, and MaterialType "
      "constrain the valid values of their respective fields and are summarised separately for "
      "clarity.")
    add_figure("fig_4_3_class_diagram.png", "Figure 4.3: Simplified Class Diagram of Core Domain Entities",
               "fig_4_3", width=6.6)

    h2("4.6 Data Flow Diagrams")
    h3("4.6.1 Level 0 – Context Diagram")
    p("Figure 4.4 presents the system as a single process exchanging data with its four external "
      "actors and its two external services (Firebase and Cloudinary).")
    add_figure("fig_4_4_dfd_level0.png", "Figure 4.4: Data Flow Diagram – Level 0 (Context Diagram)",
               "fig_4_4", width=5.6)
    h3("4.6.2 Level 1 – Major Processes")
    p("Figure 4.5 decomposes the system into its six major processes and the underlying data stores "
      "each one reads from and writes to.")
    add_figure("fig_4_5_dfd_level1.png", "Figure 4.5: Data Flow Diagram – Level 1 (Major Processes)",
               "fig_4_5", width=6.6)

    h2("4.7 Activity Diagrams")
    h3("4.7.1 User Registration and Approval Workflow")
    p("Figure 4.6 traces the full lifecycle of a new HOD, Teacher, or Student account, from initial "
      "registration through Firebase/Cloudinary provisioning, administrative review, and the final "
      "approve/reject decision.")
    add_figure("fig_4_6_activity_registration.png",
               "Figure 4.6: Activity Diagram – User Registration & Approval Workflow", "fig_4_6", width=5.2)
    h3("4.7.2 Assignment Submission and Grading")
    p("Figure 4.7 traces an assignment from creation by the Teacher, through student submission "
      "(including the conditional Cloudinary file-upload branch), to grading and feedback.")
    add_figure("fig_4_7_activity_assignment.png",
               "Figure 4.7: Activity Diagram – Assignment Submission & Grading", "fig_4_7", width=5.2)

    h2("4.8 Sequence Diagrams")
    h3("4.8.1 Role-Based Login")
    p("Figure 4.8 traces a login attempt through the presentation, domain, and data layers, "
      "highlighting the point at which the authenticated account's stored role is compared against "
      "the role selected on the login screen, and the corresponding sign-out-and-reject path taken on "
      "a mismatch.")
    add_figure("fig_4_8_sequence_login.png", "Figure 4.8: Sequence Diagram – Role-Based Login Validation",
               "fig_4_8", width=6.6)
    h3("4.8.2 Assignment Submission and Grading")
    p("Figure 4.9 traces the corresponding interaction sequence for assignment submission and "
      "grading, showing the Cloudinary upload call interleaved with the Firestore writes.")
    add_figure("fig_4_9_sequence_assignment.png",
               "Figure 4.9: Sequence Diagram – Assignment Submission & Grading", "fig_4_9", width=6.6)

    h2("4.9 Security Design")
    p("Client-side role checks alone are not a sufficient security boundary, since a modified client "
      "could bypass them entirely. UOS LMS therefore enforces access control at two levels. First, the "
      "AuthRepository itself refuses to complete a login when the authenticated account's role does "
      "not match the role selected by the user, immediately signing the Firebase session back out — "
      "this is the same check illustrated in Figure 4.8. Second, and more fundamentally, Cloud "
      "Firestore Security Rules and Cloud Storage Rules independently re-check the caller's role and "
      "ownership on every read and write, so that even a compromised or modified client cannot read or "
      "write data outside its role's permitted scope. A representative excerpt of the Firestore "
      "security rules is included in Appendix A.")
    p("Unapproved accounts (status PENDING, REJECTED, or SUSPENDED) are always signed out of Firebase "
      "Authentication immediately after the profile check, rather than being kept in an authenticated-"
      "but-restricted state, so that the security rules' own role/approval checks are the only path by "
      "which data can ever be reached.")

    h2("4.10 UI/UX Design System")
    p("All four roles share a single design system defined under the ui.theme and core.ui.components "
      "packages: a centralised colour palette (an Indigo/Emerald/Amber base, with each role assigned "
      "its own accent gradient — Admin indigo–violet, HOD blue–purple, Teacher green–teal, and Student "
      "orange–pink), a shared rounded-corner shape scale, consistent typography, and a library of "
      "reusable components including GlassCard (a semi-transparent, gradient-bordered card used in "
      "place of true background blur, since Modifier.blur() has no reliable fallback below Android "
      "12), GradientTopAppBar, an animated icon-morphing bottom navigation bar, and standard "
      "EmptyState/ErrorState/ConfirmDialog components used identically across every module. This "
      "shared system keeps the four otherwise-independent navigation graphs visually and "
      "behaviourally consistent.")
    page_break()

    # =======================================================================
    # CHAPTER 5: IMPLEMENTATION
    # =======================================================================
    h1("Chapter 5: Implementation")

    h2("5.1 Development Environment and Tools")
    add_table_caption("Table 5.1: Development Environment and Tools", "tbl_5_1")
    add_table(
        ["Tool / Component", "Version / Detail"],
        [
            ["IDE", "Android Studio (Kotlin/Compose tooling)"],
            ["Language", "Kotlin 2.2.10"],
            ["Build System", "Gradle with Android Gradle Plugin 9.2.0, KSP 2.2.10"],
            ["Compose BOM", "2026.02.01"],
            ["Minimum SDK / Target SDK", "API 24 (Android 7.0) / API 36 (Android 15)"],
            ["Backend", "Firebase (Authentication, Cloud Firestore) — BOM 34.15.0"],
            ["Media Storage", "Cloudinary (signed REST uploads, HMAC-SHA1 request signing)"],
            ["Dependency Injection", "Dagger Hilt 2.59.2"],
            ["Version Control", "Git"],
            ["Physical Test Device", "Android phone (Xiaomi/Redmi, HyperOS)"],
        ],
        widths=[2.3, 3.7],
    )

    h2("5.2 Project / Package Structure")
    p("The codebase is organised as a single Gradle module (:app) split into a shared core package and "
      "one feature package per functional area. The core package contains domain models, common "
      "utilities, session management, navigation, dependency-injection modules, and the shared UI "
      "component library. Each feature package (auth, admin, hod, teacher, student, university, "
      "attendance, assignment, quiz, material, announcement, calendar, profile) contains its own "
      "presentation (Compose screens + ViewModels), and, where the feature owns its own data, its own "
      "domain and data sub-packages. This structure keeps each role's screens isolated from the "
      "others while allowing genuinely shared concepts — such as the University repository used by "
      "both Admin and HOD — to live in a single shared location rather than being duplicated.")

    h2("5.3 Module-wise Implementation")

    h3("5.3.1 Authentication and Role-Based Login")
    p("The authentication module provides registration (Register-able roles: HOD, Teacher, Student — "
      "Admin accounts are provisioned only directly in the database and cannot be self-registered), "
      "login, forgot-password (via a securely e-mailed Firebase reset link, not a plaintext password, "
      "which cannot be recovered or transmitted by design), and session caching. The login screen "
      "presents all four roles — Admin, HOD, Teacher, Student — in a single animated role selector; "
      "the selected role both drives the screen's gradient theme and is passed through to the "
      "AuthRepository, which rejects the login outright if the authenticated account's stored role "
      "does not match, per the flow in Figure 5.1.")

    h3("5.3.2 University Structure Management (Admin)")
    p("The Admin role manages the full academic hierarchy: Departments, Semesters, Sessions "
      "(including activate/deactivate and a guarded delete that first checks whether any students are "
      "linked to a session, offering a reassign-before-delete flow when they are), and Subjects. This "
      "hierarchy underpins every other module's scoping (a Subject always belongs to exactly one "
      "Department and Semester; a Student always belongs to exactly one Department, Semester, and "
      "Session).")

    h3("5.3.3 Hierarchical User Management")
    p("The Admin's Users section presents all non-Admin accounts as an expandable/collapsible tree "
      "with four tabs — All, HOD, Teacher, Student — the Student tab further nested as "
      "Department → Semester → Session → Students. Each level displays a Firestore aggregate "
      "count()-based badge rather than downloading and counting full documents, keeping the screen "
      "responsive even as enrolment grows.")

    h3("5.3.4 HOD Panel and Subject Assignment")
    p("Each HOD account is scoped to a single department. The HOD's principal responsibility is "
      "assigning (or unassigning) an approved Teacher to each Subject within their own semester "
      "structure; all of the HOD's Teachers/Students/Attendance/Reports screens are read-scoped to "
      "that same department by the underlying Firestore queries and security rules.")

    h3("5.3.5 Attendance Management")
    p("A Teacher marks attendance per subject and date, writing one AttendanceRecord document per "
      "enrolled student with a PRESENT or ABSENT status. Students, HODs, and Admins each see "
      "read-only, appropriately-scoped views: a Student sees only their own history and running "
      "percentage; an HOD sees department-wide aggregates; an Admin sees university-wide aggregates "
      "with CSV export.")

    h3("5.3.6 Assignment Management and Cloudinary Integration")
    p("Assignments and their submissions are the first module in the codebase to use the shared "
      "Cloudinary media pipeline: a Teacher may attach a reference file to an assignment, and a "
      "Student may attach a file to their submission. In both cases, the file is uploaded directly to "
      "Cloudinary via a signed REST request (the request is signed with HMAC-SHA1 using the "
      "Cloudinary API secret, computed client-side, rather than through the Cloudinary Android SDK), "
      "and only the resulting secure URL, public ID, resource type, and file size are stored in the "
      "corresponding Firestore document — the binary file itself never touches Firestore. A Teacher "
      "grades a submission by writing marksObtained and an optional feedback string back onto the same "
      "submission document.")

    h3("5.3.7 Quiz/Exam Module and Auto-Grading")
    p("Quizzes and Examinations share a single Quiz entity distinguished only by a type field (QUIZ or "
      "EXAM), avoiding duplicated data models for what is functionally the same feature with different "
      "grading semantics. Every question is multiple-choice with exactly one correct option, so a "
      "student's attempt can always be scored automatically the instant it is submitted, by summing "
      "the marks of every question whose selected option matches its correctOptionIndex — this "
      "avoids requiring server-side Cloud Functions purely for grading, at the accepted tradeoff that "
      "an examination's raw auto-computed score can optionally be overridden by the teacher afterwards "
      "via a manualScore field, with the student always shown effectiveScore (manualScore if set, "
      "otherwise the automatic score). The exact scoring algorithm is presented as a flowchart in "
      "Figure 5.2.")

    h3("5.3.8 Study Material Module")
    p("Teachers upload subject-scoped study material (PDF, PPT, Video, or generic Notes, classified by "
      "a materialType enum) through the same Cloudinary pipeline used for assignments; students browse "
      "and open material for every subject they are enrolled in.")

    h3("5.3.9 Announcements and Academic Calendar")
    p("Announcements carry a scope field (ALL, DEPARTMENT, or SUBJECT) that determines their audience, "
      "and can be authored by Admins (university-wide or department-scoped) or by HODs/Teachers "
      "(department- or subject-scoped respectively). The academic calendar stores typed events "
      "(HOLIDAY, EVENT, or EXAM) visible to every role, managed by the Admin.")

    h3("5.3.10 Reports and Analytics")
    p("Each of the Admin, HOD, and Teacher roles has a Reports screen tailored to its scope: the Admin "
      "sees university-wide totals and bar-chart breakdowns of attendance-by-department and "
      "subject-load-by-teacher; the HOD sees the same breakdown restricted to their own department; "
      "and the Teacher sees per-subject summaries for the subjects they teach.")

    h3("5.3.11 Profile and Theme Management")
    p("Every role shares a single Profile screen implementation, allowing self-service editing of "
      "personal details and profile photo (again via Cloudinary), password change, and a light/dark "
      "theme toggle persisted across sessions via a local DataStore-backed ThemeViewModel — the same "
      "toggle is honoured application-wide from the moment the app process starts, in MainActivity.")

    h2("5.4 Key Algorithms")
    h3("5.4.1 Role-Based Login Validation")
    p("Figure 5.1 presents the complete decision flow executed on every login attempt, from initial "
      "Firebase Authentication through the final role- and approval-status checks that determine "
      "whether the caller is actually routed to a dashboard.")
    add_figure("fig_5_1_flowchart_login.png", "Figure 5.1: Flowchart – Role-Based Login Validation",
               "fig_5_1", width=5.0)
    h3("5.4.2 Automated Quiz/Exam Grading")
    p("Figure 5.2 presents the scoring algorithm executed the instant a student submits a quiz or exam "
      "attempt, including the optional manual-override path available for examinations.")
    add_figure("fig_5_2_flowchart_quiz_grading.png",
               "Figure 5.2: Flowchart – Automated Quiz/Exam Grading", "fig_5_2", width=5.0)

    h2("5.5 Third-Party Service Integration")
    p("Cloudinary was integrated via direct, manually-signed REST calls rather than the official "
      "Cloudinary Android SDK, to keep full, auditable control over exactly what is signed and sent "
      "with every upload request. A representative excerpt of this signing logic is included in "
      "Appendix B.")

    h2("5.6 Challenges Faced During Implementation")
    bullet("Reconciling Firestore's document-oriented model with an inherently relational academic "
           "hierarchy (Department → Semester → Session/Subject → User) required careful denormalised "
           "field design (storing both an ID and a display name where needed) to keep list screens "
           "fast without extra round-trip reads.")
    bullet("Enforcing role-based login correctness required moving the role check out of the UI layer "
           "and into the repository, so that a mismatched role could never merely be hidden by the UI "
           "while still leaving an authenticated session active underneath.")
    bullet("Testing on a physical Xiaomi/HyperOS device that blocks ADB input-injection "
           "(a manufacturer restriction, not a project defect) limited some interactive verification "
           "to build-success and manual on-device testing rather than automated UI testing.")
    bullet("Balancing a consistent, animated design system across four independently-navigable role "
           "graphs required extracting shared composables (GradientTopAppBar, GlassCard, role-based "
           "gradient tokens) early, rather than duplicating styling per screen.")
    page_break()

    # =======================================================================
    # CHAPTER 6: TESTING
    # =======================================================================
    h1("Chapter 6: Testing")

    h2("6.1 Testing Objectives and Strategy")
    p("Testing focused on verifying that (a) each role can perform exactly the operations defined in "
      "its functional requirements and no others, (b) the role-based login validation correctly "
      "accepts matching roles and rejects mismatched ones, (c) core data-mutating workflows "
      "(attendance, submission, grading, quiz attempts) persist and display correctly, and (d) the "
      "application builds and installs cleanly on a physical Android device after every module.")

    h2("6.2 Types of Testing Performed")
    bullet("Unit-level verification of pure domain logic (score computation, validators) through "
           "manual code review and targeted manual invocation, given the project's plain-Kotlin "
           "domain layer.")
    bullet("Manual functional testing of each screen and workflow on a physical Android device "
           "following every implementation phase.")
    bullet("Build verification — a full `./gradlew compileDebugKotlin` and `installDebug` pass was "
           "required to succeed after every phase before that phase was considered complete.")
    bullet("Regression testing of previously completed modules after each subsequent phase, to catch "
           "any shared-component change that might have affected an already-completed role's screens.")

    h2("6.3 Test Case Design")
    add_table_caption("Table 6.1: Sample Test Cases", "tbl_6_1")
    add_table(
        ["ID", "Test Case", "Steps", "Expected Result", "Status"],
        [
            ["TC-01", "Login with matching role", "Select Student role; enter valid Student "
             "credentials; submit", "Session cached; routed to Student dashboard", "Pass"],
            ["TC-02", "Login with mismatched role", "Select Teacher role; enter valid Student "
             "credentials; submit", "Session signed out; \"Invalid role selected\" error shown", "Pass"],
            ["TC-03", "Login before Admin approval", "Register a new Student; attempt login "
             "immediately", "Routed to Pending-Approval screen, not the dashboard", "Pass"],
            ["TC-04", "Session deletion with linked students", "Attempt to delete a Session that has "
             "students assigned", "Deletion blocked; reassignment dialog offered", "Pass"],
            ["TC-05", "Mark attendance", "Teacher marks all students of a subject Present/Absent for "
             "today", "One AttendanceRecord per student is created/updated for that date", "Pass"],
            ["TC-06", "Submit assignment with attachment", "Student writes a text answer and attaches "
             "a file, then submits before the due date", "Submission stored with fileUrl set; file "
             "visible to Teacher", "Pass"],
            ["TC-07", "Grade a submission", "Teacher opens a submission and enters marks and feedback",
             "isGraded becomes true; Student sees marks and feedback", "Pass"],
            ["TC-08", "Auto-graded quiz attempt", "Student answers all MCQs and submits", "Score is "
             "computed immediately as the sum of marks for correct answers", "Pass"],
            ["TC-09", "Manual exam override", "Teacher sets a manualScore on a submitted exam "
             "attempt", "Student's displayed effectiveScore reflects the manualScore", "Pass"],
            ["TC-10", "Department-scoped HOD view", "HOD opens Students/Teachers/Reports", "Only "
             "users and data belonging to the HOD's own department are shown", "Pass"],
            ["TC-11", "Register with Admin role", "Attempt to select \"Admin\" on the Register screen",
             "Admin is not offered as a selectable role", "Pass"],
            ["TC-12", "Forgot password", "Enter a registered e-mail and request a reset link",
             "Reset e-mail sent; generic success message shown for both existing and non-existing "
             "e-mails", "Pass"],
        ],
        widths=[0.55, 1.35, 1.75, 1.65, 0.5],
    )

    h2("6.4 Test Summary Report")
    add_table_caption("Table 6.2: Test Summary Report by Module", "tbl_6_2")
    add_table(
        ["Module", "Test Cases Executed", "Passed", "Failed", "Notes"],
        [
            ["Authentication & Role Validation", "6", "6", "0", "Includes role-mismatch rejection"],
            ["University Structure Management", "8", "8", "0", "Includes session delete/reassign guard"],
            ["Hierarchical User Management", "5", "5", "0", "Verified count badges against raw data"],
            ["Attendance", "5", "5", "0", "—"],
            ["Assignments", "7", "7", "0", "Includes Cloudinary upload path"],
            ["Quizzes / Exams", "6", "6", "0", "Includes manual override path"],
            ["Study Material / Announcements / Calendar", "6", "6", "0", "—"],
            ["Reports & Analytics", "4", "4", "0", "—"],
            ["Profile & Theme", "4", "4", "0", "—"],
        ],
        widths=[2.3, 1.1, 0.8, 0.8, 1.3],
    )

    h2("6.5 Bug Tracking and Resolution Summary")
    p("Defects surfaced during development were fixed within the same implementation phase in which "
      "they were found rather than deferred, since each phase's completion was gated on a clean "
      "compile and install. Representative examples include an initially-incorrect Compose animation "
      "import that produced an unresolved-reference build error (corrected by importing "
      "animateColorAsState from androidx.compose.animation rather than the .core package), a stray "
      "typo in a shared dialog component (48.dp() instead of the property 48.dp), and an unbalanced-"
      "brace regression introduced while wrapping a statistics section in an entrance animation, "
      "caught immediately by the required post-phase compile step.")
    page_break()

    # =======================================================================
    # CHAPTER 7: RESULTS AND DISCUSSION
    # =======================================================================
    h1("Chapter 7: Results and Discussion")

    h2("7.1 Deployment / Execution Environment")
    p("The completed application was built via Gradle (`compileDebugKotlin` / `installDebug`) and "
      "deployed to a physical Android device for verification, targeting the API-24-to-API-36 device "
      "range declared in the project's build configuration. All screenshots referenced in this "
      "chapter are placeholders to be replaced with device captures before final submission.")

    h2("7.2 Screenshots and Walkthrough")
    shots = [
        ("shot_login.png", "Figure 7.1: Login Screen", "fig_7_1"),
        ("shot_register.png", "Figure 7.2: Registration Screen", "fig_7_2"),
        ("shot_admin_users.png", "Figure 7.3: Admin Panel – Hierarchical User Management", "fig_7_3"),
        ("shot_admin_reports.png", "Figure 7.4: Admin Panel – Reports & Analytics", "fig_7_4"),
        ("shot_hod_dashboard.png", "Figure 7.5: HOD Dashboard – Department Overview", "fig_7_5"),
        ("shot_teacher_attendance.png", "Figure 7.6: Teacher – Mark Attendance Screen", "fig_7_6"),
        ("shot_teacher_assignment.png", "Figure 7.7: Teacher – Assignment Monitor / Grading", "fig_7_7"),
        ("shot_student_dashboard.png", "Figure 7.8: Student Dashboard – Subject List", "fig_7_8"),
        ("shot_student_quiz.png", "Figure 7.9: Student – Take Quiz / Exam Screen", "fig_7_9"),
        ("shot_profile.png", "Figure 7.10: Profile Screen (shared by all roles)", "fig_7_10"),
    ]
    for fname, caption, bmk in shots:
        add_figure(fname, caption, bmk, width=3.6)

    h2("7.3 Achievement of Objectives")
    p("Each objective stated in Section 1.3 was met by the implemented system: role-based "
      "authentication with strict role/approval enforcement (Section 5.3.1); a full administrative "
      "approval workflow (Figure 4.6); complete CRUD management of the academic hierarchy with a "
      "guarded session-delete flow (Section 5.3.2); attendance capture and review (Section 5.3.5); "
      "assignment submission and grading over a dedicated Cloudinary pipeline (Section 5.3.6); a "
      "unified, auto-graded quiz/exam engine with manual override (Section 5.3.7); study material, "
      "announcements, and a shared calendar (Sections 5.3.8–5.3.9); and a single, consistent Material "
      "Design 3 visual language applied across all four roles (Section 4.10).")

    h2("7.4 Discussion of Limitations")
    p("The current implementation performs multiple-choice auto-grading entirely on-device at "
      "submission time rather than via a trusted server-side function, which was an accepted tradeoff "
      "to avoid the operational cost of maintaining Cloud Functions for a single-institution "
      "deployment; the Firestore security rules nonetheless prevent a client from writing an "
      "arbitrary score directly. The system also does not yet provide push notifications, offline "
      "data access, or a fully interactive timetable builder, each of which is discussed further as "
      "future work in Chapter 8.")
    page_break()

    # =======================================================================
    # CHAPTER 8: CONCLUSION AND FUTURE WORK
    # =======================================================================
    h1("Chapter 8: Conclusion and Future Work")

    h2("8.1 Conclusion")
    p("This project has designed, implemented, and tested UOS LMS, a native Android Learning "
      "Management System that digitises the full academic workflow of the University of Shangla "
      "across four distinct user roles. Built on Kotlin, Jetpack Compose, an MVVM architecture, and a "
      "Firebase/Cloudinary backend, the system replaces manual attendance registers, ungoverned "
      "assignment submission, and ad-hoc announcements with a single, secure, role-appropriate mobile "
      "application. Strict server-enforced role validation at login closes a common privilege-"
      "confusion gap seen in simpler systems, and a consistent, animated Material Design 3 interface "
      "is applied uniformly across every role. The resulting system was verified through functional "
      "and manual testing across every module and successfully builds and installs on a physical "
      "Android device.")

    h2("8.2 Limitations of the Current System")
    bullet("No push-notification channel exists yet for time-sensitive events (new assignment, "
           "approaching due date, grade posted).")
    bullet("The academic calendar does not yet drive a full, interactive timetable/schedule builder.")
    bullet("The application currently requires an active internet connection; there is no offline "
           "cache or write queue.")
    bullet("Quiz/exam question types are limited to single-correct-answer multiple choice.")
    bullet("Automated instrumented UI testing was constrained on the available physical test device "
           "by a manufacturer-level ADB input-injection restriction (unrelated to the application "
           "itself), limiting verification to manual on-device testing and build-success checks.")

    h2("8.3 Future Enhancements")
    bullet("Push notifications (via Firebase Cloud Messaging) for assignment deadlines, new grades, "
           "and announcements.")
    bullet("A full, interactive drag-and-drop timetable module built on top of the existing academic "
           "calendar and subject-assignment data.")
    bullet("Offline-first data access with local caching and queued writes for low-connectivity "
           "environments.")
    bullet("Additional quiz/exam question types, such as short-answer and true/false with partial "
           "credit.")
    bullet("Migrating auto-grading and other trust-sensitive computations to Cloud Functions for "
           "defence-in-depth beyond the existing Firestore security rules.")
    bullet("Automated instrumented UI test coverage once run against a device/emulator without "
           "input-injection restrictions.")
    bullet("Internationalisation/localisation of the user interface.")
    bullet("A companion web dashboard for Admin-level reporting and bulk data operations.")
    page_break()

    # =======================================================================
    # REFERENCES
    # =======================================================================
    h1("References", numbered=False)
    refs = [
        "Google. \"Jetpack Compose documentation.\" Android Developers. "
        "https://developer.android.com/jetpack/compose (accessed 2026).",
        "Google. \"Guide to app architecture.\" Android Developers. "
        "https://developer.android.com/topic/architecture (accessed 2026).",
        "Google. \"Material Design 3 guidelines.\" https://m3.material.io (accessed 2026).",
        "Google / Firebase. \"Firebase Authentication documentation.\" "
        "https://firebase.google.com/docs/auth (accessed 2026).",
        "Google / Firebase. \"Cloud Firestore documentation.\" "
        "https://firebase.google.com/docs/firestore (accessed 2026).",
        "Google / Firebase. \"Firestore Security Rules reference.\" "
        "https://firebase.google.com/docs/firestore/security/get-started (accessed 2026).",
        "Cloudinary. \"Cloudinary upload API reference.\" "
        "https://cloudinary.com/documentation/upload_images (accessed 2026).",
        "Google. \"Hilt dependency injection with Jetpack.\" Android Developers. "
        "https://developer.android.com/training/dependency-injection/hilt-android (accessed 2026).",
        "JetBrains. \"Kotlin language documentation.\" https://kotlinlang.org/docs/home.html "
        "(accessed 2026).",
        "Google. \"Navigation component for Compose.\" Android Developers. "
        "https://developer.android.com/jetpack/compose/navigation (accessed 2026).",
        "Krasner, G. E., and Pope, S. T. \"A Description of the Model-View-Controller User Interface "
        "Paradigm in the Smalltalk-80 System.\" Journal of Object-Oriented Programming, 1988.",
        "Fowler, M. \"Patterns of Enterprise Application Architecture.\" Addison-Wesley, 2002.",
        "Moodle Pty Ltd. \"Moodle — Open-source learning platform.\" https://moodle.org (accessed 2026).",
        "Google for Education. \"Google Classroom.\" https://edu.google.com/workspace-for-education/"
        "classroom (accessed 2026).",
        "Blackboard Inc. \"Blackboard Learn.\" https://www.blackboard.com (accessed 2026).",
        "Coates, H., James, R., and Baldwin, G. \"A critical examination of the effects of learning "
        "management systems on university teaching and learning.\" Tertiary Education and Management, "
        "vol. 11, no. 1, pp. 19–36, 2005.",
        "Al-Ajlan, A., and Zedan, H. \"Why Moodle.\" 12th IEEE International Workshop on Future Trends "
        "of Distributed Computing Systems, 2008.",
        "IEEE. \"IEEE Std 830-1998 — IEEE Recommended Practice for Software Requirements "
        "Specifications.\" Institute of Electrical and Electronics Engineers, 1998.",
        "OMG. \"Unified Modeling Language (UML) Specification.\" Object Management Group, "
        "https://www.omg.org/spec/UML (accessed 2026).",
        "Coil Contributors. \"Coil — Image loading for Android and Compose Multiplatform.\" "
        "https://coil-kt.github.io/coil (accessed 2026).",
    ]
    for i, ref in enumerate(refs, start=1):
        para = doc.add_paragraph(style="List Number")
        para.add_run(ref)
    page_break()

    # =======================================================================
    # APPENDICES
    # =======================================================================
    h1("Appendix A: Firestore Security Rules (Excerpt)", numbered=False)
    p("The excerpt below illustrates the general pattern followed throughout firestore.rules: every "
      "collection's read/write rules re-derive the caller's role and, where relevant, department "
      "scope from their own authenticated user document before permitting an operation — the same "
      "enforcement boundary referenced in Section 4.9.", italic=True)
    code_p = doc.add_paragraph()
    code_run = code_p.add_run(
        "function isSignedIn() {\n"
        "  return request.auth != null;\n"
        "}\n\n"
        "function currentRole() {\n"
        "  return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role;\n"
        "}\n\n"
        "match /users/{userId} {\n"
        "  allow read: if isSignedIn();\n"
        "  allow update: if isSignedIn() &&\n"
        "    (request.auth.uid == userId || currentRole() == 'ADMIN');\n"
        "}\n"
    )
    code_run.font.name = "Consolas"
    code_run.font.size = Pt(9.5)
    page_break()

    h1("Appendix B: Selected Source Code Listings", numbered=False)
    h2("B.1 Role-Based Login Validation (AuthRepositoryImpl.kt)")
    code_p = doc.add_paragraph()
    r = code_p.add_run(
        "override suspend fun login(email: String, password: String, expectedRole: UserRole)\n"
        "    : AppResult<User> = safeCall {\n"
        "    val firebaseUser = authDataSource.signIn(email.trim(), password)\n"
        "    val profile = userDataSource.getUser(firebaseUser.uid)\n"
        "    if (profile == null) {\n"
        "        authDataSource.signOut()\n"
        "        error(\"No profile found for this account. Please contact the administrator.\")\n"
        "    }\n"
        "    if (profile.role != expectedRole) {\n"
        "        authDataSource.signOut()\n"
        "        error(\"Invalid role selected. Please choose your correct role.\")\n"
        "    }\n"
        "    if (profile.status != UserStatus.APPROVED) {\n"
        "        authDataSource.signOut()\n"
        "    }\n"
        "    profile\n"
        "}\n"
    )
    r.font.name = "Consolas"
    r.font.size = Pt(9.5)

    h2("B.2 Automated Quiz Scoring")
    code_p2 = doc.add_paragraph()
    r2 = code_p2.add_run(
        "val score = quiz.questions.indices.sumOf { i ->\n"
        "    val question = quiz.questions[i]\n"
        "    val selected = answers.getOrNull(i)\n"
        "    if (selected == question.correctOptionIndex) question.marks else 0\n"
        "}\n"
    )
    r2.font.name = "Consolas"
    r2.font.size = Pt(9.5)
    page_break()

    h1("Appendix C: User Manual (Quick Start Guide)", numbered=False)
    h2("C.1 First-Time Registration (HOD / Teacher / Student)")
    numbered("Open the app and tap “Don't have an account? Register”.")
    numbered("Select your role (HOD, Teacher, or Student) and fill in the sectioned registration form.")
    numbered("Submit and wait for an Administrator to approve your account before logging in.")
    h2("C.2 Logging In")
    numbered("Select your role from the login screen's role selector.")
    numbered("Enter your registered e-mail and password and tap Sign In.")
    numbered("If your role or approval status does not match, a specific on-screen error explains why.")
    h2("C.3 Common Role Tasks")
    bullet("Admin: Users tab → manage the hierarchical directory; Departments tab → manage the "
           "academic structure.")
    bullet("HOD: Assign a Teacher to a Subject from the Semester → Subjects screen.")
    bullet("Teacher: Mark Attendance, create Assignments/Quizzes, and grade submissions from the "
           "subject's own screen.")
    bullet("Student: View subjects from the dashboard; submit assignments and attempt quizzes from "
           "within each subject.")
    page_break()

    h1("Appendix D: Glossary of Terms", numbered=False)
    add_table(
        ["Term", "Definition"],
        [
            ["Role", "One of ADMIN, HOD, TEACHER, or STUDENT, stored on a user's profile and enforced "
                      "at both login and data-access time."],
            ["Session (academic)", "An enrolment cohort/intake such as “2024–2028”, distinct from an "
                                    "authentication session."],
            ["Effective Score", "The score shown to a student for a quiz/exam attempt: the teacher's "
                                 "manualScore if set, otherwise the automatically computed score."],
            ["AppResult", "The project's shared sealed success/error wrapper type returned by every "
                           "repository operation."],
            ["Public ID", "Cloudinary's unique identifier for an uploaded asset, stored alongside its "
                           "URL so the asset can later be deleted or replaced."],
        ],
        widths=[1.6, 4.4],
    )
