package com.example.locationtrackingapp.model

import android.net.Uri

data class LocationData(val imageUrl: Uri, val locationName:String, val latitude:String, val longitude:String, var timeStamp:String)
