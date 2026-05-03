package com.example.peka.api

data class PekaResponse<T>(
    val success: T
)

data class StopsData(
    val times: List<TimeData>,
    val bollard: BollardData
)

data class TimeData(
    val line: String,
    val minutes: Int,
    val direction: String,
    val departure: String,

    val onStopPoint: Boolean? = null,
    val realTime: Boolean? = null,
    val lfRamp: Boolean? = null,
    val leRamp: Boolean? = null,
    val ticketMachine: Boolean? = null,
    val airCnd: Boolean? = null,
    val bike: Boolean? = null,
    val lowEntranceBus: Boolean? = null,
    val lowFloorBus: Boolean? = null,
    val charger: Boolean? = null,
    val vehicle: String? = null
)

data class BollardData(
    val symbol: String,
    val name: String,
    val tag: String,
    val mainBollard: Boolean
)

data class BollardsSuccessData(
    val bollards: List<BollardItem>
)

data class BollardItem(
    val directions: List<DirectionItem>,
    val bollard: BollardData // Korzystamy z klasy, którą już masz w pliku
)

data class DirectionItem(
    val returnVariant: Boolean,
    val lineName: String,
    val direction: String
)

//data class StreetData(
//    val id: Int,
//    val name: String
//)