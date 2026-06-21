package com.example.peka.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stops")
data class BusStop(
    val stop_id: Int,
    @PrimaryKey
    val stop_code: String,
    val stop_name: String,
    val stop_lat: Double,
    val stop_lon: Double,
    val zone_id: String
)