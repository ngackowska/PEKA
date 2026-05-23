package com.example.peka.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms WHERE stop_code = :stopCode LIMIT 1")
    fun getAlarmForStop(stopCode: String): Flow<AlarmEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE stop_code = :stopCode")
    fun deleteAlarm(stopCode: String)
}