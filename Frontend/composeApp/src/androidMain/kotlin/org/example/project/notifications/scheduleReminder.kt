package org.example.project.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import org.example.project.ReminderReceiver

@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
fun scheduleReminder(
    context: Context,
    completed: Int,
    total: Int
) {

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("completed", completed)
        putExtra("total", total)
    }

    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            123,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE)
                as AlarmManager

    alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis() + 60_000,
        pendingIntent
    )
}
fun cancelReminder(context: Context) {

    val intent =
        Intent(context, ReminderReceiver::class.java)

    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            123,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE)
                as AlarmManager

    alarmManager.cancel(pendingIntent)
}
