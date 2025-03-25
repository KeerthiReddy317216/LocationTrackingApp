package com.example.locationtrackingapp.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.locationtrackingapp.model.LocationData
import com.example.locationtrackingapp.utils.CustomTypeConverters

@Database(entities = [LocationData::class], version = 1, exportSchema = false)
@TypeConverters(CustomTypeConverters::class)
abstract class DataImplementation : RoomDatabase() {

    abstract fun locationDao(): LocationDAO
}
