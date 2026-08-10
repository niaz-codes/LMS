# Builds Documentation/UOS_LMS_Thesis.docx from the actual UOS_LMS implementation.
# Documentation-only artifact: this script does not read, generate, or modify any
# application source code beyond what was already inspected manually to write accurate content.

import os
from docx import Document
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

from docx_helpers import (
    add_bookmark, add_field, add_pageref, add_toc_field, add_dot_leader_entry,
    set_page_numbers, restart_page_numbers, shade_cell, set_col_widths,
)

HERE = os.path.dirname(__file__)
DIAG = os.path.join(HERE, "..", "diagrams")
OUT = os.path.join(HERE, "..", "UOS_LMS_Thesis.docx")

NAVY = RGBColor(0x1A, 0x2B, 0x4C)
BLUE = RGBColor(0x22, 0x55, 0xAA)
GRAY = RGBColor(0x44, 0x44, 0x44)

FIGURES = []   # (bookmark, caption) in document order
TABLES = []    # (bookmark, caption) in document order

doc = Document()

# ---------------------------------------------------------------------------
# Base styles
# ---------------------------------------------------------------------------
normal = doc.styles["Normal"]
normal.font.name = "Times New Roman"
normal.font.size = Pt(12)
normal.paragraph_format.line_spacing_rule = WD_LINE_SPACING.ONE_POINT_FIVE
normal.paragraph_format.space_after = Pt(8)

for lvl, size, color in [(1, 18, NAVY), (2, 15, NAVY), (3, 13, GRAY)]:
    st = doc.styles[f"Heading {lvl}"]
    st.font.name = "Times New Roman"
    st.font.size = Pt(size)
    st.font.bold = True
    st.font.color.rgb = color
    st.font.italic = False
    st.paragraph_format.space_before = Pt(18 if lvl == 1 else 12)
    st.paragraph_format.space_after = Pt(10 if lvl == 1 else 6)
    st.paragraph_format.keep_with_next = True

title_style = doc.styles.add_style("ThesisTitle", 1)
title_style.base_style = doc.styles["Normal"]
title_style.font.size = Pt(22)
title_style.font.bold = True
title_style.font.name = "Times New Roman"

caption_style = doc.styles.add_style("FigCaption", 1)
caption_style.base_style = doc.styles["Normal"]
caption_style.font.size = Pt(10.5)
caption_style.font.italic = True
caption_style.font.name = "Times New Roman"
caption_style.paragraph_format.space_after = Pt(14)
caption_style.paragraph_format.space_before = Pt(4)

section = doc.sections[0]
section.left_margin = Inches(1.25)
section.right_margin = Inches(1.0)
section.top_margin = Inches(1.0)
section.bottom_margin = Inches(1.0)


# ---------------------------------------------------------------------------
# Content helpers
# ---------------------------------------------------------------------------
def h1(text, numbered=True):
    p = doc.add_heading(text, level=1)
    return p


def h2(text):
    return doc.add_heading(text, level=2)


def h3(text):
    return doc.add_heading(text, level=3)


def p(text="", *, italic=False, bold=False, align=None, size=None, space_after=None, style=None):
    para = doc.add_paragraph(style=style)
    run = para.add_run(text)
    run.italic = italic
    run.bold = bold
    if size:
        run.font.size = Pt(size)
    if align is not None:
        para.alignment = align
    if space_after is not None:
        para.paragraph_format.space_after = Pt(space_after)
    return para


def bullet(text, style="List Bullet"):
    return doc.add_paragraph(text, style=style)


def numbered(text, style="List Number"):
    return doc.add_paragraph(text, style=style)


def center(text, size=12, bold=False, italic=False, space_after=6):
    return p(text, align=WD_ALIGN_PARAGRAPH.CENTER, size=size, bold=bold, italic=italic, space_after=space_after)


def page_break():
    doc.add_page_break()


def add_figure(image_path, caption_text, bookmark, width=6.0):
    img_p = doc.add_paragraph()
    img_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = img_p.add_run()
    run.add_picture(os.path.join(DIAG, image_path), width=Inches(width))
    cap = doc.add_paragraph(style="FigCaption")
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    add_bookmark(cap, bookmark)
    cap.add_run(caption_text)
    FIGURES.append((bookmark, caption_text))
    return cap


def add_table_caption(caption_text, bookmark):
    cap = doc.add_paragraph(style="FigCaption")
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    add_bookmark(cap, bookmark)
    cap.add_run(caption_text)
    TABLES.append((bookmark, caption_text))
    return cap


def add_table(headers, rows, widths=None, header_fill="1A2B4C"):
    t = doc.add_table(rows=1, cols=len(headers))
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr = t.rows[0].cells
    for i, htext in enumerate(headers):
        hdr[i].text = ""
        para = hdr[i].paragraphs[0]
        run = para.add_run(htext)
        run.bold = True
        run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        run.font.size = Pt(10.5)
        shade_cell(hdr[i], header_fill)
    for row_data in rows:
        cells = t.add_row().cells
        for i, val in enumerate(row_data):
            cells[i].text = ""
            para = cells[i].paragraphs[0]
            run = para.add_run(str(val))
            run.font.size = Pt(10)
    if widths:
        set_col_widths(t, widths)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)
    return t


