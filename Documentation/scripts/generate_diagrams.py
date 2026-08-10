# Diagram generator for the UOS LMS Final Year Project thesis.
# Produces all figures referenced in the thesis as PNG files under Documentation/diagrams/.
# Documentation-only artifact: does not touch application source code.

import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch, Circle, Rectangle
from matplotlib.lines import Line2D

OUT = os.path.join(os.path.dirname(__file__), "..", "diagrams")
os.makedirs(OUT, exist_ok=True)

NAVY = "#1a2b4c"
BLUE = "#2255aa"
LIGHT_BLUE = "#dbe9ff"
GREEN = "#1f7a4d"
LIGHT_GREEN = "#dcf5e6"
AMBER = "#8a5a00"
LIGHT_AMBER = "#fdecc8"
GRAY = "#444444"
LIGHT_GRAY = "#eeeeee"
PURPLE = "#5b2a86"
LIGHT_PURPLE = "#ece0f8"


def new_fig(w, h):
    fig, ax = plt.subplots(figsize=(w, h), dpi=200)
    ax.set_xlim(0, w * 10)
    ax.set_ylim(0, h * 10)
    ax.axis("off")
    ax.invert_yaxis()
    return fig, ax


def box(ax, x, y, w, h, text, fc=LIGHT_BLUE, ec=BLUE, fontsize=9.5, fontweight="bold",
        textcolor="#10192b", rounding=0.06, lw=1.4, linestyle="solid"):
    p = FancyBboxPatch((x, y), w, h,
                        boxstyle=f"round,pad=0.02,rounding_size={rounding}",
                        fc=fc, ec=ec, lw=lw, linestyle=linestyle, zorder=3)
    ax.add_patch(p)
    ax.text(x + w / 2, y + h / 2, text, ha="center", va="center",
             fontsize=fontsize, fontweight=fontweight, color=textcolor,
             wrap=True, zorder=4)
    return p


def rect(ax, x, y, w, h, text, fc=LIGHT_GRAY, ec=GRAY, fontsize=9, fontweight="bold",
         textcolor="#111111", lw=1.2):
    p = Rectangle((x, y), w, h, fc=fc, ec=ec, lw=lw, zorder=3)
    ax.add_patch(p)
    ax.text(x + w / 2, y + h / 2, text, ha="center", va="center",
             fontsize=fontsize, fontweight=fontweight, color=textcolor, zorder=4)
    return p


def diamond(ax, cx, cy, w, h, text, fc=LIGHT_AMBER, ec=AMBER, fontsize=8.3):
    pts = [(cx, cy - h / 2), (cx + w / 2, cy), (cx, cy + h / 2), (cx - w / 2, cy)]
    p = plt.Polygon(pts, closed=True, fc=fc, ec=ec, lw=1.4, zorder=3)
    ax.add_patch(p)
    ax.text(cx, cy, text, ha="center", va="center", fontsize=fontsize,
            fontweight="bold", color="#111111", zorder=4)
    return p


def oval(ax, cx, cy, w, h, text, fc=LIGHT_GREEN, ec=GREEN, fontsize=8.6):
    from matplotlib.patches import Ellipse
    p = Ellipse((cx, cy), w, h, fc=fc, ec=ec, lw=1.4, zorder=3)
    ax.add_patch(p)
    ax.text(cx, cy, text, ha="center", va="center", fontsize=fontsize,
            fontweight="bold", color="#0d2b1a", wrap=True, zorder=4)
    return p


def arrow(ax, x1, y1, x2, y2, text=None, color=GRAY, style="-|>", lw=1.3,
          connectionstyle="arc3,rad=0.0", ls="solid", fontsize=7.6, text_bg=True):
    a = FancyArrowPatch((x1, y1), (x2, y2), arrowstyle=style, mutation_scale=12,
                         color=color, lw=lw, connectionstyle=connectionstyle,
                         linestyle=ls, zorder=2)
    ax.add_patch(a)
    if text:
        mx, my = (x1 + x2) / 2, (y1 + y2) / 2
        bbox = dict(boxstyle="round,pad=0.12", fc="white", ec="none", alpha=0.85) if text_bg else None
        ax.text(mx, my, text, ha="center", va="center", fontsize=fontsize, color="#111111",
                bbox=bbox, zorder=5)


def actor(ax, cx, cy, label, scale=1.0, color=NAVY):
    r = 0.35 * scale
    head = Circle((cx, cy - 1.1 * scale), r, fc="white", ec=color, lw=1.6, zorder=3)
    ax.add_patch(head)
    ax.plot([cx, cx], [cy - 0.75 * scale, cy + 0.35 * scale], color=color, lw=1.6, zorder=3)
    ax.plot([cx - 0.55 * scale, cx + 0.55 * scale], [cy - 0.35 * scale, cy - 0.35 * scale], color=color, lw=1.6, zorder=3)
    ax.plot([cx, cx - 0.5 * scale], [cy + 0.35 * scale, cy + 1.0 * scale], color=color, lw=1.6, zorder=3)
    ax.plot([cx, cx + 0.5 * scale], [cy + 0.35 * scale, cy + 1.0 * scale], color=color, lw=1.6, zorder=3)
    ax.text(cx, cy + 1.35 * scale, label, ha="center", va="top", fontsize=9, fontweight="bold", color=color, zorder=4)


