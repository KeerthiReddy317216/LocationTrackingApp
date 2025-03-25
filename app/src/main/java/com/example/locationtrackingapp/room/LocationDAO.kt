package com.example.locationtrackingapp.room
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.locationtrackingapp.model.LocationData


@Dao
interface LocationDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDetails(location: LocationData)

}