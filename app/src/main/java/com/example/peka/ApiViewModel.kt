package com.example.peka

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

    init {
        fetchStopPoints()
    }

    private fun fetchStopPoints() {
        viewModelScope.launch {
            try {
                val response = pekaApiService.getStopPoints()

                _departures.value = response.success.times
                _stopName.value = response.success.bollard.name

            } catch (e: Exception) {
                e.printStackTrace()
                _stopName.value = "Błąd pobierania danych"
            }
        }
    }
}