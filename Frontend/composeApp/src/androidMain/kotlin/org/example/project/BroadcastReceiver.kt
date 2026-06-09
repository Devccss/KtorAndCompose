package org.example.project

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val completed = intent.getIntExtra("completed", 0)
        val total = intent.getIntExtra("total", 0)

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Continue learning English!")
                .setContentText(
                    "You have completed $completed exercises of $total."
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        manager.notify(1, notification)
    }
}