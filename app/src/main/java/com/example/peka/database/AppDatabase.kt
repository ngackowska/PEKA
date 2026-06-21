package com.example.peka.database


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BusStop::class, AlarmEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Rejestrujemy nasze DAO
    abstract fun favoriteStopDao(): FavoriteStopDao
    abstract fun alarmDao(): AlarmDao
}