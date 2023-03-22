package com.example.tp2.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE is_visited = true")
    fun getAllVisitedPlaces(): Flow<List<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllPlaces(places: List<Place>)

    @Update
    suspend fun updatePlace(place: Place)

    @Delete
    suspend fun deletePlace(place: Place)

    @Query("DELETE FROM places")
    suspend fun deleteAllPlaces()
}