package com.example.peka.database


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BusStop::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Rejestrujemy nasze DAO
    abstract fun favoriteStopDao(): FavoriteStopDao
}