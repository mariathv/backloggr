package com.project.backloggr.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.backloggr.GameDetailActivity
import com.project.backloggr.HomeActivity
import com.project.backloggr.R
import com.project.backloggr.utils.FCMTokenManager

class BackloggrFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "backlog_reminders"
        const val CHANNEL_NAME = "Backlog Reminders"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Save token locally
        FCMTokenManager.saveToken(this, token)

        // Send token to backend if user is logged in
        val authToken = getAuthToken()
        if (authToken != null) {
            FCMTokenManager.sendTokenToServer(this, token, authToken)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Check if message contains a notification payload
        message.notification?.let { notification ->
            Log.d(TAG, "Notification title: ${notification.title}")
            Log.d(TAG, "Notification body: ${notification.body}")

            showNotification(
                notification.title ?: "Backloggr",
                notification.body ?: "",
                message.data
            )
        }

        // Check if message contains a data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data: ${message.data}")
            handleDataPayload(message.data)
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        createNotificationChannel()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent based on notification type
        val intent = when (data["type"]) {
            "backlog_reminder" -> {
                // If game_id exists, open GameDetailActivity
                if (data.containsKey("game_id")) {
                    Intent(this, GameDetailActivity::class.java).apply {
                        putExtra("GAME_ID", data["game_id"]?.toIntOrNull() ?: 0)
                        putExtra("FROM_NOTIFICATION", true)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                } else {
                    // Otherwise open HomeActivity
                    Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                }
            }
            else -> {
                Intent(this, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for backlogged games reminders"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle custom data payload if needed
        val type = data["type"]
        when (type) {
            "backlog_reminder" -> {
                Log.d(TAG, "Backlog reminder received")
            }
        }
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("BackloggrPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }
}
