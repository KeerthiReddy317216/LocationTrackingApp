package com.example.locationtrackingapp.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.content.Intent
import android.os.Build

class LocationTrackingWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    val serviceIntent = Intent(applicationContext, LocationTrackingService::class.java)
    override fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent)
        }else{
            applicationContext.startService(serviceIntent)
        }
        return Result.success()
    }
}
