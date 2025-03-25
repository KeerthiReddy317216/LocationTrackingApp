package com.example.locationtrackingapp.utils

import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.locationtrackingapp.service.LocationTrackingService
import com.example.locationtrackingapp.service.LocationTrackingWorker
import java.util.concurrent.TimeUnit

object Utils {

    fun timeFormatter(millis: Long): String {
            val hours = millis / (1000 * 60 * 60)
            val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
            val seconds = (millis % (1000 * 60)) / 1000
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    fun startLocationTracking(mContext:Context){
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val locationTrackingRequest = OneTimeWorkRequestBuilder<LocationTrackingWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(mContext).enqueue(locationTrackingRequest)
    }
    fun stopLocationTracking(mContext:Context){
        val worker = WorkManager.getInstance(mContext)
        worker.cancelAllWork()
        val serviceIntent = Intent(mContext, LocationTrackingService::class.java)
        mContext.stopService(serviceIntent)
    }


}