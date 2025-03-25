package com.example.locationtrackingapp.utils

import android.net.Uri
import androidx.room.TypeConverter

class CustomTypeConverters {

    // Convert Uri to String
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    // Convert String back to Uri
    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }
}