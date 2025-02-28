package com.example.neuroed
import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    private const val CHANNEL_ID = "LOCK_SCREEN_CHANNEL_ID"
    private const val CHANNEL_NAME = "Lock Screen Notifications"
    private const val NOTIFICATION_ID = 1001

    /**
     * Create the notification channel for lock-screen notifications.
     * Call this once in your Application class or MainActivity on startup.
     */
    fun createLockScreenNotificationChannel(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications visible on lock screen"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager =
                ContextCompat.getSystemService(activity, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Request POST_NOTIFICATIONS permission on Android 13+ if not granted.
     */
    fun requestNotificationPermission(activity: Activity, requestCode: Int = 101) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    requestCode
                )
            }
        }
    }

    /**
     * Show a lock-screen notification with full content visible.
     */
    fun showLockScreenNotification(activity: Activity, title: String, message: String) {
        val builder = NotificationCompat.Builder(activity, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)  // Replace with your own icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        NotificationManagerCompat.from(activity).notify(NOTIFICATION_ID, builder.build())
    }
}
