package com.example.uos_lms.core.domain.model

import kotlin.math.round

/**
 * HEC (Pakistan) style absolute grading scale — the only grading-scale
 * definition in the app. Bands are checked top-down against percentage, so
 * order matters (highest minPercent first).
 */
object GradeScale {
    private data class Band(val minPercent: Double, val letter: String, val gpaPoint: Double)

    private val bands = listOf(
        Band(85.0, "A", 4.00),
        Band(80.0, "A-", 3.66),
        Band(75.0, "B+", 3.33),
        Band(71.0, "B", 3.00),
        Band(68.0, "B-", 2.66),
        Band(64.0, "C+", 2.33),
        Band(61.0, "C", 2.00),
        Band(58.0, "C-", 1.66),
        Band(54.0, "D+", 1.33),
        Band(50.0, "D", 1.00),
        Band(0.0, "F", 0.00),
    )

    fun letterFor(percentage: Double): String = bands.first { percentage >= it.minPercent }.letter

    fun gpaPointFor(percentage: Double): Double = bands.first { percentage >= it.minPercent }.gpaPoint
}

data class SemesterGpa(
    val semesterId: String,
    val semesterLabel: String,
    val semesterNumber: Int,
    val gpa: Double,
    val creditHours: Int,
    val results: List<ExamResult>,
)

data class CgpaSummary(
    val cgpa: Double,
    val totalCreditHours: Int,
    val semesters: List<SemesterGpa>,
)

/**
 * Weighted by credit hours, HEC-style: semester GPA and CGPA are both
 * sum(gpaPoint * creditHours) / sum(creditHours) — over that semester's
 * APPROVED results, and over all APPROVED results respectively. Only
 * APPROVED results count; DRAFT/PENDING_APPROVAL/REJECTED never affect GPA.
 */
fun computeCgpaSummary(results: List<ExamResult>, semesters: Map<String, Semester>): CgpaSummary {
    val approved = results.filter { it.status == ResultStatus.APPROVED && it.gpaPoint != null }

    val semesterGpas = approved
        .groupBy { it.semesterId }
        .map { (semesterId, semesterResults) ->
            val totalCredits = semesterResults.sumOf { it.creditHours }
            val weightedSum = semesterResults.sumOf { it.gpaPoint!! * it.creditHours }
            val semester = semesters[semesterId]
            SemesterGpa(
                semesterId = semesterId,
                semesterLabel = semester?.displayName ?: "Semester",
                semesterNumber = semester?.number ?: 0,
                gpa = if (totalCredits == 0) 0.0 else roundTo2(weightedSum / totalCredits),
                creditHours = totalCredits,
                results = semesterResults.sortedBy { it.subjectCode },
            )
        }
        .sortedBy { it.semesterNumber }

    val totalCredits = approved.sumOf { it.creditHours }
    val weightedSum = approved.sumOf { it.gpaPoint!! * it.creditHours }
    val cgpa = if (totalCredits == 0) 0.0 else roundTo2(weightedSum / totalCredits)

    return CgpaSummary(cgpa = cgpa, totalCreditHours = totalCredits, semesters = semesterGpas)
}

private fun roundTo2(value: Double): Double = round(value * 100) / 100
