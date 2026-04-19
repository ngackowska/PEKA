package com.example.peka.database

data class BusStop(
    val stop_id: Int,
    val stop_code: String,
    val stop_name: String,
    val stop_lat: Double,
    val stop_lon: Double,
    val zone_id: String
)