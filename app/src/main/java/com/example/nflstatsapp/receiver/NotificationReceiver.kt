package com.example.nflstatsapp.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.nflstatsapp.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Create the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "nfl_game_channel"
        val channelName = "NFL Game Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH

        // For Android 8.0 and higher, create a NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.default_player_image)  // Use your app's icon here
            .setContentTitle("NFL Game Reminder")
            .setContentText("It's time for the NFL game!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Shows sound, vibration, etc.

        val notification = notificationBuilder.build()

        // Issue the notification
        notificationManager.notify(1, notification)
    }
}
