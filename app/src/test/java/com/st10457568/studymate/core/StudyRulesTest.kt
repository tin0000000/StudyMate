package com.st10457568.studymate.core

import com.st10457568.studymate.model.Assignment
import com.st10457568.studymate.model.GradeRecord
import com.st10457568.studymate.model.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyRulesTest {
    @Test
    fun validatesEmailAndPasswordInputs() {
        assertTrue(StudyRules.isValidEmail("student@example.com"))
        assertFalse(StudyRules.isValidEmail("student-at-example"))
        assertTrue(StudyRules.isStrongEnoughPassword("study1"))
        assertFalse(StudyRules.isStrongEnoughPassword("secret"))
    }

    @Test
    fun validatesAssignmentDateAndRequiredFields() {
        assertTrue(StudyRules.validateAssignment("Report", "OPSC", "2026-11-03").isEmpty())
        assertEquals(
            listOf("Title is required", "Subject is required", "Due date must use YYYY-MM-DD"),
            StudyRules.validateAssignment("", "", "03/11/2026")
        )
    }

    @Test
    fun sortsAssignmentsByDueDateThenPriority() {
        val assignments = listOf(
            assignment("b", "2026-11-03", Priority.LOW),
            assignment("c", "2026-10-01", Priority.MEDIUM),
            assignment("a", "2026-10-01", Priority.HIGH)
        )

        assertEquals(listOf("a", "c", "b"), StudyRules.sortAssignmentsByDueDate(assignments).map { it.title })
    }

    @Test
    fun calculatesAverageForSubject() {
        val grades = listOf(
            GradeRecord("1", "OPSC", "Report", 80.0, 100.0, "2026-09-03"),
            GradeRecord("2", "OPSC", "Prototype", 35.0, 50.0, "2026-11-03"),
            GradeRecord("3", "MATH", "Quiz", 10.0, 20.0, "2026-09-04")
        )

        assertEquals(75.0, StudyRules.averageForSubject(grades, "OPSC"), 0.001)
    }

    private fun assignment(title: String, dueDate: String, priority: Priority): Assignment {
        return Assignment(
            assignmentId = title,
            subjectId = "OPSC",
            title = title,
            description = "",
            dueDate = dueDate,
            priority = priority,
            completed = false,
            reminderEnabled = true
        )
    }
}