def save(fig, name):
    path = os.path.join(OUT, name)
    fig.tight_layout()
    fig.savefig(path, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("saved", path)


# ---------------------------------------------------------------------------
# Figure 4.1 - System Architecture Diagram
# ---------------------------------------------------------------------------
def fig_architecture():
    fig, ax = new_fig(11, 8)
    box(ax, 5, 3, 100, 9, "PRESENTATION LAYER\nJetpack Compose UI  —  Admin / HOD / Teacher / Student screens, shared design-system components (GradientTopAppBar, GlassCard, AppBottomNavBar)", fc=LIGHT_BLUE, ec=BLUE, fontsize=9)
    box(ax, 5, 15, 100, 9, "PRESENTATION STATE LAYER\nViewModels (MVVM)  —  StateFlow<UiState>, one-shot navigation Channels, AppResult<T> success/error wrapper", fc=LIGHT_PURPLE, ec=PURPLE, fontsize=9)
    box(ax, 5, 27, 47, 12, "DOMAIN LAYER\nUse Cases\n(LoginUseCase, RegisterUseCase,\nMarkAttendanceUseCase, ...)\nRepository Interfaces\nDomain Models (User, Subject,\nAssignment, Quiz, ...)", fc=LIGHT_GREEN, ec=GREEN, fontsize=8.6)
    box(ax, 58, 27, 47, 12, "DEPENDENCY INJECTION\nDagger Hilt modules wire\nViewModel -> UseCase -> Repository\n-> DataSource across every\nfeature module at compile time", fc=LIGHT_AMBER, ec=AMBER, fontsize=8.6)
    box(ax, 5, 43, 100, 10, "DATA LAYER\nRepository Implementations  —  FirestoreUserDataSource, FirestoreUniversityDataSource,\nFirebaseAuthDataSource, CloudinaryDataSource (signed REST + HMAC-SHA1)", fc=LIGHT_BLUE, ec=BLUE, fontsize=8.8)
    box(ax, 5, 57, 31, 11, "Firebase\nAuthentication\n(email / password,\nrole-aware sign-in)", fc="#ffe3e3", ec="#a12b2b", fontsize=8.6)
    box(ax, 39.5, 57, 31, 11, "Cloud Firestore\n(NoSQL document store —\nUsers, Departments, Subjects,\nAssignments, Quizzes, ...)", fc="#ffe3e3", ec="#a12b2b", fontsize=8.3)
    box(ax, 74, 57, 31, 11, "Cloudinary\n(media CDN — profile photos,\nassignment files, study\nmaterials, signed uploads)", fc="#ffe3e3", ec="#a12b2b", fontsize=8.3)

    for x in (20, 40, 60, 80):
        arrow(ax, x, 12, x, 15, style="<|-|>")
    for x in (20, 40, 60, 80):
        arrow(ax, x, 24, x, 27, style="<|-|>")
    arrow(ax, 55, 39, 55, 43, style="<|-|>")
    arrow(ax, 20.5, 53, 20.5, 57, style="<|-|>")
    arrow(ax, 55, 53, 55, 57, style="<|-|>")
    arrow(ax, 89.5, 53, 89.5, 57, style="<|-|>")
    save(fig, "fig_4_1_system_architecture.png")


# ---------------------------------------------------------------------------
# Figure 3.1 / 3.2 - Use Case Diagrams
# ---------------------------------------------------------------------------
def usecase_diagram(filename, title, actors_left, actors_right, use_cases, connections):
    fig, ax = new_fig(11, 8.6)
    box(ax, 20, 3, 66, 78, "", fc="white", ec=GRAY, lw=1.6)
    ax.text(53, 5, "UOS LMS", fontsize=10.5, fontweight="bold", ha="center", color=GRAY)

    for label, y in actors_left:
        actor(ax, 8, y, label, scale=1.0)
    for label, y in actors_right:
        actor(ax, 100, y, label, scale=1.0)

    positions = {}
    n = len(use_cases)
    top, bottom = 10, 76
    step = (bottom - top) / max(n - 1, 1)
    for i, uc in enumerate(use_cases):
        y = top + i * step
        positions[uc] = (53, y)
        oval(ax, 53, y, 30, 6.4, uc, fontsize=8.0)

    for actor_label, uc, side in connections:
        ax_, ay_ = (8, None)
        if side == "L":
            ay_ = [y for (lab, y) in actors_left if lab == actor_label][0]
            ux, uy = positions[uc]
            arrow(ax, 8 + 0.9, ay_, ux - 15, uy, color="#666666", style="-", lw=0.9, text_bg=False)
        else:
            ay_ = [y for (lab, y) in actors_right if lab == actor_label][0]
            ux, uy = positions[uc]
            arrow(ax, 100 - 0.9, ay_, ux + 15, uy, color="#666666", style="-", lw=0.9, text_bg=False)

    save(fig, filename)


def fig_usecase_admin_hod():
    use_cases = [
        "Register / Login\n(role-validated)",
        "Manage Departments,\nSemesters & Sessions",
        "Approve / Reject / Suspend\nUser Accounts",
        "Browse Hierarchical\nUser Directory",
        "Assign Teachers\nto Subjects",
        "Monitor Assignments\n& Quizzes / Exams",
        "View Attendance\n& Analytics Reports",
        "Post Announcements &\nManage Academic Calendar",
        "Manage Own Profile",
    ]
    connections = [
        ("Admin", use_cases[0], "L"), ("Admin", use_cases[1], "L"), ("Admin", use_cases[2], "L"),
        ("Admin", use_cases[3], "L"), ("Admin", use_cases[5], "L"), ("Admin", use_cases[6], "L"),
        ("Admin", use_cases[7], "L"), ("Admin", use_cases[8], "L"),
        ("HOD", use_cases[0], "R"), ("HOD", use_cases[4], "R"), ("HOD", use_cases[5], "R"),
        ("HOD", use_cases[6], "R"), ("HOD", use_cases[3], "R"), ("HOD", use_cases[8], "R"),
    ]
    usecase_diagram(
        "fig_3_1_usecase_admin_hod.png",
        "Figure 3.1: Use Case Diagram - Administrative Actors (Admin & HOD)",
        [("Admin", 20)], [("HOD", 20)], use_cases, connections,
    )


def fig_usecase_teacher_student():
    use_cases = [
        "Register / Login\n(role-validated)",
        "View Assigned\nSubjects",
        "Mark Subject\nAttendance",
        "Create & Grade\nAssignments",
        "Create Quizzes / Exams\n& Grade Exams",
        "Upload Study\nMaterials",
        "Submit Assignments",
        "Attempt Quizzes\n(auto-graded)",
        "View Attendance,\nGrades & Reports",
        "Manage Own Profile",
    ]
    connections = [
        ("Teacher", use_cases[0], "L"), ("Teacher", use_cases[1], "L"), ("Teacher", use_cases[2], "L"),
        ("Teacher", use_cases[3], "L"), ("Teacher", use_cases[4], "L"), ("Teacher", use_cases[5], "L"),
        ("Teacher", use_cases[8], "L"), ("Teacher", use_cases[9], "L"),
        ("Student", use_cases[0], "R"), ("Student", use_cases[1], "R"), ("Student", use_cases[6], "R"),
        ("Student", use_cases[7], "R"), ("Student", use_cases[8], "R"), ("Student", use_cases[9], "R"),
        ("Student", use_cases[5], "R"),
    ]
    usecase_diagram(
        "fig_3_2_usecase_teacher_student.png",
        "Figure 3.2: Use Case Diagram - Academic Actors (Teacher & Student)",
        [("Teacher", 20)], [("Student", 20)], use_cases, connections,
    )


# ---------------------------------------------------------------------------
# Figure 4.2 - Entity Relationship Diagram
# ---------------------------------------------------------------------------
def fig_er_diagram():
    fig, ax = new_fig(13, 10)

    def ent(x, y, w, h, title, attrs):
        box(ax, x, y, w, h, "", fc="white", ec=NAVY, lw=1.4, rounding=0.03)
        ax.text(x + w / 2, y + 3.2, title, ha="center", fontsize=9, fontweight="bold", color="white",
                bbox=dict(boxstyle="round,pad=0.25", fc=NAVY, ec=NAVY))
        ax.text(x + w / 2, y + h / 2 + 1.8, attrs, ha="center", va="center", fontsize=6.9, color="#111111")

    ent(2, 3, 22, 15, "DEPARTMENT", "PK id\nname, code\ndescription\ncreatedAt")
    ent(2, 24, 22, 13, "SEMESTER", "PK id\nFK departmentId\nnumber\ncreatedAt")
    ent(2, 43, 22, 13, "SESSION", "PK id\nFK departmentId\nlabel, isActive\ncreatedAt")
    ent(28, 3, 24, 17, "USER", "PK uid\nfullName, cnic, phone\nemail, role, status\nFK department\nFK semester, FK sessionId\nemployeeId / designation\nregNo / rollNo, createdAt")
    ent(28, 45, 24, 15, "SUBJECT", "PK id\nFK departmentId\nFK semesterId\ncode, title, creditHours\nFK teacherUid\ncreatedAt")
    ent(58, 3, 24, 13, "ATTENDANCE\nRECORD", "PK id\nFK subjectId, FK studentUid\ndateKey, status\nFK markedBy")
    ent(58, 19, 24, 15, "ASSIGNMENT", "PK id\nFK subjectId\ntitle, description\ndueDateMillis, maxMarks\nfileUrl / filePublicId")
    ent(58, 37, 24, 15, "ASSIGNMENT\nSUBMISSION", "PK id\nFK assignmentId\nFK studentUid\ntextAnswer / fileUrl\nmarksObtained, feedback")
    ent(87, 3, 24, 15, "QUIZ", "PK id\nFK subjectId\ntitle, type (QUIZ/EXAM)\nquestions[], timeLimit\ndueDateMillis")
    ent(87, 21, 24, 15, "QUIZ\nATTEMPT", "PK id\nFK quizId, FK studentUid\nanswers[], score\nmanualScore, feedback")
    ent(87, 39, 24, 13, "STUDY\nMATERIAL", "PK id\nFK subjectId\ntitle, materialType\nfileUrl / filePublicId")
    ent(58, 55, 24, 13, "ANNOUNCEMENT", "PK id\ntitle, body, scope\nFK authorUid\nFK departmentId? / subjectId?")
    ent(28, 63, 24, 13, "CALENDAR\nEVENT", "PK id\ntitle, description\ntype (HOLIDAY/EVENT/EXAM)\ndateMillis, FK createdBy")

    def link(x1, y1, x2, y2, near="1", far="M", **kw):
        arrow(ax, x1, y1, x2, y2, style="-", color="#555555", lw=1.1, text_bg=False, **kw)
        ax.text(x1 + (x2 - x1) * 0.12, y1 + (y2 - y1) * 0.12, near, fontsize=7.5, fontweight="bold", color=NAVY)
        ax.text(x1 + (x2 - x1) * 0.88, y1 + (y2 - y1) * 0.88, far, fontsize=7.5, fontweight="bold", color=NAVY)

    link(24, 10, 28, 11)      # Department -> User
    link(24, 30, 28, 20)      # Semester -> User
    link(24, 49, 28, 20)      # Session -> User
    link(24, 30, 28, 50)      # Semester -> Subject
    link(24, 12, 28, 52)      # Department -> Subject
    link(52, 12, 28, 15, near="M", far="1")  # User(teacher) -> Subject
    link(52, 52, 58, 10)      # Subject -> AttendanceRecord
    link(40, 20, 58, 8, near="1", far="M")   # User(student) -> AttendanceRecord
    link(52, 48, 58, 27)      # Subject -> Assignment
    link(82, 27, 58, 44)      # Assignment -> AssignmentSubmission
    link(40, 20, 58, 44, near="1", far="M")  # User(student) -> AssignmentSubmission
    link(82, 12, 87, 10)      # Subject -> Quiz
    link(99, 18, 99, 28)      # Quiz -> QuizAttempt
    link(40, 20, 87, 28, near="1", far="M")  # User(student) -> QuizAttempt
    link(82, 50, 87, 46)      # Subject -> StudyMaterial
    link(40, 15, 58, 60)      # User(author) -> Announcement
    link(40, 15, 28, 66)      # User(createdBy) -> CalendarEvent

    save(fig, "fig_4_2_er_diagram.png")


# ---------------------------------------------------------------------------
# Figure 4.3 - Class Diagram
# ---------------------------------------------------------------------------
def uml_class(ax, x, y, w, h, name, attrs, methods=None, fc=LIGHT_BLUE):
    box(ax, x, y, w, 4.6, name, fc=NAVY, ec=NAVY, textcolor="white", fontsize=8.6, rounding=0.0)
    box(ax, x, y + 4.6, w, h - 4.6 - (5.2 if methods else 0), attrs, fc=fc, ec=NAVY, fontsize=6.6,
        fontweight="normal", rounding=0.0)
    if methods:
        box(ax, x, y + h - 5.2, w, 5.2, methods, fc="white", ec=NAVY, fontsize=6.5, fontweight="normal", rounding=0.0)


def fig_class_diagram():
    fig, ax = new_fig(13, 9.5)

    uml_class(ax, 2, 4, 22, 24, "User", "uid: String\nfullName, cnic, phone\nemail: String\nrole: UserRole\nstatus: UserStatus\ndepartment, semester\nsessionId, employeeId\nregistrationNumber, rollNumber",
              "isApproved(): Boolean")
    uml_class(ax, 27, 4, 20, 16, "Department", "id, name, code\ndescription\ncreatedAt: Long")
    uml_class(ax, 27, 23, 20, 14, "Semester", "id, departmentId\nnumber: Int",
              "displayName: String")
    uml_class(ax, 27, 40, 20, 14, "Session", "id, departmentId\nlabel: String\nisActive: Boolean")
    uml_class(ax, 51, 4, 22, 18, "Subject", "id, departmentId\nsemesterId, code, title\ncreditHours: Int\nteacherUid, teacherName")
    uml_class(ax, 51, 25, 22, 16, "AttendanceRecord", "id, subjectId\nstudentUid, dateKey\nstatus: AttendanceStatus\nmarkedBy")
    uml_class(ax, 51, 44, 22, 18, "Assignment", "id, subjectId\ntitle, description\ndueDateMillis, maxMarks\nfileUrl, filePublicId")
    uml_class(ax, 76, 4, 22, 18, "AssignmentSubmission", "id, assignmentId\nstudentUid, textAnswer\nfileUrl, marksObtained\nfeedback, gradedBy",
              "isGraded: Boolean")
    uml_class(ax, 76, 25, 22, 18, "Quiz", "id, subjectId, title\ntype: QuizType\nquestions: List<QuizQuestion>\ntimeLimitMinutes",
              "totalMarks: Int")
    uml_class(ax, 76, 46, 22, 18, "QuizAttempt", "id, quizId, studentUid\nanswers: List<Int?>\nscore, manualScore",
              "effectiveScore: Int")
    uml_class(ax, 2, 32, 22, 22, "«enum»\nUserRole / UserStatus", "UserRole:\n ADMIN, HOD, TEACHER, STUDENT\nUserStatus:\n PENDING, APPROVED,\n REJECTED, SUSPENDED", fc=LIGHT_AMBER)

    arrow(ax, 24, 12, 27, 12, text="1..*", style="-|>")
    arrow(ax, 24, 43, 24, 32, text="1..*", style="-|>")
    arrow(ax, 47, 30, 51, 13, text="1..*", style="-|>")
    arrow(ax, 47, 33, 51, 33, text="1..*", style="-|>")
    arrow(ax, 73, 13, 76, 13, text="1", style="-|>")
    arrow(ax, 73, 33, 76, 33, text="1..*", style="-|>")
    arrow(ax, 87, 43, 87, 46, text="1..*", style="-|>")
    arrow(ax, 24, 16, 2, 32, text="uses", style="-|>", ls="dashed")

    save(fig, "fig_4_3_class_diagram.png")


# ---------------------------------------------------------------------------
# Figure 4.4 / 4.5 - DFD Level 0 (Context) and Level 1
# ---------------------------------------------------------------------------
def fig_dfd_level0():
    fig, ax = new_fig(10, 7)
    box(ax, 35, 32, 30, 16, "0.0\nUOS LMS", fc=LIGHT_GREEN, ec=GREEN, fontsize=11, rounding=1.2)

    actor(ax, 10, 10, "Admin")
    actor(ax, 10, 60, "HOD")
    actor(ax, 90, 10, "Teacher")
    actor(ax, 90, 60, "Student")
    rect(ax, 34, 5, 32, 8, "Firebase Authentication", fc=LIGHT_AMBER, ec=AMBER, fontsize=7.6)
    rect(ax, 34, 63, 32, 8, "Cloud Firestore & Cloudinary", fc=LIGHT_AMBER, ec=AMBER, fontsize=7.6)

    arrow(ax, 13, 12, 35, 34, text="credentials,\nrequests", style="-|>")
    arrow(ax, 35, 40, 13, 16, text="dashboards,\napprovals", style="-|>")
    arrow(ax, 13, 58, 35, 42, text="dept actions", style="-|>")
    arrow(ax, 35, 42, 13, 62, text="reports", style="-|>")
    arrow(ax, 65, 34, 88, 12, text="attendance,\ngrades", style="-|>")
    arrow(ax, 88, 16, 65, 40, text="subjects,\nsubmissions", style="-|>")
    arrow(ax, 65, 42, 88, 58, text="materials,\nfeedback", style="-|>")
    arrow(ax, 88, 62, 65, 44, text="submissions,\nattempts", style="-|>")
    arrow(ax, 50, 13, 50, 32, text="verify", style="<|-|>")
    arrow(ax, 50, 48, 50, 63, text="read / write data", style="<|-|>")

    save(fig, "fig_4_4_dfd_level0.png")


def fig_dfd_level1():
    fig, ax = new_fig(12, 8.6)
    procs = [
        ("1.0\nAuthentication &\nAuthorization", 6, 34),
        ("2.0\nAcademic Structure\nManagement", 30, 8),
        ("3.0\nAttendance\nManagement", 30, 60),
        ("4.0\nAssessment\nManagement", 54, 34),
        ("5.0\nContent &\nCommunication", 78, 8),
        ("6.0\nReporting &\nAnalytics", 78, 60),
    ]
    for text, x, y in procs:
        oval(ax, x + 10, y + 8, 22, 14, text, fontsize=8.2)

    stores = [
        ("D1  Users", 4, 76),
        ("D2  Academic Structure\n(Dept/Semester/Session/Subject)", 30, 76),
        ("D3  Attendance Records", 56, 76),
        ("D4  Assignments / Quizzes", 82, 76),
    ]
    for text, x, y in stores:
        rect(ax, x, y, 20, 7, text, fc=LIGHT_GRAY, ec=GRAY, fontsize=6.6)

    actor(ax, 2, 2, "Users\n(4 roles)", scale=0.8)
    arrow(ax, 5, 6, 14, 34, text="login /\nregister", style="-|>")
    arrow(ax, 16, 39, 40, 15, text="verified\nsession", style="-|>")
    arrow(ax, 16, 41, 40, 65, text="verified\nsession", style="-|>")
    arrow(ax, 40, 15, 64, 38, style="-|>")
    arrow(ax, 40, 65, 64, 41, style="-|>")
    arrow(ax, 64, 38, 88, 15, style="-|>")
    arrow(ax, 64, 40, 88, 63, style="-|>")
    arrow(ax, 14, 30, 14, 76, text="credentials", style="<|-|>")
    arrow(ax, 40, 22, 40, 76, text="structure data", style="<|-|>")
    arrow(ax, 40, 68, 66, 76, text="records", style="<|-|>")
    arrow(ax, 64, 30, 92, 76, text="assessments", style="<|-|>")
    arrow(ax, 88, 22, 92, 76, style="<|-|>")

    save(fig, "fig_4_5_dfd_level1.png")


# ---------------------------------------------------------------------------
# Figure 4.6 / 4.7 - Activity Diagrams
# ---------------------------------------------------------------------------
def fig_activity_registration():
    fig, ax = new_fig(10, 11)
    y = 3
    def step(text, h=6, fc=LIGHT_BLUE, ec=BLUE, w=60, xoff=15):
        nonlocal y
        box(ax, xoff, y, w, h, text, fc=fc, ec=ec, fontsize=8.6)
        top = y
        y += h + 3.2
        return top

    ax.add_patch(Circle((45, 2), 1.6, fc="black", ec="black", zorder=5))
    y = 6
    step("Applicant opens Register screen and\nselects role: HOD, Teacher or Student")
    step("Fills personal, contact & academic details;\noptionally attaches a profile photo")
    step("System validates fields (email, CNIC,\nphone format, password strength)")
    d1 = y
    diamond(ax, 45, d1 + 5, 46, 12, "Validation\npassed?")
    y = d1 + 5 + 6 + 4
    box(ax, 68, d1 - 2, 26, 8, "Show inline\nvalidation errors", fc="#ffe3e3", ec="#a12b2b", fontsize=7.6)
    arrow(ax, 68, d1 + 3, 45 + 23, d1 + 5, text="No", style="-|>")
    arrow(ax, 45, d1 + 11, 45, y, text="Yes", style="-|>")
    step("Firebase Auth account created;\nphoto uploaded to Cloudinary (if any)")
    step("Firestore profile document created\nwith status = PENDING")
    step("Admin reviews the pending account\nin the Users hierarchy")
    d2 = y
    diamond(ax, 45, d2 + 5, 50, 12, "Admin\ndecision?")
    y = d2 + 5 + 6 + 4
    box(ax, 3, d2 - 1, 26, 9, "REJECTED —\naccount cannot sign in", fc="#ffe3e3", ec="#a12b2b", fontsize=7.4)
    box(ax, 68, d2 - 1, 26, 9, "APPROVED —\nuser can now log in", fc=LIGHT_GREEN, ec=GREEN, fontsize=7.4)
    arrow(ax, 45 - 23, d2 + 5, 16, d2 + 3, text="Reject", style="-|>")
    arrow(ax, 45 + 23, d2 + 5, 81, d2 + 3, text="Approve", style="-|>")
    step("User signs in with the approved role\nand is routed to their dashboard")
    ax.add_patch(Circle((45, y + 2), 1.6, fc="white", ec="black", lw=1.6, zorder=5))
    ax.add_patch(Circle((45, y + 2), 0.8, fc="black", zorder=5))

    ax.set_ylim(0, (y + 8) )
    ax.invert_yaxis()
    save(fig, "fig_4_6_activity_registration.png")


def fig_activity_assignment():
    fig, ax = new_fig(10, 11)
    y = 6
    ax.add_patch(Circle((45, 2), 1.6, fc="black", ec="black", zorder=5))

    def step(text, fc=LIGHT_BLUE, ec=BLUE, h=6, w=60, xoff=15, fontsize=8.6):
        nonlocal y
        box(ax, xoff, y, w, h, text, fc=fc, ec=ec, fontsize=fontsize)
        y += h + 3.2

    step("Teacher creates an assignment for a\nsubject (title, description, due date, marks)")
    step("Assignment is stored in Firestore and\nvisible to enrolled students")
    step("Student opens the assignment and writes\na text answer and/or attaches a file")
    d1 = y
    diamond(ax, 45, d1 + 5, 40, 12, "File\nattached?")
    y = d1 + 5 + 6 + 4
    box(ax, 68, d1 - 2, 26, 8, "Upload file to\nCloudinary, get URL", fc=LIGHT_AMBER, ec=AMBER, fontsize=7.4)
    arrow(ax, 45 + 20, d1 + 5, 71, d1 + 2, text="Yes", style="-|>")
    arrow(ax, 45, d1 + 11, 45, y, text="No", style="-|>")
    step("Submission document saved to Firestore\n(text / file URL, submittedAt)")
    step("Teacher reviews submissions for the\nassignment in Assignment Monitor")
    step("Teacher enters marksObtained and\noptional feedback for the submission")
    step("Submission is marked as graded;\nstudent views marks & feedback")
    ax.add_patch(Circle((45, y + 2), 1.6, fc="white", ec="black", lw=1.6, zorder=5))
    ax.add_patch(Circle((45, y + 2), 0.8, fc="black", zorder=5))

    ax.set_ylim(0, y + 8)
    ax.invert_yaxis()
    save(fig, "fig_4_7_activity_assignment.png")


# ---------------------------------------------------------------------------
# Figure 4.8 / 4.9 - Sequence Diagrams
# ---------------------------------------------------------------------------
def sequence_diagram(filename, title, lifelines, messages, height=9.5):
    n = len(lifelines)
    fig, ax = new_fig(12, height)
    xs = {}
    span = 100
    step = span / (n - 1) if n > 1 else span
    for i, name in enumerate(lifelines):
        x = 6 + i * step
        xs[name] = x
        box(ax, x - 9, 3, 18, 6, name, fc=NAVY, ec=NAVY, textcolor="white", fontsize=8)
        ax.plot([x, x], [9, 9 + (len(messages) * 6.6) + 6], color="#999999", lw=1.1, ls="dashed", zorder=1)

    y = 12
    for src, dst, text, kind in messages:
        x1, x2 = xs[src], xs[dst]
        if src == dst:
            arrow(ax, x1, y, x1 + 14, y, style="-|>", color=GRAY if kind != "return" else "#888888",
                  ls="solid" if kind != "return" else "dashed", text=text, fontsize=7.2)
            arrow(ax, x1 + 14, y + 3.2, x1, y + 3.2, style="-|>", color="#888888", ls="dashed", text=None)
            y += 7.2
        else:
            arrow(ax, x1, y, x2, y, style="-|>", color=GRAY if kind != "return" else "#888888",
                  ls="solid" if kind != "return" else "dashed", text=text, fontsize=7.2)
            y += 6.6

    ax.set_ylim(0, y + 5)
    ax.invert_yaxis()
    ax.text(50, y + 4, title, ha="center", fontsize=9.8, fontweight="bold")
    save(fig, filename)


def fig_sequence_login():
    lifelines = ["Student\n(Actor)", "LoginScreen", "LoginViewModel", "LoginUseCase", "AuthRepository", "FirebaseAuth /\nFirestore"]
    messages = [
        ("Student\n(Actor)", "LoginScreen", "select role, enter\nemail & password", "call"),
        ("LoginScreen", "LoginViewModel", "login()", "call"),
        ("LoginViewModel", "LoginUseCase", "invoke(email, pass,\nexpectedRole)", "call"),
        ("LoginUseCase", "AuthRepository", "login(email, pass,\nexpectedRole)", "call"),
        ("AuthRepository", "FirebaseAuth /\nFirestore", "signIn() then\ngetUser(uid)", "call"),
        ("FirebaseAuth /\nFirestore", "AuthRepository", "FirebaseUser +\nUser profile", "return"),
        ("AuthRepository", "AuthRepository", "role == expectedRole ?", "call"),
        ("AuthRepository", "FirebaseAuth /\nFirestore", "[mismatch] signOut()", "call"),
        ("AuthRepository", "LoginUseCase", "AppResult.Error(\n\"Invalid role selected\")", "return"),
        ("AuthRepository", "LoginUseCase", "[match] AppResult.\nSuccess(user)", "return"),
        ("LoginUseCase", "LoginViewModel", "AppResult<User>", "return"),
        ("LoginViewModel", "LoginScreen", "cache session, navigate\nto role dashboard", "return"),
    ]
    sequence_diagram("fig_4_8_sequence_login.png",
                      "Figure 4.8: Sequence Diagram - Role-Based Login Validation",
                      lifelines, messages, height=11)


def fig_sequence_assignment():
    lifelines = ["Student", "Submit-\nAssignment\nScreen", "AssignmentVM", "AssignmentRepo", "Cloudinary", "Firestore", "Teacher"]
    messages = [
        ("Student", "Submit-\nAssignment\nScreen", "write answer,\nattach file", "call"),
        ("Submit-\nAssignment\nScreen", "AssignmentVM", "submit()", "call"),
        ("AssignmentVM", "AssignmentRepo", "submitAssignment()", "call"),
        ("AssignmentRepo", "Cloudinary", "uploadFile(uri)", "call"),
        ("Cloudinary", "AssignmentRepo", "url, publicId", "return"),
        ("AssignmentRepo", "Firestore", "save submission\ndocument", "call"),
        ("Firestore", "AssignmentRepo", "ack", "return"),
        ("AssignmentRepo", "AssignmentVM", "AppResult.Success", "return"),
        ("Teacher", "Firestore", "observe submissions\nfor assignment", "call"),
        ("Teacher", "AssignmentRepo", "gradeSubmission(marks,\nfeedback)", "call"),
        ("AssignmentRepo", "Firestore", "update marksObtained,\nfeedback, gradedBy", "call"),
        ("Student", "Firestore", "observe graded\nsubmission", "call"),
    ]
    sequence_diagram("fig_4_9_sequence_assignment.png",
                      "Figure 4.9: Sequence Diagram - Assignment Submission & Grading",
                      lifelines, messages, height=11)


# ---------------------------------------------------------------------------
# Figure 5.1 / 5.2 - Flowcharts
# ---------------------------------------------------------------------------
def fig_flowchart_login():
    fig, ax = new_fig(10, 12)
    ax.add_patch(Circle((45, 2), 1.6, fc="black"))
    ax.text(45, 5, "Start", ha="center", fontsize=8, fontweight="bold")
    y = 7
    box(ax, 15, y, 60, 6, "Enter email & password;\nselect role (Admin/HOD/Teacher/Student)"); y += 9.5
    box(ax, 15, y, 60, 6, "Call Firebase Authentication\nsignInWithEmailAndPassword()"); y += 9.5
    d1y = y
    diamond(ax, 45, d1y + 6, 42, 12, "Credentials\nvalid?");
    box(ax, 70, d1y + 1, 26, 8, "Show error:\n\"Invalid email or password\"", fc="#ffe3e3", ec="#a12b2b", fontsize=7.2)
    arrow(ax, 66, d1y + 6, 71, d1y + 5, text="No")
    y = d1y + 12 + 5
    box(ax, 15, y, 60, 6, "Fetch user profile document\nfrom Cloud Firestore by uid"); y += 9.5
    d2y = y
    diamond(ax, 45, d2y + 6, 42, 12, "Profile\nexists?")
    box(ax, 70, d2y + 1, 26, 8, "signOut(); show error:\n\"No profile found\"", fc="#ffe3e3", ec="#a12b2b", fontsize=7.2)
    arrow(ax, 66, d2y + 6, 71, d2y + 5, text="No")
    y = d2y + 12 + 5
    d3y = y
    diamond(ax, 45, d3y + 6, 48, 12, "profile.role ==\nselected role?")
    box(ax, 70, d3y + 1, 26, 9, "signOut(); show error:\n\"Invalid role selected.\nPlease choose your\ncorrect role.\"", fc="#ffe3e3", ec="#a12b2b", fontsize=6.9)
    arrow(ax, 69, d3y + 6, 71, d3y + 5, text="No")
    y = d3y + 12 + 5
    d4y = y
    diamond(ax, 45, d4y + 6, 46, 12, "status ==\nAPPROVED?")
    box(ax, 70, d4y + 1, 26, 9, "signOut(); route to\nPending / Rejected /\nSuspended screen", fc=LIGHT_AMBER, ec=AMBER, fontsize=7.0)
    arrow(ax, 68, d4y + 6, 71, d4y + 5, text="No")
    y = d4y + 12 + 5
    box(ax, 15, y, 60, 7, "Cache session (uid, role, name);\nnavigate to role-specific dashboard", fc=LIGHT_GREEN, ec=GREEN); y += 10.5
    ax.add_patch(Circle((45, y + 1.5), 1.6, fc="white", ec="black", lw=1.6))
    ax.add_patch(Circle((45, y + 1.5), 0.8, fc="black"))
    ax.text(45, y + 4.5, "End", ha="center", fontsize=8, fontweight="bold")

    ax.set_ylim(0, y + 7)
    ax.invert_yaxis()
    save(fig, "fig_5_1_flowchart_login.png")


def fig_flowchart_quiz_grading():
    fig, ax = new_fig(10, 11)
    ax.add_patch(Circle((45, 2), 1.6, fc="black"))
    ax.text(45, 5, "Start", ha="center", fontsize=8, fontweight="bold")
    y = 7
    box(ax, 12, y, 66, 6, "Student attempts quiz/exam and\nselects an option for each question"); y += 9.5
    box(ax, 12, y, 66, 6, "Student submits the attempt\nbefore the time limit expires"); y += 9.5
    box(ax, 12, y, 66, 7, "System initialises score = 0 and\niterates through every QuizQuestion", fc=LIGHT_AMBER, ec=AMBER); y += 10.5
    dy = y
    diamond(ax, 45, dy + 6, 50, 12, "selectedOption ==\ncorrectOptionIndex?")
    arrow(ax, 45, dy + 12, 45, dy + 12 + 5, text="Yes")
    box(ax, 70, dy + 1, 24, 8, "Move to next\nquestion", fc="white", ec=GRAY, fontsize=7.2)
    arrow(ax, 66, dy + 6, 71, dy + 5, text="No")
    y = dy + 12 + 5
    box(ax, 12, y, 66, 6, "score += question.marks"); y += 9.5
    d2 = y
    diamond(ax, 45, d2 + 6, 44, 12, "More questions\nremaining?")
    arrow(ax, 24, d2 + 6, 12, d2 + 6, text="Yes — loop back", style="-|>", connectionstyle="arc3,rad=-0.6")
    y = d2 + 12 + 5
    box(ax, 12, y, 66, 6, "Persist QuizAttempt with\nscore and totalMarks", fc=LIGHT_GREEN, ec=GREEN); y += 9.5
    d3 = y
    diamond(ax, 45, d3 + 6, 46, 12, "type == EXAM?")
    box(ax, 70, d3 + 1, 24, 9, "Teacher may set\nmanualScore &\nfeedback later", fc=LIGHT_PURPLE, ec=PURPLE, fontsize=7.0)
    arrow(ax, 68, d3 + 6, 71, d3 + 5, text="Yes")
    y = d3 + 12 + 5
    box(ax, 12, y, 66, 6, "effectiveScore = manualScore ?: score\nis shown to the student", fc=LIGHT_BLUE, ec=BLUE); y += 9.5
    ax.add_patch(Circle((45, y + 1.5), 1.6, fc="white", ec="black", lw=1.6))
    ax.add_patch(Circle((45, y + 1.5), 0.8, fc="black"))
    ax.text(45, y + 4.5, "End", ha="center", fontsize=8, fontweight="bold")

    ax.set_ylim(0, y + 7)
    ax.invert_yaxis()
    save(fig, "fig_5_2_flowchart_quiz_grading.png")


# ---------------------------------------------------------------------------
# Screenshot placeholders (clearly labelled — not fabricated screenshots)
# ---------------------------------------------------------------------------
def screenshot_placeholder(filename, label, w=6.4, h=11.5):
    fig, ax = plt.subplots(figsize=(w, h), dpi=150)
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 18)
    ax.axis("off")
    p = FancyBboxPatch((0.4, 0.4), 9.2, 17.2, boxstyle="round,pad=0.02,rounding_size=0.3",
                        fc="#f4f5f7", ec="#9aa0a8", lw=1.6, linestyle="dashed")
    ax.add_patch(p)
    ax.text(5, 9.6, "SCREENSHOT PLACEHOLDER", ha="center", va="center", fontsize=13, fontweight="bold", color="#6b7280")
    ax.text(5, 8.6, label, ha="center", va="center", fontsize=10.5, color="#4b5563", wrap=True)
    ax.text(5, 1.3, "Insert an actual device / emulator screenshot here before final submission.",
            ha="center", va="center", fontsize=7.6, color="#9aa0a8", style="italic")
    fig.savefig(os.path.join(OUT, filename), bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("saved", filename)


def gen_screenshot_placeholders():
    shots = [
        ("shot_login.png", "Login Screen\n(animated role gradient, glassmorphism card)"),
        ("shot_register.png", "Registration Screen\n(sectioned form, HOD/Teacher/Student roles)"),
        ("shot_admin_users.png", "Admin Panel - Hierarchical User\nManagement (All / HOD / Teacher / Student tabs)"),
        ("shot_admin_reports.png", "Admin Panel - Reports & Analytics"),
        ("shot_hod_dashboard.png", "HOD Dashboard - Department Overview"),
        ("shot_teacher_attendance.png", "Teacher - Mark Attendance Screen"),
        ("shot_teacher_assignment.png", "Teacher - Assignment Monitor /\nGrading Screen"),
        ("shot_student_dashboard.png", "Student Dashboard - Subject List"),
        ("shot_student_quiz.png", "Student - Take Quiz / Exam Screen"),
        ("shot_profile.png", "Profile Screen - Self-service profile\n& theme toggle (shared by all roles)"),
    ]
    for fname, label in shots:
        screenshot_placeholder(fname, label)


if __name__ == "__main__":
    fig_architecture()
    fig_usecase_admin_hod()
    fig_usecase_teacher_student()
    fig_er_diagram()
    fig_class_diagram()
    fig_dfd_level0()
    fig_dfd_level1()
    fig_activity_registration()
    fig_activity_assignment()
    fig_sequence_login()
    fig_sequence_assignment()
    fig_flowchart_login()
    fig_flowchart_quiz_grading()
    gen_screenshot_placeholders()
    print("All diagrams generated.")