# ===========================================================================
# FRONT MATTER
# ===========================================================================

# ---- Title Page -----------------------------------------------------------
for _ in range(3):
    p("")
center("UNIVERSITY OF SHANGLA", size=16, bold=True, space_after=2)
center("Department of Computer Science", size=13, space_after=2)
center("[ Faculty of Computing / Faculty Name Placeholder ]", size=11, italic=True, space_after=30)

center("UOS LMS", size=30, bold=True, space_after=2)
center("A Role-Based Mobile Learning Management System for", size=14, space_after=0)
center("the University of Shangla", size=14, space_after=30)

center("Final Year Project Thesis", size=14, bold=True, space_after=2)
center("submitted in partial fulfilment of the requirements for the degree of", size=11, italic=True, space_after=2)
center("BS Computer Science", size=13, bold=True, space_after=30)

center("Submitted by", size=11, space_after=2)
center("[ Student Full Name ]                Reg. No: [ Registration Number ]", size=12, bold=True, space_after=30)

center("Supervised by", size=11, space_after=2)
center("[ Supervisor Name ], [ Designation ]", size=12, bold=True, space_after=40)

center("[ Month, Year ]", size=12, space_after=0)
page_break()

# ---- Certificate of Approval ----------------------------------------------
h1("Certificate of Approval", numbered=False)
p("It is certified that the Final Year Project report titled “UOS LMS – A Role-Based Mobile "
  "Learning Management System for the University of Shangla”, submitted by [ Student Full Name ], "
  "Registration No. [ Registration Number ], has been carried out under our supervision and is "
  "found satisfactory in scope and quality for the partial fulfilment of the degree of BS Computer "
  "Science.")
for _ in range(2):
    p("")
p("Supervisor: ______________________________        Signature: ____________________")
p("[ Supervisor Name ]")
p("[ Designation, Department of Computer Science ]")
for _ in range(2):
    p("")
p("External Examiner: ________________________        Signature: ____________________")
for _ in range(2):
    p("")
p("Head of Department: ______________________        Signature: ____________________")
p("Department of Computer Science, University of Shangla")
page_break()

# ---- Declaration ------------------------------------------------------------
h1("Declaration", numbered=False)
p("I hereby declare that the work presented in this Final Year Project report, titled “UOS LMS – A "
  "Role-Based Mobile Learning Management System for the University of Shangla”, is the result of my "
  "own effort and has not been submitted elsewhere for the award of any other degree or qualification. "
  "All sources of information used in this report have been duly acknowledged in the References "
  "section.")
for _ in range(3):
    p("")
p("Student Name: [ Student Full Name ]")
p("Registration No: [ Registration Number ]")
p("Signature: ____________________          Date: ____________________")
page_break()

# ---- Acknowledgement --------------------------------------------------------
h1("Acknowledgement", numbered=False)
p("All praise and gratitude are due first and foremost to Almighty Allah, for granting me the "
  "strength, patience and ability to complete this Final Year Project.")
p("I would like to express my sincere gratitude to my supervisor, [ Supervisor Name ], for the "
  "invaluable guidance, encouragement, and constructive feedback provided throughout every stage of "
  "this project — from the initial requirement analysis to the final testing and documentation.")
p("I am also thankful to the faculty members of the Department of Computer Science, University of "
  "Shangla, for the knowledge and skills imparted throughout the course of my degree, which formed "
  "the foundation for the design and development of this system.")
p("Finally, I extend my heartfelt appreciation to my family and friends for their continuous support, "
  "patience, and motivation, without which the successful completion of this project would not have "
  "been possible.")
page_break()

# ---- Abstract ---------------------------------------------------------------
h1("Abstract", numbered=False)
p("Universities in remote and under-resourced regions often continue to depend on manual, paper-based "
  "processes for day-to-day academic administration — attendance registers, hand-graded assignments, "
  "notice boards for announcements, and spreadsheets for result compilation. These processes are slow, "
  "error-prone, difficult to audit, and inaccessible outside office hours, and they scale poorly as "
  "student enrolment grows. This project presents UOS LMS, a native Android Learning Management "
  "System built for the University of Shangla that digitises the complete academic workflow across "
  "four distinct user roles: Administrator, Head of Department (HOD), Teacher, and Student.")
p("The system is built with Kotlin and Jetpack Compose following the Model-View-ViewModel (MVVM) "
  "architectural pattern, with Dagger Hilt providing compile-time dependency injection across a "
  "modular, feature-based package structure. Cloud Firestore serves as the primary NoSQL data store "
  "and Firebase Authentication handles credential verification, while Cloudinary provides a dedicated, "
  "signed media pipeline for profile photographs, assignment attachments, and study materials — "
  "keeping large binary files out of the database and storing only URLs and metadata. A single "
  "unified login screen enforces strict role-based access: a user must select the correct role for "
  "their account before signing in, and the server-side repository actively rejects and signs out any "
  "session where the authenticated account's role does not match the role selected at login, closing "
  "a common class of privilege-confusion vulnerability found in simpler LMS implementations.")
