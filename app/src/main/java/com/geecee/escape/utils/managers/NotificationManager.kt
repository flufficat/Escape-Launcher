package com.geecee.escape.utils.managers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.geecee.escape.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Get the notification data
        val notification = remoteMessage.notification

        val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/georgeclensy/escape"))
        if (notification != null) {
            notification.title?.let {
                notification.body?.let { it1 ->
                    sendNotification(
                        this, it1,
                        it, "updates", "Updates", notificationIntent)
                }
            }
        }
    }

    // Override onNewToken to get the FCM token
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Send the token to your server to store it
        // ...
    }
}

fun sendNotification(context: Context, title: String, message: String, channelID: String, channelName: String, intent: Intent) {
    val notificationId = 1

    // Create a notification channel (only needed for Android 8.0+)
    val channel = NotificationChannel(
        channelID,
        channelName,
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = channelName
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    // Register the channel with the system
    val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(channel)

    // Build the notification
    val notification = NotificationCompat.Builder(context, channelID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notify(notificationId, notification)
    }
}