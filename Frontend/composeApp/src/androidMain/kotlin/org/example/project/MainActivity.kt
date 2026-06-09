package org.example.project

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationChannel.DEFAULT_CHANNEL_ID
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.example.project.notifications.cancelReminder
import org.example.project.notifications.scheduleReminder

const val CHANNEL_ID = "lex_context_notifications"
class MainActivity : ComponentActivity() {



    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidActivityProvider.activity = this

        createNotificationChannel(this)

        setContent {
            App()
        }
    }
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onStop() {
        super.onStop()

        scheduleReminder(
            this,
            completed = NotificationData.completedExercises,
            total = NotificationData.totalExercises
        )
    }

    override fun onStart() {
        super.onStart()

        cancelReminder(this)
    }


    private fun showProgressNotification(
        title: String,
        message: String
    ) {

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager

        notificationManager.notify(
            1,
            builder.build()
        )
    }

    private fun cancelNotification() {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        notificationManager.cancel(1)
    }

    fun createNotificationChannel(context: Context) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}
