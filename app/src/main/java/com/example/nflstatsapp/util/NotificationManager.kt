package com.example.nflstatsapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import com.example.nflstatsapp.receiver.NotificationReceiver
import java.util.*

class NotificationManager {

    // Schedule notification based on Central Time (converted to the user's local time)
    fun scheduleCentralTimeNotification(context: Context, hour: Int, minute: Int, dayOfWeek: Int) {
        // Get the user's local time zone
        val userTimeZone = TimeZone.getDefault()

        // Get the Central Time Zone (America/Chicago)
        val centralTimeZone = TimeZone.getTimeZone("America/Chicago")

        // Create a calendar for the target time in Central Time
        val centralCalendar = Calendar.getInstance(centralTimeZone)
        centralCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        centralCalendar.set(Calendar.HOUR_OF_DAY, hour)
        centralCalendar.set(Calendar.MINUTE, minute)
        centralCalendar.set(Calendar.SECOND, 0)
        centralCalendar.set(Calendar.MILLISECOND, 0)

        // Convert Central Time to the user's local time zone
        val localCalendar = Calendar.getInstance(userTimeZone)
        localCalendar.timeInMillis = centralCalendar.timeInMillis

        if (localCalendar.timeInMillis < System.currentTimeMillis()) {
            // If the time has already passed for today, set it for next week
            localCalendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        // Now localCalendar has the target time in the user's local time zone.
        // You can use this calendar to schedule the notification
        scheduleNotification(context, localCalendar, dayOfWeek)
    }

    // Schedule the notification
    private fun scheduleNotification(context: Context, calendar: Calendar, dayOfWeek: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Unique request code for each notification, based on day of the week
        val requestCode = dayOfWeek

        val alarmIntent = PendingIntent.getBroadcast(
            context, // context
            requestCode, // Unique request code based on the day of the week
            Intent(context, NotificationReceiver::class.java), // The Intent to trigger the alarm
            PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE here if you don't need to modify it
        )

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)

    }
}
