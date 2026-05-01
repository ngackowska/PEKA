package com.example.peka.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ApiViewModel : ViewModel() {

    private val _departures = MutableStateFlow<List<TimeData>>(emptyList())
    val departures: StateFlow<List<TimeData>> = _departures

    private val _stopName = MutableStateFlow("Pobieranie...")
    val stopName: StateFlow<String> = _stopName

//    private val _streets = MutableStateFlow<List<StreetData>>(emptyList())
//    val streets: StateFlow<List<StreetData>> = _streets

    init {
        fetchStopPoints("{\"symbol\":\"TRAU43\"}")
    }

    private fun fetchStopPoints(p0Json: String) {
        viewModelScope.launch {
            try {
                val response = pekaApiService.getTimes(p0 = p0Json)

                _departures.value = response.success.times
                _stopName.value = response.success.bollard.name

            } catch (e: Exception) {
                e.printStackTrace()
                _stopName.value = "Błąd pobierania danych"
            }
        }
    }

//    fun fetchStreets() {
//        viewModelScope.launch {
//            try {
//                val response = pekaApiService.getStreets()
//                _streets.value = response.success
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Opcjonalna obsługa błędu dla ulic
//            }
//        }
//    }
}