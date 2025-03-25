package com.example.locationtrackingapp.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LocationData")
data class LocationData( @PrimaryKey val id: Int ,val imageUrl: Uri, val locationName:String, val latitude:String, val longitude:String,var timeStamp:String)
