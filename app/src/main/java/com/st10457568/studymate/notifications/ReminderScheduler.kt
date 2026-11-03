package com.st10457568.studymate.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.st10457568.studymate.model.Assignment
import java.time.LocalDate
import java.time.ZoneId

object ReminderScheduler {
    fun schedule(context: Context, assignment: Assignment) {
        if (!assignment.reminderEnabled) return
        val dueDate = runCatching { LocalDate.parse(assignment.dueDate) }.getOrNull() ?: return
        val triggerAt = dueDate.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerAt <= System.currentTimeMillis()) return
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context, assignment)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        else manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
    }

    fun cancel(context: Context, assignmentId: String) {
        val intent = Intent(context, AssignmentReminderReceiver::class.java).putExtra(EXTRA_ASSIGNMENT_ID, assignmentId)
        val pending = PendingIntent.getBroadcast(
            context,
            assignmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
    }

    private fun pendingIntent(context: Context, assignment: Assignment): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            assignment.assignmentId.hashCode(),
            Intent(context, AssignmentReminderReceiver::class.java)
                .putExtra(EXTRA_ASSIGNMENT_ID, assignment.assignmentId)
                .putExtra(EXTRA_TITLE, assignment.title)
                .putExtra(EXTRA_DUE_DATE, assignment.dueDate),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    const val EXTRA_ASSIGNMENT_ID = "assignment_id"
    const val EXTRA_TITLE = "assignment_title"
    const val EXTRA_DUE_DATE = "assignment_due_date"
}
