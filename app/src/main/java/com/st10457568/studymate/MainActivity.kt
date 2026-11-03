package com.st10457568.studymate

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.st10457568.studymate.core.StudyRules
import com.st10457568.studymate.data.LocalStudyRepository
import com.st10457568.studymate.model.Priority
import com.st10457568.studymate.network.QuoteClient
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var repo: LocalStudyRepository
    private val quoteClient = QuoteClient()
    private val blue = Color.rgb(33, 102, 165)
    private val green = Color.rgb(27, 138, 90)
    private val mist = Color.rgb(244, 248, 250)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = LocalStudyRepository(this)
        if (repo.currentUser() == null) showAuth() else {
            repo.startCloudSync()
            showShell(Screen.DASHBOARD)
        }
    }

    private fun showAuth() {
        val root = vertical().apply {
            setPadding(32, 60, 32, 32)
            setBackgroundColor(mist)
        }

        root.addView(title("StudyMate", 32))
        root.addView(label("Student planner and progress tracker"))
        val name = input("Display name")
        val email = input("Email")
        val password = input("Password").apply { inputType = 0x00000081 }
        root.addView(name)
        root.addView(email)
        root.addView(password)

        val actions = horizontal()
        actions.addView(actionButton("Register") {
            repo.register(email.text.toString(), name.text.toString(), password.text.toString())
                .onSuccess { showShell(Screen.DASHBOARD) }
                .onFailure { toast(it.message ?: "Registration failed") }
        })
        actions.addView(actionButton("Log in") {
            repo.login(email.text.toString(), password.text.toString())
                .onSuccess { showShell(Screen.DASHBOARD) }
                .onFailure { toast(it.message ?: "Login failed") }
        })
        root.addView(actions)
        root.addView(label("Passwords are stored as salted PBKDF2 hashes in the demo fallback store. Firebase Auth is declared for the hosted build."))

        setContentView(scroll(root))
    }

    private fun showShell(selected: Screen) {
        val root = vertical().apply { setBackgroundColor(Color.WHITE) }
        val toolbar = horizontal().apply {
            setPadding(24, 24, 24, 12)
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(mist)
        }
        toolbar.addView(title(selected.label, 24), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        toolbar.addView(actionButton("Settings") { renderContent(root, Screen.SETTINGS) })
        root.addView(toolbar)

        val content = vertical().apply { tag = "content"; setPadding(24, 20, 24, 20) }
        root.addView(scroll(content), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        val nav = horizontal().apply {
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 16)
            setBackgroundColor(mist)
        }
        listOf(Screen.DASHBOARD, Screen.TASKS, Screen.TIMETABLE, Screen.PROGRESS).forEach { screen ->
            nav.addView(actionButton(screen.label) { showShell(screen) }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(nav)
        setContentView(root)
        renderContent(root, selected)
    }

    private fun renderContent(root: LinearLayout, screen: Screen) {
        val content = root.findViewWithTag<LinearLayout>("content")
        content.removeAllViews()
        when (screen) {
            Screen.DASHBOARD -> dashboard(content)
            Screen.TASKS -> tasks(content)
            Screen.TIMETABLE -> timetable(content)
            Screen.PROGRESS -> progress(content)
            Screen.SETTINGS -> settings(content)
        }
    }

    private fun dashboard(content: LinearLayout) {
        val user = repo.currentUser()
        content.addView(title("Hello, ${user?.displayName ?: "Student"}", 26))
        val quoteView = card("Loading daily quote...")
        content.addView(quoteView)
        thread {
            val quote = try { quoteClient.fetchDailyQuote() } catch (_: Exception) { QuoteClient.FALLBACK }
            runOnUiThread { quoteView.text = "\"${quote.text}\"\n- ${quote.author}" }
        }

        val assignments = StudyRules.sortAssignmentsByDueDate(repo.assignments()).filterNot { it.completed }.take(3)
        content.addView(section("Upcoming deadlines"))
        if (assignments.isEmpty()) content.addView(label("No urgent assignments."))
        assignments.forEach { content.addView(card("${it.title}\n${subjectName(it.subjectId)} • ${it.priority} • due ${it.dueDate}")) }

        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val todayItems = repo.timetable().filter { it.dayOfWeek.equals(today, ignoreCase = true) }
        content.addView(section("Today"))
        if (todayItems.isEmpty()) content.addView(label("No timetable entries for $today."))
        todayItems.forEach { content.addView(card("${subjectName(it.subjectId)}\n${it.startTime}-${it.endTime} • ${it.location}")) }

        content.addView(section("Weekly progress"))
        content.addView(card("${StudyRules.weeklyProgress(repo.assignments())}% of assignments completed"))
        content.addView(actionButton("Quick add assignment") { showShell(Screen.TASKS) })
    }

    private fun tasks(content: LinearLayout) {
        content.addView(section("Add assignment"))
        val subject = subjectSpinner()
        val title = input("Title")
        val desc = input("Description")
        val due = input("Due date YYYY-MM-DD")
        val priority = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, Priority.entries.map { it.name })
        }
        val reminder = CheckBox(this).apply { text = "Reminder enabled"; isChecked = true }
        listOf(subject, title, desc, due, priority, reminder).forEach(content::addView)
        content.addView(actionButton("Save assignment") {
            val subjectId = selectedSubjectId(subject)
            val selectedPriority = Priority.valueOf(priority.selectedItem as String)
            repo.saveAssignment(subjectId, title.text.toString(), desc.text.toString(), due.text.toString(), selectedPriority, reminder.isChecked)
                .onSuccess { showShell(Screen.TASKS) }
                .onFailure { toast(it.message ?: "Could not save assignment") }
        })

        content.addView(section("Assignments"))
        StudyRules.sortAssignmentsByDueDate(repo.assignments()).forEach { assignment ->
            val row = vertical()
            row.addView(card("${if (assignment.completed) "Done" else "Open"} • ${assignment.title}\n${subjectName(assignment.subjectId)} • ${assignment.priority} • ${assignment.dueDate}\n${assignment.description}"))
            val buttons = horizontal()
            buttons.addView(actionButton("Toggle") { repo.toggleAssignment(assignment.assignmentId); showShell(Screen.TASKS) })
            buttons.addView(actionButton("Delete") { repo.deleteAssignment(assignment.assignmentId); showShell(Screen.TASKS) })
            row.addView(buttons)
            content.addView(row)
        }
    }

    private fun timetable(content: LinearLayout) {
        content.addView(section("Add timetable entry"))
        val subject = subjectSpinner()
        val day = input("Day, e.g. Monday")
        val start = input("Start time, e.g. 09:00")
        val end = input("End time, e.g. 10:30")
        val location = input("Location")
        listOf(subject, day, start, end, location).forEach(content::addView)
        content.addView(actionButton("Save timetable entry") {
            repo.saveTimetable(selectedSubjectId(subject), day.text.toString(), start.text.toString(), end.text.toString(), location.text.toString())
                .onSuccess { showShell(Screen.TIMETABLE) }
                .onFailure { toast(it.message ?: "Could not save timetable entry") }
        })

        content.addView(section("Weekly timetable"))
        repo.timetable().sortedWith(compareBy({ it.dayOfWeek }, { it.startTime })).forEach {
            val row = horizontal()
            row.addView(card("${it.dayOfWeek} ${it.startTime}-${it.endTime}\n${subjectName(it.subjectId)} • ${it.location}"), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(actionButton("Delete") { repo.deleteTimetable(it.entryId); showShell(Screen.TIMETABLE) })
            content.addView(row)
        }
    }

    private fun progress(content: LinearLayout) {
        content.addView(section("Subjects"))
        val subjectName = input("Subject name")
        content.addView(subjectName)
        content.addView(actionButton("Add subject") {
            repo.saveSubject(subjectName.text.toString())
                .onSuccess { showShell(Screen.PROGRESS) }
                .onFailure { toast(it.message ?: "Could not save subject") }
        })
        repo.subjects().forEach { subject ->
            val average = StudyRules.averageForSubject(repo.grades(), subject.subjectId)
            val row = horizontal()
            row.addView(card("${subject.name}\nAverage: ${"%.1f".format(average)}%"), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(actionButton("Delete") { repo.deleteSubject(subject.subjectId); showShell(Screen.PROGRESS) })
            content.addView(row)
        }

        content.addView(section("Add mark"))
        val subject = subjectSpinner()
        val name = input("Assessment name")
        val mark = input("Mark")
        val total = input("Total")
        val date = input("Date YYYY-MM-DD")
        listOf(subject, name, mark, total, date).forEach(content::addView)
        content.addView(actionButton("Save mark") {
            repo.saveGrade(
                selectedSubjectId(subject),
                name.text.toString(),
                mark.text.toString().toDoubleOrNull() ?: -1.0,
                total.text.toString().toDoubleOrNull() ?: 0.0,
                date.text.toString()
            ).onSuccess { showShell(Screen.PROGRESS) }
                .onFailure { toast(it.message ?: "Could not save mark") }
        })

        content.addView(section("Marks"))
        repo.grades().forEach { content.addView(card("${subjectName(it.subjectId)} • ${it.assessmentName}\n${it.mark}/${it.total} on ${it.date}")) }
    }

    private fun settings(content: LinearLayout) {
        content.addView(section("Settings"))
        content.addView(label("App language"))
        val languages = listOf("English", "Afrikaans", "isiZulu")
        val languagePicker = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, languages)
            setSelection(languages.indexOf(repo.language()).coerceAtLeast(0))
        }
        content.addView(languagePicker)
        content.addView(actionButton("Save language") {
            repo.setLanguage(languagePicker.selectedItem as String)
            showShell(Screen.SETTINGS)
        })
        content.addView(actionButton("Sync with Firebase") {
            repo.syncWithCloud { result ->
                runOnUiThread {
                    val message = result.fold(
                        onSuccess = { online -> if (online) "Cloud sync complete." else "Offline: sync is queued for when you reconnect." },
                        onFailure = { it.message ?: "Cloud sync failed." }
                    )
                    toast(message)
                }
            }
        })
        val notifications = CheckBox(this).apply {
            text = "Assignment reminders enabled"
            isChecked = repo.notificationEnabled()
            setOnCheckedChangeListener { _, checked -> repo.setNotificationEnabled(checked) }
        }
        content.addView(notifications)
        content.addView(card("Offline-ready NoSQL storage: changes are saved on this device and remain available without an internet connection."))
        content.addView(actionButton("Log out") {
            repo.logout()
            showAuth()
        })
    }

    private fun subjectSpinner(): Spinner {
        val subjects = repo.subjects()
        return Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, subjects.map { it.name })
            contentDescription = "Subject"
        }
    }

    private fun selectedSubjectId(spinner: Spinner): String =
        repo.subjects().getOrNull(spinner.selectedItemPosition)?.subjectId.orEmpty()

    private fun subjectName(subjectId: String): String = repo.subjects().firstOrNull { it.subjectId == subjectId }?.name ?: subjectId

    private fun scroll(child: View): ScrollView = ScrollView(this).apply { addView(child) }

    private fun vertical(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun horizontal(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun title(text: String, size: Int): TextView = TextView(this).apply {
        this.text = localized(text)
        textSize = size.toFloat()
        setTextColor(blue)
        typeface = Typeface.DEFAULT_BOLD
        setPadding(0, 8, 0, 8)
    }

    private fun section(text: String): TextView = TextView(this).apply {
        this.text = localized(text)
        textSize = 20f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(green)
        setPadding(0, 24, 0, 8)
    }

    private fun label(text: String): TextView = TextView(this).apply {
        this.text = localized(text)
        textSize = 15f
        setTextColor(Color.DKGRAY)
        setPadding(0, 8, 0, 8)
    }

    private fun card(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 16f
        setTextColor(Color.rgb(33, 40, 46))
        setBackgroundColor(mist)
        setPadding(20, 16, 20, 16)
    }

    private fun input(hint: String): EditText = EditText(this).apply {
        this.hint = localized(hint)
        textSize = 16f
        setSingleLine(false)
    }

    private fun actionButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = localized(text)
        setTextColor(Color.WHITE)
        setBackgroundColor(if (text == "Delete") Color.rgb(160, 46, 46) else blue)
        setOnClickListener { onClick() }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun localized(text: String): String {
        val language = repo.language()
        val translations = when (language) {
            "Afrikaans" -> mapOf(
                "Dashboard" to "Kontrolebord", "Tasks" to "Take", "Timetable" to "Rooster",
                "Progress" to "Vordering", "Settings" to "Instellings", "Upcoming deadlines" to "Komende sperdatums",
                "Today" to "Vandag", "Weekly progress" to "Weeklikse vordering", "Add assignment" to "Voeg taak by",
                "Assignments" to "Take", "Save assignment" to "Stoor taak", "Add timetable entry" to "Voeg roosterinskrywing by",
                "Weekly timetable" to "Weekrooster", "Subjects" to "Vakke", "Add mark" to "Voeg punt by",
                "Marks" to "Punte", "App language" to "Programtaal", "Save language" to "Stoor taal",
                "Assignment reminders enabled" to "Taakherinneringe aangeskakel", "Log out" to "Teken af",
                "Quick add assignment" to "Voeg taak vinnig by", "Title" to "Titel", "Description" to "Beskrywing",
                "Subject name" to "Vaknaam", "Assessment name" to "Assesseringnaam", "Location" to "Ligging",
                "Add subject" to "Voeg vak by", "Save mark" to "Stoor punt", "Save timetable entry" to "Stoor roosterinskrywing",
                "Sync with Firebase" to "Sinkroniseer met Firebase",
                "Register" to "Registreer", "Log in" to "Teken aan", "Email" to "E-pos", "Password" to "Wagwoord",
                "Display name" to "Vertoonnaam", "Student planner and progress tracker" to "Studente beplanner en vorderingsnaspoorder"
            )
            "isiZulu" -> mapOf(
                "Dashboard" to "Ibhodi", "Tasks" to "Imisebenzi", "Timetable" to "Uhlelo lwezikhathi",
                "Progress" to "Intuthuko", "Settings" to "Izilungiselelo", "Upcoming deadlines" to "Izinsuku zokugcina ezizayo",
                "Today" to "Namuhla", "Weekly progress" to "Intuthuko yeviki", "Add assignment" to "Engeza umsebenzi",
                "Assignments" to "Imisebenzi", "Save assignment" to "Londoloza umsebenzi", "Add timetable entry" to "Engeza ohlelweni lwezikhathi",
                "Weekly timetable" to "Uhlelo lweviki", "Subjects" to "Izifundo", "Add mark" to "Engeza amamaki",
                "Marks" to "Amamaki", "App language" to "Ulimi lohlelo", "Save language" to "Londoloza ulimi",
                "Assignment reminders enabled" to "Izikhumbuzi zemisebenzi zivuliwe", "Log out" to "Phuma",
                "Quick add assignment" to "Engeza umsebenzi ngokushesha", "Title" to "Isihloko", "Description" to "Incazelo",
                "Subject name" to "Igama lesifundo", "Assessment name" to "Igama lokuhlolwa", "Location" to "Indawo",
                "Add subject" to "Engeza isifundo", "Save mark" to "Londoloza amamaki", "Save timetable entry" to "Londoloza uhlelo",
                "Sync with Firebase" to "Vumelanisa ne-Firebase",
                "Register" to "Bhalisa", "Log in" to "Ngena ngemvume", "Email" to "I-imeyili", "Password" to "Iphasiwedi",
                "Display name" to "Igama eliboniswayo", "Student planner and progress tracker" to "Uhlelo lokuhlela nokulandelela intuthuko yomfundi"
            )
            else -> emptyMap()
        }
        return translations[text] ?: text
    }

    private enum class Screen(val label: String) {
        DASHBOARD("Dashboard"),
        TASKS("Tasks"),
        TIMETABLE("Timetable"),
        PROGRESS("Progress"),
        SETTINGS("Settings")
    }
}
