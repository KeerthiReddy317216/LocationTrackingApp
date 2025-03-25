package com.example.locationtrackingapp.room

import android.content.Context
import androidx.room.Room
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory


object RoomDatabaseInstance {

    fun provideRoomDatabase( app: Context): DataImplementation {
        SQLiteDatabase.loadLibs(app)
        val passphrase = SQLiteDatabase.getBytes("A64pdjghX4tYZhkhsjnbsh657343".toCharArray())
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(app, DataImplementation::class.java, "location_data")
            .openHelperFactory(factory)
            .allowMainThreadQueries().build()
    }

    fun insertLocation(database: DataImplementation): LocationDAO {
        return database.locationDao()
    }

}
