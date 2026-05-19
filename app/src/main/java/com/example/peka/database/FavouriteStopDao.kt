package com.example.peka.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStopDao {
    @Query("SELECT * FROM favorite_stops")
    fun getAllFavorites(): Flow<List<BusStop>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stops WHERE stop_code = :stopCode)")
    fun isFavorite(stopCode: String): Flow<Boolean>

    // USUŃ słówko suspend i zwracany typ
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stop: BusStop)

    // USUŃ słówko suspend i zwracany typ
    @Delete
    fun delete(stop: BusStop)
}