p("Functionally, the system covers hierarchical university-structure management (departments, "
  "semesters, sessions and subjects), an approval-gated registration workflow, department-scoped "
  "teacher-subject assignment, attendance capture and reporting, assignment submission with file "
  "upload and manual grading, a unified quiz/exam engine with automatic multiple-choice grading and "
  "optional manual override for exams, study-material distribution, scoped announcements, an academic "
  "calendar, and role-appropriate analytics dashboards. The user interface follows Material Design 3 "
  "with a consistent, animated, glassmorphism-influenced design language applied uniformly across all "
  "four roles.")
p("The resulting application was implemented, unit- and manually-tested, and deployed to a physical "
  "Android device for verification. This report documents the complete software development "
  "lifecycle followed to build the system — requirement analysis, system design (including Use Case, "
  "ER, Class, Activity, Sequence and Data Flow diagrams), implementation, and testing — and concludes "
  "with a discussion of the system's limitations and recommended directions for future work, including "
  "push notifications, a full interactive timetable module, and offline-first data access.")
p("Keywords: Learning Management System, Android, Jetpack Compose, Firebase, Firestore, MVVM, "
  "Role-Based Access Control, Mobile Application Development.", italic=True)
page_break()

# ---- Table of Contents -------------------------------------------------------
h1("Table of Contents", numbered=False)
p("The Table of Contents below is generated from this document's heading styles. If it appears empty "
  "or out of date, select the field and press F9 (or right-click and choose “Update Field”) in "
  "Microsoft Word.", italic=True, size=10)
add_toc_field(doc)
page_break()

LOF_PLACEHOLDER = doc.add_paragraph()   # filled in at the very end, once FIGURES is fully known
h1_lof = h1("List of Figures", numbered=False)
LOF_ANCHOR = doc.paragraphs[-1]
page_break()

h1("List of Tables", numbered=False)
LOT_ANCHOR = doc.paragraphs[-1]
page_break()

h1("List of Abbreviations", numbered=False)
abbrev_rows = [
    ("LMS", "Learning Management System"),
    ("UOS", "University of Shangla"),
    ("HOD", "Head of Department"),
    ("MVVM", "Model-View-ViewModel"),
    ("UI / UX", "User Interface / User Experience"),
    ("API", "Application Programming Interface"),
    ("SDK", "Software Development Kit"),
    ("DI", "Dependency Injection"),
    ("CRUD", "Create, Read, Update, Delete"),
    ("ER Diagram", "Entity Relationship Diagram"),
    ("DFD", "Data Flow Diagram"),
    ("UML", "Unified Modelling Language"),
    ("MCQ", "Multiple Choice Question"),
    ("CDN", "Content Delivery Network"),
    ("REST", "Representational State Transfer"),
    ("HMAC", "Hash-based Message Authentication Code"),
    ("BaaS", "Backend as a Service"),
    ("PK / FK", "Primary Key / Foreign Key"),
    ("APK", "Android Package (application bundle)"),
    ("IDE", "Integrated Development Environment"),
]
add_table(["Abbreviation", "Description"], abbrev_rows, widths=[1.5, 4.8])
page_break()

print("Front matter written. Continuing with chapters in build_thesis_chapters.py content...")

# The remainder of the document (Chapters 1-8, References, Appendices) is appended
# by the chapter-writing functions imported below, which operate on the same `doc`.
import build_thesis_chapters as chapters  # noqa: E402
chapters.write_all(
    doc=doc, h1=h1, h2=h2, h3=h3, p=p, bullet=bullet, numbered=numbered, center=center,
    page_break=page_break, add_figure=add_figure, add_table=add_table,
    add_table_caption=add_table_caption, DIAG=DIAG,
)

# ===========================================================================
# Finalise: page numbers + retro-fill List of Figures / List of Tables
# ===========================================================================
set_page_numbers(section)

# Rebuild the List of Figures / List of Tables paragraphs now that FIGURES/TABLES are fully known.
def insert_dot_leader_list(anchor_paragraph, entries, empty_message):
    anchor_el = anchor_paragraph._p
    if not entries:
        np = doc.add_paragraph(empty_message)
        anchor_el.addnext(np._p)
        return
    prev_el = anchor_el
    for bookmark, caption in entries:
        np = doc.add_paragraph()
        tab_stops = np.paragraph_format.tab_stops
        from docx.enum.text import WD_TAB_ALIGNMENT, WD_TAB_LEADER
        tab_stops.add_tab_stop(Inches(6.3), WD_TAB_ALIGNMENT.RIGHT, WD_TAB_LEADER.DOTS)
        np.add_run(caption + "\t")
        add_pageref(np, bookmark)
        prev_el.addnext(np._p)
        prev_el = np._p


insert_dot_leader_list(LOF_ANCHOR, FIGURES, "No figures.")
insert_dot_leader_list(LOT_ANCHOR, TABLES, "No tables.")

doc.paragraphs[0].text = doc.paragraphs[0].text  # no-op, keeps python-docx happy about empty first para

doc.save(OUT)
print("Saved thesis to", OUT)
