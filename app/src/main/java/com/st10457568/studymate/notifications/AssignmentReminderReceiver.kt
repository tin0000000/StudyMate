package com.st10457568.studymate.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.st10457568.studymate.R

class AssignmentReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: return
        val dueDate = intent.getStringExtra(ReminderScheduler.EXTRA_DUE_DATE).orEmpty()
        val assignmentId = intent.getStringExtra(ReminderScheduler.EXTRA_ASSIGNMENT_ID).orEmpty()
        createChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Assignment due today")
            .setContentText("$title • $dueDate")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(assignmentId.hashCode(), notification) }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Assignment reminders", NotificationManager.IMPORTANCE_DEFAULT)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "assignment_reminders"
    }
}
