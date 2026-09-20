package com.st10457568.studymate.data

import android.content.Context
import com.st10457568.studymate.core.StudyRules
import com.st10457568.studymate.model.Assignment
import com.st10457568.studymate.model.GradeRecord
import com.st10457568.studymate.model.Priority
import com.st10457568.studymate.model.Subject
import com.st10457568.studymate.model.TimetableEntry
import com.st10457568.studymate.model.UserAccount
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LocalStudyRepository(context: Context) {
    private val prefs = context.getSharedPreferences("studymate_store", Context.MODE_PRIVATE)

    var currentUserId: String?
        get() = prefs.getString(KEY_CURRENT_USER, null)
        private set(value) {
            prefs.edit().putString(KEY_CURRENT_USER, value).apply()
        }

    fun register(email: String, displayName: String, password: String): Result<UserAccount> {
        val cleanedEmail = email.trim().lowercase()
        if (!StudyRules.isValidEmail(cleanedEmail)) return Result.failure(IllegalArgumentException("Enter a valid email address."))
        if (!StudyRules.isStrongEnoughPassword(password)) return Result.failure(IllegalArgumentException("Password needs 6+ characters with letters and numbers."))
        if (readUsers().any { it.email == cleanedEmail }) return Result.failure(IllegalArgumentException("This email is already registered."))

        val salt = SecurityUtils.newSalt()
        val user = UserAccount(
            userId = UUID.randomUUID().toString(),
            email = cleanedEmail,
            displayName = displayName.trim().ifEmpty { "Student" },
            passwordHash = SecurityUtils.hashPassword(password, salt),
            salt = salt
        )
        writeUsers(readUsers() + user)
        currentUserId = user.userId
        seedIfEmpty(user.userId)
        return Result.success(user)
    }

    fun login(email: String, password: String): Result<UserAccount> {
        val user = readUsers().firstOrNull { it.email == email.trim().lowercase() }
            ?: return Result.failure(IllegalArgumentException("No account found for this email."))
        if (!SecurityUtils.matches(password, user.salt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Password does not match."))
        }
        currentUserId = user.userId
        seedIfEmpty(user.userId)
        return Result.success(user)
    }

    fun logout() {
        currentUserId = null
    }

    fun currentUser(): UserAccount? {
        val id = currentUserId ?: return null
        return readUsers().firstOrNull { it.userId == id }
    }

    fun notificationEnabled(): Boolean = prefs.getBoolean(key("notifications"), true)

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(key("notifications"), enabled).apply()
    }

    fun subjects(): List<Subject> = readArray(key("subjects")).map {
        Subject(
            subjectId = it.getString("subjectId"),
            name = it.getString("name"),
            color = it.optString("color", "#2166A5")
        )
    }

    fun saveSubject(name: String, color: String = "#2166A5"): Result<Subject> {
        if (name.trim().isEmpty()) return Result.failure(IllegalArgumentException("Subject name is required."))
        val subject = Subject(UUID.randomUUID().toString(), name.trim(), color)
        writeArray(key("subjects"), subjects().map(::subjectToJson) + subjectToJson(subject))
        return Result.success(subject)
    }

    fun deleteSubject(subjectId: String) {
        writeArray(key("subjects"), subjects().filterNot { it.subjectId == subjectId }.map(::subjectToJson))
        writeArray(key("assignments"), assignments().filterNot { it.subjectId == subjectId }.map(::assignmentToJson))
        writeArray(key("timetable"), timetable().filterNot { it.subjectId == subjectId }.map(::timetableToJson))
        writeArray(key("grades"), grades().filterNot { it.subjectId == subjectId }.map(::gradeToJson))
    }

    fun assignments(): List<Assignment> = readArray(key("assignments")).map {
        Assignment(
            assignmentId = it.getString("assignmentId"),
            subjectId = it.getString("subjectId"),
            title = it.getString("title"),
            description = it.optString("description"),
            dueDate = it.getString("dueDate"),
            priority = Priority.valueOf(it.getString("priority")),
            completed = it.optBoolean("completed"),
            reminderEnabled = it.optBoolean("reminderEnabled", true)
        )
    }

    fun saveAssignment(subjectId: String, title: String, description: String, dueDate: String, priority: Priority, reminderEnabled: Boolean): Result<Assignment> {
        val errors = StudyRules.validateAssignment(title, subjectId, dueDate)
        if (errors.isNotEmpty()) return Result.failure(IllegalArgumentException(errors.joinToString("\n")))
        val assignment = Assignment(UUID.randomUUID().toString(), subjectId, title.trim(), description.trim(), dueDate.trim(), priority, false, reminderEnabled)
        writeArray(key("assignments"), assignments().map(::assignmentToJson) + assignmentToJson(assignment))
        return Result.success(assignment)
    }

    fun toggleAssignment(assignmentId: String) {
        val updated = assignments().map {
            if (it.assignmentId == assignmentId) it.copy(completed = !it.completed) else it
        }
        writeArray(key("assignments"), updated.map(::assignmentToJson))
    }

    fun deleteAssignment(assignmentId: String) {
        writeArray(key("assignments"), assignments().filterNot { it.assignmentId == assignmentId }.map(::assignmentToJson))
    }

    fun timetable(): List<TimetableEntry> = readArray(key("timetable")).map {
        TimetableEntry(
            entryId = it.getString("entryId"),
            subjectId = it.getString("subjectId"),
            dayOfWeek = it.getString("dayOfWeek"),
            startTime = it.getString("startTime"),
            endTime = it.getString("endTime"),
            location = it.optString("location")
        )
    }

    fun saveTimetable(subjectId: String, day: String, start: String, end: String, location: String): Result<TimetableEntry> {
        if (subjectId.trim().isEmpty() || day.trim().isEmpty() || start.trim().isEmpty() || end.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Subject, day, start, and end time are required."))
        }
        val entry = TimetableEntry(UUID.randomUUID().toString(), subjectId, day.trim(), start.trim(), end.trim(), location.trim())
        writeArray(key("timetable"), timetable().map(::timetableToJson) + timetableToJson(entry))
        return Result.success(entry)
    }

    fun deleteTimetable(entryId: String) {
        writeArray(key("timetable"), timetable().filterNot { it.entryId == entryId }.map(::timetableToJson))
    }

    fun grades(): List<GradeRecord> = readArray(key("grades")).map {
        GradeRecord(
            gradeId = it.getString("gradeId"),
            subjectId = it.getString("subjectId"),
            assessmentName = it.getString("assessmentName"),
            mark = it.getDouble("mark"),
            total = it.getDouble("total"),
            date = it.getString("date")
        )
    }

    fun saveGrade(subjectId: String, name: String, mark: Double, total: Double, date: String): Result<GradeRecord> {
        if (subjectId.trim().isEmpty() || name.trim().isEmpty()) return Result.failure(IllegalArgumentException("Subject and assessment name are required."))
        if (mark < 0.0 || total <= 0.0 || mark > total) return Result.failure(IllegalArgumentException("Mark must be between 0 and total."))
        if (!StudyRules.isValidIsoDate(date)) return Result.failure(IllegalArgumentException("Date must use YYYY-MM-DD."))
        val grade = GradeRecord(UUID.randomUUID().toString(), subjectId, name.trim(), mark, total, date.trim())
        writeArray(key("grades"), grades().map(::gradeToJson) + gradeToJson(grade))
        return Result.success(grade)
    }

    private fun seedIfEmpty(userId: String) {
        if (subjects().isNotEmpty()) return
        val maths = Subject("mathematics-$userId", "Mathematics", "#2166A5")
        val opsc = Subject("opsc-$userId", "OPSC", "#1B8A5A")
        writeArray(key("subjects"), listOf(maths, opsc).map(::subjectToJson))
        writeArray(
            key("assignments"),
            listOf(
                Assignment(UUID.randomUUID().toString(), opsc.subjectId, "Part 2 prototype video", "Record login, settings, API, and app features.", "2026-11-03", Priority.HIGH, false, true),
                Assignment(UUID.randomUUID().toString(), maths.subjectId, "Quiz revision", "Revise averages and graphs.", "2026-10-15", Priority.MEDIUM, true, true)
            ).map(::assignmentToJson)
        )
        writeArray(
            key("timetable"),
            listOf(
                TimetableEntry(UUID.randomUUID().toString(), opsc.subjectId, "Monday", "09:00", "10:30", "Lab 2"),
                TimetableEntry(UUID.randomUUID().toString(), maths.subjectId, "Wednesday", "11:00", "12:00", "Room 14")
            ).map(::timetableToJson)
        )
        writeArray(
            key("grades"),
            listOf(
                GradeRecord(UUID.randomUUID().toString(), opsc.subjectId, "Research report", 82.0, 100.0, "2026-09-03"),
                GradeRecord(UUID.randomUUID().toString(), opsc.subjectId, "Planning report", 76.0, 100.0, "2026-09-03")
            ).map(::gradeToJson)
        )
    }

    private fun readUsers(): List<UserAccount> = readArray(KEY_USERS).map {
        UserAccount(
            userId = it.getString("userId"),
            email = it.getString("email"),
            displayName = it.getString("displayName"),
            passwordHash = it.getString("passwordHash"),
            salt = it.getString("salt")
        )
    }

    private fun writeUsers(users: List<UserAccount>) {
        writeArray(KEY_USERS, users.map {
            JSONObject()
                .put("userId", it.userId)
                .put("email", it.email)
                .put("displayName", it.displayName)
                .put("passwordHash", it.passwordHash)
                .put("salt", it.salt)
        })
    }

    private fun subjectToJson(value: Subject) = JSONObject()
        .put("subjectId", value.subjectId)
        .put("name", value.name)
        .put("color", value.color)

    private fun assignmentToJson(value: Assignment) = JSONObject()
        .put("assignmentId", value.assignmentId)
        .put("subjectId", value.subjectId)
        .put("title", value.title)
        .put("description", value.description)
        .put("dueDate", value.dueDate)
        .put("priority", value.priority.name)
        .put("completed", value.completed)
        .put("reminderEnabled", value.reminderEnabled)

    private fun timetableToJson(value: TimetableEntry) = JSONObject()
        .put("entryId", value.entryId)
        .put("subjectId", value.subjectId)
        .put("dayOfWeek", value.dayOfWeek)
        .put("startTime", value.startTime)
        .put("endTime", value.endTime)
        .put("location", value.location)

    private fun gradeToJson(value: GradeRecord) = JSONObject()
        .put("gradeId", value.gradeId)
        .put("subjectId", value.subjectId)
        .put("assessmentName", value.assessmentName)
        .put("mark", value.mark)
        .put("total", value.total)
        .put("date", value.date)

    private fun readArray(prefKey: String): List<JSONObject> {
        val raw = prefs.getString(prefKey, "[]") ?: "[]"
        val array = JSONArray(raw)
        return (0 until array.length()).map { array.getJSONObject(it) }
    }

    private fun writeArray(prefKey: String, values: List<JSONObject>) {
        val array = JSONArray()
        values.forEach(array::put)
        prefs.edit().putString(prefKey, array.toString()).apply()
    }

    private fun key(name: String): String = "${currentUserId ?: "guest"}_$name"

    companion object {
        private const val KEY_USERS = "users"
        private const val KEY_CURRENT_USER = "current_user"
    }
}
