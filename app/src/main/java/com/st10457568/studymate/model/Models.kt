package com.st10457568.studymate.model

data class UserAccount(
    val userId: String,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val salt: String
)

data class Subject(
    val subjectId: String,
    val name: String,
    val color: String
)

data class Assignment(
    val assignmentId: String,
    val subjectId: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: Priority,
    val completed: Boolean,
    val reminderEnabled: Boolean,
    val attachmentUrl: String = ""
)

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

data class TimetableEntry(
    val entryId: String,
    val subjectId: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val location: String
)

data class GradeRecord(
    val gradeId: String,
    val subjectId: String,
    val assessmentName: String,
    val mark: Double,
    val total: Double,
    val date: String
)

data class Quote(
    val text: String,
    val author: String
)
