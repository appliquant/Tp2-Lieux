package com.example.tp2.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Place::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true
)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao

    object DBSingelton {
        fun getInstance(context: Context): PlaceDatabase {

            val instance = Room.databaseBuilder(
                context, PlaceDatabase::class.java,
                "place_database_2"
            )
                .createFromAsset("database/places.db")
                .build()

            return instance
        }
    }
}
