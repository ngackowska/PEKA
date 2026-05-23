package com.example.peka.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val stop_code: String,
    val line: String,
    val startTime: String, // Format np. "07:00"
    val endTime: String,   // Format np. "09:00"
    val minutesBefore: Int
)