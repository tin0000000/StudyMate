package com.st10457568.studymate.core

import com.st10457568.studymate.model.Assignment
import com.st10457568.studymate.model.GradeRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object StudyRules {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun isValidEmail(email: String): Boolean {
        return email.trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
    }

    fun isStrongEnoughPassword(password: String): Boolean {
        return password.length >= 6 && password.any(Char::isDigit) && password.any(Char::isLetter)
    }

    fun isValidIsoDate(value: String): Boolean {
        return try {
            LocalDate.parse(value.trim(), dateFormatter)
            true
        } catch (_: DateTimeParseException) {
            false
        }
    }

    fun validateAssignment(title: String, subjectId: String, dueDate: String): List<String> {
        val errors = mutableListOf<String>()
        if (title.trim().isEmpty()) errors += "Title is required"
        if (subjectId.trim().isEmpty()) errors += "Subject is required"
        if (!isValidIsoDate(dueDate)) errors += "Due date must use YYYY-MM-DD"
        return errors
    }

    fun sortAssignmentsByDueDate(assignments: List<Assignment>): List<Assignment> {
        return assignments.sortedWith(
            compareBy<Assignment> { LocalDate.parse(it.dueDate, dateFormatter) }
                .thenByDescending { it.priority.ordinal }
                .thenBy { it.title.lowercase() }
        )
    }

    fun averageForSubject(grades: List<GradeRecord>, subjectId: String): Double {
        val percentages = grades
            .filter { it.subjectId == subjectId && it.total > 0.0 }
            .map { (it.mark / it.total) * 100.0 }

        if (percentages.isEmpty()) return 0.0
        return percentages.average()
    }

    fun weeklyProgress(assignments: List<Assignment>): Int {
        if (assignments.isEmpty()) return 0
        val completed = assignments.count { it.completed }
        return ((completed.toDouble() / assignments.size.toDouble()) * 100.0).toInt()
    }
}
