package com.example.locationtrackingapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

 class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val CHANNEL_ID = "LocationTrackingChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows location tracking in the background"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val notification = createNotification()
            notificationManager.notify(NOTIFICATION_ID,notification)
            startForeground(NOTIFICATION_ID, notification)

        }

    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Your location is being tracked in the background.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Makes the notification non-dismissible
        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
